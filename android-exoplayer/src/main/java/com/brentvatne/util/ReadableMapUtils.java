package com.brentvatne.util;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import javax.annotation.Nullable;

/**
 * Utility class for that provides a way to safely read from a {@link ReadableMap}.
 */
public class ReadableMapUtils {

    @Nullable
    public static ReadableMap getMap(@Nullable ReadableMap map, @NonNull String key) {
        if (map == null) {
            return null;
        }
        return map.hasKey(key) ? map.getMap(key) : null;
    }

    @Nullable
    public static ReadableArray getArray(@Nullable ReadableMap map, @NonNull String key) {
        if (map == null) {
            return null;
        }
        return map.hasKey(key) ? map.getArray(key) : null;
    }

    @Nullable
    public static String getString(@Nullable ReadableMap map, @NonNull String key) {
        if (map == null) {
            return null;
        }
        return map.hasKey(key) ? map.getString(key) : null;
    }

    public static boolean getBoolean(@Nullable ReadableMap map, @NonNull String key) {
        if (map == null) {
            return false;
        }
        return map.hasKey(key) && map.getBoolean(key);
    }

    public static int getInt(@Nullable ReadableMap map, @NonNull String key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        return map.hasKey(key) ? map.getInt(key) : defaultValue;
    }

    public static int getInt(@Nullable ReadableMap map, @NonNull String key) {
        return getInt(map, key, 0);
    }

    public static double getDouble(@Nullable ReadableMap map, @NonNull String key) {
        if (map == null) {
            return 0;
        }
        return map.hasKey(key) ? map.getDouble(key) : 0;
    }

    public static boolean contain(ReadableArray array, ReadableMap map) {
        for (int i = 0; i < array.size(); i++) {
            if (array.getMap(i).equals(map)) {
                return true;
            }
        }
        return false;
    }

    public static ReadableMap updateConfigMuxDataPlayerName(ReadableMap src, String name) {
        if (src == null || !src.hasKey("config")) return src;
        ReadableMap config = src.getMap("config");
        if (config == null || !config.hasKey("muxData")) return src;
        ReadableMap muxData = config.getMap("muxData");
        if (muxData == null || !muxData.hasKey("playerName")) return src;
        String newPlayerName = muxData.getString("playerName") + "-" + name;
        WritableMap newMuxData = Arguments.createMap();
        newMuxData.merge(muxData);
        newMuxData.putString("playerName", newPlayerName);
        WritableMap newConfig = Arguments.createMap();
        newConfig.merge(config);
        newConfig.putMap("muxData", newMuxData);
        WritableMap newSrc = Arguments.createMap();
        newSrc.merge(src);
        newSrc.putMap("config", newConfig);
        return newSrc;
    }
}
