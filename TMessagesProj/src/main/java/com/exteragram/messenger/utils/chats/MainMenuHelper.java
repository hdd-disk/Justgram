package com.exteragram.messenger.utils.chats;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.exteragram.messenger.MainMenuItem;
import com.exteragram.messenger.plugins.PluginsController;
import com.exteragram.messenger.plugins.hooks.MenuItemRecord;
import com.exteragram.messenger.plugins.ui.PluginsActivity;
import com.exteragram.messenger.plugins.utils.MenuContextBuilder;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.CallLogActivity;
import org.telegram.ui.CameraScanActivity;
import org.telegram.ui.ChannelCreateActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.WebAppDisclaimerAlert;
import org.telegram.ui.bots.BotWebViewSheet;
import org.telegram.ui.web.SearchEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class MainMenuHelper {

    public static final class MenuItemInfo {
        private final int iconRes;
        private final Runnable onClick;
        private final Runnable onLongClick;
        private final CharSequence text;

        public MenuItemInfo(int iconRes, CharSequence text, Runnable onClick, Runnable onLongClick) {
            this.iconRes = iconRes;
            this.text = text;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }

        public int iconRes() {
            return this.iconRes;
        }

        public Runnable onClick() {
            return this.onClick;
        }

        public Runnable onLongClick() {
            return this.onLongClick;
        }

        public CharSequence text() {
            return this.text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MenuItemInfo)) return false;
            MenuItemInfo that = (MenuItemInfo) o;
            return iconRes == that.iconRes &&
                    Objects.equals(onClick, that.onClick) &&
                    Objects.equals(onLongClick, that.onLongClick) &&
                    Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(iconRes, text, onClick, onLongClick);
        }
    }

    public static final class MenuContext {
        private final Runnable archiveClick;
        private final int currentAccount;
        private final BaseFragment fragment;
        private final Map<String, Object> pluginContextData;

        public MenuContext(int currentAccount, BaseFragment fragment, Runnable archiveClick, Map<String, Object> pluginContextData) {
            this.currentAccount = currentAccount;
            this.fragment = fragment;
            this.archiveClick = archiveClick;
            this.pluginContextData = pluginContextData;
        }

        public Runnable archiveClick() {
            return this.archiveClick;
        }

        public int currentAccount() {
            return this.currentAccount;
        }

        public BaseFragment fragment() {
            return this.fragment;
        }

        public Map<String, Object> pluginContextData() {
            return this.pluginContextData;
        }
    }

    public static final class AttachMenuBotInfo {
        private final TLRPC.TL_attachMenuBot bot;
        private final int iconRes;
        private final Runnable onClick;
        private final Runnable onLongClick;
        private final CharSequence text;

        public AttachMenuBotInfo(int iconRes, CharSequence text, TLRPC.TL_attachMenuBot bot, Runnable onClick, Runnable onLongClick) {
            this.iconRes = iconRes;
            this.text = text;
            this.bot = bot;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }

        public TLRPC.TL_attachMenuBot bot() {
            return this.bot;
        }

        public int iconRes() {
            return this.iconRes;
        }

        public Runnable onClick() {
            return this.onClick;
        }

        public Runnable onLongClick() {
            return this.onLongClick;
        }

        public CharSequence text() {
            return this.text;
        }
    }

    public static MenuContext createMenuContext(int currentAccount, BaseFragment fragment) {
        return new MenuContext(currentAccount, fragment, null, null);
    }

    public static MenuContext createMenuContext(int currentAccount, BaseFragment fragment, Runnable archiveClick, Map<String, Object> pluginContextData) {
        return new MenuContext(currentAccount, fragment, archiveClick, pluginContextData);
    }

    public static Map<String, Object> createPluginContextData(int account, BaseFragment fragment) {
        MenuContextBuilder builder = MenuContextBuilder.create().withAccount(account);
        if (fragment != null) {
            builder.withContext(fragment.getContext() != null ? fragment.getContext() : fragment.getParentActivity());
        }
        TLRPC.User currentUser = UserConfig.getInstance(account).getCurrentUser();
        if (currentUser != null) {
            builder.withUser(currentUser);
        }
        return builder.build();
    }

    public static List<MenuItemInfo> resolveDrawerMenuItems(int id, MenuContext menuContext) {
        MainMenuItem item = MainMenuItem.getById(id);
        if (item == null) {
            return Collections.emptyList();
        }
        switch (item) {
            case BOTS:
                return resolveDrawerBotMenuItems(menuContext);
            case PLUGINS:
                return resolveDrawerPluginMenuItems(menuContext);
            default:
                MenuItemInfo menuItemInfo = resolveMenuItem(id, menuContext);
                return menuItemInfo == null ? Collections.emptyList() : Collections.singletonList(menuItemInfo);
        }
    }

    public static MenuItemInfo resolveMenuItem(int id, MenuContext menuContext) {
        MainMenuItem item = MainMenuItem.getById(id);
        if (item == null || menuContext.fragment() == null) {
            return null;
        }
        final int currentAccount = menuContext.currentAccount();
        final BaseFragment fragment = menuContext.fragment();
        switch (item) {
            case PLUGINS:
                if (PluginsController.isPluginEngineSupported()) {
                    return new MenuItemInfo(R.drawable.msg_plugins, LocaleController.getString(R.string.Plugins), () -> fragment.presentFragment(new PluginsActivity()), null);
                }
                break;
            case PROFILE:
                return new MenuItemInfo(R.drawable.left_status_profile, LocaleController.getString(R.string.MyProfile), () -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("user_id", UserConfig.getInstance(currentAccount).getClientUserId());
                    bundle.putBoolean("my_profile", true);
                    fragment.presentFragment(new ProfileActivity(bundle));
                }, null);
            case ARCHIVE:
                return new MenuItemInfo(R.drawable.msg_archive, LocaleController.getString(R.string.ArchivedChats), menuContext.archiveClick() != null ? menuContext.archiveClick() : () -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("folderId", 1);
                    fragment.presentFragment(new DialogsActivity(bundle));
                }, null);
            case NEW_GROUP:
                return new MenuItemInfo(R.drawable.msg_groups, LocaleController.getString(R.string.NewGroup), () -> fragment.presentFragment(new GroupCreateActivity(new Bundle())), null);
            case CONTACTS:
                return new MenuItemInfo(R.drawable.msg_contacts, LocaleController.getString(R.string.Contacts), () -> {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("needPhonebook", true);
                    bundle.putBoolean("needFinishFragment", false);
                    fragment.presentFragment(new ContactsActivity(bundle));
                }, null);
            case CALLS:
                return new MenuItemInfo(R.drawable.msg_calls, LocaleController.getString(R.string.Calls), () -> fragment.presentFragment(new CallLogActivity()), null);
            case NEW_CHANNEL:
                return new MenuItemInfo(R.drawable.msg_channel, LocaleController.getString(R.string.NewChannel), () -> {
                    SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                    if (!BuildVars.DEBUG_VERSION && globalMainSettings.getBoolean("channel_intro", false)) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("step", 0);
                        fragment.presentFragment(new ChannelCreateActivity(bundle));
                    } else {
                        fragment.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANNEL_CREATE));
                        globalMainSettings.edit().putBoolean("channel_intro", true).apply();
                    }
                }, null);
            case SAVED:
                return new MenuItemInfo(R.drawable.msg_saved, LocaleController.getString(R.string.SavedMessages), () -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("user_id", UserConfig.getInstance(currentAccount).getClientUserId());
                    fragment.presentFragment(new ChatActivity(bundle));
                }, null);
            case FEED:
                return new MenuItemInfo(R.drawable.ic_feed, LocaleController.getString(R.string.Feed), () -> {
                }, null);
            case SETTINGS:
                return new MenuItemInfo(R.drawable.msg_settings, LocaleController.getString(R.string.Settings), () -> fragment.presentFragment(new SettingsActivity()), null);
            case BROWSER:
                return new MenuItemInfo(R.drawable.msg2_language, LocaleController.getString(R.string.BrowserSettingsTitle), () -> {
                    SearchEngine current = SearchEngine.getCurrent();
                    String homepage = current.search_url != null ? current.search_url : "https://www.google.com";
                    Activity parentActivity = fragment.getParentActivity();
                    Browser.openInTelegramBrowser(parentActivity, homepage, null);
                }, null);
            case QR:
                return new MenuItemInfo(R.drawable.msg_qrcode, LocaleController.getString(R.string.AuthAnotherClient), () -> {
                    LaunchActivity launchActivity = LaunchActivity.instance;
                    if (launchActivity != null && launchActivity.checkCallingOrSelfPermission("android.permission.CAMERA") != 0) {
                        LaunchActivity.instance.requestPermissions(new String[]{"android.permission.CAMERA"}, 34);
                    } else {
                        CameraScanActivity.showAsSheet(fragment, true, 1, new CameraScanActivity.CameraScanActivityDelegate() {
                            @Override
                            public boolean processQr(String str, Runnable runnable) {
                                runnable.run();
                                return true;
                            }
                        });
                    }
                }, null);
            default:
                return null;
        }
        return null;
    }

    public static List<AttachMenuBotInfo> getAttachMenuBotItems(MenuContext menuContext) {
        BaseFragment fragment = menuContext.fragment();
        LaunchActivity launchActivity = findLaunchActivity(fragment);
        TLRPC.TL_attachMenuBots attachMenuBots = MediaDataController.getInstance(menuContext.currentAccount()).getAttachMenuBots();
        if (fragment == null || launchActivity == null || attachMenuBots == null || attachMenuBots.bots == null || attachMenuBots.bots.isEmpty()) {
            return Collections.emptyList();
        }
        List<AttachMenuBotInfo> list = new ArrayList<>();
        for (TLRPC.TL_attachMenuBot bot : attachMenuBots.bots) {
            if (bot.show_in_side_menu) {
                list.add(new AttachMenuBotInfo(getAttachMenuBotIconRes(bot), bot.short_name, bot,
                        createAttachMenuBotClickAction(menuContext, bot, launchActivity),
                        () -> BotWebViewSheet.deleteBot(menuContext.currentAccount(), bot.bot_id, null)));
            }
        }
        return list;
    }

    public static List<MenuItemRecord> getPluginMenuItems(MenuContext menuContext) {
        return PluginsController.getInstance().getMenuItemsForLocation("main_menu", getPluginContextData(menuContext));
    }

    public static Runnable createPluginClickAction(MenuItemRecord menuItemRecord, MenuContext menuContext) {
        return () -> menuItemRecord.executeClick(getPluginContextData(menuContext));
    }

    private static List<MenuItemInfo> resolveDrawerBotMenuItems(MenuContext menuContext) {
        List<AttachMenuBotInfo> botItems = getAttachMenuBotItems(menuContext);
        if (botItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<MenuItemInfo> list = new ArrayList<>(botItems.size());
        for (AttachMenuBotInfo info : botItems) {
            list.add(new MenuItemInfo(info.iconRes(), info.text(), info.onClick(), info.onLongClick()));
        }
        return list;
    }

    private static List<MenuItemInfo> resolveDrawerPluginMenuItems(MenuContext menuContext) {
        if (menuContext.fragment() == null) {
            return Collections.emptyList();
        }
        MenuItemInfo pluginInfo = resolveMenuItem(MainMenuItem.PLUGINS.getId(), menuContext);
        List<MenuItemRecord> pluginMenuItems = getPluginMenuItems(menuContext);
        if (pluginMenuItems.isEmpty()) {
            return pluginInfo == null ? Collections.emptyList() : Collections.singletonList(pluginInfo);
        }
        List<MenuItemInfo> list = new ArrayList<>(pluginMenuItems.size() + (pluginInfo != null ? 1 : 0));
        if (pluginInfo != null) {
            list.add(pluginInfo);
        }
        for (MenuItemRecord record : pluginMenuItems) {
            if (record != null && !TextUtils.isEmpty(record.text)) {
                list.add(new MenuItemInfo(record.iconResId != 0 ? record.iconResId : R.drawable.msg_plugins, record.text,
                        createPluginClickAction(record, menuContext),
                        () -> menuContext.fragment().presentFragment(new PluginsActivity())));
            }
        }
        return list.isEmpty() ? (pluginInfo == null ? Collections.emptyList() : Collections.singletonList(pluginInfo)) : list;
    }

    private static Runnable createAttachMenuBotClickAction(MenuContext menuContext, TLRPC.TL_attachMenuBot bot, LaunchActivity launchActivity) {
        return () -> {
            if (bot.inactive || bot.side_menu_disclaimer_needed) {
                WebAppDisclaimerAlert.show(menuContext.fragment().getContext() != null ? menuContext.fragment().getContext() : launchActivity,
                        accepted -> {
                            TLRPC.TL_messages_toggleBotInAttachMenu req = new TLRPC.TL_messages_toggleBotInAttachMenu();
                            req.bot = MessagesController.getInstance(menuContext.currentAccount()).getInputUser(bot.bot_id);
                            req.enabled = true;
                            req.write_allowed = true;
                            ConnectionsManager.getInstance(menuContext.currentAccount()).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                                bot.side_menu_disclaimer_needed = false;
                                bot.inactive = false;
                                LaunchActivity.showAttachMenuBot(launchActivity, menuContext.currentAccount(), bot, null, true);
                                MediaDataController.getInstance(menuContext.currentAccount()).updateAttachMenuBotsInCache();
                            }), 66);
                        }, null, null);
            } else {
                LaunchActivity.showAttachMenuBot(launchActivity, menuContext.currentAccount(), bot, null, true);
            }
        };
    }

    public static Map<String, Object> getPluginContextData(MenuContext menuContext) {
        if (menuContext.pluginContextData() != null) {
            return menuContext.pluginContextData();
        }
        return createPluginContextData(menuContext.currentAccount(), menuContext.fragment());
    }

    private static int getAttachMenuBotIconRes(TLRPC.TL_attachMenuBot bot) {
        return R.drawable.msg_bot;
    }

    private static LaunchActivity findLaunchActivity(BaseFragment fragment) {
        if (fragment == null) {
            return LaunchActivity.instance;
        }
        Activity activity = AndroidUtilities.findActivity(fragment.getContext() != null ? fragment.getContext() : fragment.getParentActivity());
        return activity instanceof LaunchActivity ? (LaunchActivity) activity : LaunchActivity.instance;
    }
}
