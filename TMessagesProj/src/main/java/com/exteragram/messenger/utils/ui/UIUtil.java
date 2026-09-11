package com.exteragram.messenger.utils.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.ColorUtils;
import kotlin.ranges.RangesKt;
import org.telegram.messenger.AndroidUtilities;

public final class UIUtil {
    public static final UIUtil INSTANCE = new UIUtil();
    private static final float[] nowPlayingPattern = {-5.5f, 20.0f, 20.0f, 0.35f, -5.5f, -20.0f, 20.0f, 0.35f, -36.0f, -42.0f, 22.0f, 0.375f, -36.0f, 0.0f, 25.0f, 0.425f, -36.0f, 42.0f, 22.0f, 0.375f, -70.0f, 22.0f, 23.0f, 0.35f, -70.0f, -22.0f, 23.0f, 0.35f, -99.0f, 46.0f, 21.0f, 0.275f, -99.0f, 0.0f, 22.0f, 0.325f, -99.0f, -46.0f, 21.0f, 0.275f, -128.0f, -23.0f, 20.0f, 0.225f, -128.0f, 23.0f, 20.0f, 0.225f};

    private UIUtil() {
    }

    public static int adjustHsl$default(UIUtil uIUtil, int i, float f, float f2, int i2, Object obj) {
        if ((i2 & 4) != 0) {
            f2 = -1.0f;
        }
        return uIUtil.adjustHsl(i, f, f2);
    }

    public final int adjustHsl(int i, float f, float f2) {
        float[] fArr = new float[3];
        ColorUtils.colorToHSL(i, fArr);
        if (f2 > 0.0f) {
            fArr[1] = RangesKt.coerceAtMost(fArr[1] * f2, 1.0f);
        }
        fArr[2] = RangesKt.coerceAtMost(fArr[2] * f, 1.0f);
        return ColorUtils.HSLToColor(fArr);
    }

    public final void drawNowPlayingPattern(Canvas canvas, Drawable drawable, float f, float f2, float f3) {
        if (f3 <= 0.0f) {
            return;
        }
        int i = 0;
        while (true) {
            float[] fArr = nowPlayingPattern;
            if (i >= fArr.length) {
                return;
            }
            float f4 = fArr[i];
            float f5 = fArr[i + 1];
            float f6 = fArr[i + 2];
            float f7 = fArr[i + 3];
            float f8 = f2 / 2.0f;
            drawable.setBounds((int) ((AndroidUtilities.dpf2(f4) + f) - (AndroidUtilities.dpf2(f6) / 2.0f)), (int) ((AndroidUtilities.dpf2(f5) + f8) - (AndroidUtilities.dpf2(f6) / 2.0f)), (int) (AndroidUtilities.dpf2(f4) + f + (AndroidUtilities.dpf2(f6) / 2.0f)), (int) (f8 + AndroidUtilities.dpf2(f5) + (AndroidUtilities.dpf2(f6) / 2.0f)));
            drawable.setAlpha((int) (255.0f * f3 * f7));
            drawable.draw(canvas);
            i += 4;
        }
    }
}
