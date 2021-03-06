#import <React/RCTConvert.h>
#import "RCTVideo.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/UIView+React.h>
#include <MediaAccessibility/MediaAccessibility.h>
#include <AVFoundation/AVFoundation.h>
#include "DiceUtils.h"
#include "DiceBeaconRequest.h"
#include "DiceHTTPRequester.h"


//@import ReactVideoSubtitleSideloader;
#if TARGET_OS_IOS
#import <ReactVideoSubtitleSideloader/ReactVideoSubtitleSideloader-Swift.h>
#elif TARGET_OS_TV
#import <ReactVideoSubtitleSideloader_tvOS/ReactVideoSubtitleSideloader_tvOS-Swift.h>
#endif

#if TARGET_OS_IOS
#import <dice_shield_ios/dice_shield_ios-Swift.h>
@import MuxCore;
@import MUXSDKStats;
#elif TARGET_OS_TV
#import <dice_shield_ios/dice_shield_ios-Swift.h>
//#import <dice_shield_tvos/dice_shield_tvos-Swift.h>
@import MuxCoreTv;
@import MUXSDKStatsTv;
@import AVDoris;
#endif


static NSString *const statusKeyPath = @"status";
static NSString *const playbackLikelyToKeepUpKeyPath = @"playbackLikelyToKeepUp";
static NSString *const playbackBufferEmptyKeyPath = @"playbackBufferEmpty";
static NSString *const readyForDisplayKeyPath = @"readyForDisplay";
static NSString *const playbackRate = @"rate";
static NSString *const timedMetadata = @"timedMetadata";
static NSString *const currentItem = @"currentItem";

static NSString *const playerVersion = @"react-native-video/3.3.1";

static int const RCTVideoUnset = -1;

#ifdef DEBUG
    #define DebugLog(...) NSLog(__VA_ARGS__)
#else
    #define DebugLog(...) (void)0
#endif

@implementation RCTVideo
{
  AVPlayerItem *_playerItem;
  BOOL _playerItemObserversSet;
  BOOL _playerBufferEmpty;
  AVPlayerLayer *_playerLayer;
  BOOL _playerLayerObserverSet;
  RCTVideoPlayerViewController *_playerViewController;
  NSURL *_videoURL;
  
  /* Required to publish events */
  RCTEventDispatcher *_eventDispatcher;
  BOOL _playbackRateObserverRegistered;
  BOOL _currentItemObserverRegistered;
  BOOL _videoLoadStarted;

  bool _pendingSeek;
  float _pendingSeekTime;
  float _lastSeekTime;
  
  /* For sending videoProgress events */
  Float64 _progressUpdateInterval;
  BOOL _controls;
  id _timeObserver;
  
  /* Keep track of any modifiers, need to be applied after each play */
  float _volume;
  float _rate;
  BOOL _muted;
  BOOL _paused;
  BOOL _repeat;
  BOOL _allowsExternalPlayback;
  NSArray * _textTracks;
  NSDictionary * _selectedTextTrack;
  NSDictionary * _selectedAudioTrack;
  BOOL _playbackStalled;
  BOOL _playInBackground;
  BOOL _playWhenInactive;
  NSString * _ignoreSilentSwitch;
  NSString * _resizeMode;
  BOOL _fullscreenPlayerPresented;
  UIViewController * _presentingViewController;
  // keep reference to actionToken so resourceLoaderDelegate is not garbage collected
  ActionToken * _actionToken;
  DiceBeaconRequest * _diceBeaconRequst;
  BOOL _diceBeaconRequestOngoing;
  MUXSDKCustomerVideoData * _videoData;
  MUXSDKCustomerPlayerData * _playerData;
#if __has_include(<react-native-video/RCTVideoCache.h>)
  RCTVideoCache * _videoCache;
#endif
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
  if ((self = [super init])) {
    _eventDispatcher = eventDispatcher;
    _playbackRateObserverRegistered = NO;
    _playbackStalled = NO;
    _rate = 1.0;
    _volume = 1.0;
    _resizeMode = @"AVLayerVideoGravityResizeAspectFill";
    _pendingSeek = false;
    _pendingSeekTime = 0.0f;
    _lastSeekTime = 0.0f;
    _progressUpdateInterval = 250;
    _controls = NO;
    _playerBufferEmpty = YES;
    _playInBackground = false;
    _allowsExternalPlayback = YES;
    _playWhenInactive = false;
    _ignoreSilentSwitch = @"inherit"; // inherit, ignore, obey
    _diceBeaconRequestOngoing = NO;
#if __has_include(<react-native-video/RCTVideoCache.h>)
    _videoCache = [RCTVideoCache sharedInstance];
#endif
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(applicationWillResignActive:)
                                                 name:UIApplicationWillResignActiveNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(applicationDidEnterBackground:)
                                                 name:UIApplicationDidEnterBackgroundNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(applicationWillEnterForeground:)
                                                 name:UIApplicationWillEnterForegroundNotification
                                               object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(audioRouteChanged:)
                                                 name:AVAudioSessionRouteChangeNotification
                                               object:nil];
  }
  
  return self;
}

- (AVPlayerViewController*)createPlayerViewController:(AVDorisPlayer*)player {
    RCTVideoPlayerViewController* playerLayer= [[RCTVideoPlayerViewController alloc] init];
    playerLayer.showsPlaybackControls = _controls;
    playerLayer.rctDelegate = self;
    playerLayer.view.frame = self.bounds;
    playerLayer.player = player;
    playerLayer.view.frame = self.bounds;
    return playerLayer;
}

/* ---------------------------------------------------------
 **  Get the duration for a AVPlayerItem.
 ** ------------------------------------------------------- */

- (CMTime)playerItemDuration
{
  AVPlayerItem *playerItem = [self.player currentItem];
  if (playerItem.status == AVPlayerItemStatusReadyToPlay)
  {
    return([playerItem duration]);
  }
  
  return(kCMTimeInvalid);
}

- (CMTimeRange)playerItemSeekableTimeRange
{
  AVPlayerItem *playerItem = [self.player currentItem];
  if (playerItem.status == AVPlayerItemStatusReadyToPlay)
  {
    return [playerItem seekableTimeRanges].firstObject.CMTimeRangeValue;
  }
  
  return (kCMTimeRangeZero);
}

-(void)addPlayerTimeObserver
{
  const Float64 progressUpdateIntervalMS = _progressUpdateInterval / 1000;
  // @see endScrubbing in AVPlayerDemoPlaybackViewController.m
  // of https://developer.apple.com/library/ios/samplecode/AVPlayerDemo/Introduction/Intro.html
  __weak RCTVideo *weakSelf = self;
  _timeObserver = [self.player addPeriodicTimeObserverForInterval:CMTimeMakeWithSeconds(progressUpdateIntervalMS, NSEC_PER_SEC)
                                                        queue:NULL
                                                   usingBlock:^(CMTime time) { [weakSelf sendProgressUpdate]; }
                   ];
}

/* Cancels the previously registered time observer. */
-(void)removePlayerTimeObserver
{
  if (_timeObserver)
  {
    [self.player removeTimeObserver:_timeObserver];
    _timeObserver = nil;
  }
}

#pragma mark - DICE Beacon

- (void)startDiceBeaconCallsAfter:(long)seconds
{
    [self startDiceBeaconCallsAfter:seconds ongoing:NO];
}

- (void)startDiceBeaconCallsAfter:(long)seconds ongoing:(BOOL)ongoing
{
    if (_diceBeaconRequst == nil) {
        return;
    }
    if (_diceBeaconRequestOngoing && !ongoing) {
        DICELog(@"startDiceBeaconCallsAfter ONGOING request. INGNORING.");
        return;
    }
    _diceBeaconRequestOngoing = YES;
    DICELog(@"startDiceBeaconCallsAfter %ld", seconds);
    __weak RCTVideo *weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(seconds * NSEC_PER_SEC)), dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        // in case there is ongoing request
        [_diceBeaconRequst cancel];
        [_diceBeaconRequst makeRequestWithCompletionHandler:^(DiceBeaconResponse *response, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf handleBeaconResponse:response error:error];
            });
        }];
    });
}

