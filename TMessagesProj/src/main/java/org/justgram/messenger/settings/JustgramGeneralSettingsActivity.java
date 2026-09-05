package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
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

    private final static int ID_DISABLE_ADS = 1;
    private final static int ID_SHOW_ACCOUNT_ID = 2;
    private final static int ID_WS_TRANSPORT = 3;
    private final static int ID_WS_DOMAIN = 4;
    private final static int ID_FINGERPRINT = 5;
    private final static int ID_ALT_SOUND_IN = 6;

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
        listView.setSections();
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.adapter.update(false);

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        adapter.whiteSectionStart();
        items.add(UItem.asCheck(ID_DISABLE_ADS, getString(R.string.DisableAds)).setChecked(JustgramConfig.disableAds));
        items.add(UItem.asCheck(ID_SHOW_ACCOUNT_ID, getString(R.string.ShowAccountId)).setChecked(JustgramConfig.showAccountId));
        adapter.whiteSectionEnd();

        items.add(UItem.asShadow(null));

        adapter.whiteSectionStart();
        items.add(UItem.asCheck(ID_WS_TRANSPORT, getString(R.string.WebSocketTransport)).setChecked(JustgramConfig.webSocketTransport));
        if (JustgramConfig.webSocketTransport) {
            items.add(UItem.asSettingsCell(ID_WS_DOMAIN, getString(R.string.WebSocketDomain), getWebSocketDomainText()));
        }
        adapter.whiteSectionEnd();
        items.add(UItem.asShadow(getString(R.string.WebSocketTransportInfo)));

        adapter.whiteSectionStart();
        items.add(UItem.asCheck(ID_FINGERPRINT, getString(R.string.FingerprintProtection)).setChecked(JustgramConfig.fingerprintProtection));
        adapter.whiteSectionEnd();
        items.add(UItem.asShadow(getString(R.string.FingerprintProtectionInfo)));

        adapter.whiteSectionStart();
        items.add(UItem.asCheck(ID_ALT_SOUND_IN,getString(R.string.AltSoundIn)).setChecked(JustgramConfig.altSoundIn));
        adapter.whiteSectionEnd();
    }

    private void onClick(UItem item, View view) {
        switch (item.id) {
            case ID_DISABLE_ADS:
                JustgramConfig.disableAds = !JustgramConfig.disableAds;
                JustgramConfig.saveConfig();
                listView.adapter.update(true);
                break;
            case ID_SHOW_ACCOUNT_ID:
                JustgramConfig.showAccountId = !JustgramConfig.showAccountId;
                JustgramConfig.saveConfig();
                listView.adapter.update(true);
                break;
            case ID_FINGERPRINT:
                JustgramConfig.toggleFingerprintProtection();
                listView.adapter.update(true);
                break;
            case ID_WS_TRANSPORT:
                JustgramConfig.webSocketTransport = !JustgramConfig.webSocketTransport;
                JustgramConfig.saveConfig();
                org.telegram.tgnet.ConnectionsManager.setWebSocketEnabled(JustgramConfig.webSocketTransport, JustgramConfig.webSocketDomain);
                listView.adapter.update(true);
                break;
            case ID_WS_DOMAIN:
                showWebSocketDomainDialog();
                break;
            case ID_ALT_SOUND_IN:
                JustgramConfig.altSoundIn = !JustgramConfig.altSoundIn;
                JustgramConfig.saveConfig();
                listView.adapter.update(true);
                break;
        }
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
