package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

public class JustgramAboutActivity extends BaseFragment {

    private final static int ID_GITHUB = 1;
    private final static int ID_CHANNEL = 2;

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.JustgramSettingsAbout));

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

        listView = new UniversalRecyclerView(this, this::fillItems, (item, view, position, x, y) -> onClick(item), null);
        listView.setSections();
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.adapter.update(false);

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        adapter.whiteSectionStart();
        items.add(SettingsActivity.SettingCell.Factory.of(ID_GITHUB, IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom, R.drawable.menu_website, getString(R.string.Github)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CHANNEL, IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom, R.drawable.settings_channel, getString(R.string.TelegramChannel)));
        adapter.whiteSectionEnd();

        items.add(UItem.asShadow(null));
    }

    private void onClick(UItem item) {
        switch (item.id) {
            case ID_GITHUB:
                Browser.openUrl(getParentActivity(), "https://github.com/hdd-disk/justgram");
                break;
            case ID_CHANNEL:
                Browser.openUrl(getParentActivity(), "https://t.me/Justgram_client");
                break;
        }
    }
}