-(void)handleBeaconResponse:(DiceBeaconResponse *)response error:(NSError *)error
{
    DICELog(@"handleBeaconResponse error=%@", error);
    if (self.player.timeControlStatus == AVPlayerTimeControlStatusPaused) {
        // video is not playing back, so no point
        DICELog(@"handleBeaconResponse player is paused. STOP beacons.");
        _diceBeaconRequestOngoing = NO;
        return;
    }
    
    if (error != nil) {
        DICELog(@"handleBeaconResponse error on call. STOP beacons.");
        // raise an error and stop playback
        NSNumber *code = [[NSNumber alloc] initWithInt:-1];
        self.onVideoError(@{@"error": @{@"code": code,
                                        @"domain": @"DiceBeacon",
                                        @"messages": @[@"Failed to make beacon request", error.localizedDescription]
                                        },
                            @"rawError": RCTJSErrorFromNSError(error),
                            @"target": self.reactTag});
        _diceBeaconRequestOngoing = NO;
        return;
    }
    
    if (response == nil || !response.OK) {
        // raise an error and stop playback
        NSNumber *code = [[NSNumber alloc] initWithInt:-2];
        NSString *rawResponse = @"";
        NSArray<NSString *> *errorMessages = @[];
        if (response != nil) {
            if (response.rawResponse != nil && response.rawResponse.length > 0) {
                rawResponse = [NSString stringWithUTF8String:[response.rawResponse bytes]];
            }
            if (rawResponse == nil) {
                rawResponse = @"";
            }
            if (response.errorMessages != nil) {
                errorMessages = response.errorMessages;
            }
        }
        self.onVideoError(@{@"error": @{@"code": code,
                                        @"domain": @"DiceBeacon",
                                        @"messages": errorMessages
                                        },
                            @"rawResponse": rawResponse,
                            @"target": self.reactTag});
        [self setPaused:YES];
        _diceBeaconRequestOngoing = NO;
        return;
    }
    [self startDiceBeaconCallsAfter:response.frequency ongoing:YES];
}

- (void)setupBeaconFromSource:(NSDictionary *)source
{
    id configObject = [source objectForKey:@"config"];
    id beaconObject = nil;
    if (configObject != nil && [configObject isKindOfClass:NSDictionary.class]) {
         beaconObject = [((NSDictionary *)configObject) objectForKey:@"beacon"];
    }
    
    if (beaconObject != nil) {
        if ([beaconObject isKindOfClass:NSString.class]) {
            NSString * beaconString = beaconObject;
            NSError *error = nil;
            beaconObject = [NSJSONSerialization JSONObjectWithData:[beaconString dataUsingEncoding:kCFStringEncodingUTF8]  options:0 error:&error];
            if (error != nil) {
                DICELog(@"Failed to create JSON object from provided beacon: %@", beaconString);
            }
        }
        if ([beaconObject isKindOfClass:NSDictionary.class]) {
            NSDictionary *beacon = beaconObject;
            NSString* url = [beacon objectForKey:@"url"];
            NSDictionary<NSString *, NSString *> *headers = [beacon objectForKey:@"headers"];
            NSDictionary* body = [beacon objectForKey:@"body"];
            _diceBeaconRequst = [DiceBeaconRequest requestWithURLString:url headers:headers body:body];
            [self startDiceBeaconCallsAfter:0];
        } else {
            DICELog(@"Failed to read dictionary object provided beacon: %@", beaconObject);
        }
    }
}

#pragma mark - Mux Data
- (NSString * _Nullable)stringFromDict:(NSDictionary *)dict forKey:(id _Nonnull)key
{
    id obj = [dict objectForKey:key];
    if (obj != nil && [obj isKindOfClass:NSString.class]) {
        return obj;
    }
    return nil;
}

- (void)setupMuxDataFromSource:(NSDictionary *)source
{
    id configObject = [source objectForKey:@"config"];
    id muxData = nil;
    if (configObject != nil && [configObject isKindOfClass:NSDictionary.class]) {
        muxData = [((NSDictionary *)configObject) objectForKey:@"muxData"];
    }
    
    if (muxData != nil) {
        if ([muxData isKindOfClass:NSString.class]) {
            NSString * muxDataString = muxData;
            NSError *error = nil;
            muxData = [NSJSONSerialization JSONObjectWithData:[muxDataString dataUsingEncoding:kCFStringEncodingUTF8]  options:0 error:&error];
            if (error != nil) {
                DICELog(@"Failed to create JSON object from provided playbackData: %@", muxDataString);
            }
        }
        if ([muxData isKindOfClass:NSDictionary.class]) {
            /*
             {
                muxData: {
                    envKey:"theKey"
                    viewerUserId: "userId",
                    experimentName: "",
                    playerName: "",
                    playerVersion: "",
                    subPropertyId: "",
                    videoId:"",
                    videoTitle:""
                    videoSeries:""
                    videoDuration:11111, //in miliseconds
                    videoIsLive:true,
                    videoStreamType: "",
                    videoCdn:""
                }
            }
             */
            NSDictionary *muxDict = muxData;

            NSString* envKey = [muxDict objectForKey:@"envKey"];
            if (envKey == nil) {
                DICELog(@"envKey is not present. Mux will not be available.");
                return;
            }
            
            NSString *value = nil;
            // Video metadata (cleared with videoChangeForPlayer:withVideoData:)
            BOOL isReplace = NO;
            if (_videoData != nil) {
                isReplace = YES;
            } else {
                // Environment and player data that persists until the player is destroyed
                _playerData = [[MUXSDKCustomerPlayerData alloc] initWithEnvironmentKey:envKey];
                // ...insert player metadata
                value = [self stringFromDict:muxDict forKey:@"viewerUserId"];
                [_playerData setViewerUserId:value];
                
                [_playerData setPlayerVersion:playerVersion];
                
                [_playerData setPlayerName:@"react-native-video/dice"];
                
                value = [self stringFromDict:muxDict forKey:@"subPropertyId"];
                [_playerData setSubPropertyId:value];
                
                value = [self stringFromDict:muxDict forKey:@"experimentName"];
                [_playerData setExperimentName:value];
            }
            
            _videoData = [MUXSDKCustomerVideoData new];
            
            // ...insert video metadata
            value = [self stringFromDict:muxDict forKey:@"videoTitle"];
            [_videoData setVideoTitle:value];
            
            value = [self stringFromDict:muxDict forKey:@"videoId"];
            [_videoData setVideoId:value];
            
            value = [self stringFromDict:muxDict forKey:@"videoSeries"];
            [_videoData setVideoSeries:value];
            
            value = [self stringFromDict:muxDict forKey:@"videoCdn"];
            [_videoData setVideoCdn:value];
            
            id videoIsLive = [muxDict objectForKey:@"videoIsLive"];
            if (videoIsLive != nil && [videoIsLive isKindOfClass:NSNumber.class]) {
                NSNumber* num = videoIsLive;
                [_videoData setVideoIsLive:num];
            } else {
                [_videoData setVideoIsLive:nil];
            }
            
            id videoDuration = [muxDict objectForKey:@"videoDuration"];
            if (videoDuration != nil && [videoDuration isKindOfClass:NSNumber.class]) {
                [_videoData setVideoDuration:((NSNumber*)videoDuration)];
            } else {
                [_videoData setVideoDuration:nil];
            }
            
            value = [self stringFromDict:muxDict forKey:@"videoStreamType"];
            [_videoData setVideoStreamType:value];
            
            
            if (isReplace) {
                [MUXSDKStats videoChangeForPlayer:@"dicePlayer" withVideoData:_videoData];
            } else {
                [self setupMux];
            }
        } else {
            DICELog(@"Failed to read dictionary object provided playbackData: %@", muxData);
        }
    }
}

- (void)setupMux
{
    if (_playerData == nil || _videoData == nil) {
        return;
    }
    if (_playerLayer != nil) {
        [MUXSDKStats monitorAVPlayerLayer:_playerLayer withPlayerName:@"dicePlayer" playerData:_playerData videoData:_videoData];
    } else if (_playerViewController != nil) {
        [MUXSDKStats monitorAVPlayerViewController:_playerViewController withPlayerName:@"dicePlayer" playerData:_playerData videoData:_videoData];
    }
}


#pragma mark - Progress

- (void)dealloc
{
  if (_playerData || _videoData) {
    [MUXSDKStats destroyPlayer:@"dicePlayer"];
    _playerData = nil;
    _videoData = nil;
  }
  [[NSNotificationCenter defaultCenter] removeObserver:self];
  [self removePlayerLayer];
  [self removePlayerItemObservers];
  [self.player removeObserver:self forKeyPath:playbackRate context:nil];
  [self.player removeObserver:self forKeyPath:currentItem context:nil];
  [_diceBeaconRequst cancel];
  _diceBeaconRequst = nil;
}

#pragma mark - App lifecycle handlers

- (void)applicationWillResignActive:(NSNotification *)notification
{
  if (_playInBackground || _playWhenInactive || _paused) return;
  
  [self.player pause];
  [self.player setRate:0.0];
}

