package com.exteragram.messenger.feed;

import org.telegram.messenger.UserConfig;

public class FeedController {
    private static final FeedController[] Instance = new FeedController[UserConfig.MAX_ACCOUNT_COUNT];

    public static FeedController getInstance(int num) {
        FeedController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (FeedController.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    localInstance = new FeedController(num);
                    Instance[num] = localInstance;
                }
            }
        }
        return localInstance;
    }

    private final int currentAccount;

    public FeedController(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    public int getUnreadCount() {
        return 0;
    }
}
