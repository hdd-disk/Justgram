package com.exteragram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import com.exteragram.messenger.badges.BadgesController;
import com.exteragram.messenger.config.BottomNavigationBar;
import com.exteragram.messenger.plugins.PluginsConstants;
import com.exteragram.messenger.plugins.PluginsController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.telegram.messenger.ApplicationLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ExteraConfig {
    private static final Object sync = new Object();
    private static boolean configLoaded;
    private static boolean initialized;

    public static final Gson GSON = new Gson();
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;
    public static boolean pluginsEngine;
    public static boolean pluginsSafeMode;
    public static boolean pluginsDevMode;
    public static boolean pluginsCompactView;
    public static boolean pluginsDisableArtOpts;
    public static boolean pluginsPySdkAutoUpdate;
    public static boolean pluginsPySdkBetaVersions;
    public static long sdkUpdateScheduleTimestamp;
    public static Set<String> pinnedPlugins = Collections.emptySet();

    private static ArrayList<Integer> mainMenuLayout = new ArrayList<>();
    private static ArrayList<Integer> mainMenuHiddenItems = new ArrayList<>();

    private ExteraConfig() {
    }

    public enum DrawerItem {
        PLUGINS(102);

        public final int id;

        DrawerItem(int id) {
            this.id = id;
        }

        public static DrawerItem getById(int id) {
            for (DrawerItem item : values()) {
                if (item.id == id) {
                    return item;
                }
            }
            return null;
        }
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static int getSectionRadiusDp() {
        return preferences != null ? preferences.getInt("sectionRadius", 16) : 16;
    }

    public static boolean getNavigationDrawer() {
        return preferences != null ? preferences.getBoolean("navigationDrawer", true) : true;
    }

    public static void setNavigationDrawer(boolean value) {
        if (editor != null) {
            editor.putBoolean("navigationDrawer", value).apply();
        }
    }

    public static boolean getImmersiveDrawerAnimation() {
        return preferences != null ? preferences.getBoolean("immersiveDrawerAnimation", false) : false;
    }

    public static void setImmersiveDrawerAnimation(boolean value) {
        if (editor != null) {
            editor.putBoolean("immersiveDrawerAnimation", value).apply();
        }
    }

    public static boolean getSpringAnimations() {
        return preferences != null ? preferences.getBoolean("springAnimations", true) : true;
    }

    public static void setSpringAnimations(boolean value) {
        if (editor != null) {
            editor.putBoolean("springAnimations", value).apply();
        }
    }

    public static int getTabletMode() {
        return preferences != null ? preferences.getInt("tabletMode", 0) : 0;
    }

    public static void setTabletMode(int value) {
        if (editor != null) {
            editor.putInt("tabletMode", value).apply();
        }
    }

    public static float getPredictiveBackIntensity() {
        return preferences != null ? preferences.getFloat("predictiveBackIntensity", 1.0f) : 1.0f;
    }

    public static void setPredictiveBackIntensity(float value) {
        if (editor != null) {
            editor.putFloat("predictiveBackIntensity", value).apply();
        }
    }

    public static boolean getHidePhoneNumber() {
        return preferences != null ? preferences.getBoolean("hidePhoneNumber", false) : false;
    }

    public static float getAvatarCorners() {
        return preferences != null ? preferences.getFloat("avatarCorners", 28.0f) : 28.0f;
    }

    public static int getAvatarCorners(float size) {
        float corners = getAvatarCorners();
        if (corners == 0.0f) {
            return 0;
        }
        return (int) ((corners * size) / 56.0f);
    }

    public static ArrayList<Integer> getMainMenuLayout() {
        return mainMenuLayout;
    }

    public static ArrayList<Integer> getMainMenuHiddenItems() {
        return mainMenuHiddenItems;
    }

    public static ArrayList<Integer> getDefaultMainMenuLayout() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(MainMenuItem.ARCHIVE.getId());
        if (BottomNavigationBar.hidden()) {
            arrayList.add(MainMenuItem.PROFILE.getId());
        }
        arrayList.add(MainMenuItem.NEW_GROUP.getId());
        if (BottomNavigationBar.hidden()) {
            arrayList.add(MainMenuItem.CONTACTS.getId());
        }
        arrayList.add(MainMenuItem.SAVED.getId());
        arrayList.add(MainMenuItem.FEED.getId());
        arrayList.add(MainMenuItem.BOTS.getId());
        if (BottomNavigationBar.hidden()) {
            arrayList.add(MainMenuItem.SETTINGS.getId());
        }
        return arrayList;
    }

    public static void ensureSettingsVisibility() {
        if (BottomNavigationBar.hidden()) {
            int id = MainMenuItem.SETTINGS.getId();
            if (mainMenuLayout.contains(id)) {
                return;
            }
            mainMenuHiddenItems.remove((Integer) id);
            mainMenuLayout.add(id);
            saveMainMenuLayout();
        }
    }

    public static void sanitizeMenu() {
        boolean changed = mainMenuLayout.removeIf(num -> num != MainMenuItem.DIVIDER.getId() && MainMenuItem.getById(num) == null);
        changed |= mainMenuHiddenItems.removeIf(num -> num != MainMenuItem.DIVIDER.getId() && MainMenuItem.getById(num) == null);
        for (MainMenuItem mainMenuItem : MainMenuItem.values()) {
            if (mainMenuItem != MainMenuItem.DIVIDER && (mainMenuItem != MainMenuItem.PLUGINS || PluginsController.isPluginEngineSupported())) {
                if (!mainMenuLayout.contains(mainMenuItem.getId()) && !mainMenuHiddenItems.contains(mainMenuItem.getId())) {
                    mainMenuHiddenItems.add(mainMenuItem.getId());
                    changed = true;
                }
            }
        }
        if (changed) {
            saveMainMenuLayout();
        }
    }

    public static void saveMainMenuLayout() {
        if (editor != null) {
            editor.putString("mainMenuLayout", GSON.toJson(mainMenuLayout))
                  .putString("mainMenuHiddenItems", GSON.toJson(mainMenuHiddenItems))
                  .apply();
        }
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            Context context = ApplicationLoader.applicationContext;
            if (context == null) {
                return;
            }
            preferences = context.getSharedPreferences("exteraconfig", Context.MODE_PRIVATE);
            editor = preferences.edit();
            pluginsEngine = PluginsController.isPluginEngineSupported() && preferences.getBoolean("pluginsEngine", false);
            pluginsSafeMode = preferences.getBoolean("pluginsSafeMode", false);
            pluginsDevMode = preferences.getBoolean("pluginsDevMode", false);
            pluginsCompactView = preferences.getBoolean("pluginsCompactView", false);
            pluginsDisableArtOpts = preferences.getBoolean("pluginsDisableArtOpts", false);
            pluginsPySdkAutoUpdate = preferences.getBoolean("pluginsPySdkAutoUpdate", false);
            pluginsPySdkBetaVersions = preferences.getBoolean("pluginsPySdkBetaVersions", false);
            sdkUpdateScheduleTimestamp = preferences.getLong("sdkUpdateScheduleTimestamp", 0L);
            pinnedPlugins = new HashSet<>(preferences.getStringSet("pinnedPlugins", Collections.emptySet()));
            BottomNavigationBar.setMode(preferences.getInt("bottomNavigationBarMode", 0));

            String layoutJson = preferences.getString("mainMenuLayout", null);
            String hiddenJson = preferences.getString("mainMenuHiddenItems", null);
            if (layoutJson != null) {
                mainMenuLayout = GSON.fromJson(layoutJson, new TypeToken<ArrayList<Integer>>(){}.getType());
                if (hiddenJson != null) {
                    mainMenuHiddenItems = GSON.fromJson(hiddenJson, new TypeToken<ArrayList<Integer>>(){}.getType());
                } else {
                    mainMenuHiddenItems = new ArrayList<>();
                }
            } else {
                mainMenuLayout = new ArrayList<>();
                mainMenuHiddenItems = new ArrayList<>();
                mainMenuLayout.addAll(getDefaultMainMenuLayout());
                for (MainMenuItem item : MainMenuItem.values()) {
                    if (item != MainMenuItem.DIVIDER && !mainMenuLayout.contains(item.getId()) && (item != MainMenuItem.PLUGINS || PluginsController.isPluginEngineSupported())) {
                        mainMenuHiddenItems.add(item.getId());
                    }
                }
                saveMainMenuLayout();
            }
            if (!PluginsController.isPluginEngineSupported()) {
                int id = MainMenuItem.PLUGINS.getId();
                mainMenuLayout.remove((Integer) id);
                mainMenuHiddenItems.remove((Integer) id);
            } else {
                int id = MainMenuItem.PLUGINS.getId();
                if (!mainMenuLayout.contains(id) && !mainMenuHiddenItems.contains(id)) {
                    mainMenuHiddenItems.add(id);
                }
            }
            int feedId = MainMenuItem.FEED.getId();
            if (!mainMenuLayout.contains(feedId) && !mainMenuHiddenItems.contains(feedId)) {
                mainMenuLayout.add(feedId);
                saveMainMenuLayout();
            }
            ensureSettingsVisibility();
            sanitizeMenu();

            configLoaded = true;
        }
    }

    public static void reloadConfig() {
        synchronized (sync) {
            configLoaded = false;
        }
        loadConfig();
    }

    public static void init() {
        synchronized (sync) {
            if (initialized) {
                return;
            }
            initialized = true;
        }
        loadConfig();
        BadgesController.INSTANCE.init();
        PluginsController.getInstance().init(() -> PluginsController.getInstance().executeOnAppEvent(PluginsConstants.APP_START));
    }
}