- (void)applicationDidEnterBackground:(NSNotification *)notification
{
  if (_playInBackground) {
    // Needed to play sound in background. See https://developer.apple.com/library/ios/qa/qa1668/_index.html
    [_playerLayer setPlayer:nil];
  }
}

- (void)applicationWillEnterForeground:(NSNotification *)notification
{
  [self applyModifiers];
  if (_playInBackground) {
    [_playerLayer setPlayer:self.player];
  }
}

#pragma mark - Audio events

- (void)audioRouteChanged:(NSNotification *)notification
{
    NSNumber *reason = [[notification userInfo] objectForKey:AVAudioSessionRouteChangeReasonKey];
    NSNumber *previousRoute = [[notification userInfo] objectForKey:AVAudioSessionRouteChangePreviousRouteKey];
    if (reason.unsignedIntValue == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
        self.onVideoAudioBecomingNoisy(@{@"target": self.reactTag});
    }
}

#pragma mark - Progress

- (void)sendProgressUpdate
{
  AVPlayerItem *video = [self.player currentItem];
  if (video == nil || video.status != AVPlayerItemStatusReadyToPlay) {
    return;
  }
  
  CMTime playerDuration = [self playerItemDuration];
  if (CMTIME_IS_INVALID(playerDuration)) {
    return;
  }
  
  CMTime currentTime = self.player.currentTime;
  const Float64 duration = CMTimeGetSeconds(playerDuration);
  const Float64 currentTimeSecs = CMTimeGetSeconds(currentTime);
  
  [[NSNotificationCenter defaultCenter] postNotificationName:@"RCTVideo_progress" object:nil userInfo:@{@"progress": [NSNumber numberWithDouble: currentTimeSecs / duration]}];
  
  if( currentTimeSecs >= 0 && self.onVideoProgress) {
    self.onVideoProgress(@{
                           @"currentTime": [NSNumber numberWithFloat:CMTimeGetSeconds(currentTime)],
                           @"playableDuration": [self calculatePlayableDuration],
                           @"atValue": [NSNumber numberWithLongLong:currentTime.value],
                           @"atTimescale": [NSNumber numberWithInt:currentTime.timescale],
                           @"target": self.reactTag,
                           @"seekableDuration": [self calculateSeekableDuration],
                           });
      if (self.onVideoAboutToEnd) {
          bool isAboutToEnd;
          if (currentTimeSecs >= duration - 10) {
              isAboutToEnd = YES;
          } else {
              isAboutToEnd = NO;
          }
          self.onVideoAboutToEnd(@{@"isAboutToEnd": [NSNumber numberWithBool:isAboutToEnd]});
      }
  }
}

/*!
 * Calculates and returns the playable duration of the current player item using its loaded time ranges.
 *
 * \returns The playable duration of the current player item in seconds.
 */
- (NSNumber *)calculatePlayableDuration
{
  AVPlayerItem *video = self.player.currentItem;
  if (video.status == AVPlayerItemStatusReadyToPlay) {
    __block CMTimeRange effectiveTimeRange;
    [video.loadedTimeRanges enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
      CMTimeRange timeRange = [obj CMTimeRangeValue];
      if (CMTimeRangeContainsTime(timeRange, video.currentTime)) {
        effectiveTimeRange = timeRange;
        *stop = YES;
      }
    }];
    Float64 playableDuration = CMTimeGetSeconds(CMTimeRangeGetEnd(effectiveTimeRange));
    if (playableDuration > 0) {
      return [NSNumber numberWithFloat:playableDuration];
    }
  }
  return [NSNumber numberWithInteger:0];
}

- (NSNumber *)calculateSeekableDuration
{
  CMTimeRange timeRange = [self playerItemSeekableTimeRange];
  if (CMTIME_IS_NUMERIC(timeRange.duration))
  {
    return [NSNumber numberWithFloat:CMTimeGetSeconds(timeRange.duration)];
  }
  return [NSNumber numberWithInteger:0];
}

- (void)addPlayerItemObservers
{
  [_playerItem addObserver:self forKeyPath:statusKeyPath options:0 context:nil];
  [_playerItem addObserver:self forKeyPath:playbackBufferEmptyKeyPath options:0 context:nil];
  [_playerItem addObserver:self forKeyPath:playbackLikelyToKeepUpKeyPath options:0 context:nil];
  [_playerItem addObserver:self forKeyPath:timedMetadata options:NSKeyValueObservingOptionNew context:nil];
  _playerItemObserversSet = YES;
}

/* Fixes https://github.com/brentvatne/react-native-video/issues/43
 * Crashes caused when trying to remove the observer when there is no
 * observer set */
- (void)removePlayerItemObservers
{
  if (_playerItemObserversSet) {
    [_playerItem removeObserver:self forKeyPath:statusKeyPath];
    [_playerItem removeObserver:self forKeyPath:playbackBufferEmptyKeyPath];
    [_playerItem removeObserver:self forKeyPath:playbackLikelyToKeepUpKeyPath];
    [_playerItem removeObserver:self forKeyPath:timedMetadata];
    _playerItemObserversSet = NO;
  }
}

#pragma mark - Player and source

static void extracted(RCTVideo *object, NSDictionary *source) {
  
  [object playerItemForSource:source withCallback:^(AVPlayerItem * playerItem) {
    [object.player pause];
    [object->_playerViewController.view removeFromSuperview];
    object->_playerViewController = nil;
    
    if (object->_playbackRateObserverRegistered) {
      [object.player removeObserver:object forKeyPath:playbackRate context:nil];
      object->_playbackRateObserverRegistered = NO;
    }
      
    if (object->_playbackRateObserverRegistered) {
        [object.player removeObserver:object forKeyPath:currentItem context:nil];
        object->_currentItemObserverRegistered = NO;
    }
    
    object.player = [AVDorisPlayer new];
    object.avdoris = [[AVDoris alloc] initWithPlayer:object.player];

    [object.player addObserver:object forKeyPath:currentItem options:0 context:nil];
    [object.player addObserver:object forKeyPath:playbackRate options:0 context:nil];
    object->_playbackRateObserverRegistered = YES;
    object->_currentItemObserverRegistered = YES;

    id imaObject = [source objectForKey:@"ima"];
    
    if ([imaObject isKindOfClass:NSDictionary.class]) {
      [object setupPlaybackWithAds:imaObject playerItem:playerItem];
    } else {
      [object.player replaceCurrentItemWithPlayerItem:playerItem];
    }
    
    object.player.actionAtItemEnd = AVPlayerActionAtItemEndNone;

    [object addPlayerTimeObserver];

    if (object.onVideoLoadStart) {
      id uri = [source objectForKey:@"uri"];
      id type = [source objectForKey:@"type"];
      object.onVideoLoadStart(@{@"src": @{
                                    @"uri": uri ? uri : [NSNull null],
                                    @"type": type ? type : [NSNull null],
                                    @"isNetwork": [NSNumber numberWithBool:(bool)[source objectForKey:@"isNetwork"]]},
                                @"target": object.reactTag
                              });
    }
  }];
}

- (void)setupPlaybackWithAds:(NSDictionary *)imaDict playerItem:(AVPlayerItem *)playerItem {
  [self usePlayerViewController];
  
  self.avdoris.delegate = _playerViewController;
  _playerViewController.avdoris = self.avdoris;
  
  id assetKey = [imaDict objectForKey:@"assetKey"];
  id contentSourceId = [imaDict objectForKey:@"contentSourceId"];
  id videoId = [imaDict objectForKey:@"videoId"];
  id authToken = [imaDict objectForKey:@"authToken"];
  id adTagParameters = [imaDict objectForKey:@"adTagParameters"];
    
  AVDorisIMAStreamRequest *streamRequest;
  
  if (assetKey) {
    if ([assetKey isKindOfClass:NSString.class]) {
      streamRequest = [[AVDorisIMALiveStreamRequest alloc]
                       initWithAssetKey:assetKey
                       adContainerView:self->_playerViewController.adView
                       adContainerViewController:self->_playerViewController];
    }
  } else if (contentSourceId && videoId && authToken) {
    if ([contentSourceId isKindOfClass:NSString.class] &&
        [videoId isKindOfClass:NSString.class]) {
      streamRequest = [[AVDorisIMAVODStreamRequest alloc]
                       initWithContentSourceId:contentSourceId
                       videoId:videoId
                       adContainerView:self->_playerViewController.adView
                       adContainerViewController:self->_playerViewController];
    }
  }
  
  if (streamRequest) {
    if ([authToken isKindOfClass:NSString.class]) {
      streamRequest.authToken = authToken;
    }
    
    if ([adTagParameters isKindOfClass:NSDictionary.class]) {
      NSString* __nullable customParams = [adTagParameters stringForKey:@"cust_params"];
      
      if (customParams) {
        NSString* widthString = [NSString stringWithFormat: @"&pw=%.0f", self.bounds.size.width];
        NSString* heightString = [NSString stringWithFormat: @"&ph=%.0f", self.bounds.size.height];
        
        customParams = [customParams stringByAppendingString:widthString];
        customParams = [customParams stringByAppendingString:heightString];
        [adTagParameters setValue:customParams forKey:@"cust_params"];
      }
      

        [adTagParameters setValue:@"0" forKey:@"is_lat"];
        [self fetchAppIdWithCompletion:^(NSNumber * _Nullable appId) {
            if (appId) {
                [adTagParameters setValue:appId.stringValue forKey:@"msid"];
            } else {
                [adTagParameters setValue:@"0" forKey:@"msid"];
            }
            streamRequest.adTagParameters = adTagParameters;
            [self.avdoris requestIMAStreamWithStreamRequest:streamRequest];
        }];
    }
  } else {
    [self.player replaceCurrentItemWithPlayerItem:playerItem];
  }
}

