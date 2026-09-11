package com.exteragram.messenger.nowplaying.ui.components;

import android.graphics.Bitmap;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import com.exteragram.messenger.api.dto.NowPlayingDTO;
import com.exteragram.messenger.utils.ui.UIUtil;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

public final class NowPlayingCardData {
    public static final Companion Companion = new Companion(null);
    private final Integer accentColor;
    private final Integer backgroundColor;
    private final Bitmap coverBitmap;
    private final ImageLocation imageLocation;
    private final NowPlayingDTO nowPlayingDTO;
    private long userEmoji;

    public interface Callback {
        void onDataLoaded(NowPlayingCardData nowPlayingCardData);
    }

    public static final void create(NowPlayingDTO nowPlayingDTO, TLRPC.Document document, Callback callback) {
        Companion.create(nowPlayingDTO, document, callback);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NowPlayingCardData)) {
            return false;
        }
        NowPlayingCardData nowPlayingCardData = (NowPlayingCardData) obj;
        return Intrinsics.areEqual(this.nowPlayingDTO, nowPlayingCardData.nowPlayingDTO) && Intrinsics.areEqual(this.backgroundColor, nowPlayingCardData.backgroundColor) && Intrinsics.areEqual(this.accentColor, nowPlayingCardData.accentColor) && Intrinsics.areEqual(this.coverBitmap, nowPlayingCardData.coverBitmap) && Intrinsics.areEqual(this.imageLocation, nowPlayingCardData.imageLocation) && this.userEmoji == nowPlayingCardData.userEmoji;
    }

    public int hashCode() {
        int iHashCode = this.nowPlayingDTO != null ? this.nowPlayingDTO.hashCode() * 31 : 0;
        Integer num = this.backgroundColor;
        int iHashCode2 = (iHashCode + (num == null ? 0 : num.hashCode())) * 31;
        Integer num2 = this.accentColor;
        int iHashCode3 = (iHashCode2 + (num2 == null ? 0 : num2.hashCode())) * 31;
        Bitmap bitmap = this.coverBitmap;
        int iHashCode4 = (iHashCode3 + (bitmap == null ? 0 : bitmap.hashCode())) * 31;
        ImageLocation imageLocation = this.imageLocation;
        return ((iHashCode4 + (imageLocation != null ? imageLocation.hashCode() : 0)) * 31) + Long.hashCode(this.userEmoji);
    }

    public String toString() {
        return "NowPlayingCardData(nowPlayingDTO=" + this.nowPlayingDTO + ", backgroundColor=" + this.backgroundColor + ", accentColor=" + this.accentColor + ", coverBitmap=" + this.coverBitmap + ", imageLocation=" + this.imageLocation + ", userEmoji=" + this.userEmoji + ')';
    }

    public NowPlayingCardData(NowPlayingDTO nowPlayingDTO, Integer num, Integer num2, Bitmap bitmap, ImageLocation imageLocation, long j) {
        this.nowPlayingDTO = nowPlayingDTO;
        this.backgroundColor = num;
        this.accentColor = num2;
        this.coverBitmap = bitmap;
        this.imageLocation = imageLocation;
        this.userEmoji = j;
    }

    public /* synthetic */ NowPlayingCardData(NowPlayingDTO nowPlayingDTO, Integer num, Integer num2, Bitmap bitmap, ImageLocation imageLocation, long j, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(nowPlayingDTO, num, num2, bitmap, (i & 16) != 0 ? null : imageLocation, (i & 32) != 0 ? -1L : j);
    }

    public final NowPlayingDTO getNowPlayingDTO() {
        return this.nowPlayingDTO;
    }

    public final Integer getBackgroundColor() {
        return this.backgroundColor;
    }

    public final Integer getAccentColor() {
        return this.accentColor;
    }

    public final Bitmap getCoverBitmap() {
        return this.coverBitmap;
    }

    public final ImageLocation getImageLocation() {
        return this.imageLocation;
    }

    public final long getUserEmoji() {
        return this.userEmoji;
    }

    public final void setUserEmoji(long j) {
        this.userEmoji = j;
    }

    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final void create(final NowPlayingDTO nowPlayingDTO, TLRPC.Document document, final Callback callback) {
            String coverUrl;
            TLRPC.PhotoSize closestPhotoSizeWithSize;
            ImageLocation forDocument = (!Intrinsics.areEqual(nowPlayingDTO.getPlatform(), "TELEGRAM") || document == null || (closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 1000)) == null) ? null : ImageLocation.getForDocument(closestPhotoSizeWithSize, document);
            if (forDocument == null && (coverUrl = nowPlayingDTO.getCoverUrl()) != null && coverUrl.length() != 0) {
                forDocument = ImageLocation.getForPath(nowPlayingDTO.getCoverUrl());
            }
            final ImageLocation imageLocation = forDocument;
            if (imageLocation == null) {
                AndroidUtilities.runOnUIThread(() -> callback.onDataLoaded(new NowPlayingCardData(nowPlayingDTO, null, null, null, null, 0L, 32, null)));
                return;
            }
            final ImageReceiver imageReceiver = new ImageReceiver(null);
            final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            final Ref.ObjectRef<Runnable> ref$ObjectRef = new Ref.ObjectRef<>();
            ref$ObjectRef.element = () -> create$finish(atomicBoolean, ref$ObjectRef, imageReceiver, callback, new NowPlayingCardData(nowPlayingDTO, null, null, null, imageLocation, 0L, 32, null));
            AndroidUtilities.runOnUIThread(() -> m1242$r8$lambda$AOf_1ZcjMCUyw5uKAO3aSljOk(imageReceiver, ref$ObjectRef, imageLocation, nowPlayingDTO, atomicBoolean, callback));
        }

        private static void create$finish(AtomicBoolean atomicBoolean, Ref.ObjectRef<Runnable> ref$ObjectRef, ImageReceiver imageReceiver, Callback callback, NowPlayingCardData nowPlayingCardData) {
            if (atomicBoolean.compareAndSet(false, true)) {
                Runnable runnable = ref$ObjectRef.element;
                AndroidUtilities.cancelRunOnUIThread(runnable);
                imageReceiver.setDelegate(null);
                imageReceiver.onDetachedFromWindow();
                callback.onDataLoaded(nowPlayingCardData);
            }
        }

        public static void m1242$r8$lambda$AOf_1ZcjMCUyw5uKAO3aSljOk(final ImageReceiver imageReceiver, final Ref.ObjectRef<Runnable> ref$ObjectRef, final ImageLocation imageLocation, final NowPlayingDTO nowPlayingDTO, final AtomicBoolean atomicBoolean, final Callback callback) {
            imageReceiver.onAttachedToWindow();
            imageReceiver.setDelegate((imageReceiver2, z, z2, z3) -> create$lambda$2$0(nowPlayingDTO, imageLocation, atomicBoolean, ref$ObjectRef, imageReceiver, callback, imageReceiver2, z, z2, z3));
            Runnable t = ref$ObjectRef.element;
            AndroidUtilities.runOnUIThread(t, 15000L);
            imageReceiver.setImage(imageLocation, null, null, null, null, 0);
        }

        private static void create$lambda$2$0(final NowPlayingDTO nowPlayingDTO, final ImageLocation imageLocation, final AtomicBoolean atomicBoolean, final Ref.ObjectRef<Runnable> ref$ObjectRef, final ImageReceiver imageReceiver, final Callback callback, ImageReceiver imageReceiver2, boolean z, boolean z2, boolean z3) {
            if (!z || z2) {
                return;
            }
            final Bitmap bitmap = imageReceiver2.getBitmap();
            Utilities.themeQueue.postRunnable(() -> create$lambda$2$0$0(bitmap, nowPlayingDTO, imageLocation, atomicBoolean, ref$ObjectRef, imageReceiver, callback));
        }

        private static void create$lambda$2$0$0(final Bitmap bitmap, final NowPlayingDTO nowPlayingDTO, final ImageLocation imageLocation, final AtomicBoolean atomicBoolean, final Ref.ObjectRef<Runnable> ref$ObjectRef, final ImageReceiver imageReceiver, final Callback callback) {
            Pair<Integer, Integer> pairExtractColors = extractColors(bitmap);
            final Integer numComponent1 = pairExtractColors.getFirst();
            final Integer numComponent2 = pairExtractColors.getSecond();
            AndroidUtilities.runOnUIThread(() -> create$lambda$2$0$0$0(nowPlayingDTO, numComponent1, numComponent2, bitmap, imageLocation, atomicBoolean, ref$ObjectRef, imageReceiver, callback));
        }

        private static void create$lambda$2$0$0$0(NowPlayingDTO nowPlayingDTO, Integer num, Integer num2, Bitmap bitmap, ImageLocation imageLocation, AtomicBoolean atomicBoolean, Ref.ObjectRef<Runnable> ref$ObjectRef, ImageReceiver imageReceiver, Callback callback) {
            create$finish(atomicBoolean, ref$ObjectRef, imageReceiver, callback, new NowPlayingCardData(nowPlayingDTO, num, num2, bitmap, imageLocation, 0L, 32, null));
        }

        private static Pair<Integer, Integer> extractColors(Bitmap bitmap) {
            int iIntValue;
            float f = 0.5f;
            float f2;
            if (bitmap == null) {
                return TuplesKt.to(null, null);
            }
            Palette paletteGenerate = Palette.from(bitmap).generate();
            Palette.Swatch darkVibrantSwatch = paletteGenerate.getDarkVibrantSwatch();
            if (darkVibrantSwatch != null) {
                iIntValue = darkVibrantSwatch.getRgb();
            } else {
                Palette.Swatch mutedSwatch = paletteGenerate.getMutedSwatch();
                if (mutedSwatch != null) {
                    iIntValue = mutedSwatch.getRgb();
                } else {
                    Palette.Swatch darkMutedSwatch = paletteGenerate.getDarkMutedSwatch();
                    Integer numValueOf = darkMutedSwatch != null ? Integer.valueOf(darkMutedSwatch.getRgb()) : null;
                    if (numValueOf != null) {
                        iIntValue = numValueOf.intValue();
                    } else {
                        Palette.Swatch dominantSwatch = paletteGenerate.getDominantSwatch();
                        Integer numValueOf2 = dominantSwatch != null ? Integer.valueOf(dominantSwatch.getRgb()) : null;
                        iIntValue = numValueOf2 != null ? numValueOf2.intValue() : AndroidUtilities.getDominantColor(bitmap);
                    }
                }
            }
            int iAdjustHsl$default = iIntValue;
            double dCalculateContrast = ColorUtils.calculateContrast(-1, iAdjustHsl$default);
            if (dCalculateContrast > 15.0d) {
                iAdjustHsl$default = UIUtil.adjustHsl$default(UIUtil.INSTANCE, iAdjustHsl$default, 2.0f, 0.0f, 4, null);
            } else if (dCalculateContrast < 10.0d) {
                iAdjustHsl$default = UIUtil.adjustHsl$default(UIUtil.INSTANCE, iAdjustHsl$default, 0.5f, 0.0f, 4, null);
            }
            if (ColorUtils.calculateContrast(-1, iAdjustHsl$default) < 3.0d) {
                iAdjustHsl$default = ColorUtils.blendARGB(iAdjustHsl$default, -16777216, 0.3f);
            }
            int i = iAdjustHsl$default;
            float[] fArr = new float[3];
            ColorUtils.colorToHSL(i, fArr);
            float f3 = fArr[2];
            if (0.0f <= f3 && f3 <= 0.25f) {
                f2 = 2.0f;
            } else {
                if (0.25f > f3 || f3 > 0.5f) {
                    if (0.5f > f3 || f3 > 0.75f) {
                        f = 0.5f;
                    } else {
                        f2 = 1.0f;
                    }
                    return TuplesKt.to(Integer.valueOf(i), Integer.valueOf(UIUtil.adjustHsl$default(UIUtil.INSTANCE, i, f, 0.0f, 4, null)));
                }
                f2 = 1.5f;
            }
            f = f2;
            return TuplesKt.to(Integer.valueOf(i), Integer.valueOf(UIUtil.adjustHsl$default(UIUtil.INSTANCE, i, f, 0.0f, 4, null)));
        }
    }
}
