/*
 * This is the source code of Telegram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class BuildVars {

    public static boolean DEBUG_VERSION = BuildConfig.DEBUG_VERSION;
    public static boolean LOGS_ENABLED = BuildConfig.DEBUG_VERSION;
    public static boolean DEBUG_PRIVATE_VERSION = BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = true;
    public static boolean NO_SCOPED_STORAGE = Build.VERSION.SDK_INT <= 29;
    public static String BUILD_VERSION_STRING = BuildConfig.BUILD_VERSION_STRING;

    public static int BUILD_VERSION_CODE = 69919;

    public static String PACKAGE_INSTALLER = "com.google.android.packageinstaller";
    public static String PACKAGE_ORIGINAL = "org.telegram.messenger.web";

    public static HashSet<String> LOCKED_LOCAL_STRINGS = new HashSet<>(Arrays.asList(
            "AppName",
            "Page1Message",
            "TelegramVersion",
            "UnsupportedMedia2"
    ));

    public static int APP_ID = 4;
    public static String APP_HASH = "014b35b6184100b085b0d0572f9b5103";

    // SafetyNet key for Google Identity SDK, disabled on FOSS builds
    public static String SAFETYNET_KEY = "";
    public static String PLAYSTORE_APP_URL = "";
    public static String HUAWEI_STORE_URL = "";
    public static String GOOGLE_AUTH_CLIENT_ID = "";

    // You can use this flag to disable Google Play Billing (If you're making fork and want it to be in Google Play)
    public static boolean IS_BILLING_UNAVAILABLE = false;

    // works only on official app ids, disable on your forks
    public static boolean SUPPORTS_PASSKEYS = true;

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION);
            if (LOGS_ENABLED) {
                final Thread.UncaughtExceptionHandler pastHandler = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
                    FileLog.fatal(exception, false);
                    if (pastHandler != null) {
                        pastHandler.uncaughtException(thread, exception);
                    }
                });
            }
        }
    }

    public static boolean useInvoiceBilling() {
        return true;
    }

    public static boolean isBetaApp() {
        return false;
    }


    public static boolean isHuaweiStoreApp() {
        return false;
    }

    public static String getSmsHash() {
        return "w0lkcmTZkKh";
    }
}