- (void)setSrc:(NSDictionary *)source
{
  [self removePlayerLayer];
  [self removePlayerTimeObserver];
  [self removePlayerItemObservers];
    
    if (_currentItemObserverRegistered) {
        [self.player removeObserver:self forKeyPath:currentItem context:nil];
        _currentItemObserverRegistered = NO;
    }

  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) 0), dispatch_get_main_queue(), ^{

    // perform on next run loop, otherwise other passed react-props may not be set
      extracted(self, source);
  });
  _videoLoadStarted = YES;
}

- (NSURL*) urlFilePath:(NSString*) filepath {
  if ([filepath containsString:@"file://"]) {
    return [NSURL URLWithString:filepath];
  }
  
  // code to support local caching
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString* relativeFilePath = [filepath lastPathComponent];
  // the file may be multiple levels below the documents directory
  NSArray* fileComponents = [filepath componentsSeparatedByString:@"Documents/"];
  if (fileComponents.count > 1) {
    relativeFilePath = [fileComponents objectAtIndex:1];
  }
  
  NSString *path = [paths.firstObject stringByAppendingPathComponent:relativeFilePath];
  if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
    return [NSURL fileURLWithPath:path];
  }
  return nil;
}

- (void)playerItemPrepareText:(AVAsset *)asset assetOptions:(NSDictionary * __nullable)assetOptions withCallback:(void(^)(AVPlayerItem *))handler
{
  if (!_textTracks) {
    handler([AVPlayerItem playerItemWithAsset:asset]);
    return;
  }

  // sideload text tracks
  AVMutableComposition *mixComposition = [[AVMutableComposition alloc] init];
  
  AVAssetTrack *videoAsset = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
  AVMutableCompositionTrack *videoCompTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];
  [videoCompTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, videoAsset.timeRange.duration)
                          ofTrack:videoAsset
                           atTime:kCMTimeZero
                            error:nil];

  AVAssetTrack *audioAsset = [asset tracksWithMediaType:AVMediaTypeAudio].firstObject;
  AVMutableCompositionTrack *audioCompTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
  [audioCompTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, videoAsset.timeRange.duration)
                          ofTrack:audioAsset
                           atTime:kCMTimeZero
                            error:nil];

  NSMutableArray* validTextTracks = [NSMutableArray array];
  for (int i = 0; i < _textTracks.count; ++i) {
    AVURLAsset *textURLAsset;
    NSString *textUri = [_textTracks objectAtIndex:i][@"uri"];
    if ([[textUri lowercaseString] hasPrefix:@"http"]) {
      textURLAsset = [AVURLAsset URLAssetWithURL:[NSURL URLWithString:textUri] options:assetOptions];
    } else {
      textURLAsset = [AVURLAsset URLAssetWithURL:[self urlFilePath:textUri] options:nil];
    }
    AVAssetTrack *textTrackAsset = [textURLAsset tracksWithMediaType:AVMediaTypeText].firstObject;
    if (!textTrackAsset) continue; // fix when there's no textTrackAsset
    [validTextTracks addObject:[_textTracks objectAtIndex:i]];
    AVMutableCompositionTrack *textCompTrack = [mixComposition
                                                addMutableTrackWithMediaType:AVMediaTypeText
                                                preferredTrackID:kCMPersistentTrackID_Invalid];
    [textCompTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, videoAsset.timeRange.duration)
                               ofTrack:textTrackAsset
                                atTime:kCMTimeZero
                                 error:nil];
  }
  if (validTextTracks.count != _textTracks.count) {
    [self setTextTracks:validTextTracks];
  }

  handler([AVPlayerItem playerItemWithAsset:mixComposition]);
}

SubtitleResourceLoaderDelegate* _delegate;
dispatch_queue_t delegateQueue;

- (void)playerItemForSource:(NSDictionary *)source withCallback:(void(^)(AVPlayerItem *))handler
{
  bool isNetwork = [RCTConvert BOOL:[source objectForKey:@"isNetwork"]];
  bool isAsset = [RCTConvert BOOL:[source objectForKey:@"isAsset"]];
  NSString *uri = [source objectForKey:@"uri"];
  NSString *type = [source objectForKey:@"type"];

  NSURL *url = isNetwork || isAsset
    ? [NSURL URLWithString:uri]
    : [[NSURL alloc] initFileURLWithPath:[[NSBundle mainBundle] pathForResource:uri ofType:type]];
  NSMutableDictionary *assetOptions = [[NSMutableDictionary alloc] init];
    
  
  
  [self setupMuxDataFromSource:source];

  if (isNetwork) {
    [self setupBeaconFromSource:source];
  }
    
  id drmObject = [source objectForKey:@"drm"];
  if (drmObject) {
      ActionToken* ac = nil;
      if ([drmObject isKindOfClass:NSDictionary.class]) {
          NSDictionary* drmDictionary = drmObject;
          ac = [[ActionToken alloc] initWithDict:drmDictionary contentUrl:uri];
      } else if ([drmObject isKindOfClass:NSString.class]) {
          NSString* drmString = drmObject;
          ac = [ActionToken createFrom: drmString contentUrl:uri];
      }
      if (ac) {
        _actionToken = ac;
        AVURLAsset* asset = [ac urlAsset];
        [self playerItemPrepareText:asset assetOptions:[NSDictionary alloc] withCallback:handler];
        return;
      } else {
        DebugLog(@"Failed to created action token for playback.");
      }
  } else {
      // we can try subtitles if it's not a DRM file
      id subtitleObjects = [source objectForKey:@"subtitles"];
      if ([subtitleObjects isKindOfClass:NSArray.class]) {
          NSArray* subs = subtitleObjects;
          NSArray* subtitleTracks = [SubtitleResourceLoaderDelegate createSubtitleTracksFromArray:subs];
          SubtitleResourceLoaderDelegate* delegate = [[SubtitleResourceLoaderDelegate alloc] initWithM3u8URL:url subtitles:subtitleTracks];
          _delegate = delegate;
          url = delegate.redirectURL;
          if (!delegateQueue) {
              delegateQueue = dispatch_queue_create("SubtitleResourceLoaderDelegate", 0);
          }
          AVURLAsset* asset = [AVURLAsset URLAssetWithURL:url options:nil];
          [asset.resourceLoader setDelegate:delegate queue:delegateQueue];
          [self playerItemPrepareText:asset assetOptions:[NSDictionary alloc] withCallback:handler];
          return;
      }
  }
    
//  [self setupMuxDataFromSource:source];
  
  if (isNetwork) {
//    [self setupBeaconFromSource:source];
    /* Per #1091, this is not a public API.
     * We need to either get approval from Apple to use this  or use a different approach.
     NSDictionary *headers = [source objectForKey:@"requestHeaders"];
     if ([headers count] > 0) {
       [assetOptions setObject:headers forKey:@"AVURLAssetHTTPHeaderFieldsKey"];
     }
     */
    NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
    [assetOptions setObject:cookies forKey:AVURLAssetHTTPCookiesKey];

#if __has_include(<react-native-video/RCTVideoCache.h>)
    if (!_textTracks) {
      /* The DVURLAsset created by cache doesn't have a tracksWithMediaType property, so trying
       *  to bring in the text track code will crash. I suspect this is because the asset hasn't fully loaded.
       * Until this is fixed, we need to bypass caching when text tracks are specified.
       */
      DebugLog(@"Caching is not supported for uri '%@' because text tracks are not compatible with the cache. Checkout https://github.com/react-native-community/react-native-video/blob/master/docs/caching.md", uri);
      [self playerItemForSourceUsingCache:uri assetOptions:assetOptions withCallback:handler];
      return;
    }
#endif

    AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:assetOptions];
    [self playerItemPrepareText:asset assetOptions:assetOptions withCallback:handler];
    return;
  } else if (isAsset) {
    AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:nil];
    [self playerItemPrepareText:asset assetOptions:assetOptions withCallback:handler];
    return;
  }

  AVURLAsset *asset = [AVURLAsset URLAssetWithURL:[[NSURL alloc] initFileURLWithPath:[[NSBundle mainBundle] pathForResource:uri ofType:type]] options:nil];
  [self playerItemPrepareText:asset assetOptions:assetOptions withCallback:handler];
}

