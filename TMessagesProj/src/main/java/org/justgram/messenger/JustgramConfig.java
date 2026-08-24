package org.justgram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public class JustgramConfig {

    private static final Object sync = new Object();
    private static boolean loaded = false;

    public static boolean disableAds = true;
    public static boolean showAccountId = true;
    public static boolean fingerprintProtection = false;
    public static boolean webSocketTransport = false;
    public static String webSocketDomain = "";
    public static boolean hideTabsSubtitles = false;
    public static float liquidGlassOpacity = 0.85f;

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
            showAccountId = preferences.getBoolean("showAccountId", true);
            fingerprintProtection = preferences.getBoolean("fingerprintProtection", false);
            hideTabsSubtitles = preferences.getBoolean("hideTabsSubtitles", false);
            liquidGlassOpacity = preferences.getFloat("liquidGlassOpacity", 0.85f);
            loaded = true;
        }
    }

    public static void saveConfig() {
        synchronized (sync) {
            SharedPreferences preferences = getSettings();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("disableAds", disableAds);
            editor.putBoolean("showAccountId", showAccountId);
            editor.putBoolean("fingerprintProtection", fingerprintProtection);
            editor.putBoolean("webSocketTransport", webSocketTransport);
            editor.putString("webSocketDomain", webSocketDomain);
            editor.putBoolean("hideTabsSubtitles", hideTabsSubtitles);
            editor.putFloat("liquidGlassOpacity", liquidGlassOpacity);
            editor.apply();
        }
    }

    public static SharedPreferences getSettings() {
        return ApplicationLoader.applicationContext.getSharedPreferences("JustgramConfig", Context.MODE_PRIVATE);
    }

    public static void toggleFingerprintProtection() {
        fingerprintProtection = !fingerprintProtection;
        saveConfig();
    }
}
