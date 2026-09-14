package com.exteragram.messenger.utils.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Components.LayoutHelper;

public abstract class PopupUtils {

    @FunctionalInterface
    public interface OnItemClickListener {
        void onClick(int i);
    }

    public interface OnMultiSelectListener {
        void onClick(boolean[] zArr);
    }

    public static void showDialog(CharSequence[] charSequenceArr, String str, int i, Context context, OnItemClickListener onItemClickListener) {
        showDialog(charSequenceArr, null, str, i, context, onItemClickListener, null, true);
    }

    public static void showDialog(CharSequence[] charSequenceArr, int[] iArr, String str, int i, Context context, OnItemClickListener onItemClickListener) {
        showDialog(charSequenceArr, iArr, str, i, context, onItemClickListener, null, true);
    }

    public static void showDialog(CharSequence[] charSequenceArr, int[] iArr, String str, int i, Context context, OnItemClickListener onItemClickListener, Theme.ResourcesProvider resourcesProvider, boolean z) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(str);
        if (z) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            builder.setView(linearLayout);
            for (int i2 = 0; i2 < charSequenceArr.length; i2++) {
                RadioColorCell radioColorCell = new RadioColorCell(context);
                radioColorCell.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), 0);
                radioColorCell.setTag(i2);
                radioColorCell.setCheckColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_dialogRadioBackgroundChecked, resourcesProvider));
                radioColorCell.setTextAndValue(charSequenceArr[i2], i == i2);
                radioColorCell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 2));
                linearLayout.addView(radioColorCell);
                radioColorCell.setOnClickListener(view -> {
                    Integer num = (Integer) view.getTag();
                    builder.getDismissRunnable().run();
                    onItemClickListener.onClick(num);
                });
            }
        } else {
            if (iArr != null) {
                builder.setItems(charSequenceArr, iArr, (dialogInterface, i3) -> {
                    builder.getDismissRunnable().run();
                    onItemClickListener.onClick(i3);
                });
            } else {
                builder.setItems(charSequenceArr, (dialogInterface, i3) -> {
                    builder.getDismissRunnable().run();
                    onItemClickListener.onClick(i3);
                });
            }
            builder.create();
        }
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.show();
    }

    public static void showDialogWithoutRadio(ArrayList<? extends CharSequence> arrayList, String str, Context context, OnItemClickListener onItemClickListener) {
        CharSequence[] items = arrayList.toArray(new CharSequence[0]);
        showDialog(items, null, str, -1, context, onItemClickListener, null, false);
    }

    public static void showMultiSelectDialog(CharSequence[] charSequenceArr, boolean[] zArr, String str, Context context, OnMultiSelectListener onMultiSelectListener, Theme.ResourcesProvider resourcesProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(str);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < charSequenceArr.length; i++) {
            CheckBoxCell checkBoxCell = new CheckBoxCell(context, 4, 21, true, resourcesProvider);
            checkBoxCell.getCheckBoxRound().setColor(Theme.key_switch2TrackChecked, Theme.key_radioBackground, Theme.key_checkboxCheck);
            checkBoxCell.setText(charSequenceArr[i], null, i < zArr.length && zArr[i], i < charSequenceArr.length - 1);
            checkBoxCell.setOnClickListener(view -> checkBoxCell.setChecked(!checkBoxCell.isChecked(), true));
            checkBoxCell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 2));
            linearLayout.addView(checkBoxCell, LayoutHelper.createLinear(-1, -2));
        }
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString(R.string.OK), (alertDialog, i2) -> {
            int childCount = linearLayout.getChildCount();
            boolean[] result = new boolean[childCount];
            for (int i2Pos = 0; i2Pos < childCount; i2Pos++) {
                result[i2Pos] = ((CheckBoxCell) linearLayout.getChildAt(i2Pos)).isChecked();
            }
            onMultiSelectListener.onClick(result);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.show();
    }
}