#if __has_include(<react-native-video/RCTVideoCache.h>)

- (void)playerItemForSourceUsingCache:(NSString *)uri assetOptions:(NSDictionary *)options withCallback:(void(^)(AVPlayerItem *))handler {
    NSURL *url = [NSURL URLWithString:uri];
    [_videoCache getItemForUri:uri withCallback:^(RCTVideoCacheStatus videoCacheStatus, AVAsset * _Nullable cachedAsset) {
        switch (videoCacheStatus) {
            case RCTVideoCacheStatusMissingFileExtension: {
                DebugLog(@"Could not generate cache key for uri '%@'. It is currently not supported to cache urls that do not include a file extension. The video file will not be cached. Checkout https://github.com/react-native-community/react-native-video/blob/master/docs/caching.md", uri);
                AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:options];
                [self playerItemPrepareText:asset assetOptions:options withCallback:handler];
                return;
            }
            case RCTVideoCacheStatusUnsupportedFileExtension: {
                DebugLog(@"Could not generate cache key for uri '%@'. The file extension of that uri is currently not supported. The video file will not be cached. Checkout https://github.com/react-native-community/react-native-video/blob/master/docs/caching.md", uri);
                AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:options];
                [self playerItemPrepareText:asset assetOptions:options withCallback:handler];
                return;
            }
            default:
                if (cachedAsset) {
                    DebugLog(@"Playing back uri '%@' from cache", uri);
                    // See note in playerItemForSource about not being able to support text tracks & caching
                    handler([AVPlayerItem playerItemWithAsset:asset]);
                    return;
                }
        }

        DVURLAsset *asset = [[DVURLAsset alloc] initWithURL:url options:options networkTimeout:10000];
        asset.loaderDelegate = self;
        
        /* More granular code to have control over the DVURLAsset
        DVAssetLoaderDelegate *resourceLoaderDelegate = [[DVAssetLoaderDelegate alloc] initWithURL:url];
        resourceLoaderDelegate.delegate = self;
        NSURLComponents *components = [[NSURLComponents alloc] initWithURL:url resolvingAgainstBaseURL:NO];
        components.scheme = [DVAssetLoaderDelegate scheme];
        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:[components URL] options:options];
        [asset.resourceLoader setDelegate:resourceLoaderDelegate queue:dispatch_get_main_queue()];
        */

        handler([AVPlayerItem playerItemWithAsset:asset]);
    }];
}

#pragma mark - DVAssetLoaderDelegate

- (void)dvAssetLoaderDelegate:(DVAssetLoaderDelegate *)loaderDelegate
                  didLoadData:(NSData *)data
                       forURL:(NSURL *)url {
    [_videoCache storeItem:data forUri:[url absoluteString] withCallback:^(BOOL success) {
        DebugLog(@"Cache data stored successfully 🎉");
    }];
}

#endif

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
  if (object == _playerItem) {
    // When timeMetadata is read the event onTimedMetadata is triggered
    if ([keyPath isEqualToString:timedMetadata]) {
      NSArray<AVMetadataItem *> *items = [change objectForKey:@"new"];
      if (items && ![items isEqual:[NSNull null]] && items.count > 0) {
        NSMutableArray *array = [NSMutableArray new];
        for (AVMetadataItem *item in items) {
          NSString *value = (NSString *)item.value;
          NSString *identifier = item.identifier;
          
          if (![value isEqual: [NSNull null]]) {
            NSDictionary *dictionary = [[NSDictionary alloc] initWithObjects:@[value, identifier] forKeys:@[@"value", @"identifier"]];
            
            [array addObject:dictionary];
          }
        }
        
        self.onTimedMetadata(@{
                               @"target": self.reactTag,
                               @"metadata": array
                               });
      }
    }
    
    if ([keyPath isEqualToString:statusKeyPath]) {
      // Handle player item status change.
      if (_playerItem.status == AVPlayerItemStatusReadyToPlay) {
        float duration = CMTimeGetSeconds(_playerItem.asset.duration);
        
        if (isnan(duration)) {
          duration = 0.0;
        }
        
        NSObject *width = @"undefined";
        NSObject *height = @"undefined";
        NSString *orientation = @"undefined";
        
        if ([_playerItem.asset tracksWithMediaType:AVMediaTypeVideo].count > 0) {
          AVAssetTrack *videoTrack = [[_playerItem.asset tracksWithMediaType:AVMediaTypeVideo] objectAtIndex:0];
          width = [NSNumber numberWithFloat:videoTrack.naturalSize.width];
          height = [NSNumber numberWithFloat:videoTrack.naturalSize.height];
          CGAffineTransform preferredTransform = [videoTrack preferredTransform];
          
          if ((videoTrack.naturalSize.width == preferredTransform.tx
               && videoTrack.naturalSize.height == preferredTransform.ty)
              || (preferredTransform.tx == 0 && preferredTransform.ty == 0))
          {
            orientation = @"landscape";
          } else {
            orientation = @"portrait";
          }
        }
        
        if (self.onVideoLoad && _videoLoadStarted) {
          self.onVideoLoad(@{@"duration": [NSNumber numberWithFloat:duration],
                             @"currentTime": [NSNumber numberWithFloat:CMTimeGetSeconds(_playerItem.currentTime)],
                             @"canPlayReverse": [NSNumber numberWithBool:_playerItem.canPlayReverse],
                             @"canPlayFastForward": [NSNumber numberWithBool:_playerItem.canPlayFastForward],
                             @"canPlaySlowForward": [NSNumber numberWithBool:_playerItem.canPlaySlowForward],
                             @"canPlaySlowReverse": [NSNumber numberWithBool:_playerItem.canPlaySlowReverse],
                             @"canStepBackward": [NSNumber numberWithBool:_playerItem.canStepBackward],
                             @"canStepForward": [NSNumber numberWithBool:_playerItem.canStepForward],
                             @"naturalSize": @{
                                 @"width": width,
                                 @"height": height,
                                 @"orientation": orientation
                                 },
                             @"audioTracks": [self getAudioTrackInfo],
                             @"textTracks": [self getTextTrackInfo],
                             @"target": self.reactTag});
        }
        _videoLoadStarted = NO;
        
        [self attachListeners];
        [self applyModifiers];
      } else if (_playerItem.status == AVPlayerItemStatusFailed && self.onVideoError) {
        self.onVideoError(@{@"error": @{@"code": [NSNumber numberWithInteger: _playerItem.error.code],
                                        @"domain": _playerItem.error.domain},
                            @"target": self.reactTag});
      }
    } else if ([keyPath isEqualToString:playbackBufferEmptyKeyPath]) {
      _playerBufferEmpty = YES;
      self.onVideoBuffer(@{@"isBuffering": @(YES), @"target": self.reactTag});
    } else if ([keyPath isEqualToString:playbackLikelyToKeepUpKeyPath]) {
      // Continue playing (or not if paused) after being paused due to hitting an unbuffered zone.
      if ((!(_controls || _fullscreenPlayerPresented) || _playerBufferEmpty) && _playerItem.playbackLikelyToKeepUp) {
        [self setPaused:_paused];
      }
      _playerBufferEmpty = NO;
      self.onVideoBuffer(@{@"isBuffering": @(NO), @"target": self.reactTag});
    }
  } else if (object == _playerLayer) {
    if([keyPath isEqualToString:readyForDisplayKeyPath] && [change objectForKey:NSKeyValueChangeNewKey]) {
      if([change objectForKey:NSKeyValueChangeNewKey] && self.onReadyForDisplay) {
        self.onReadyForDisplay(@{@"target": self.reactTag});
      }
    }
  } else if (object == self.player) {
    if([keyPath isEqualToString:playbackRate]) {
      if(self.onPlaybackRateChange) {
        self.onPlaybackRateChange(@{@"playbackRate": [NSNumber numberWithFloat:self.player.rate],
                                    @"target": self.reactTag});
      }
      if(self.player.rate > 0) {
          [self startDiceBeaconCallsAfter:0];
      } else {
          [_diceBeaconRequst cancel];
      }
      if(_playbackStalled && self.player.rate > 0) {
        if(self.onPlaybackResume) {
          self.onPlaybackResume(@{@"playbackRate": [NSNumber numberWithFloat:self.player.rate],
                                  @"target": self.reactTag});
        }
        _playbackStalled = NO;
      }
    } else if([keyPath isEqualToString:currentItem]) {
      _playerItem = self.player.currentItem;
      [self addPlayerItemObservers];
    }
  } else {
    [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
  }
}

