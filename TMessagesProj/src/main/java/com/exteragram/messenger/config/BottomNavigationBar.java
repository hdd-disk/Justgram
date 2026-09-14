package com.exteragram.messenger.config;

import androidx.core.math.MathUtils;

public abstract class BottomNavigationBar {
    private static int mode;

    public static int getMode() {
        mode = MathUtils.clamp(mode, 0, 2);
        return mode;
    }

    public static void setMode(int i) {
        mode = i;
        getMode();
    }

    public static boolean hidden() {
        return getMode() == 1;
    }

    public static boolean visible() {
        return getMode() != 1;
    }

    public static boolean floating() {
        return getMode() == 2;
    }
}
