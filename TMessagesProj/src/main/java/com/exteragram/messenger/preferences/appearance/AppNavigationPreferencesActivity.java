package com.exteragram.messenger.preferences.appearance;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.exteragram.messenger.ExteraConfig;
import com.exteragram.messenger.MainMenuItem;
import com.exteragram.messenger.config.BottomNavigationBar;
import com.exteragram.messenger.plugins.PluginsController;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.exteragram.messenger.preferences.components.AltSeekbar;
import com.exteragram.messenger.utils.ui.PopupUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

public class AppNavigationPreferencesActivity extends BasePreferencesActivity {
    private CharSequence[] bottomNavigationModes;
    private AltSeekbar predictiveBackSeekbar;
    private Drawable reorderIcon;
    private ActionBarMenuItem resetItem;
    private final HashMap<Integer, ItemInfo> itemDetails = new HashMap<>();
    private final ArrayList<Integer> stableDividerIds = new ArrayList<>();
    private int nextDividerId = -2000;

    public enum AppNavigationItem {
        DRAWER,
        IMMERSIVE_ANIMATION,
        BOTTOM_NAVIGATION_BAR_MODE,
        PREDICTIVE_BACK_ANIMATION,
        SPRING_ANIMATIONS;

        public int getId() {
            return ordinal() + 150;
        }

        public static AppNavigationItem fromId(int i) {
            int i2 = i - 150;
            if (i2 < 0 || i2 >= values().length) {
                return null;
            }
            return values()[i2];
        }
    }

    public static class ItemInfo {
        int iconRes;
        CharSequence name;

        public ItemInfo(CharSequence charSequence, int i) {
            this.name = charSequence;
            this.iconRes = i;
        }
    }

    @Override
    public void initializeOptionStrings() {
        initItemDetails();
        this.bottomNavigationModes = new CharSequence[]{LocaleController.getString(R.string.BottomNavigationModeShow), LocaleController.getString(R.string.BottomNavigationModeHide), LocaleController.getString(R.string.BottomNavigationModeFloating)};
    }

    private void initItemDetails() {
        this.itemDetails.put(MainMenuItem.PROFILE.getId(), new ItemInfo(LocaleController.getString(R.string.MyProfile), R.drawable.left_status_profile));
        this.itemDetails.put(MainMenuItem.ARCHIVE.getId(), new ItemInfo(LocaleController.getString(R.string.ArchivedChats), R.drawable.msg_archive));
        this.itemDetails.put(MainMenuItem.BOTS.getId(), new ItemInfo(LocaleController.getString(R.string.FilterBots), R.drawable.msg_bot));
        this.itemDetails.put(MainMenuItem.NEW_GROUP.getId(), new ItemInfo(LocaleController.getString(R.string.NewGroup), R.drawable.msg_groups));
        this.itemDetails.put(MainMenuItem.CONTACTS.getId(), new ItemInfo(LocaleController.getString(R.string.Contacts), R.drawable.msg_contacts));
        this.itemDetails.put(MainMenuItem.NEW_CHANNEL.getId(), new ItemInfo(LocaleController.getString(R.string.NewChannel), R.drawable.msg_channel));
        this.itemDetails.put(MainMenuItem.CALLS.getId(), new ItemInfo(LocaleController.getString(R.string.Calls), R.drawable.msg_calls));
        this.itemDetails.put(MainMenuItem.SAVED.getId(), new ItemInfo(LocaleController.getString(R.string.SavedMessages), R.drawable.msg_saved));
        this.itemDetails.put(MainMenuItem.FEED.getId(), new ItemInfo(LocaleController.getString(R.string.Feed), R.drawable.ic_feed));
        this.itemDetails.put(MainMenuItem.SETTINGS.getId(), new ItemInfo(LocaleController.getString(R.string.Settings), R.drawable.msg_settings));
        this.itemDetails.put(MainMenuItem.PLUGINS.getId(), new ItemInfo(LocaleController.getString(R.string.Plugins), R.drawable.msg_plugins));
        this.itemDetails.put(MainMenuItem.BROWSER.getId(), new ItemInfo(LocaleController.getString(R.string.BrowserSettingsTitle), R.drawable.msg2_language));
        this.itemDetails.put(MainMenuItem.QR.getId(), new ItemInfo(LocaleController.getString(R.string.AuthAnotherClient), R.drawable.msg_qrcode));
        ExteraConfig.getMainMenuHiddenItems().removeIf(num -> num.intValue() == MainMenuItem.DIVIDER.getId());
        this.stableDividerIds.clear();
        this.nextDividerId = -2000;
        ArrayList<Integer> mainMenuLayout = ExteraConfig.getMainMenuLayout();
        for (Integer num : mainMenuLayout) {
            if (num.intValue() == MainMenuItem.DIVIDER.getId()) {
                ArrayList<Integer> arrayList = this.stableDividerIds;
                int i2 = this.nextDividerId;
                this.nextDividerId = i2 - 1;
                arrayList.add(i2);
            }
        }
    }

