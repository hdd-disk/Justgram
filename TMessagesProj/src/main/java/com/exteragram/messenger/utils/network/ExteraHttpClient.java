package com.exteragram.messenger.utils.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public final class ExteraHttpClient {
    public static final ExteraHttpClient INSTANCE = new ExteraHttpClient();
    private static OkHttpClient client;

    private ExteraHttpClient() {
    }

    public synchronized OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10L, TimeUnit.SECONDS)
                    .readTimeout(10L, TimeUnit.SECONDS)
                    .writeTimeout(10L, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }
}
