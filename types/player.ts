import { ViewProps } from 'react-native';

import { IVideoPlayerButtons } from './buttons';
import { IVideoPlayerCallbacks } from './callbacks';
import { IVideoPlayerMetadata } from './metadata';
import { VideoResizeMode } from './resizeMode';
import { IVideoPlayerSource } from './source';
import { IVideoPlayerTranslations } from './translations';
import { IVideoPlayerTheme } from './theme';
import { IVideoBufferConfig } from './buffer';
import { IRelatedVideos } from './relatedVideos';
import { INowPlaying } from "./nowPlaying";

export interface IVideoPlayer extends IVideoPlayerCallbacks, ViewProps {
  audioOnly?: boolean;
  bufferConfig?: IVideoBufferConfig;
  buttons?: IVideoPlayerButtons;
  colorProgressBar?: string;
  controls?: boolean;
  disableFocus?: boolean;
  hasEpg?: boolean;
  hasStats?: boolean;
  height?: number;
  isFavourite?: boolean;
  isInWatchlist?: boolean;
  labelFontName?: string;
  locale?: string;
  live?: boolean;
  mediaKeys?: boolean;
  metadata?: IVideoPlayerMetadata;
  muted?: boolean;
  nowPlaying?: INowPlaying;
  overlayAutoHideTimeout?: number;
  poster?: string;
  paused?: boolean;
  playInBackground?: boolean;
  source: IVideoPlayerSource;
  relatedVideos?: IRelatedVideos;
  resizeMode: VideoResizeMode;
  repeat?: boolean;
  theme?: IVideoPlayerTheme;
  translations?: IVideoPlayerTranslations;
  stateMiddleCoreControls?: string;
  selectedAudioTrack?: any // TODO
  width?: number;
}
