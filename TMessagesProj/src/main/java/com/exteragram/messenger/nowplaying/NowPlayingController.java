package com.exteragram.messenger.nowplaying;

import com.exteragram.messenger.api.dto.NowPlayingDTO;
import com.exteragram.messenger.nowplaying.ui.components.NowPlayingCardData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.ProfileMusicView;

public final class NowPlayingController {
    public static final NowPlayingController INSTANCE = new NowPlayingController();
    private static final Pattern ARTISTS_SPLITTER = Pattern.compile("(?i)\\s*(?:,|&|\\bfeat\\b\\.?|\\bft\\b\\.?)\\s*");

    public static final class Job {
        private volatile boolean cancelled;
        private volatile boolean active = true;

        public void cancel(Object cause) {
            cancelled = true;
            active = false;
        }

        public boolean isActive() {
            return active && !cancelled;
        }

        public void finish() {
            active = false;
        }
    }

    private NowPlayingController() {
    }

    public static boolean shouldShowCard(NowPlayingCardData nowPlayingCardData) {
        if (nowPlayingCardData == null) {
            return false;
        }
        return !(Objects.equals(nowPlayingCardData.getNowPlayingDTO().getPlatform(), "TELEGRAM") && isSeparateStylesSupported());
    }

    public static boolean isSeparateStylesSupported() {
        return false;
    }

    public static Job getCurrentPlayingTrack(long userId, TLRPC.Document savedMusic, boolean checkApi, BiConsumer<NowPlayingDTO, Long> callback) {
        Job job = new Job();
        long startTime = System.currentTimeMillis();

        Utilities.globalQueue.postRunnable(() -> {
            if (!job.isActive()) {
                return;
            }
            NowPlayingDTO resultDto = processSavedMusic(savedMusic);
            if (!job.isActive()) {
                return;
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            AndroidUtilities.runOnUIThread(() -> {
                if (job.isActive()) {
                    job.finish();
                    if (callback != null) {
                        callback.accept(resultDto, elapsedTime);
                    }
                }
            });
        });

        return job;
    }

    public static NowPlayingDTO processSavedMusic(TLRPC.Document document) {
        if (document == null) {
            return null;
        }

        CharSequence title = ProfileMusicView.getTitle(document);
        String titleStr = title != null ? title.toString() : LocaleController.getString(R.string.AudioUnknownTitle);

        CharSequence author = ProfileMusicView.getAuthor(document);
        String authorStr = author != null ? author.toString() : LocaleController.getString(R.string.AudioUnknownArtist);

        String[] split = ARTISTS_SPLITTER.split(authorStr);
        List<String> artistsList = new ArrayList<>();
        for (String item : split) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                artistsList.add(trimmed);
            }
        }

        return new NowPlayingDTO(
                titleStr,
                artistsList,
                null,
                null,
                null,
                null,
                true,
                null,
                "TELEGRAM",
                null
        );
    }
}