    @Override
    public CharSequence getTitle() {
        return LocaleController.getString(R.string.AppNavigation);
    }

    @Override
    public View createView(Context context) {
        View viewCreateView = super.createView(context);
        if (Build.VERSION.SDK_INT >= 34) {
            this.predictiveBackSeekbar = new AltSeekbar(context, this::onPredictiveBackIntensityChanged, 0, 2, LocaleController.getString(R.string.PredictiveBackIntensity), LocaleController.getString(R.string.BlurOff), LocaleController.getString(R.string.PredictiveBackMax)) {
                @Override
                public boolean useExactEndpointHaptic() {
                    return true;
                }

                @Override
                public CharSequence getTextForHeader() {
                    float fRound = Math.round(this.currentValue * 10.0f) / 10.0f;
                    if (fRound <= 0.0f) {
                        return this.leftTextView.getText().toString().toUpperCase();
                    }
                    if (fRound >= 2.0f) {
                        return this.rightTextView.getText().toString().toUpperCase();
                    }
                    int i = (int) fRound;
                    if (fRound == i) {
                        return String.valueOf(i);
                    }
                    return String.format(Locale.US, "%.1f", fRound);
                }
            };
            float fMin = Math.min(ExteraConfig.getPredictiveBackIntensity(), 2.0f);
            if (fMin != ExteraConfig.getPredictiveBackIntensity()) {
                ExteraConfig.setPredictiveBackIntensity(fMin);
            }
            this.predictiveBackSeekbar.setProgress(fMin);
        }
        ActionBarMenuItem actionBarMenuItemAddItem = this.actionBar.createMenu().addItem(0, R.drawable.msg_reset);
        this.resetItem = actionBarMenuItemAddItem;
        actionBarMenuItemAddItem.setContentDescription(LocaleController.getString(R.string.Reset));
        updateResetButtonVisibility();
        this.resetItem.setOnClickListener(this::onResetItemClicked);
        UniversalRecyclerView universalRecyclerView = this.listView;
        if (universalRecyclerView != null) {
            universalRecyclerView.allowReorder(true);
            this.listView.listenReorder((Utilities.Callback2<Integer, ArrayList<UItem>>) this::updateConfigFromReorder);
        }
        return viewCreateView;
    }

    private void onPredictiveBackIntensityChanged(float f) {
        boolean zIsPredictiveBackOff = isPredictiveBackOff(ExteraConfig.getPredictiveBackIntensity());
        boolean zIsPredictiveBackOff2 = isPredictiveBackOff(f);
        ExteraConfig.setPredictiveBackIntensity(f);
        if (this.predictiveBackSeekbar != null) {
            this.predictiveBackSeekbar.updateHeader(f);
        }
        if (zIsPredictiveBackOff != zIsPredictiveBackOff2) {
            showRestartBulletin();
        }
    }

    private void onResetItemClicked(View view) {
        resetToDefault();
    }

    private boolean isPredictiveBackOff(float f) {
        return ((float) Math.round(f * 10.0f)) / 10.0f <= 0.0f;
    }

