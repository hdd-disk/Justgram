package org.justgram.messenger.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

public class JustgramSettingsActivity extends BaseFragment {

    private final static int ID_GENERAL = 1;
    private final static int ID_APPEARANCE = 2;
    private final static int ID_EXPERIMENTAL = 3;
    private final static int ID_ABOUT = 4;

    private UniversalRecyclerView listView;
    private FrameLayout topView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.JustgramSettings));

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

        topView = new FrameLayout(context);

        FrameLayout logoContainer = new FrameLayout(context);
        BackupImageView logoView = new BackupImageView(context);

        logoView.setImageDrawable(AppCompatResources.getDrawable(context, R.mipmap.ic_launcher));
        logoContainer.addView(logoView, LayoutHelper.createFrame(90, 90, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15, 0, 0));
        topView.addView(logoContainer, LayoutHelper.createFrame(120, 120, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 23 - 12, 0, 0));

        TextView titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setGravity(Gravity.CENTER);
        titleView.setSingleLine();
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setText(LocaleController.getString(R.string.AppName));
        titleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        topView.addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 138.333f - 12, 0, 0));

        TextView subtitleView = new TextView(context);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setSingleLine();
        subtitleView.setEllipsize(TextUtils.TruncateAt.END);
        subtitleView.setText(BuildConfig.BUILD_VERSION_STRING);
        subtitleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
        topView.addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 168 - 12, 0, 24));

        listView.adapter.update(false);

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        UItem header = UItem.asTopView(getString(R.string.AppName), BuildConfig.BUILD_VERSION_STRING, R.mipmap.ic_launcher);
        header.accent = true;
        items.add(header);

        adapter.whiteSectionStart();
        items.add(SettingsActivity.SettingCell.Factory.of(ID_GENERAL, IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom, R.drawable.settings_account, getString(R.string.JustgramSettingsGeneral)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_APPEARANCE, IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom, R.drawable.settings_chat, getString(R.string.JustgramSettingsAppearance)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_EXPERIMENTAL, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom, R.drawable.settings_features, getString(R.string.JustgramSettingsExperimental)));
        adapter.whiteSectionEnd();

        items.add(UItem.asShadow(null));

        adapter.whiteSectionStart();
        items.add(SettingsActivity.SettingCell.Factory.of(ID_ABOUT, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_faq, getString(R.string.JustgramSettingsAbout)));
        adapter.whiteSectionEnd();

        items.add(UItem.asShadow(null));
    }

    private void onClick(UItem item) {
        switch (item.id) {
            case ID_GENERAL:
                presentFragment(new JustgramGeneralSettingsActivity());
                break;
            case ID_APPEARANCE:
                presentFragment(new JustgramAppearanceSettingsActivity());
                break;
            case ID_ABOUT:
                presentFragment(new JustgramAboutActivity());
                break;
        }
    }
}
