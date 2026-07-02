package com.clxmhcs.dianshivideo.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/** Stores only local playback positions; no account or remote tracking is used. */
public final class WatchHistory {
    private static final String PREFS = "watch_history";
    private static final int MINIMUM_POSITION_MS = 5_000;

    private WatchHistory() {
    }

    public static int position(Context context, String videoId) {
        return preferences(context).getInt(videoId, 0);
    }

    public static void save(Context context, String videoId, int positionMs, int durationMs) {
        SharedPreferences.Editor editor = preferences(context).edit();
        if (durationMs > 0 && positionMs >= durationMs - MINIMUM_POSITION_MS) {
            editor.remove(videoId);
        } else if (positionMs > 0) {
            editor.putInt(videoId, positionMs);
        }
        editor.apply();
    }

    public static void clear(Context context, String videoId) {
        preferences(context).edit().remove(videoId).apply();
    }

    public static List<VideoItem> inProgress(Context context, List<CatalogSection> sections) {
        List<VideoItem> result = new ArrayList<>();
        for (CatalogSection section : sections) {
            for (VideoItem item : section.items) {
                if (position(context, item.id) >= MINIMUM_POSITION_MS) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