    @Override
    public void fillItems(ArrayList<UItem> arrayList, UniversalAdapter universalAdapter) {
        if (this.reorderIcon == null) {
            this.reorderIcon = ContextCompat.getDrawable(getContext(), R.drawable.list_reorder);
        }
        arrayList.add(UItem.asHeader(LocaleController.getString(R.string.General)));
        arrayList.add(UItem.asButton(AppNavigationItem.BOTTOM_NAVIGATION_BAR_MODE.getId(), LocaleController.getString(R.string.BottomNavigationBarMode), this.bottomNavigationModes[BottomNavigationBar.getMode()]));
        arrayList.add(UItem.asCheck(AppNavigationItem.SPRING_ANIMATIONS.getId(), LocaleController.getString(R.string.SpringAnimations)).setChecked(ExteraConfig.getSpringAnimations()));
        arrayList.add(UItem.asShadow(LocaleController.getString(R.string.SpringAnimationsInfo)));
        if (Build.VERSION.SDK_INT >= 34 && this.predictiveBackSeekbar != null) {
            arrayList.add(UItem.asCustom(this.predictiveBackSeekbar));
            arrayList.add(UItem.asShadow(LocaleController.getString(R.string.PredictiveBackInfo)));
        }
        arrayList.add(UItem.asHeader(LocaleController.getString(R.string.AppNavigation)));
        arrayList.add(UItem.asCheck(AppNavigationItem.DRAWER.getId(), LocaleController.getString(R.string.NavigationDrawer)).setChecked(ExteraConfig.getNavigationDrawer()));
        if (ExteraConfig.getNavigationDrawer()) {
            arrayList.add(UItem.asCheck(AppNavigationItem.IMMERSIVE_ANIMATION.getId(), LocaleController.getString(R.string.NavigationDrawerImmersiveAnimation)).setChecked(ExteraConfig.getImmersiveDrawerAnimation()));
        }
        arrayList.add(UItem.asShadow(LocaleController.getString(R.string.NavigationDrawerInfo)));
        addMenuSection(arrayList, universalAdapter, LocaleController.getString(R.string.MainMenuItems), ExteraConfig.getMainMenuLayout(), true);
        arrayList.add(UItem.asShadow(LocaleController.getString(R.string.MainMenuItemsInfo)));
        if (!ExteraConfig.getMainMenuHiddenItems().isEmpty()) {
            addMenuSection(arrayList, universalAdapter, LocaleController.getString(R.string.MainMenuHiddenItems), ExteraConfig.getMainMenuHiddenItems(), false);
            arrayList.add(UItem.asShadow(null));
        }
    }

    private void addMenuSection(ArrayList<UItem> arrayList, UniversalAdapter universalAdapter, String str, ArrayList<Integer> arrayList2, boolean z) {
        universalAdapter.whiteSectionStart();
        arrayList.add(UItem.asHeader(str));
        universalAdapter.reorderSectionStart();
        int size = arrayList2.size();
        int i = 0;
        int i2 = 0;
        while (i2 < size) {
            Integer num = arrayList2.get(i2);
            i2++;
            Integer num2 = num;
            if (num2.intValue() != MainMenuItem.PLUGINS.getId() || PluginsController.isPluginEngineSupported()) {
                int iIntValue = num2.intValue();
                MainMenuItem mainMenuItem = MainMenuItem.DIVIDER;
                if (iIntValue == mainMenuItem.getId()) {
                    if (z && i < this.stableDividerIds.size()) {
                        arrayList.add(createMenuItem(this.stableDividerIds.get(i), null));
                    } else if (!z) {
                        arrayList.add(createMenuItem(mainMenuItem.getId(), null));
                    }
                    i++;
                } else {
                    ItemInfo itemInfo = this.itemDetails.get(num2);
                    if (itemInfo != null) {
                        arrayList.add(createMenuItem(num2, itemInfo));
                    }
                }
            }
        }
        universalAdapter.reorderSectionEnd();
        if (z) {
            arrayList.add(UItem.asButton(-200, R.drawable.msg_add, LocaleController.getString(R.string.MainMenuAddDivider)).accent());
        }
        universalAdapter.whiteSectionEnd();
    }

    private UItem createMenuItem(int i, ItemInfo itemInfo) {
        UItem uItemAsButton;
        if (i <= -2000 || i == MainMenuItem.DIVIDER.getId()) {
            uItemAsButton = UItem.asButton(i, R.drawable.msg_block, LocaleController.getString(R.string.MainMenuDivider));
        } else {
            if (itemInfo == null) {
                return null;
            }
            uItemAsButton = UItem.asButton(i, itemInfo.iconRes, itemInfo.name);
        }
        uItemAsButton.object2 = this.reorderIcon;
        return uItemAsButton;
    }

