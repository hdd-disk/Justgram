package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import org.justgram.messenger.JustgramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

public class JustgramGeneralSettingsActivity extends BaseFragment {

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.JustgramSettingsGeneral));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        listView = new UniversalRecyclerView(this, this::fillItems, (item, view, position, x, y) -> onClick(item, view), null);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.adapter.update(false);

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asCheck(1, getString(R.string.DisableAds)).setChecked(JustgramConfig.disableAds));
        items.add(UItem.asCheck(2, getString(R.string.ShowAccountId)).setChecked(JustgramConfig.showAccountId));
        items.add(UItem.asButtonCheck(3, LocaleController.getString(R.string.WebSocketTransport), LocaleController.getString(R.string.WebSocketTransportInfo))
            .setChecked(JustgramConfig.webSocketTransport).setMultiline(true));
        if (JustgramConfig.webSocketTransport) {
            items.add(UItem.asSettingsCell(4, LocaleController.getString(R.string.WebSocketDomain), getWebSocketDomainText()));
        }
    }

    private void onClick(UItem item, View view) {
        if (item.id == 1) {
            JustgramConfig.disableAds = !JustgramConfig.disableAds;
            JustgramConfig.saveConfig();
            listView.adapter.update(true);
        } else if (item.id == 2) {
            JustgramConfig.showAccountId = !JustgramConfig.showAccountId;
            JustgramConfig.saveConfig();
            listView.adapter.update(true);
        } else if (item.id == 3) {
            JustgramConfig.webSocketTransport = !JustgramConfig.webSocketTransport;
            JustgramConfig.saveConfig();
            org.telegram.tgnet.ConnectionsManager.setWebSocketEnabled(JustgramConfig.webSocketTransport, JustgramConfig.webSocketDomain);
            listView.adapter.update(true);
        } else if (item.id == 4) {
            showWebSocketDomainDialog();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));

        return themeDescriptions;
    }

    private static String getWebSocketDomainText() {
        return TextUtils.isEmpty(JustgramConfig.webSocketDomain) ? LocaleController.getString(R.string.WebSocketDomainAuto) : JustgramConfig.webSocketDomain;
    }

    private void showWebSocketDomainDialog() {
        AlertsCreator.createSimpleTextInputAlert(
            getContext(),
            this,
            LocaleController.getString(R.string.WebSocketDomain),
            null,
            null,
            JustgramConfig.webSocketDomain,
            255,
            LocaleController.getString(R.string.Save),
            null,
            (result) -> {
                String domain = org.telegram.tgnet.ConnectionsManager.normalizeWebSocketDomain(result);
                if (domain.isEmpty() && !result.trim().isEmpty()) {
                    org.telegram.ui.Components.BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.InvalidFormatError)).show();
                    return;
                }
                JustgramConfig.webSocketDomain = domain;
                JustgramConfig.saveConfig();
                listView.adapter.update(false);
                if (JustgramConfig.webSocketTransport) {
                    org.telegram.tgnet.ConnectionsManager.setWebSocketEnabled(true, domain);
                }
            });
    }
}
