package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import org.justgram.messenger.xray.XrayManager;
import org.justgram.messenger.xray.XrayProfile;
import org.justgram.messenger.xray.XraySubscription;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JustgramXraySettingsActivity extends BaseFragment {

    private final static int ID_ENABLE_XRAY = 1;
    private final static int ID_RANDOM_PORT = 2;
    private final static int ID_LOCAL_PORT = 3;
    private final static int ID_IMPORT_CLIPBOARD = 4;
    private final static int ID_ADD_MANUALLY = 5;
    private final static int ID_PING_ALL = 6;
    private final static int ID_UPDATE_SUBS = 7;
    private final static int ID_VIEW_CONFIG = 8;
    private final static int ID_SUB_OFFSET = 100;
    private final static int ID_PROFILE_OFFSET = 1000;

    private UniversalRecyclerView listView;
    private NumberTextView selectedCountTextView;
    private ActionBarMenuItem deleteMenuItem;
    private final HashSet<String> selectedProfileIds = new HashSet<>();

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.JustgramSettingsXray));

        ActionBarMenu actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.bold());
        selectedCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        selectedCountTextView.setOnTouchListener((v, event) -> true);

        deleteMenuItem = actionMode.addItemWithWidth(1, R.drawable.msg_delete, AndroidUtilities.dp(54));
        deleteMenuItem.setContentDescription(getString(R.string.Delete));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (actionBar.isActionModeShowed()) {
                        clearSelection();
                    } else {
                        finishFragment();
                    }
                } else if (id == 1) {
                    showDeleteSelectedDialog();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        listView = new UniversalRecyclerView(this, this::fillItems, (item, view, position, x, y) -> onClick(item, view), (item, view, position, x, y) -> onLongClick(item, view));
        listView.setSections();
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.adapter.update(false);
        actionBar.setAdaptiveBackground(listView);

        return fragmentView;
    }

    private void updateSelectionMode() {
        int count = selectedProfileIds.size();
        if (count > 0) {
            selectedCountTextView.setNumber(count, actionBar.isActionModeShowed());
            if (!actionBar.isActionModeShowed()) {
                actionBar.showActionMode();
            }
        } else {
            if (actionBar.isActionModeShowed()) {
                actionBar.hideActionMode();
            }
        }
    }

    private void clearSelection() {
        selectedProfileIds.clear();
        updateSelectionMode();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private CharSequence formatEmoji(CharSequence text) {
        if (TextUtils.isEmpty(text)) return text;
        return Emoji.replaceEmoji(text, Theme.chat_msgTextPaint.getFontMetricsInt(), false);
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean isRunning = XrayManager.getInstance().isRunning();
        adapter.whiteSectionStart();
        items.add(UItem.asCheck(ID_ENABLE_XRAY, getString(R.string.XrayEnable)).setChecked(isRunning));

        items.add(UItem.asCheck(ID_RANDOM_PORT, getString(R.string.XrayRandomPort)).setChecked(XrayManager.getInstance().isRandomPort()));

        String portText = XrayManager.getInstance().isRandomPort()
                ? (XrayManager.getInstance().getCurrentActivePort() + " (Auto)")
                : String.valueOf(XrayManager.getInstance().getLocalPort());
        items.add(UItem.asSettingsCell(ID_LOCAL_PORT, getString(R.string.XrayLocalPort), portText));
        adapter.whiteSectionEnd();
        items.add(UItem.asShadow(null));

        adapter.whiteSectionStart();
        items.add(UItem.asHeader(getString(R.string.XrayProfiles)));
        items.add(UItem.asButton(ID_IMPORT_CLIPBOARD, getString(R.string.XrayImportClipboard)));
        items.add(UItem.asButton(ID_ADD_MANUALLY, getString(R.string.XrayAddLinkOrSub)));

        List<XrayProfile> allProfiles = XrayManager.getInstance().getProfiles();
        List<XraySubscription> subs = XrayManager.getInstance().getSubscriptions();
        XrayProfile active = XrayManager.getInstance().getActiveProfile();

        if (!allProfiles.isEmpty()) {
            items.add(UItem.asButton(ID_PING_ALL, getString(R.string.XrayPingAll)));
        }
        if (!subs.isEmpty()) {
            items.add(UItem.asButton(ID_UPDATE_SUBS, getString(R.string.XrayUpdateSubscriptions)));
        }
        adapter.whiteSectionEnd();

        items.add(UItem.asShadow(null));

        if (allProfiles.isEmpty() && subs.isEmpty()) {
            adapter.whiteSectionStart();
            items.add(UItem.asSettingsCell(0, getString(R.string.XrayNoProfiles), ""));
            adapter.whiteSectionEnd();
            items.add(UItem.asShadow(null));
        } else {
            // Subscriptions
            for (int s = 0; s < subs.size(); s++) {
                XraySubscription sub = subs.get(s);
                adapter.whiteSectionStart();
                UItem subHeader = UItem.asHeader(ID_SUB_OFFSET + s, formatEmoji(sub.name));
                subHeader.textValue = sub.profileCount + " profiles";
                items.add(subHeader);

                for (int p = 0; p < allProfiles.size(); p++) {
                    XrayProfile profile = allProfiles.get(p);
                    if (sub.id.equals(profile.subscriptionId)) {
                        addProfileItem(items, profile, active, p);
                    }
                }
                adapter.whiteSectionEnd();
                items.add(UItem.asShadow(null));
            }

            // Manual profiles
            List<XrayProfile> manualProfiles = new ArrayList<>();
            for (XrayProfile p : allProfiles) {
                if (TextUtils.isEmpty(p.subscriptionId)) {
                    manualProfiles.add(p);
                }
            }

            if (!manualProfiles.isEmpty()) {
                adapter.whiteSectionStart();
                items.add(UItem.asHeader(getString(R.string.XrayManualProfiles)));
                for (XrayProfile profile : manualProfiles) {
                    int originalIndex = allProfiles.indexOf(profile);
                    addProfileItem(items, profile, active, originalIndex);
                }
                adapter.whiteSectionEnd();
                items.add(UItem.asShadow(null));
            }
        }

        adapter.whiteSectionStart();
        items.add(UItem.asButton(ID_VIEW_CONFIG, getString(R.string.XrayViewConfig)));
        items.add(UItem.asSettingsCell(0, getString(R.string.XrayVersion), XrayManager.getInstance().getXrayVersion()));
        adapter.whiteSectionEnd();
        items.add(UItem.asShadow(null));
    }

    private void addProfileItem(ArrayList<UItem> items, XrayProfile profile, XrayProfile active, int index) {
        boolean isChecked;
        if (actionBar.isActionModeShowed()) {
            isChecked = selectedProfileIds.contains(profile.id);
        } else {
            isChecked = active != null && active.id.equals(profile.id);
        }

        String title = TextUtils.isEmpty(profile.name) ? (profile.server + ":" + profile.port) : profile.name;
        String subtitle = profile.type.toUpperCase() + " • " + profile.server + ":" + profile.port;
        if (profile.ping >= 0) {
            subtitle += " • " + profile.ping + " ms";
        } else if (profile.ping == -2) {
            subtitle += " • Error";
        }

        UItem profileItem = UItem.asCheck(ID_PROFILE_OFFSET + index, formatEmoji(title)).setChecked(isChecked);
        profileItem.textValue = formatEmoji(subtitle);
        items.add(profileItem);
    }

    private void onClick(UItem item, View view) {
        if (item.id == ID_ENABLE_XRAY) {
            if (XrayManager.getInstance().isRunning()) {
                XrayManager.getInstance().stopService();
            } else {
                XrayProfile active = XrayManager.getInstance().getActiveProfile();
                if (active == null) {
                    BulletinFactory.of(this).createErrorBulletin(getString(R.string.XrayNoProfiles)).show();
                } else {
                    boolean ok = XrayManager.getInstance().startService();
                    if (!ok) {
                        String err = XrayManager.getInstance().getLastError();
                        if (TextUtils.isEmpty(err)) err = "Failed to start Xray proxy";
                        BulletinFactory.of(this).createErrorBulletin(err).show();
                    }
                }
            }
            listView.adapter.update(true);
        } else if (item.id == ID_RANDOM_PORT) {
            XrayManager.getInstance().setRandomPort(!XrayManager.getInstance().isRandomPort());
            listView.adapter.update(true);
        } else if (item.id == ID_LOCAL_PORT) {
            if (!XrayManager.getInstance().isRandomPort()) {
                showLocalPortDialog();
            }
        } else if (item.id == ID_UPDATE_SUBS) {
            XrayManager.getInstance().updateSubscriptions(
                    (count) -> {
                        listView.adapter.update(true);
                        BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_copy, String.format(getString(R.string.XraySubUpdated), count)).show();
                    },
                    (error) -> BulletinFactory.of(this).createErrorBulletin(error).show()
            );
        } else if (item.id == ID_IMPORT_CLIPBOARD) {
            importFromClipboard();
        } else if (item.id == ID_ADD_MANUALLY) {
            showAddLinkOrSubDialog();
        } else if (item.id == ID_PING_ALL) {
            XrayManager.getInstance().testPingAll(() -> listView.adapter.update(true));
        } else if (item.id >= ID_SUB_OFFSET && item.id < ID_PROFILE_OFFSET) {
            int index = item.id - ID_SUB_OFFSET;
            List<XraySubscription> subs = XrayManager.getInstance().getSubscriptions();
            if (index >= 0 && index < subs.size()) {
                XraySubscription sub = subs.get(index);
                showSubscriptionOptions(sub, view);
            }
        } else if (item.id >= ID_PROFILE_OFFSET) {
            int index = item.id - ID_PROFILE_OFFSET;
            List<XrayProfile> profiles = XrayManager.getInstance().getProfiles();
            if (index >= 0 && index < profiles.size()) {
                XrayProfile profile = profiles.get(index);
                if (actionBar.isActionModeShowed()) {
                    if (selectedProfileIds.contains(profile.id)) {
                        selectedProfileIds.remove(profile.id);
                    } else {
                        selectedProfileIds.add(profile.id);
                    }
                    updateSelectionMode();
                    listView.adapter.update(true);
                } else {
                    XrayManager.getInstance().setActiveProfile(profile);
                    listView.adapter.update(true);
                }
            }
        } else if (item.id == ID_VIEW_CONFIG) {
            showConfigJsonDialog();
        }
    }

    private boolean onLongClick(UItem item, View view) {
        if (item.id >= ID_PROFILE_OFFSET) {
            int index = item.id - ID_PROFILE_OFFSET;
            List<XrayProfile> profiles = XrayManager.getInstance().getProfiles();
            if (index >= 0 && index < profiles.size()) {
                XrayProfile profile = profiles.get(index);
                if (selectedProfileIds.isEmpty()) {
                    selectedProfileIds.add(profile.id);
                    updateSelectionMode();
                    listView.adapter.update(true);
                } else {
                    showProfileOptions(profile, view);
                }
                return true;
            }
        } else if (item.id >= ID_SUB_OFFSET && item.id < ID_PROFILE_OFFSET) {
            int index = item.id - ID_SUB_OFFSET;
            List<XraySubscription> subs = XrayManager.getInstance().getSubscriptions();
            if (index >= 0 && index < subs.size()) {
                XraySubscription sub = subs.get(index);
                showSubscriptionOptions(sub, view);
                return true;
            }
        }
        return false;
    }

    private void importFromClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getParentActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence text = clip.getItemAt(0).getText();
                    if (!TextUtils.isEmpty(text)) {
                        XrayManager.getInstance().addProfilesOrSubscription(
                                text.toString(),
                                (count) -> {
                                    listView.adapter.update(true);
                                    BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_copy, String.format(getString(R.string.XrayImportSuccess), count)).show();
                                },
                                (error) -> BulletinFactory.of(this).createErrorBulletin(getString(R.string.XrayImportError)).show()
                        );
                        return;
                    }
                }
            }
            BulletinFactory.of(this).createErrorBulletin(getString(R.string.XrayImportError)).show();
        } catch (Exception e) {
            BulletinFactory.of(this).createErrorBulletin(getString(R.string.XrayImportError)).show();
        }
    }

    private void showAddLinkOrSubDialog() {
        AlertsCreator.createSimpleTextInputAlert(
                getContext(),
                this,
                getString(R.string.XrayAddLinkOrSub),
                null,
                null,
                "",
                1000,
                getString(R.string.Save),
                null,
                (result) -> {
                    if (!TextUtils.isEmpty(result)) {
                        XrayManager.getInstance().addProfilesOrSubscription(
                                result,
                                (count) -> {
                                    listView.adapter.update(true);
                                    BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_copy, String.format(getString(R.string.XrayImportSuccess), count)).show();
                                },
                                (err) -> BulletinFactory.of(this).createErrorBulletin(err).show()
                        );
                    }
                });
    }

    private void showLocalPortDialog() {
        AlertsCreator.createSimpleTextInputAlert(
                getContext(),
                this,
                getString(R.string.XrayLocalPort),
                null,
                null,
                String.valueOf(XrayManager.getInstance().getLocalPort()),
                5,
                getString(R.string.Save),
                null,
                (result) -> {
                    int port = Utilities.parseInt(result);
                    if (port > 0 && port < 65536) {
                        XrayManager.getInstance().setLocalPort(port);
                        listView.adapter.update(true);
                    } else {
                        BulletinFactory.of(this).createErrorBulletin(getString(R.string.InvalidFormatError)).show();
                    }
                });
    }

    private void showProfileOptions(XrayProfile profile, View view) {
        if (getParentActivity() == null) return;
        ItemOptions options = ItemOptions.makeOptions(this, view);
        options.add(R.drawable.msg_stats, getString(R.string.XrayTestPing), () -> XrayManager.getInstance().testPing(profile, () -> listView.adapter.update(true)));
        options.add(R.drawable.msg_delete, getString(R.string.XrayDeleteProfile), true, () -> {
            XrayManager.getInstance().deleteProfile(profile);
            listView.adapter.update(true);
        });
        options.show();
    }

    private void showSubscriptionOptions(XraySubscription sub, View view) {
        if (getParentActivity() == null) return;
        ItemOptions options = ItemOptions.makeOptions(this, view);
        options.add(R.drawable.msg_copy, getString(R.string.XrayUpdateSub), () -> {
            XrayManager.getInstance().updateSubscription(sub, (count) -> {
                listView.adapter.update(true);
                BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_copy, String.format(getString(R.string.XraySubUpdated), count)).show();
            }, (error) -> BulletinFactory.of(this).createErrorBulletin(error).show());
        });
        options.add(R.drawable.msg_delete, getString(R.string.XrayDeleteSub), true, () -> {
            XrayManager.getInstance().deleteSubscription(sub);
            listView.adapter.update(true);
        });
        options.show();
    }

    private void showDeleteSelectedDialog() {
        if (getParentActivity() == null || selectedProfileIds.isEmpty()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle(getString(R.string.Delete));
        builder.setMessage("Delete " + selectedProfileIds.size() + " profile(s)?");
        builder.setPositiveButton(getString(R.string.Delete), (dialog, which) -> {
            XrayManager.getInstance().deleteProfiles(selectedProfileIds);
            clearSelection();
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showConfigJsonDialog() {
        if (getParentActivity() == null) return;
        try {
            String json = XrayManager.getInstance().generateConfigJson();
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
            builder.setTitle(getString(R.string.XrayViewConfig));
            builder.setMessage(json);
            builder.setPositiveButton(getString(R.string.OK), null);
            builder.setNeutralButton(getString(R.string.Copy), (dialog, which) -> {
                ClipboardManager clipboard = (ClipboardManager) getParentActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Config", json));
                    BulletinFactory.of(this).createCopyBulletin(getString(R.string.TextCopied)).show();
                }
            });
            showDialog(builder.create());
        } catch (Exception e) {
            BulletinFactory.of(this).createErrorBulletin(e.getMessage()).show();
        }
    }
}
