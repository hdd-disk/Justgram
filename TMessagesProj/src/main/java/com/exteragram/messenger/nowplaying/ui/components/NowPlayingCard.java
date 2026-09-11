package com.exteragram.messenger.nowplaying.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.exteragram.messenger.ExteraConfig;
import com.exteragram.messenger.api.dto.NowPlayingDTO;
import com.exteragram.messenger.nowplaying.ServiceEmoji;
import com.exteragram.messenger.utils.ui.UIUtil;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.utils.ViewOutlineProviderImpl;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PlayPauseDrawable;
import org.telegram.ui.Components.ScaleStateListAnimator;

@SuppressLint({"ViewConstructor"})
public abstract class NowPlayingCard extends FrameLayout {
    private final TextView albumView;
    private final TextView artistView;
    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private AudioFocusRequest audioFocusRequest;
    private final AudioManager audioManager;
    private final FrameLayout cardLayout;
    private long currentDocId;
    private String currentPreviewUrl;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji;
    private final BackupImageView imageView;
    private boolean isPlaying;
    private final TextView nameView;
    private NowPlayingCardData nowPlayingCardData;
    private final ImageView playPauseButton;
    private PlayPauseDrawable playPauseDrawable;
    private ExoPlayer player;
    private final Theme.ResourcesProvider resourcesProvider;
    private boolean resumeOnFocusGain;

    public abstract void onSavedMusicClick();

