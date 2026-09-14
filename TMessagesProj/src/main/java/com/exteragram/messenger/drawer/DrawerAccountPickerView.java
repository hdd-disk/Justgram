package com.exteragram.messenger.drawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.ExteraConfig;
import com.exteragram.messenger.api.dto.BadgeDTO;
import com.exteragram.messenger.badges.BadgesController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LoginActivity;

public class DrawerAccountPickerView extends FrameLayout {
    private final ArrayList<Integer> accounts;
    private final AccountAdapter adapter;
    private BadgeDTO badgeOverride;
    private final Paint bgPaint;
    private final RectF bgRect;
    private LinearGradient bottomGradient;
    private final Paint bottomGradientPaint;
    private final Paint clipMaskPaint;
    private final FrameLayout clipWrapper;
    private final float cornerRadius;
    private int currentAnimatedHeight;
    private View draggingItemView;
    private ValueAnimator expandAnimator;
    private boolean expanded;
    private final ItemTouchHelper itemTouchHelper;
    private int lastHeight;
    private OnAccountLongClick onAccountLongClick;
    private Runnable onAccountSelected;
    private final RecyclerView recyclerView;
    private LinearGradient topGradient;
    private final Paint topGradientPaint;
    private static final int COLOR_KEY_BACKGROUND = Theme.key_windowBackgroundGray;
    private static final int COLOR_KEY_SELECTOR = Theme.key_listSelector;
    private static final int COLOR_KEY_SURFACE = Theme.key_windowBackgroundWhite;
    private static final int COLOR_KEY_TEXT = Theme.key_windowBackgroundWhiteBlackText;
    private static final int COLOR_KEY_STATUS = Theme.key_profile_verifiedBackground;
    private static final int COLOR_KEY_ACCENT = Theme.key_featuredStickers_addButton;
    private static final int COLOR_KEY_ADD_ICON = Theme.key_featuredStickers_buttonText;

    @FunctionalInterface
    public interface OnAccountLongClick {
        void onLongClick(int i, View view);
    }

