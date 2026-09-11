package com.exteragram.messenger.nowplaying;

import kotlin.jvm.internal.DefaultConstructorMarker;

public enum ServiceEmoji {
    TELEGRAM(5325674462522144646L);

    private final long documentId;
    public static final Companion Companion = new Companion(null);

    ServiceEmoji(long j) {
        this.documentId = j;
    }

    public final long getDocumentId() {
        return this.documentId;
    }

    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final ServiceEmoji fromString(String str) {
            return ServiceEmoji.TELEGRAM;
        }
    }
}