    public NowPlayingCard(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.audioFocusChangeListener = i -> handleAudioFocusChange(i);
        this.currentDocId = -1L;
        setClickable(false);
        setWillNotDraw(false);
        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            public void dispatchDraw(Canvas canvas) {
                if (NowPlayingCard.this.nowPlayingCardData != null) {
                    Integer accentColor = NowPlayingCard.this.nowPlayingCardData.getAccentColor();
                    if (accentColor == null) {
                        accentColor = NowPlayingCard.this.getThemedColor(Theme.key_windowBackgroundWhiteBlackText);
                    }
                    NowPlayingCard.this.emoji.setColor(accentColor);
                    UIUtil uIUtil = UIUtil.INSTANCE;
                    AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable2 = NowPlayingCard.this.emoji;
                    float width = getWidth();
                    float height = getHeight();
                    uIUtil.drawNowPlayingPattern(canvas, swapAnimatedEmojiDrawable2, width, height, NowPlayingCard.this.nowPlayingCardData.getCoverBitmap() == null ? 0.4f : 1.0f);
                }
                super.dispatchDraw(canvas);
            }
        };
        GradientDrawable gradientDrawable = new GradientDrawable() {
            @Override
            public void onBoundsChange(Rect rect) {
                super.onBoundsChange(rect);
                setGradientRadius(rect.width() * 2.0f);
            }
        };
        gradientDrawable.setCornerRadius(AndroidUtilities.dpf2(ExteraConfig.getSectionRadiusDp()));
        frameLayout.setBackground(gradientDrawable);
        frameLayout.setClipToOutline(true);
        frameLayout.setOutlineProvider(ViewOutlineProviderImpl.fromDrawable(gradientDrawable));
        frameLayout.setClickable(true);
        ScaleStateListAnimator.apply(frameLayout, 0.035f, 1.5f);
        this.cardLayout = frameLayout;
        addView(frameLayout, LayoutHelper.createFrame(-1, -2.0f));
        this.emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(frameLayout, false, AndroidUtilities.dp(20.0f), 13);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        frameLayout.addView(linearLayout, LayoutHelper.createLinear(-1, -2, 119, 12, 12, 12, 12));
        BackupImageView backupImageView = new BackupImageView(context);
        backupImageView.setClipToOutline(true);
        backupImageView.setOutlineProvider(ViewOutlineProviderImpl.boundsWithPaddingRoundRect(0, getCoverCornerRadius()));
        this.imageView = backupImageView;
        linearLayout.addView(backupImageView, LayoutHelper.createLinear(68, 68, 51, 0, 0, 12, 0));
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayout2, LayoutHelper.createLinear(0, -2, 1.0f, 16));
        TextView textView = new TextView(context);
        textView.setGravity(3);
        textView.setTextColor(-1);
        textView.setTextSize(1, 16.0f);
        textView.setSingleLine(true);
        TextUtils.TruncateAt truncateAt = TextUtils.TruncateAt.END;
        textView.setEllipsize(truncateAt);
        textView.setTypeface(AndroidUtilities.bold());
        NotificationCenter.listenEmojiLoading(textView);
        this.nameView = textView;
        linearLayout2.addView(textView, LayoutHelper.createLinear(-1, -2));
        TextView textView2 = new TextView(context);
        textView2.setGravity(3);
        textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView2.setTextSize(1, 14.0f);
        textView2.setSingleLine(true);
        textView2.setEllipsize(truncateAt);
        textView2.setTextColor(-1);
        textView2.setAlpha(0.6f);
        NotificationCenter.listenEmojiLoading(textView2);
        this.artistView = textView2;
        linearLayout2.addView(textView2, LayoutHelper.createLinear(-1, -2, 0.0f, 2.0f, 0.0f, 0.0f));
        TextView textView3 = new TextView(context);
        textView3.setGravity(3);
        textView3.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView3.setTextSize(1, 14.0f);
        textView3.setSingleLine(true);
        textView3.setEllipsize(truncateAt);
        textView3.setTextColor(-1);
        textView3.setAlpha(0.6f);
        NotificationCenter.listenEmojiLoading(textView3);
        this.albumView = textView3;
        linearLayout2.addView(textView3, LayoutHelper.createLinear(-1, -2, 0.0f, 2.0f, 0.0f, 0.0f));
        PlayPauseDrawable playPauseDrawable = new PlayPauseDrawable(16);
        playPauseDrawable.setPause(false);
        playPauseDrawable.setColor(-1);
        this.playPauseDrawable = playPauseDrawable;
        ImageView imageView = new ImageView(context);
        ScaleStateListAnimator.apply(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageDrawable(this.playPauseDrawable);
        imageView.setOnClickListener(v -> togglePlayPause());
        this.playPauseButton = imageView;
        linearLayout.addView(imageView, LayoutHelper.createLinear(32, 32, 16, 8, 0, 8, 0));
    }

    private void handleAudioFocusChange(int i) {
        if (i == -3 || i == -2) {
            if (this.player != null && this.player.isPlaying()) {
                this.resumeOnFocusGain = true;
                if (this.player != null) {
                    this.player.pause();
                }
            }
        } else if (i == -1) {
            this.resumeOnFocusGain = false;
            if (this.player != null) {
                this.player.pause();
            }
        } else if (i == 1 && this.resumeOnFocusGain) {
            if (this.player != null) {
                this.player.play();
            }
            this.resumeOnFocusGain = false;
        }
    }

    private float getCoverCornerRadius() {
        return AndroidUtilities.dpf2(RangesKt.coerceAtLeast(ExteraConfig.getSectionRadiusDp() - 12, 8));
    }

    public final void set(final NowPlayingCardData nowPlayingCardData) {
        this.nowPlayingCardData = nowPlayingCardData;
        final NowPlayingDTO nowPlayingDTO = nowPlayingCardData.getNowPlayingDTO();
        List<String> artists = nowPlayingDTO.getArtists();
        this.artistView.setText((CharSequence) null);
        this.nameView.setText((CharSequence) null);
        this.albumView.setText((CharSequence) null);
        if (artists == null || artists.isEmpty()) {
            this.artistView.setText(LocaleController.getString(R.string.AudioUnknownArtist));
        } else {
            this.artistView.setText(String.join(", ", artists));
        }
        this.nameView.setText(Emoji.replaceEmoji(nowPlayingDTO.getTrackName(), this.nameView.getPaint().getFontMetricsInt(), false));
        TextView textView = this.albumView;
        String albumName = nowPlayingDTO.getAlbumName();
        textView.setVisibility(albumName != null && albumName.length() != 0 && !Intrinsics.areEqual(nowPlayingDTO.getTrackName(), nowPlayingDTO.getAlbumName()) ? View.VISIBLE : View.GONE);
        if (this.albumView.getVisibility() == View.VISIBLE) {
            this.albumView.setText(Emoji.replaceEmoji(nowPlayingDTO.getAlbumName(), this.albumView.getPaint().getFontMetricsInt(), false));
        }
        setPadding(0, 0, 0, 0);
        Drawable background = this.cardLayout.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.mutate();
            gradientDrawable.setDither(true);
            gradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            gradientDrawable.setGradientCenter(1.0f, 0.5f);
            Integer backgroundColor = nowPlayingCardData.getBackgroundColor();
            int iIntValue = backgroundColor != null ? backgroundColor.intValue() : getThemedColor(Theme.key_windowBackgroundWhite);
            gradientDrawable.setColors(new int[]{iIntValue, nowPlayingCardData.getBackgroundColor() != null ? UIUtil.adjustHsl$default(UIUtil.INSTANCE, iIntValue, 1.5f, 0.0f, 4, null) : iIntValue});
        }
        ImageView imageView = this.playPauseButton;
        String previewUrl = nowPlayingDTO.getPreviewUrl();
        imageView.setVisibility(previewUrl != null && previewUrl.length() != 0 && !Intrinsics.areEqual(nowPlayingDTO.getPlatform(), "TELEGRAM") ? View.VISIBLE : View.GONE);
        ImageView imageView2 = this.playPauseButton;
        int iDp = AndroidUtilities.dp(32.0f);
        Integer accentColor = nowPlayingCardData.getAccentColor();
        imageView2.setBackground(Theme.createCircleDrawable(iDp, accentColor != null ? accentColor.intValue() : getThemedColor(Theme.key_featuredStickers_addButton)));
        long documentId = (nowPlayingCardData.getUserEmoji() <= 0 || !Intrinsics.areEqual(nowPlayingDTO.getPlatform(), "TELEGRAM")) ? ServiceEmoji.Companion.fromString(nowPlayingDTO.getPlatform()).getDocumentId() : nowPlayingCardData.getUserEmoji();
        if (documentId != this.currentDocId) {
            this.currentDocId = documentId;
            this.emoji.set(documentId, true);
        }
        final FrameLayout frameLayout = this.cardLayout;
        frameLayout.setOnClickListener(view -> onSavedMusicClick());
        frameLayout.setOnLongClickListener(view -> {
            onSavedMusicClick();
            return true;
        });
        if (nowPlayingCardData.getImageLocation() != null) {
            Bitmap coverBitmap = nowPlayingCardData.getCoverBitmap();
            this.imageView.setImage(nowPlayingCardData.getImageLocation(), (String) null, coverBitmap != null ? new BitmapDrawable(getContext().getResources(), coverBitmap) : null, 0, (Object) null);
            if (nowPlayingCardData.getCoverBitmap() != null) {
                this.artistView.setTextColor(-1);
                this.nameView.setTextColor(-1);
                this.albumView.setTextColor(-1);
            } else {
                int themedColor = getThemedColor(Theme.key_windowBackgroundWhiteBlackText);
                this.artistView.setTextColor(themedColor);
                this.nameView.setTextColor(themedColor);
                this.albumView.setTextColor(themedColor);
            }
        } else {
            this.imageView.setImageResource(R.drawable.nocover, getThemedColor(Theme.key_player_button));
            int themedColor2 = getThemedColor(Theme.key_windowBackgroundWhiteBlackText);
            this.artistView.setTextColor(themedColor2);
            this.nameView.setTextColor(themedColor2);
            this.albumView.setTextColor(themedColor2);
        }
        String previewUrl2 = nowPlayingCardData.getNowPlayingDTO().getPreviewUrl();
        if (!Intrinsics.areEqual(previewUrl2, this.currentPreviewUrl)) {
            this.currentPreviewUrl = previewUrl2;
            initializePlayer();
        }
        invalidate();
    }

    private void initializePlayer() {
        releasePlayer();
        if (this.nowPlayingCardData == null) {
            return;
        }
        String previewUrl = this.nowPlayingCardData.getNowPlayingDTO().getPreviewUrl();
        if (previewUrl == null) {
            return;
        }
        ExoPlayer exoPlayerBuild = new ExoPlayer.Builder(getContext()).build();
        exoPlayerBuild.setMediaSource(new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(getContext())).createMediaSource(MediaItem.fromUri(Uri.parse(previewUrl))));
        exoPlayerBuild.prepare();
        exoPlayerBuild.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean z) {
                NowPlayingCard.this.isPlaying = z;
                NowPlayingCard.this.updatePlayPauseButton();
                if (!z) {
                    NowPlayingCard.this.abandonAudioFocus();
                }
            }

            @Override
            public void onPlaybackStateChanged(int i) {
                if (i == Player.STATE_ENDED) {
                    NowPlayingCard.this.abandonAudioFocus();
                }
            }
        });
        this.player = exoPlayerBuild;
    }

    private void togglePlayPause() {
        if (this.player == null) {
            return;
        }
        if (this.player.isPlaying()) {
            this.player.pause();
            abandonAudioFocus();
        } else if (requestAudioFocus()) {
            if (this.player.getPlaybackState() == Player.STATE_ENDED) {
                this.player.seekTo(0L);
            }
            this.player.play();
        }
    }

    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT < 26) {
            return this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        AudioFocusRequest audioFocusRequestBuild = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setOnAudioFocusChangeListener(this.audioFocusChangeListener)
                .build();
        this.audioFocusRequest = audioFocusRequestBuild;
        return this.audioManager.requestAudioFocus(audioFocusRequestBuild) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (this.audioFocusRequest != null) {
                this.audioManager.abandonAudioFocusRequest(this.audioFocusRequest);
                this.audioFocusRequest = null;
            }
            return;
        }
        this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
    }

    private void updatePlayPauseButton() {
        if (this.playPauseDrawable != null) {
            this.playPauseDrawable.setPause(this.isPlaying);
        }
    }

    private void releasePlayer() {
        if (this.player != null) {
            this.player.release();
        }
        this.player = null;
        this.isPlaying = false;
        updatePlayPauseButton();
        abandonAudioFocus();
    }

    private int getThemedColor(int i) {
        return Theme.getColor(i, this.resourcesProvider);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.emoji.attach();
        if (this.player == null && this.nowPlayingCardData != null) {
            initializePlayer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.emoji.detach();
        releasePlayer();
    }
}
