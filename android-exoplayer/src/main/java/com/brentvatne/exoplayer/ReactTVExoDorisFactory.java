package com.brentvatne.exoplayer;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AdViewProvider;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector.Parameters;

import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
import com.diceplatform.doris.common.ad.ui.AdLabels;
import com.diceplatform.doris.entity.DorisAdEvent.AdType;
import com.diceplatform.doris.entity.TracksPolicy;
import com.diceplatform.doris.ext.imacsai.ExoDorisImaCsaiBuilder;
import com.diceplatform.doris.ext.imacsailive.ExoDorisImaCsaiLiveBuilder;
import com.diceplatform.doris.ext.imadai.ExoDorisImaDaiBuilder;
import com.diceplatform.doris.ext.yossai.ExoDorisYoSsaiBuilder;
import com.diceplatform.doris.plugin.Plugin;

import java.util.List;

public final class ReactTVExoDorisFactory {

    public ExoDoris createPlayer(
            @NonNull Context context,
            AdType adType,
            int loadBufferMs,
            long forwardIncrementMs,
            long rewindIncrementMs,
            @Nullable AdViewProvider adViewProvider,
            TracksPolicy tracksPolicy) {
        return createPlayer(
                context,
                adType,
                true,
                null,
                loadBufferMs,
                forwardIncrementMs,
                rewindIncrementMs,
                null,
                null,
                adViewProvider,
                tracksPolicy);
    }

    public ExoDoris createPlayer(
            @NonNull Context context,
            AdType adType,
            boolean playWhenReady,
            @Nullable String userAgent,
            int loadBufferMs,
            long forwardIncrementMs,
            long rewindIncrementMs,
            @Nullable List<Plugin> plugins,
            @Nullable Parameters.Builder parametersBuilder,
            @Nullable AdViewProvider adViewProvider,
            @Nullable TracksPolicy tracksPolicy) {
        final ExoDorisBuilder builder;
        if (adType == AdType.YO_SSAI) {
            builder = new ExoDorisYoSsaiBuilder(context).setAdViewProvider(checkNotNull(adViewProvider)).setAdLabels(new AdLabels());
        } else if (adType == AdType.IMA_DAI) {
            builder = new ExoDorisImaDaiBuilder(context).setAdViewProvider(checkNotNull(adViewProvider));
        } else if (adType == AdType.IMA_CSAI_LIVE) {
            builder = new ExoDorisImaCsaiLiveBuilder(context).setAdViewProvider(checkNotNull(adViewProvider));
        } else if (adType == AdType.IMA_CSAI) {
            builder = new ExoDorisImaCsaiBuilder(context).setAdViewProvider(checkNotNull(adViewProvider));
        } else {
            builder = new ExoDorisBuilder(context);
        }

        return builder
                .setPlayWhenReady(playWhenReady)
                .setUserAgent(userAgent)
                .setLoadBufferMs(loadBufferMs)
                .setForwardIncrementMs(forwardIncrementMs)
                .setRewindIncrementMs(rewindIncrementMs)
                .setPlugins(plugins)
                .setParamsBuilder(parametersBuilder)
                .setTracksPolicy(tracksPolicy)
                .build();
    }
}
