package com.exteragram.messenger.api.model;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public enum NowPlayingServiceType {
    NONE(LocaleController.getString(R.string.None)),
    TELEGRAM("Telegram");

    private final String displayName;

    NowPlayingServiceType(String str) {
        this.displayName = str;
    }

    public final String getDisplayName() {
        return this.displayName;
    }
}