- (void)attachListeners
{
  // listen for end of file
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:AVPlayerItemDidPlayToEndTimeNotification
                                                object:[self.player currentItem]];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerItemDidReachEnd:)
                                               name:AVPlayerItemDidPlayToEndTimeNotification
                                             object:[self.player currentItem]];

  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:AVPlayerItemPlaybackStalledNotification
                                                object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playbackStalled:)
                                               name:AVPlayerItemPlaybackStalledNotification
                                             object:nil];
}

- (void)playbackStalled:(NSNotification *)notification
{
  if(self.onPlaybackStalled) {
    self.onPlaybackStalled(@{@"target": self.reactTag});
  }
  _playbackStalled = YES;
}

- (void)playerItemDidReachEnd:(NSNotification *)notification
{
  if(self.onVideoEnd) {
    self.onVideoEnd(@{@"target": self.reactTag});
  }
  
  if (_repeat) {
    AVPlayerItem *item = [notification object];
    [item seekToTime:kCMTimeZero];
    [self applyModifiers];
  } else {
    [self removePlayerTimeObserver];
  }
}

#pragma mark - Prop setters

- (void)setResizeMode:(NSString*)mode
{
  if( _controls )
  {
    _playerViewController.videoGravity = mode;
  }
  else
  {
    _playerLayer.videoGravity = mode;
  }
  _resizeMode = mode;
}

- (void)setPlayInBackground:(BOOL)playInBackground
{
  _playInBackground = playInBackground;
}

- (void)setAllowsExternalPlayback:(BOOL)allowsExternalPlayback
{
    _allowsExternalPlayback = allowsExternalPlayback;
    self.player.allowsExternalPlayback = _allowsExternalPlayback;
}

- (void)setPlayWhenInactive:(BOOL)playWhenInactive
{
  _playWhenInactive = playWhenInactive;
}

- (void)setIgnoreSilentSwitch:(NSString *)ignoreSilentSwitch
{
  _ignoreSilentSwitch = ignoreSilentSwitch;
  [self applyModifiers];
}

