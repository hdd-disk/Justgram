package org.telegram.ui.Components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.loadingindicator.LoadingIndicator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class M3ExpressiveLoadingView extends FrameLayout {

    private final LoadingIndicator loadingIndicator;

    public M3ExpressiveLoadingView(@NonNull Context context) {
        this(context, 48);
    }

    public M3ExpressiveLoadingView(@NonNull Context context, int sizeDp) {
        this(context, null, 0, sizeDp);
    }

    public M3ExpressiveLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int sizeDp) {
        super(context, attrs, defStyleAttr);

        loadingIndicator = new LoadingIndicator(context, attrs, defStyleAttr);
        int px = AndroidUtilities.dp(sizeDp);
        loadingIndicator.setIndicatorSize(px);
        loadingIndicator.setContainerWidth(px);
        loadingIndicator.setContainerHeight(px);

        int color = Theme.getColor(Theme.key_dialog_inlineProgress);
        loadingIndicator.setIndicatorColor(color);

        addView(loadingIndicator, LayoutHelper.createFrame(sizeDp, sizeDp, Gravity.CENTER));
        loadingIndicator.show();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (loadingIndicator != null) {
            loadingIndicator.getDrawable().setVisible(getVisibility() == VISIBLE, true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (loadingIndicator != null) {
            loadingIndicator.getDrawable().setVisible(false, false);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (loadingIndicator != null) {
            boolean visible = visibility == VISIBLE && getVisibility() == VISIBLE;
            loadingIndicator.getDrawable().setVisible(visible, false);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (loadingIndicator != null) {
            boolean visible = visibility == VISIBLE && getVisibility() == VISIBLE;
            loadingIndicator.getDrawable().setVisible(visible, false);
        }
    }

    public void setProgressColor(int color) {
        if (loadingIndicator != null) {
            loadingIndicator.setIndicatorColor(color);
        }
    }

    public void setSize(int sizeDp) {
        int px = AndroidUtilities.dp(sizeDp);
        if (loadingIndicator != null) {
            loadingIndicator.setIndicatorSize(px);
            loadingIndicator.setContainerWidth(px);
            loadingIndicator.setContainerHeight(px);
            loadingIndicator.setLayoutParams(LayoutHelper.createFrame(sizeDp, sizeDp, Gravity.CENTER));
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        if (loadingIndicator != null) {
            loadingIndicator.show();
            loadingIndicator.getDrawable().setVisible(true, true);
        }
    }

    public void hide() {
        if (loadingIndicator != null) {
            loadingIndicator.hide();
            loadingIndicator.getDrawable().setVisible(false, false);
        }
        setVisibility(GONE);
    }
}