    public DrawerAccountPickerView(Context context) {
        super(context);
        this.accounts = new ArrayList<>();
        this.bgPaint = new Paint(1);
        Paint paint = new Paint(1);
        this.clipMaskPaint = paint;
        this.bgRect = new RectF();
        this.cornerRadius = AndroidUtilities.dp(16.0f);
        Paint paint2 = new Paint();
        this.topGradientPaint = paint2;
        Paint paint3 = new Paint();
        this.bottomGradientPaint = paint3;
        this.currentAnimatedHeight = -1;
        this.expanded = MessagesController.getGlobalMainSettings().getBoolean("accountsShown", true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
        paint2.setXfermode(new PorterDuffXfermode(mode));
        paint3.setXfermode(new PorterDuffXfermode(mode));
        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            public void onMeasure(int i, int i2) {
                int itemCount = DrawerAccountPickerView.this.adapter.getItemCount();
                int iDp = (int) ((AndroidUtilities.dp(48.0f) * (itemCount <= 6 ? itemCount : 5.5f)) + (AndroidUtilities.dp(4.0f) * 2));
                int size = MeasureSpec.getSize(i);
                measureChildren(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(iDp, MeasureSpec.EXACTLY));
                if (DrawerAccountPickerView.this.currentAnimatedHeight >= 0) {
                    setMeasuredDimension(size, DrawerAccountPickerView.this.currentAnimatedHeight);
                    return;
                }
                int size2 = MeasureSpec.getSize(i2);
                if (MeasureSpec.getMode(i2) == MeasureSpec.UNSPECIFIED || size2 > iDp) {
                    i2 = MeasureSpec.makeMeasureSpec(iDp, MeasureSpec.AT_MOST);
                }
                super.onMeasure(i, i2);
            }

            @Override
            public void dispatchDraw(Canvas canvas) {
                Canvas canvas2;
                DrawerAccountPickerView.this.bgPaint.setColor(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_BACKGROUND));
                DrawerAccountPickerView.this.bgRect.set(0.0f, 0.0f, getWidth(), getHeight());
                canvas.drawRoundRect(DrawerAccountPickerView.this.bgRect, DrawerAccountPickerView.this.cornerRadius, DrawerAccountPickerView.this.cornerRadius, DrawerAccountPickerView.this.bgPaint);
                int iSaveLayer = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null);
                super.dispatchDraw(canvas);
                if (DrawerAccountPickerView.this.topGradient == null || getHeight() != DrawerAccountPickerView.this.lastHeight) {
                    DrawerAccountPickerView.this.lastHeight = getHeight();
                    DrawerAccountPickerView drawerAccountPickerView = DrawerAccountPickerView.this;
                    Shader.TileMode tileMode = Shader.TileMode.CLAMP;
                    drawerAccountPickerView.topGradient = new LinearGradient(0.0f, 0.0f, 0.0f, AndroidUtilities.dp(16.0f), new int[]{-16777216, 0}, (float[]) null, tileMode);
                    DrawerAccountPickerView.this.topGradientPaint.setShader(DrawerAccountPickerView.this.topGradient);
                    DrawerAccountPickerView.this.bottomGradient = new LinearGradient(0.0f, getHeight(), 0.0f, getHeight() - AndroidUtilities.dp(16.0f), new int[]{-16777216, 0}, (float[]) null, tileMode);
                    DrawerAccountPickerView.this.bottomGradientPaint.setShader(DrawerAccountPickerView.this.bottomGradient);
                }
                int iDp = AndroidUtilities.dp(16.0f);
                int iComputeVerticalScrollOffset = DrawerAccountPickerView.this.recyclerView.computeVerticalScrollOffset();
                int iMax = Math.max(0, (DrawerAccountPickerView.this.recyclerView.computeVerticalScrollRange() - DrawerAccountPickerView.this.recyclerView.computeVerticalScrollExtent()) - iComputeVerticalScrollOffset);
                float f = iDp;
                float fMin = Math.min(1.0f, Math.max(0.0f, iComputeVerticalScrollOffset / f));
                float fMin2 = Math.min(1.0f, iMax / f);
                if (fMin > 0.0f) {
                    DrawerAccountPickerView.this.topGradientPaint.setAlpha((int) (fMin * 255.0f));
                    canvas.drawRect(0.0f, 0.0f, getWidth(), AndroidUtilities.dp(16.0f), DrawerAccountPickerView.this.topGradientPaint);
                }
                if (fMin2 > 0.0f) {
                    DrawerAccountPickerView.this.bottomGradientPaint.setAlpha((int) (fMin2 * 255.0f));
                    canvas2 = canvas;
                    canvas2.drawRect(0.0f, getHeight() - AndroidUtilities.dp(16.0f), getWidth(), getHeight(), DrawerAccountPickerView.this.bottomGradientPaint);
                } else {
                    canvas2 = canvas;
                }
                canvas2.drawRoundRect(DrawerAccountPickerView.this.bgRect, DrawerAccountPickerView.this.cornerRadius, DrawerAccountPickerView.this.cornerRadius, DrawerAccountPickerView.this.clipMaskPaint);
                canvas2.restoreToCount(iSaveLayer);
            }
        };
        this.clipWrapper = frameLayout;
        frameLayout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), DrawerAccountPickerView.this.cornerRadius);
            }
        });
        frameLayout.setClipToOutline(true);
        addView(frameLayout, LayoutHelper.createFrame(-1, 0.0f, 48, 12.0f, 0.0f, 12.0f, 0.0f));
        AccountAdapter accountAdapter = new AccountAdapter();
        this.adapter = accountAdapter;
        RecyclerView recyclerView = new RecyclerView(context);
        this.recyclerView = recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(accountAdapter);
        final int iDp = AndroidUtilities.dp(4.0f);
        recyclerView.setPadding(iDp, iDp, iDp, iDp);
        recyclerView.setClipToPadding(false);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView2, RecyclerView.State state) {
                int childAdapterPosition = recyclerView2.getChildAdapterPosition(view);
                int itemCount = state.getItemCount();
                if (childAdapterPosition < 0 || childAdapterPosition >= itemCount - 1) {
                    return;
                }
                rect.bottom = iDp;
            }
        });
        recyclerView.setOverScrollMode(2);
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView2, int i, int i2) {
                DrawerAccountPickerView.this.clipWrapper.invalidate();
            }
        });
        frameLayout.addView(recyclerView, LayoutHelper.createFrame(-1, -2.0f));
        final RecyclerView.ChildDrawingOrderCallback childDrawingOrderCallback = this::resolveChildDrawingOrder;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView2, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getAdapterPosition() >= DrawerAccountPickerView.this.accounts.size()) {
                    return 0;
                }
                return ItemTouchHelper.Callback.makeMovementFlags(3, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView2, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
                int adapterPosition = viewHolder.getAdapterPosition();
                int adapterPosition2 = viewHolder2.getAdapterPosition();
                if (adapterPosition >= DrawerAccountPickerView.this.accounts.size() || adapterPosition2 >= DrawerAccountPickerView.this.accounts.size()) {
                    return false;
                }
                DrawerAccountPickerView.this.adapter.swapElements(adapterPosition, adapterPosition2);
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
                if (i != 2 || viewHolder == null) {
                    return;
                }
                DrawerAccountPickerView.this.draggingItemView = viewHolder.itemView;
                DrawerAccountPickerView.this.draggingItemView.setPressed(false);
                DrawerAccountPickerView.this.draggingItemView.jumpDrawablesToCurrentState();
                DrawerAccountPickerView.this.recyclerView.setChildDrawingOrderCallback(childDrawingOrderCallback);
                DrawerAccountPickerView.this.recyclerView.invalidate();
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView2, RecyclerView.ViewHolder viewHolder, float f, float f2, int i, boolean z) {
                viewHolder.itemView.setTranslationX(f);
                viewHolder.itemView.setTranslationY(f2);
            }

            @Override
            public void clearView(RecyclerView recyclerView2, RecyclerView.ViewHolder viewHolder) {
                viewHolder.itemView.setTranslationX(0.0f);
                viewHolder.itemView.setTranslationY(0.0f);
                viewHolder.itemView.setPressed(false);
                if (DrawerAccountPickerView.this.draggingItemView == viewHolder.itemView) {
                    DrawerAccountPickerView.this.draggingItemView = null;
                }
                recyclerView2.setChildDrawingOrderCallback(null);
                recyclerView2.invalidate();
            }
        });
        this.itemTouchHelper = itemTouchHelper;
        itemTouchHelper.attachToRecyclerView(recyclerView);
        if (this.expanded) {
            loadAccounts();
            setVisibility(VISIBLE);
            ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
            layoutParams.height = -2;
            frameLayout.setLayoutParams(layoutParams);
            return;
        }
        setVisibility(GONE);
    }

    private int resolveChildDrawingOrder(int i, int i2) {
        int iIndexOfChild;
        View view = this.draggingItemView;
        if (view != null && (iIndexOfChild = this.recyclerView.indexOfChild(view)) >= 0) {
            if (i2 == i - 1) {
                return iIndexOfChild;
            }
            if (i2 >= iIndexOfChild) {
                return i2 + 1;
            }
        }
        return i2;
    }

    public void setOnAccountSelected(Runnable runnable) {
        this.onAccountSelected = runnable;
    }

    public void setOnAccountLongClick(OnAccountLongClick onAccountLongClick) {
        this.onAccountLongClick = onAccountLongClick;
    }

    public void loadAccounts() {
        loadAccounts(null);
    }

    public void loadAccounts(BadgeDTO badgeDTO) {
        this.badgeOverride = badgeDTO;
        this.accounts.clear();
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            if (UserConfig.getInstance(i).isClientActivated()) {
                this.accounts.add(i);
            }
        }
        this.accounts.sort(Comparator.comparingLong(num -> UserConfig.getInstance(num).loginTime));
        this.adapter.notifyDataSetChanged();
    }

    public void toggleExpand() {
        setExpanded(!this.expanded);
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean z) {
        if (this.expanded == z) {
            return;
        }
        this.expanded = z;
        MessagesController.getGlobalMainSettings().edit().putBoolean("accountsShown", z).apply();
        if (z) {
            loadAccounts();
            setVisibility(VISIBLE);
        }
        ValueAnimator valueAnimator = this.expandAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        int measuredHeight = this.currentAnimatedHeight;
        if (measuredHeight < 0) {
            measuredHeight = this.clipWrapper.getLayoutParams().height;
            if (measuredHeight < 0) {
                measuredHeight = this.clipWrapper.getHeight();
            }
            if (measuredHeight < 0) {
                measuredHeight = z ? 0 : this.clipWrapper.getMeasuredHeight();
            }
        }
        this.currentAnimatedHeight = -1;
        int itemCount = this.adapter.getItemCount();
        this.clipWrapper.measure(MeasureSpec.makeMeasureSpec(((View) getParent()).getMeasuredWidth() - AndroidUtilities.dp(24.0f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) ((AndroidUtilities.dp(48.0f) * (itemCount <= 6 ? itemCount : 5.5f)) + (AndroidUtilities.dp(4.0f) * 2)), MeasureSpec.AT_MOST));
        int measuredHeight2 = z ? this.clipWrapper.getMeasuredHeight() : 0;
        this.currentAnimatedHeight = measuredHeight;
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(measuredHeight, measuredHeight2);
        this.expandAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.setDuration(250L);
        valueAnimatorOfInt.setInterpolator(CubicBezierInterpolator.DEFAULT);
        valueAnimatorOfInt.addUpdateListener(this::onExpandAnimationUpdate);
        valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (this.cancelled || DrawerAccountPickerView.this.expandAnimator != animator) {
                    return;
                }
                DrawerAccountPickerView.this.expandAnimator = null;
                DrawerAccountPickerView.this.currentAnimatedHeight = -1;
                boolean z2 = DrawerAccountPickerView.this.expanded;
                DrawerAccountPickerView drawerAccountPickerView = DrawerAccountPickerView.this;
                if (!z2) {
                    drawerAccountPickerView.setVisibility(GONE);
                    return;
                }
                ViewGroup.LayoutParams layoutParams = drawerAccountPickerView.clipWrapper.getLayoutParams();
                layoutParams.height = -2;
                DrawerAccountPickerView.this.clipWrapper.setLayoutParams(layoutParams);
            }
        });
        valueAnimatorOfInt.start();
    }

    private void onExpandAnimationUpdate(ValueAnimator valueAnimator) {
        this.currentAnimatedHeight = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        this.clipWrapper.requestLayout();
    }

    public void updateColors() {
        this.bgPaint.setColor(Theme.getColor(COLOR_KEY_BACKGROUND));
        invalidate();
        this.adapter.notifyDataSetChanged();
    }

    public void updateUnreadCounters() {
        if (this.recyclerView.getChildCount() == 0) {
            return;
        }
        for (int i = 0; i < this.recyclerView.getChildCount(); i++) {
            View childAt = this.recyclerView.getChildAt(i);
            if (childAt instanceof AccountRowView) {
                ((AccountRowView) childAt).updateUnreadCounter();
            }
        }
    }

    public void dispose() {
        ValueAnimator valueAnimator = this.expandAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.expandAnimator = null;
        }
        this.draggingItemView = null;
        this.recyclerView.setChildDrawingOrderCallback(null);
        this.recyclerView.stopScroll();
    }

    private void openAddAccountFlow() {
        BaseFragment safeLastFragment;
        Activity activityFindActivity = AndroidUtilities.findActivity(getContext());
        LaunchActivity launchActivity = activityFindActivity instanceof LaunchActivity ? (LaunchActivity) activityFindActivity : LaunchActivity.instance;
        if (launchActivity == null) {
            return;
        }
        Integer availableAccountForAdd = getAvailableAccountForAdd();
        if (availableAccountForAdd != null) {
            launchActivity.presentFragment(new LoginActivity(availableAccountForAdd));
        } else {
            if (UserConfig.hasPremiumOnAccounts() || (safeLastFragment = LaunchActivity.getSafeLastFragment()) == null) {
                return;
            }
            safeLastFragment.showDialog(new LimitReachedBottomSheet(safeLastFragment, launchActivity, 7, safeLastFragment.getCurrentAccount(), null));
        }
    }

    private int freeAccountsForAdd() {
        int active = UserConfig.getActivatedAccountsCount();
        int maxAllowed = UserConfig.hasPremiumOnAccounts() ? UserConfig.MAX_ACCOUNT_COUNT : Math.min(4, UserConfig.MAX_ACCOUNT_COUNT);
        return Math.max(0, maxAllowed - active);
    }

    private Integer getAvailableAccountForAdd() {
        if (freeAccountsForAdd() <= 0) {
            return null;
        }
        for (int i = UserConfig.MAX_ACCOUNT_COUNT - 1; i >= 0; i--) {
            if (!UserConfig.getInstance(i).isClientActivated()) {
                return i;
            }
        }
        return null;
    }

    private boolean canAddAccount() {
        return getAvailableAccountForAdd() != null;
    }

    private static Drawable createAccountItemRippleDrawable() {
        return Theme.createRadSelectorDrawable(Theme.getColor(COLOR_KEY_SELECTOR), 12, 12);
    }

    private static Drawable createSelectedAccountBackgroundDrawable() {
        return Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(12.0f), Theme.getColor(COLOR_KEY_SURFACE), Theme.getColor(COLOR_KEY_SELECTOR));
    }

    public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private AccountAdapter() {
        }

        @Override
        public int getItemViewType(int i) {
            return i < DrawerAccountPickerView.this.accounts.size() ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return DrawerAccountPickerView.this.accounts.size() + (DrawerAccountPickerView.this.canAddAccount() ? 1 : 0);
        }

        public void swapElements(int i, int i2) {
            if (i < 0 || i2 < 0 || i >= DrawerAccountPickerView.this.accounts.size() || i2 >= DrawerAccountPickerView.this.accounts.size()) {
                return;
            }
            UserConfig userConfig = UserConfig.getInstance(DrawerAccountPickerView.this.accounts.get(i));
            UserConfig userConfig2 = UserConfig.getInstance(DrawerAccountPickerView.this.accounts.get(i2));
            int i3 = userConfig.loginTime;
            userConfig.loginTime = userConfig2.loginTime;
            userConfig2.loginTime = i3;
            userConfig.saveConfig(false);
            userConfig2.saveConfig(false);
            Collections.swap(DrawerAccountPickerView.this.accounts, i, i2);
            notifyItemMoved(i, i2);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i == 1) {
                return new RecyclerView.ViewHolder(new AddAccountView(viewGroup.getContext())) {
                };
            }
            return new RecyclerView.ViewHolder(new AccountRowView(viewGroup.getContext())) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int itemViewType = getItemViewType(i);
            if (itemViewType == 0) {
                bindAccountViewHolder(viewHolder, i);
            } else {
                if (itemViewType != 1) {
                    return;
                }
                bindAddAccountViewHolder(viewHolder);
            }
        }

        private void bindAccountViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
            View view = viewHolder.itemView;
            if (view instanceof AccountRowView) {
                AccountRowView accountRowView = (AccountRowView) view;
                final Integer num = DrawerAccountPickerView.this.accounts.get(i);
                accountRowView.bind(num, num == UserConfig.selectedAccount ? DrawerAccountPickerView.this.badgeOverride : null);
                accountRowView.setOnClickListener(view2 -> onAccountRowClicked(num, view2));
                accountRowView.setOnLongClickListener(view2 -> onAccountRowLongClicked(num, viewHolder, view2));
            }
        }

        private void onAccountRowClicked(Integer num, View view) {
            if (num != UserConfig.selectedAccount) {
                if (DrawerAccountPickerView.this.onAccountSelected != null) {
                    DrawerAccountPickerView.this.onAccountSelected.run();
                }
                Context context = DrawerAccountPickerView.this.getContext();
                if (context instanceof LaunchActivity) {
                    ((LaunchActivity) context).switchToAccount(num, true);
                }
            }
        }

        private boolean onAccountRowLongClicked(Integer num, RecyclerView.ViewHolder viewHolder, View view) {
            int iIntValue = num;
            int i = UserConfig.selectedAccount;
            DrawerAccountPickerView drawerAccountPickerView = DrawerAccountPickerView.this;
            if (iIntValue == i) {
                drawerAccountPickerView.itemTouchHelper.startDrag(viewHolder);
                return true;
            }
            if (drawerAccountPickerView.onAccountLongClick == null) {
                return true;
            }
            DrawerAccountPickerView.this.onAccountLongClick.onLongClick(num, view);
            return true;
        }

        private void bindAddAccountViewHolder(RecyclerView.ViewHolder viewHolder) {
            View view = viewHolder.itemView;
            if (view instanceof AddAccountView) {
                ((AddAccountView) view).updateColors();
            }
            viewHolder.itemView.setOnClickListener(this::onAddAccountRowClicked);
        }

        private void onAddAccountRowClicked(View view) {
            if (DrawerAccountPickerView.this.onAccountSelected != null) {
                DrawerAccountPickerView.this.onAccountSelected.run();
            }
            final DrawerAccountPickerView drawerAccountPickerView = DrawerAccountPickerView.this;
            AndroidUtilities.runOnUIThread(drawerAccountPickerView::openAddAccountFlow, 150L);
        }
    }

    public static class AccountRowView extends FrameLayout {
        private final AvatarDrawable avatarDrawable;
        private final RectF avatarRect;
        private final BackupImageView avatarView;
        private final Paint checkPaint;
        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable exteraBadgeDrawable;
        private final SimpleTextView nameView;
        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable premiumStatusDrawable;
        private boolean selected;
        private final DrawerAccountUnreadBadge unreadBadge;

        public AccountRowView(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.checkPaint = paint;
            this.avatarRect = new RectF();
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(44.0f)));
            setBackground(DrawerAccountPickerView.createAccountItemRippleDrawable());
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            this.avatarDrawable = avatarDrawable;
            avatarDrawable.setTextSize(AndroidUtilities.dp(20.0f));
            BackupImageView backupImageView = new BackupImageView(context);
            this.avatarView = backupImageView;
            updateAvatarRadius();
            addView(backupImageView, LayoutHelper.createFrame(34, 34.0f, 19, 8.0f, 0.0f, 0.0f, 0.0f));
            SimpleTextView simpleTextView = new SimpleTextView(context);
            this.nameView = simpleTextView;
            simpleTextView.setTextSize(15);
            simpleTextView.setTypeface(AndroidUtilities.bold());
            simpleTextView.setTextColor(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_TEXT));
            simpleTextView.setGravity(19);
            simpleTextView.setEllipsizeByGradient(true);
            simpleTextView.setCanHideRightDrawable(false);
            simpleTextView.setRightDrawableOutside(true);
            addView(simpleTextView, LayoutHelper.createFrame(-1, -1.0f, 3, 54.0f, 0.0f, 12.0f, 0.0f));
            this.premiumStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(simpleTextView, AndroidUtilities.dp(18.0f));
            this.exteraBadgeDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(simpleTextView, AndroidUtilities.dp(18.0f));
            this.unreadBadge = new DrawerAccountUnreadBadge();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(AndroidUtilities.dp(1.67f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        public void bind(int i, BadgeDTO badgeDTO) {
            Drawable drawable;
            org.telegram.tgnet.TLRPC.User currentUser = UserConfig.getInstance(i).getCurrentUser();
            if (currentUser == null) {
                return;
            }
            updateAvatarRadius();
            this.avatarDrawable.setInfo(i, currentUser);
            this.nameView.setTextColor(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_TEXT));
            this.nameView.setText(ContactsController.formatName(currentUser));
            this.avatarView.getImageReceiver().setCurrentAccount(i);
            this.avatarView.setForUserOrChat((TLObject) (Object) currentUser, this.avatarDrawable);
            this.premiumStatusDrawable.setCurrentAccount(i);
            this.exteraBadgeDrawable.setCurrentAccount(i);
            int color = Theme.getColor(DrawerAccountPickerView.COLOR_KEY_STATUS);
            long emojiStatusDocumentId = DialogObject.getEmojiStatusDocumentId(currentUser.emoji_status);
            boolean zIsPremiumUser = MessagesController.getInstance(i).isPremiumUser(currentUser);
            if (badgeDTO == null) {
                badgeDTO = BadgesController.INSTANCE.getBadge(currentUser);
            }
            Drawable drawable2 = null;
            if (emojiStatusDocumentId != 0) {
                this.premiumStatusDrawable.set(emojiStatusDocumentId, false);
                drawable = this.premiumStatusDrawable;
            } else {
                AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable = this.premiumStatusDrawable;
                if (zIsPremiumUser) {
                    swapAnimatedEmojiDrawable.set(PremiumGradient.getInstance().premiumStarDrawableMini, false);
                    drawable = this.premiumStatusDrawable;
                } else {
                    swapAnimatedEmojiDrawable.set((Drawable) null, false);
                    drawable = null;
                }
            }
            this.premiumStatusDrawable.setColor(color);
            this.premiumStatusDrawable.setParticles(DialogObject.isEmojiStatusCollectible(currentUser.emoji_status), false);
            Drawable drawableUpdateBadgeDrawable = updateBadgeDrawable(badgeDTO, color);
            if (drawableUpdateBadgeDrawable != null) {
                if (drawable != null) {
                    drawable2 = drawableUpdateBadgeDrawable;
                } else {
                    drawable = drawableUpdateBadgeDrawable;
                }
            }
            applyNameDrawables(drawable, drawable2);
            this.unreadBadge.bind(i, this.nameView);
            this.checkPaint.setColor(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_ACCENT));
            boolean z = i == UserConfig.selectedAccount;
            this.selected = z;
            float f = z ? 0.785f : 1.0f;
            this.avatarView.setScaleX(f);
            this.avatarView.setScaleY(f);
            setBackground(this.selected ? DrawerAccountPickerView.createSelectedAccountBackgroundDrawable() : DrawerAccountPickerView.createAccountItemRippleDrawable());
            setPadding(0, 0, 0, 0);
            invalidate();
        }

        private void updateAvatarRadius() {
            this.avatarView.setRoundRadius(AndroidUtilities.dp(17.0f));
        }

        public void updateUnreadCounter() {
            this.unreadBadge.update(this.nameView);
            invalidate();
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.premiumStatusDrawable.attach();
            this.exteraBadgeDrawable.attach();
        }

        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.premiumStatusDrawable.detach();
            this.exteraBadgeDrawable.detach();
        }

        private Drawable updateBadgeDrawable(BadgeDTO badgeDTO, int i) {
            if (badgeDTO == null) {
                clearBadgeDrawables();
                return null;
            }
            this.exteraBadgeDrawable.set(badgeDTO.getDocumentId(), false);
            this.exteraBadgeDrawable.setParticles(true, false);
            this.exteraBadgeDrawable.setColor(i);
            return this.exteraBadgeDrawable;
        }

        private void clearBadgeDrawables() {
            this.exteraBadgeDrawable.set((Drawable) null, false);
            this.exteraBadgeDrawable.setParticles(false, false);
            this.exteraBadgeDrawable.setColor(null);
        }

        private void applyNameDrawables(Drawable drawable, Drawable drawable2) {
            if (drawable != null && drawable == this.nameView.getRightDrawable2()) {
                this.nameView.setRightDrawable2(null);
            }
            this.nameView.setRightDrawable(drawable);
            this.nameView.setRightDrawable2(drawable2);
        }

        @Override
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            this.unreadBadge.draw(this, canvas);
            if (this.selected) {
                float strokeWidth = this.checkPaint.getStrokeWidth() / 2.0f;
                this.avatarRect.set(this.avatarView.getLeft() + strokeWidth, this.avatarView.getTop() + strokeWidth, this.avatarView.getRight() - strokeWidth, this.avatarView.getBottom() - strokeWidth);
                float avatarCorners = AndroidUtilities.dp(17.0f);
                canvas.drawRoundRect(this.avatarRect, avatarCorners, avatarCorners, this.checkPaint);
            }
        }
    }

    public static class AddAccountView extends LinearLayout {
        private final Drawable circleDrawable;
        private final Drawable plusDrawable;
        private final SimpleTextView textView;

        public AddAccountView(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(16);
            setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(44.0f)));
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.poll_add_circle, null);
            this.circleDrawable = drawable;
            Drawable drawable2 = ResourcesCompat.getDrawable(context.getResources(), R.drawable.poll_add_plus, null);
            this.plusDrawable = drawable2;
            if (drawable != null) {
                drawable.mutate();
            }
            if (drawable2 != null) {
                drawable2.mutate();
            }
            CombinedDrawable combinedDrawable = new CombinedDrawable(drawable, drawable2) {
                @Override
                public void setColorFilter(ColorFilter colorFilter) {
                }
            };
            combinedDrawable.setCustomSize(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(24.0f));
            imageView.setImageDrawable(combinedDrawable);
            addView(imageView, LayoutHelper.createLinear(34, 34, 16, 8, 0, 0, 0));
            SimpleTextView simpleTextView = new SimpleTextView(context);
            this.textView = simpleTextView;
            simpleTextView.setTextSize(15);
            simpleTextView.setTypeface(AndroidUtilities.bold());
            simpleTextView.setGravity(19);
            simpleTextView.setText(LocaleController.getString(R.string.AddAccount));
            addView(simpleTextView, LayoutHelper.createLinear(-2, -1, 16, 12, 0, 12, 0));
            updateColors();
        }

        public void updateColors() {
            setBackground(DrawerAccountPickerView.createAccountItemRippleDrawable());
            Drawable drawable = this.circleDrawable;
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_ACCENT), PorterDuff.Mode.SRC_IN));
            }
            Drawable drawable2 = this.plusDrawable;
            if (drawable2 != null) {
                drawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_ADD_ICON), PorterDuff.Mode.SRC_IN));
            }
            this.textView.setTextColor(Theme.getColor(DrawerAccountPickerView.COLOR_KEY_TEXT));
        }
    }
}