- (void)setPaused:(BOOL)paused
{
  if (paused) {
    [self.player pause];
    [self.player setRate:0.0];
  } else {
    if([_ignoreSilentSwitch isEqualToString:@"ignore"]) {
      [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
    } else if([_ignoreSilentSwitch isEqualToString:@"obey"]) {
      [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryAmbient error:nil];
    }
    [self.player play];
    [self.player setRate:_rate];
    [self startDiceBeaconCallsAfter:0];
  }
  
  _paused = paused;
}

- (float)getCurrentTime
{
  return _playerItem != NULL ? CMTimeGetSeconds(_playerItem.currentTime) : 0;
}

- (void)setCurrentTime:(float)currentTime
{
  NSDictionary *info = @{
                         @"time": [NSNumber numberWithFloat:currentTime],
                         @"tolerance": [NSNumber numberWithInt:100]
                         };
  [self setSeek:info];
}

- (void)setSeek:(NSDictionary *)info
{
  NSNumber *seekTime = info[@"time"];
  NSNumber *seekTolerance = info[@"tolerance"];
  
  int timeScale = 1000;
  
  AVPlayerItem *item = self.player.currentItem;
  if (item && item.status == AVPlayerItemStatusReadyToPlay) {
    // TODO check loadedTimeRanges
    
    CMTime cmSeekTime = CMTimeMakeWithSeconds([seekTime floatValue], timeScale);
    CMTime current = item.currentTime;
    // TODO figure out a good tolerance level
    CMTime tolerance = CMTimeMake([seekTolerance floatValue], timeScale);
    BOOL wasPaused = _paused;
    
    if (CMTimeCompare(current, cmSeekTime) != 0) {
      if (!wasPaused) [self.player pause];
      [self.player seekToTime:cmSeekTime toleranceBefore:tolerance toleranceAfter:tolerance completionHandler:^(BOOL finished) {
        if (!_timeObserver) {
          [self addPlayerTimeObserver];
        }
        if (!wasPaused) {
          [self setPaused:false];
        }
        if(self.onVideoSeek) {
          self.onVideoSeek(@{@"currentTime": [NSNumber numberWithFloat:CMTimeGetSeconds(item.currentTime)],
                             @"seekTime": seekTime,
                             @"target": self.reactTag});
        }
      }];
      
      _pendingSeek = false;
    }
    
  } else {
    // TODO: See if this makes sense and if so, actually implement it
    _pendingSeek = true;
    _pendingSeekTime = [seekTime floatValue];
  }
}

- (void)setRate:(float)rate
{
  _rate = rate;
  [self applyModifiers];
}

- (void)setMuted:(BOOL)muted
{
  _muted = muted;
  [self applyModifiers];
}

- (void)setVolume:(float)volume
{
  _volume = volume;
  [self applyModifiers];
}

- (void)applyModifiers
{
  if (_muted) {
    [self.player setVolume:0];
    [self.player setMuted:YES];
  } else {
    [self.player setVolume:_volume];
    [self.player setMuted:NO];
  }

  [self setSelectedAudioTrack:_selectedAudioTrack];
  [self setSelectedTextTrack:_selectedTextTrack];
  [self setResizeMode:_resizeMode];
  [self setRepeat:_repeat];
  [self setPaused:_paused];
  [self setControls:_controls];
  [self setAllowsExternalPlayback:_allowsExternalPlayback];
  if (_pendingSeek) {
    NSDictionary *info = @{
                           @"time": [NSNumber numberWithFloat:_pendingSeekTime],
                           @"tolerance": [NSNumber numberWithInt:100]
                           };
    [self setSeek:info];
   }
}

- (void)setRepeat:(BOOL)repeat {
  _repeat = repeat;
}

- (void)setMediaSelectionTrackForCharacteristic:(AVMediaCharacteristic)characteristic
                                   withCriteria:(NSDictionary *)criteria
{
    NSString *type = criteria[@"type"];
    AVMediaSelectionGroup *group = [self.player.currentItem.asset
                                    mediaSelectionGroupForMediaCharacteristic:characteristic];
    AVMediaSelectionOption *mediaOption;
    
    if ([type isEqualToString:@"disabled"]) {
        // Do nothing. We want to ensure option is nil
    } else if ([type isEqualToString:@"language"] || [type isEqualToString:@"title"]) {
        NSString *value = criteria[@"value"];
        for (int i = 0; i < group.options.count; ++i) {
            AVMediaSelectionOption *currentOption = [group.options objectAtIndex:i];
            NSString *optionValue;
            if ([type isEqualToString:@"language"]) {
                optionValue = [currentOption extendedLanguageTag];
            } else {
                optionValue = [[[currentOption commonMetadata]
                                valueForKey:@"value"]
                               objectAtIndex:0];
            }
            if ([value isEqualToString:optionValue]) {
                mediaOption = currentOption;
                break;
            }
        }
        //} else if ([type isEqualToString:@"default"]) {
        //  option = group.defaultOption; */
    } else if ([type isEqualToString:@"index"]) {
        if ([criteria[@"value"] isKindOfClass:[NSNumber class]]) {
            int index = [criteria[@"value"] intValue];
            if (group.options.count > index) {
                mediaOption = [group.options objectAtIndex:index];
            }
        }
    } else { // default. invalid type or "system"
        [self.player.currentItem selectMediaOptionAutomaticallyInMediaSelectionGroup:group];
        return;
    }
    
    // If a match isn't found, option will be nil and text tracks will be disabled
    [self.player.currentItem selectMediaOption:mediaOption inMediaSelectionGroup:group];
}

- (void)setSelectedAudioTrack:(NSDictionary *)selectedAudioTrack {
    if (!selectedAudioTrack) return;
    _selectedAudioTrack = selectedAudioTrack;
    [self setMediaSelectionTrackForCharacteristic:AVMediaCharacteristicAudible
                                        withCriteria:_selectedAudioTrack];
}

- (void)setSelectedTextTrack:(NSDictionary *)selectedTextTrack {
    if (!selectedTextTrack) return;
  _selectedTextTrack = selectedTextTrack;
  if (_textTracks) { // sideloaded text tracks
    [self setSideloadedText];
  } else { // text tracks included in the HLS playlist
    [self setMediaSelectionTrackForCharacteristic:AVMediaCharacteristicLegible
                                     withCriteria:_selectedTextTrack];
  }
}

- (void) setSideloadedText {
  NSString *type = _selectedTextTrack[@"type"];
  NSArray *textTracks = [self getTextTrackInfo];
  
  // The first few tracks will be audio & video track
  int firstTextIndex = 0;
  for (firstTextIndex = 0; firstTextIndex < self.player.currentItem.tracks.count; ++firstTextIndex) {
    if ([self.player.currentItem.tracks[firstTextIndex].assetTrack hasMediaCharacteristic:AVMediaCharacteristicLegible]) {
      break;
    }
  }
  
  int selectedTrackIndex = RCTVideoUnset;
  
  if ([type isEqualToString:@"disabled"]) {
    // Do nothing. We want to ensure option is nil
  } else if ([type isEqualToString:@"language"]) {
    NSString *selectedValue = _selectedTextTrack[@"value"];
    for (int i = 0; i < textTracks.count; ++i) {
      NSDictionary *currentTextTrack = [textTracks objectAtIndex:i];
      if ([selectedValue isEqualToString:currentTextTrack[@"language"]]) {
        selectedTrackIndex = i;
        break;
      }
    }
  } else if ([type isEqualToString:@"title"]) {
    NSString *selectedValue = _selectedTextTrack[@"value"];
    for (int i = 0; i < textTracks.count; ++i) {
      NSDictionary *currentTextTrack = [textTracks objectAtIndex:i];
      if ([selectedValue isEqualToString:currentTextTrack[@"title"]]) {
        selectedTrackIndex = i;
        break;
      }
    }
  } else if ([type isEqualToString:@"index"]) {
    if ([_selectedTextTrack[@"value"] isKindOfClass:[NSNumber class]]) {
      int index = [_selectedTextTrack[@"value"] intValue];
      if (textTracks.count > index) {
        selectedTrackIndex = index;
      }
    }
  }
  
  // in the situation that a selected text track is not available (eg. specifies a textTrack not available)
  if (![type isEqualToString:@"disabled"] && selectedTrackIndex == RCTVideoUnset) {
    CFArrayRef captioningMediaCharacteristics = MACaptionAppearanceCopyPreferredCaptioningMediaCharacteristics(kMACaptionAppearanceDomainUser);
    NSArray *captionSettings = (__bridge NSArray*)captioningMediaCharacteristics;
    if ([captionSettings containsObject:AVMediaCharacteristicTranscribesSpokenDialogForAccessibility]) {
      selectedTrackIndex = 0; // If we can't find a match, use the first available track
      NSString *systemLanguage = [[NSLocale preferredLanguages] firstObject];
      for (int i = 0; i < textTracks.count; ++i) {
        NSDictionary *currentTextTrack = [textTracks objectAtIndex:i];
        if ([systemLanguage isEqualToString:currentTextTrack[@"language"]]) {
          selectedTrackIndex = i;
          break;
        }
      }
    }
  }
    
  for (int i = firstTextIndex; i < self.player.currentItem.tracks.count; ++i) {
    BOOL isEnabled = NO;
    if (selectedTrackIndex != RCTVideoUnset) {
      isEnabled = i == selectedTrackIndex + firstTextIndex;
    }
    [self.player.currentItem.tracks[i] setEnabled:isEnabled];
  }
}

-(void) setStreamingText {
  NSString *type = _selectedTextTrack[@"type"];
  AVMediaSelectionGroup *group = [self.player.currentItem.asset
                                  mediaSelectionGroupForMediaCharacteristic:AVMediaCharacteristicLegible];
  AVMediaSelectionOption *mediaOption;
  
  if ([type isEqualToString:@"disabled"]) {
    // Do nothing. We want to ensure option is nil
  } else if ([type isEqualToString:@"language"] || [type isEqualToString:@"title"]) {
    NSString *value = _selectedTextTrack[@"value"];
    for (int i = 0; i < group.options.count; ++i) {
      AVMediaSelectionOption *currentOption = [group.options objectAtIndex:i];
      NSString *optionValue;
      if ([type isEqualToString:@"language"]) {
        optionValue = [currentOption extendedLanguageTag];
      } else {
        optionValue = [[[currentOption commonMetadata]
                        valueForKey:@"value"]
                       objectAtIndex:0];
      }
      if ([value isEqualToString:optionValue]) {
        mediaOption = currentOption;
        break;
      }
    }
    //} else if ([type isEqualToString:@"default"]) {
    //  option = group.defaultOption; */
  } else if ([type isEqualToString:@"index"]) {
    if ([_selectedTextTrack[@"value"] isKindOfClass:[NSNumber class]]) {
      int index = [_selectedTextTrack[@"value"] intValue];
      if (group.options.count > index) {
        mediaOption = [group.options objectAtIndex:index];
      }
    }
  } else { // default. invalid type or "system"
    [self.player.currentItem selectMediaOptionAutomaticallyInMediaSelectionGroup:group];
    return;
  }
  
  // If a match isn't found, option will be nil and text tracks will be disabled
  [self.player.currentItem selectMediaOption:mediaOption inMediaSelectionGroup:group];
}

- (void)setTextTracks:(NSArray*) textTracks;
{
  _textTracks = textTracks;

  // in case textTracks was set after selectedTextTrack
  if (_selectedTextTrack) [self setSelectedTextTrack:_selectedTextTrack];
}

- (NSArray *)getAudioTrackInfo
{
    NSMutableArray *audioTracks = [[NSMutableArray alloc] init];
    AVMediaSelectionGroup *group = [self.player.currentItem.asset
                                    mediaSelectionGroupForMediaCharacteristic:AVMediaCharacteristicAudible];
    for (int i = 0; i < group.options.count; ++i) {
        AVMediaSelectionOption *currentOption = [group.options objectAtIndex:i];
        NSString *title = @"";
        NSArray *values = [[currentOption commonMetadata] valueForKey:@"value"];
        if (values.count > 0) {
            title = [values objectAtIndex:0];
        }
        NSString *language = [currentOption.locale languageCode] ? [currentOption.locale languageCode] : @"";
        NSDictionary *audioTrack = @{
                                    @"index": [NSNumber numberWithInt:i],
                                    @"title": title,
                                    @"language": language
                                    };
        [audioTracks addObject:audioTrack];
    }
    return audioTracks;
}

- (NSArray *)getTextTrackInfo
{
  // if sideloaded, textTracks will already be set
  if (_textTracks) return _textTracks;
  
  // if streaming video, we extract the text tracks
  NSMutableArray *textTracks = [[NSMutableArray alloc] init];
  AVMediaSelectionGroup *group = [self.player.currentItem.asset
                                  mediaSelectionGroupForMediaCharacteristic:AVMediaCharacteristicLegible];
  for (int i = 0; i < group.options.count; ++i) {
    AVMediaSelectionOption *currentOption = [group.options objectAtIndex:i];
    NSString *title = @"";
    NSArray *values = [[currentOption commonMetadata] valueForKey:@"value"];
    if (values.count > 0) {
      title = [values objectAtIndex:0];
    }
    NSString *language = [currentOption extendedLanguageTag] ? [currentOption extendedLanguageTag] : @"";
    NSDictionary *textTrack = @{
                                @"index": [NSNumber numberWithInt:i],
                                @"title": title,
                                @"language": language
                                };
    [textTracks addObject:textTrack];
  }
  return textTracks;
}

- (BOOL)getFullscreen
{
  return _fullscreenPlayerPresented;
}

- (void)setFullscreen:(BOOL)fullscreen
{
  if( fullscreen && !_fullscreenPlayerPresented )
  {
    // Ensure player view controller is not null
    if( !_playerViewController )
    {
      [self usePlayerViewController];
    }
    // Set presentation style to fullscreen
    [_playerViewController setModalPresentationStyle:UIModalPresentationFullScreen];
    
    // Find the nearest view controller
    UIViewController *viewController = [self firstAvailableUIViewController];
    if( !viewController )
    {
      UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
      viewController = keyWindow.rootViewController;
      if( viewController.childViewControllers.count > 0 )
      {
        viewController = viewController.childViewControllers.lastObject;
      }
    }
    if( viewController )
    {
      _presentingViewController = viewController;
      if(self.onVideoFullscreenPlayerWillPresent) {
        self.onVideoFullscreenPlayerWillPresent(@{@"target": self.reactTag});
      }
      [viewController presentViewController:_playerViewController animated:true completion:^{
        _playerViewController.showsPlaybackControls = YES;
        _fullscreenPlayerPresented = fullscreen;
        if(self.onVideoFullscreenPlayerDidPresent) {
          self.onVideoFullscreenPlayerDidPresent(@{@"target": self.reactTag});
        }
      }];
    }
  }
  else if ( !fullscreen && _fullscreenPlayerPresented )
  {
    [self videoPlayerViewControllerWillDismiss:_playerViewController];
    [_presentingViewController dismissViewControllerAnimated:true completion:^{
      [self videoPlayerViewControllerDidDismiss:_playerViewController];
    }];
  }
}

- (void)usePlayerViewController
{
  if( self.player )
  {
    _playerViewController = [self createPlayerViewController:self.player];
    // to prevent video from being animated when resizeMode is 'cover'
    // resize mode must be set before subview is added
    [self.player addObserver:self->_playerViewController forKeyPath:currentItem options:0 context:nil];

    [self setResizeMode:_resizeMode];
    [self addSubview:_playerViewController.view];
    [self setupMux];
  }
}

- (void)usePlayerLayer
{
  if( self.player )
  {
    _playerLayer = [AVPlayerLayer playerLayerWithPlayer:self.player];
    _playerLayer.frame = self.bounds;
    _playerLayer.needsDisplayOnBoundsChange = YES;
    
    // to prevent video from being animated when resizeMode is 'cover'
    // resize mode must be set before layer is added
    [self setResizeMode:_resizeMode];
    [_playerLayer addObserver:self forKeyPath:readyForDisplayKeyPath options:NSKeyValueObservingOptionNew context:nil];
    _playerLayerObserverSet = YES;
    
    [self.layer addSublayer:_playerLayer];
    self.layer.needsDisplayOnBoundsChange = YES;
    [self setupMux];
  }
}

- (void)setControls:(BOOL)controls
{
    NSLog(@">>>>>> controls %s", controls ? "true" : "false");
  #if TARGET_OS_IOS
    if( _controls != controls || (!_playerLayer && !_playerViewController) )
    {
      _controls = controls;
      if( _controls )
      {
        [self removePlayerLayer];
        [self usePlayerViewController];
      }
      else
      {
        [_playerViewController.view removeFromSuperview];
        _playerViewController = nil;
        [self usePlayerLayer];
      }
    }
  #elif TARGET_OS_TV
    if( _controls != controls || !_playerViewController )
    {
      if(!_playerViewController) {
        [self usePlayerViewController];
      }
      _controls = controls;
      _playerViewController.view.userInteractionEnabled = _controls;
      _playerViewController.showsPlaybackControls = _controls;
    }
  #endif
}

- (void)setProgressUpdateInterval:(float)progressUpdateInterval
{
  _progressUpdateInterval = progressUpdateInterval;

  if (_timeObserver) {
    [self removePlayerTimeObserver];
    [self addPlayerTimeObserver];
  }
}

- (void)removePlayerLayer
{
  [_playerLayer removeFromSuperlayer];
  if (_playerLayerObserverSet) {
    [_playerLayer removeObserver:self forKeyPath:readyForDisplayKeyPath];
    _playerLayerObserverSet = NO;
  }
  _playerLayer = nil;
}

#pragma mark - RCTVideoPlayerViewControllerDelegate

- (void)videoPlayerViewControllerWillDismiss:(AVPlayerViewController *)playerViewController
{
  if (_playerViewController == playerViewController && _fullscreenPlayerPresented && self.onVideoFullscreenPlayerWillDismiss)
  {
    self.onVideoFullscreenPlayerWillDismiss(@{@"target": self.reactTag});
  }
}

- (void)videoPlayerViewControllerDidDismiss:(AVPlayerViewController *)playerViewController
{
  if (_playerViewController == playerViewController && _fullscreenPlayerPresented)
  {
    _fullscreenPlayerPresented = false;
    _presentingViewController = nil;
    _playerViewController = nil;
    [self applyModifiers];
    if(self.onVideoFullscreenPlayerDidDismiss) {
      self.onVideoFullscreenPlayerDidDismiss(@{@"target": self.reactTag});
    }
  }
}

- (void)didRequestAdTagParametersUpdate:(NSTimeInterval)timeIntervalSince1970 {
  if(self.onRequireAdParameters) {
    NSNumber* _timeIntervalSince1970 = [[NSNumber alloc] initWithDouble:timeIntervalSince1970];
    self.onPlaybackRateChange(@{@"date": _timeIntervalSince1970});
  }
}

- (void)didFailWithError:(AVDorisError)error errorData:(AVDorisErrorData *)errorData {
    if(self.onVideoError) {
        self.onVideoError(@{@"error": @{@"code": [[NSNumber alloc] initWithInteger:errorData.code],
                                        @"domain": @"IMA",
                                        @"messages": @[errorData.message]
        },
                            @"target": self.reactTag});
    }
}

#pragma mark - React View Management

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
  // We are early in the game and somebody wants to set a subview.
  // That can only be in the context of playerViewController.
  if( !_controls && !_playerLayer && !_playerViewController )
  {
    [self setControls:true];
  }
  
  if( _controls )
  {
    view.frame = self.bounds;
    [_playerViewController.contentOverlayView insertSubview:view atIndex:atIndex];
  }
  else
  {
    RCTLogError(@"video cannot have any subviews");
  }
  return;
}

