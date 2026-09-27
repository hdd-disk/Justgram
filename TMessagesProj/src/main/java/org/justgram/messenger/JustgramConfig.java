package org.justgram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public class JustgramConfig {

    private static final Object sync = new Object();
    private static boolean loaded = false;

    public static boolean disableAds = true;
    public static boolean showAccountId = true;
    public static boolean fingerprintProtection = true;
    public static boolean webSocketTransport = false;
    public static String webSocketDomain = "";
    public static boolean hideTabsSubtitles = false;
    public static float liquidGlassOpacity = 0.85f;
    public static boolean sectionsSeparatedHeaders = true;
    public static boolean adaptiveChatTitle = true;
    public static boolean hideCallButton = false;

    public static boolean altSoundIn = false;
    public static boolean useChatAttachMediaMenu = false;
    public static boolean iOSMessageInputField = false;

    public static boolean xrayRandomPort = false;
    public static int xrayLocalPort = 25565;
    public static String xrayProfilesJson = "[]";
    public static String xraySubscriptionsJson = "[]";
    public static String xrayActiveProfileId = null;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (loaded) {
                return;
            }
            Context context = ApplicationLoader.applicationContext;
            if (context == null) {
                return;
            }
            SharedPreferences preferences = getSettings();
            disableAds = preferences.getBoolean("disableAds", true);
            showAccountId = preferences.getBoolean("showAccountId", true);
            fingerprintProtection = preferences.getBoolean("fingerprintProtection", true);
            webSocketTransport = preferences.getBoolean("webSocketTransport", false);
            webSocketDomain = preferences.getString("webSocketDomain", "");
            hideTabsSubtitles = preferences.getBoolean("hideTabsSubtitles", false);
            liquidGlassOpacity = preferences.getFloat("liquidGlassOpacity", 0.85f);
            sectionsSeparatedHeaders = preferences.getBoolean("sectionsSeparatedHeaders", true);
            adaptiveChatTitle = preferences.getBoolean("adaptiveChatTitle", true);
            hideCallButton = preferences.getBoolean("hideCallButton", false);
            altSoundIn = preferences.getBoolean("altSoundIn", false);
            useChatAttachMediaMenu = preferences.getBoolean("useChatAttachMediaMenu", false);
            iOSMessageInputField = preferences.getBoolean("iOSMessageInputField", false);

            xrayRandomPort = preferences.getBoolean("xrayRandomPort", false);
            xrayLocalPort = preferences.getInt("xrayLocalPort", 25565);
            xrayProfilesJson = preferences.getString("xrayProfilesJson", "[]");
            xraySubscriptionsJson = preferences.getString("xraySubscriptionsJson", "[]");
            xrayActiveProfileId = preferences.getString("xrayActiveProfileId", null);

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
            editor.putBoolean("sectionsSeparatedHeaders", sectionsSeparatedHeaders);
            editor.putBoolean("adaptiveChatTitle", adaptiveChatTitle);
            editor.putBoolean("hideCallButton", hideCallButton);
            editor.putBoolean("altSoundIn", altSoundIn);
            editor.putBoolean("useChatAttachMediaMenu", useChatAttachMediaMenu);
            editor.putBoolean("iOSMessageInputField", iOSMessageInputField);

            editor.putBoolean("xrayRandomPort", xrayRandomPort);
            editor.putInt("xrayLocalPort", xrayLocalPort);
            editor.putString("xrayProfilesJson", xrayProfilesJson);
            editor.putString("xraySubscriptionsJson", xraySubscriptionsJson);
            editor.putString("xrayActiveProfileId", xrayActiveProfileId);

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