    private void updateConfigFromReorder(int i, ArrayList<UItem> arrayList) {
        ArrayList<Integer> arrayList2 = new ArrayList<>();
        ArrayList<Integer> arrayList3 = new ArrayList<>();
        for (UItem uItem2 : arrayList) {
            int i3 = uItem2.id;
            if (i3 > -2000 && i3 != MainMenuItem.DIVIDER.getId()) {
                arrayList2.add(uItem2.id);
            } else if (i == 0) {
                arrayList2.add(MainMenuItem.DIVIDER.getId());
                int i4 = uItem2.id;
                if (i4 > -2000) {
                    i4 = this.nextDividerId;
                    this.nextDividerId = i4 - 1;
                }
                arrayList3.add(i4);
            }
        }
        if (i == 0) {
            this.stableDividerIds.clear();
            this.stableDividerIds.addAll(arrayList3);
            if (BottomNavigationBar.hidden()) {
                Integer numValueOf = MainMenuItem.SETTINGS.getId();
                if (!arrayList2.contains(numValueOf)) {
                    arrayList2.add(numValueOf);
                }
            }
            ExteraConfig.getMainMenuLayout().clear();
            ExteraConfig.getMainMenuLayout().addAll(arrayList2);
        } else if (i == 1) {
            if (BottomNavigationBar.hidden()) {
                arrayList2.remove((Integer) MainMenuItem.SETTINGS.getId());
            }
            ExteraConfig.getMainMenuHiddenItems().clear();
            ExteraConfig.getMainMenuHiddenItems().addAll(arrayList2);
        }
        saveAndNotify();
    }

