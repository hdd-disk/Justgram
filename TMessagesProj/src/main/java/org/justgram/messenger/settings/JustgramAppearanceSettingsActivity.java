package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.justgram.messenger.JustgramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.MainTabsActivity;

import java.util.ArrayList;
import java.util.List;

public class JustgramAppearanceSettingsActivity extends BaseFragment {
    private UniversalRecyclerView listView;

    private final static int ID_HIDE_BOTTOM_TABS_SUBTITLES = 1;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.JustgramSettingsAppearance));

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
        items.add(UItem.asCheck(ID_HIDE_BOTTOM_TABS_SUBTITLES, LocaleController.getString(R.string.HideBottomTabsSubtitles)).setChecked(JustgramConfig.hideTabsSubtitles));
        adapter.whiteSectionEnd();

        adapter.whiteSectionStart();
        items.add(UItem.asHeader(LocaleController.getString(R.string.LiquidGlassOpacity)));
        items.add(UItem.asIntSlideView(
                1,
                0, (int) ((1f - JustgramConfig.liquidGlassOpacity) * 100), 100,
                val -> val + "%",
                val -> JustgramConfig.liquidGlassOpacity = 1f - (val / 100f),
                val -> {
                    JustgramConfig.saveConfig();
                    if (parentLayout != null) {
                        parentLayout.rebuildAllFragmentViews(true, true);
                    }
                }
        ));
        adapter.whiteSectionEnd();
    }

    private void onClick(UItem item, View view) {
        switch (item.id) {
            case ID_HIDE_BOTTOM_TABS_SUBTITLES:
                JustgramConfig.hideTabsSubtitles = !JustgramConfig.hideTabsSubtitles;
                JustgramConfig.saveConfig();
                listView.adapter.update(true);

                if (LaunchActivity.instance != null) {
                    INavigationLayout navigationLayout = LaunchActivity.instance.getActionBarLayout();
                    if (navigationLayout != null) {
                        List<BaseFragment> fragments = navigationLayout.getFragmentStack();
                        for (BaseFragment fragment : fragments) {
                            if (fragment instanceof MainTabsActivity) {
                                ((MainTabsActivity) fragment).updateBottomTabsLayout();
                            }
                        }
                    }
                }
                break;
        }
    }
}
