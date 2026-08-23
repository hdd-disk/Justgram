package org.justgram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public class JustgramConfig {

    private static final Object sync = new Object();
    private static boolean loaded = false;

    public static boolean disableAds = true;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (loaded) {
                return;
            }
            SharedPreferences preferences = getSettings();
            disableAds = preferences.getBoolean("disableAds", true);
            loaded = true;
        }
    }

    public static void saveConfig() {
        synchronized (sync) {
            SharedPreferences preferences = getSettings();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("disableAds", disableAds);
            editor.apply();
        }
    }

    public static SharedPreferences getSettings() {
        return ApplicationLoader.applicationContext.getSharedPreferences("JustgramConfig", Context.MODE_PRIVATE);
    }
}