    @Override
    public void onClick(UItem uItem, View view, int i, float f, float f2) {
        UniversalAdapter universalAdapter;
        int i2 = uItem.id;
        AppNavigationItem appNavigationItemFromId = AppNavigationItem.fromId(i2);
        int i3 = 0;
        if (appNavigationItemFromId != null) {
            switch (appNavigationItemFromId) {
                case DRAWER:
                    toggleBooleanSettingAndRefresh(uItem, val -> ExteraConfig.setNavigationDrawer(val));
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    UniversalRecyclerView universalRecyclerView = this.listView;
                    if (universalRecyclerView != null && universalRecyclerView.adapter != null) {
                        universalRecyclerView.adapter.update(true);
                    }
                    this.parentLayout.rebuildFragments(0);
                    return;
                case IMMERSIVE_ANIMATION:
                    toggleBooleanSettingAndRefresh(uItem, val -> ExteraConfig.setImmersiveDrawerAnimation(val));
                    return;
                case BOTTOM_NAVIGATION_BAR_MODE:
                    showListDialog(uItem, this.bottomNavigationModes, LocaleController.getString(R.string.BottomNavigationBarMode), BottomNavigationBar.getMode(), this::onBottomNavModeSelected);
                    return;
                case SPRING_ANIMATIONS:
                    toggleBooleanSettingAndRefresh(uItem, val -> {
                        boolean bool = val;
                        ExteraConfig.setSpringAnimations(bool);
                        if (bool) {
                            MessagesController.getGlobalMainSettings().edit().putBoolean("view_animations", true).apply();
                            SharedConfig.setAnimationsEnabled(true);
                        }
                    });
                    return;
            }
        }
        if (i2 == -200) {
            ArrayList<Integer> arrayList = this.stableDividerIds;
            int i5 = this.nextDividerId;
            this.nextDividerId = i5 - 1;
            arrayList.add(i5);
            ExteraConfig.getMainMenuLayout().add(MainMenuItem.DIVIDER.getId());
            saveAndNotify();
            return;
        }
        if (i2 <= -2000) {
            int iIndexOf = this.stableDividerIds.indexOf(i2);
            if (iIndexOf != -1) {
                int i6 = 0;
                while (true) {
                    if (i3 >= ExteraConfig.getMainMenuLayout().size()) {
                        i3 = -1;
                        break;
                    }
                    if (ExteraConfig.getMainMenuLayout().get(i3).intValue() == MainMenuItem.DIVIDER.getId()) {
                        if (i6 == iIndexOf) {
                            break;
                        } else {
                            i6++;
                        }
                    }
                    i3++;
                }
                if (i3 != -1) {
                    this.stableDividerIds.remove(iIndexOf);
                    ExteraConfig.getMainMenuLayout().remove(i3);
                    saveAndNotify();
                    return;
                }
                return;
            }
            return;
        }
        MainMenuItem mainMenuItem = MainMenuItem.DIVIDER;
        if (i2 == mainMenuItem.getId()) {
            ExteraConfig.getMainMenuHiddenItems().remove((Integer) i2);
            saveAndNotify();
            return;
        }
        if (BottomNavigationBar.hidden() && i2 == MainMenuItem.SETTINGS.getId() && ExteraConfig.getMainMenuLayout().contains(i2)) {
            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.MainMenuRemoveSettingsInfo)).show();
            return;
        }
        if (ExteraConfig.getMainMenuLayout().contains(i2)) {
            ExteraConfig.getMainMenuLayout().remove((Integer) i2);
            if (i2 != mainMenuItem.getId() && !ExteraConfig.getMainMenuHiddenItems().contains(i2)) {
                ExteraConfig.getMainMenuHiddenItems().add(0, i2);
            }
        } else if (ExteraConfig.getMainMenuHiddenItems().contains(i2)) {
            ExteraConfig.getMainMenuHiddenItems().remove((Integer) i2);
            ExteraConfig.getMainMenuLayout().add(i2);
        }
        saveAndNotify();
    }

    private void onBottomNavModeSelected(int i) {
        ExteraConfig.getPreferences().edit().putInt("bottomNavigationBarMode", i).apply();
        BottomNavigationBar.setMode(i);
        ExteraConfig.ensureSettingsVisibility();
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        refreshEditorList();
        updateResetButtonVisibility();
        this.parentLayout.rebuildFragments(0);
    }

    private void saveAndNotify() {
        ExteraConfig.saveMainMenuLayout();
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        refreshEditorList();
        updateResetButtonVisibility();
    }

    private void refreshEditorList() {
        UniversalRecyclerView universalRecyclerView = this.listView;
        if (universalRecyclerView == null || universalRecyclerView.adapter == null) {
            return;
        }
        universalRecyclerView.hideSelector(false);
        this.listView.cancelClickRunnables(false);
        this.listView.adapter.update(true);
    }

    private void updateResetButtonVisibility() {
        if (this.resetItem == null) {
            return;
        }
        boolean zEquals = ExteraConfig.getMainMenuLayout().equals(ExteraConfig.getDefaultMainMenuLayout());
        if (!zEquals && this.resetItem.getVisibility() == View.GONE) {
            AndroidUtilities.updateViewVisibilityAnimated(this.resetItem, true, 0.5f, true);
        } else if (zEquals && this.resetItem.getVisibility() == View.VISIBLE) {
            AndroidUtilities.updateViewVisibilityAnimated(this.resetItem, false, 0.5f, true);
        }
    }

    private void resetToDefault() {
        ExteraConfig.getMainMenuLayout().clear();
        ExteraConfig.getMainMenuLayout().addAll(ExteraConfig.getDefaultMainMenuLayout());
        ExteraConfig.getMainMenuHiddenItems().clear();
        for (MainMenuItem mainMenuItem : MainMenuItem.values()) {
            if (mainMenuItem != MainMenuItem.DIVIDER && !ExteraConfig.getMainMenuLayout().contains(mainMenuItem.getId()) && (mainMenuItem != MainMenuItem.PLUGINS || PluginsController.isPluginEngineSupported())) {
                ExteraConfig.getMainMenuHiddenItems().add(mainMenuItem.getId());
            }
        }
        this.stableDividerIds.clear();
        this.nextDividerId = -2000;
        ArrayList<Integer> mainMenuLayout = ExteraConfig.getMainMenuLayout();
        for (Integer num : mainMenuLayout) {
            if (num.intValue() == MainMenuItem.DIVIDER.getId()) {
                ArrayList<Integer> arrayList = this.stableDividerIds;
                int i2 = this.nextDividerId;
                this.nextDividerId = i2 - 1;
                arrayList.add(i2);
            }
        }
        ExteraConfig.saveMainMenuLayout();
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        refreshEditorList();
        updateResetButtonVisibility();
    }
}
