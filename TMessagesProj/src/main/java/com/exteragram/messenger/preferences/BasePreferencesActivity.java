package com.exteragram.messenger.preferences;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.exteragram.messenger.utils.ui.PopupUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalFragment;

import java.util.function.Consumer;

public abstract class BasePreferencesActivity extends UniversalFragment {

    public void initializeOptionStrings() {
    }

    @Override
    public View createView(Context context) {
        initializeOptionStrings();
        View view = super.createView(context);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setAdaptiveBackground(listView);
        listView.setSections();
        listView.adapter.setApplyBackground(false);
        listView.setClipToPadding(false);
        return view;
    }

    public void showListDialog(UItem uItem, CharSequence[] items, String title, int selectedIndex, PopupUtils.OnItemClickListener listener) {
        showListDialog(uItem, items, null, title, selectedIndex, listener, true, true);
    }

    public void showListDialog(UItem uItem, CharSequence[] items, int[] icons, String title, int selectedIndex, PopupUtils.OnItemClickListener listener, boolean z, boolean z2) {
        if (getParentActivity() == null) {
            return;
        }
        PopupUtils.showDialog(items, icons, title, selectedIndex, getContext(), idx -> {
            if (z2 && selectedIndex == idx) {
                return;
            }
            listener.onClick(idx);
            View view = listView.findViewByItemId(uItem.id);
            if (view instanceof TextCell) {
                ((TextCell) view).setValue(items[idx], true);
            }
            listView.adapter.update(true);
        }, getResourceProvider(), z);
    }

    public void showRestartBulletin() {
        BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.RestartRequired), LocaleController.getString(R.string.BotUnblock), () -> {
            Context context = getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            Intent intent = Intent.makeRestartActivityTask(launchIntent == null ? null : launchIntent.getComponent());
            if (launchIntent != null) {
                intent.setPackage(context.getPackageName());
            }
            context.startActivity(intent);
            Runtime.getRuntime().exit(0);
        }).show();
    }

    public void toggleBooleanSettingAndRefresh(UItem uItem, Consumer<Boolean> consumer) {
        boolean newValue = !uItem.checked;
        consumer.accept(newValue);
        uItem.setChecked(newValue);
        View view = listView.findViewByItemId(uItem.id);
        if (view instanceof CheckBoxCell) {
            ((CheckBoxCell) view).setChecked(newValue, true);
        } else if (view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(newValue);
        }
        listView.adapter.update(true);
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