- (void)removeReactSubview:(UIView *)subview
{
  if( _controls )
  {
    [subview removeFromSuperview];
  }
  else
  {
    RCTLogError(@"video cannot have any subviews");
  }
  return;
}

- (void)layoutSubviews
{
  [super layoutSubviews];
  if( _controls )
  {
    _playerViewController.view.frame = self.bounds;
    
    // also adjust all subviews of contentOverlayView
    for (UIView* subview in _playerViewController.contentOverlayView.subviews) {
      subview.frame = self.bounds;
    }
  }
  else
  {
    [CATransaction begin];
    [CATransaction setAnimationDuration:0];
    _playerLayer.frame = self.bounds;
    [CATransaction commit];
  }
}

#pragma mark - Lifecycle

- (void)removeFromSuperview
{
  [self.player pause];
  if (_playbackRateObserverRegistered) {
    [self.player removeObserver:self forKeyPath:playbackRate context:nil];
    _playbackRateObserverRegistered = NO;
  }
  if (_currentItemObserverRegistered) {
    [self.player removeObserver:self forKeyPath:currentItem context:nil];
    _currentItemObserverRegistered = NO;
  }
  self.player = nil;
  
  [self removePlayerLayer];
  
  [_playerViewController.view removeFromSuperview];
  _playerViewController = nil;
  
  [self removePlayerTimeObserver];
  [self removePlayerItemObservers];
  
  _eventDispatcher = nil;
  [[NSNotificationCenter defaultCenter] removeObserver:self];
  
  [super removeFromSuperview];
}

- (void)fetchAppIdWithCompletion:(void (^)(NSNumber* _Nullable appId))completionBlock {
  NSURL* url = [self iTunesURLFromString];
  NSMutableURLRequest* request = [[NSMutableURLRequest alloc] initWithURL:url];
  [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
  request.HTTPMethod = @"GET";
  [[NSURLSession.sharedSession dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
    completionBlock([self parseAppIdwithData:data response:response error:error]);
  }] resume];
}

- (NSURL *)iTunesURLFromString {
  NSURLComponents* components = [NSURLComponents new];
  components.scheme = @"https";
  components.host = @"itunes.apple.com";
  components.path = @"/lookup";
  
  NSURLQueryItem* item = [[NSURLQueryItem alloc] initWithName:@"bundleId" value:NSBundle.mainBundle.bundleIdentifier];
  components.queryItems = @[item];
  return components.URL;
}

- (nullable NSNumber*)parseAppIdwithData:(nullable NSData*)data response:(NSURLResponse*)response error:(NSError*)error {
  if (error) {
    return nil;
  }
  
  if (data) {
    NSError *error = nil;
    id object = [NSJSONSerialization
                 JSONObjectWithData:data
                 options:0
                 error:&error];
    
    if(error) {
      return nil;
    } else if([object isKindOfClass:[NSDictionary class]]) {
      NSDictionary *dict = object;
      NSArray* results = [dict mutableArrayValueForKey:@"results"];
      NSDictionary *dict2 = [results firstObject];
      id appid = [dict2 objectForKey:@"trackId"];
      if ([appid isKindOfClass:NSNumber.class]) {
        return appid;
      }
    }
  }
  return  nil;
}

@end
