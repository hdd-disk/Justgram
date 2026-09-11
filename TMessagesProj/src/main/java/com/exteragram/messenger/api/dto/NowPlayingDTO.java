package com.exteragram.messenger.api.dto;

import java.util.List;
import kotlin.jvm.internal.Intrinsics;

public final class NowPlayingDTO {
    private final String albumName;
    private final List<String> artists;
    private final String coverUrl;
    private final String deviceName;
    private final Long duration;
    private final boolean isPlaying;
    private final String platform;
    private final String previewUrl;
    private final String songUrl;
    private final String trackName;

    public static /* synthetic */ NowPlayingDTO copy$default(NowPlayingDTO nowPlayingDTO, String str, List list, String str2, String str3, String str4, String str5, boolean z, String str6, String str7, Long l, int i, Object obj) {
        if ((i & 1) != 0) {
            str = nowPlayingDTO.trackName;
        }
        if ((i & 2) != 0) {
            list = nowPlayingDTO.artists;
        }
        if ((i & 4) != 0) {
            str2 = nowPlayingDTO.albumName;
        }
        if ((i & 8) != 0) {
            str3 = nowPlayingDTO.coverUrl;
        }
        if ((i & 16) != 0) {
            str4 = nowPlayingDTO.previewUrl;
        }
        if ((i & 32) != 0) {
            str5 = nowPlayingDTO.songUrl;
        }
        if ((i & 64) != 0) {
            z = nowPlayingDTO.isPlaying;
        }
        if ((i & 128) != 0) {
            str6 = nowPlayingDTO.deviceName;
        }
        if ((i & 256) != 0) {
            str7 = nowPlayingDTO.platform;
        }
        if ((i & 512) != 0) {
            l = nowPlayingDTO.duration;
        }
        return nowPlayingDTO.copy(str, list, str2, str3, str4, str5, z, str6, str7, l);
    }

    public final NowPlayingDTO copy(String str, List<String> list, String str2, String str3, String str4, String str5, boolean z, String str6, String str7, Long l) {
        return new NowPlayingDTO(str, list, str2, str3, str4, str5, z, str6, str7, l);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NowPlayingDTO)) {
            return false;
        }
        NowPlayingDTO nowPlayingDTO = (NowPlayingDTO) obj;
        return Intrinsics.areEqual(this.trackName, nowPlayingDTO.trackName) && Intrinsics.areEqual(this.artists, nowPlayingDTO.artists) && Intrinsics.areEqual(this.albumName, nowPlayingDTO.albumName) && Intrinsics.areEqual(this.coverUrl, nowPlayingDTO.coverUrl) && Intrinsics.areEqual(this.previewUrl, nowPlayingDTO.previewUrl) && Intrinsics.areEqual(this.songUrl, nowPlayingDTO.songUrl) && this.isPlaying == nowPlayingDTO.isPlaying && Intrinsics.areEqual(this.deviceName, nowPlayingDTO.deviceName) && Intrinsics.areEqual(this.platform, nowPlayingDTO.platform) && Intrinsics.areEqual(this.duration, nowPlayingDTO.duration);
    }

    public int hashCode() {
        int iHashCode = this.trackName != null ? this.trackName.hashCode() * 31 : 0;
        List<String> list = this.artists;
        int iHashCode2 = (iHashCode + (list == null ? 0 : list.hashCode())) * 31;
        String str = this.albumName;
        int iHashCode3 = (iHashCode2 + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.coverUrl;
        int iHashCode4 = (iHashCode3 + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.previewUrl;
        int iHashCode5 = (iHashCode4 + (str3 == null ? 0 : str3.hashCode())) * 31;
        String str4 = this.songUrl;
        int iHashCode6 = (((iHashCode5 + (str4 == null ? 0 : str4.hashCode())) * 31) + Boolean.hashCode(this.isPlaying)) * 31;
        String str5 = this.deviceName;
        int iHashCode7 = (iHashCode6 + (str5 == null ? 0 : str5.hashCode())) * 31;
        String str6 = this.platform;
        int iHashCode8 = (iHashCode7 + (str6 == null ? 0 : str6.hashCode())) * 31;
        Long l = this.duration;
        return iHashCode8 + (l != null ? l.hashCode() : 0);
    }

    public String toString() {
        return "NowPlayingDTO(trackName=" + this.trackName + ", artists=" + this.artists + ", albumName=" + this.albumName + ", coverUrl=" + this.coverUrl + ", previewUrl=" + this.previewUrl + ", songUrl=" + this.songUrl + ", isPlaying=" + this.isPlaying + ", deviceName=" + this.deviceName + ", platform=" + this.platform + ", duration=" + this.duration + ')';
    }

    public NowPlayingDTO(String str, List<String> list, String str2, String str3, String str4, String str5, boolean z, String str6, String str7, Long l) {
        this.trackName = str;
        this.artists = list;
        this.albumName = str2;
        this.coverUrl = str3;
        this.previewUrl = str4;
        this.songUrl = str5;
        this.isPlaying = z;
        this.deviceName = str6;
        this.platform = str7;
        this.duration = l;
    }

    public final String getTrackName() {
        return this.trackName;
    }

    public final List<String> getArtists() {
        return this.artists;
    }

    public final String getAlbumName() {
        return this.albumName;
    }

    public final String getCoverUrl() {
        return this.coverUrl;
    }

    public final String getPreviewUrl() {
        return this.previewUrl;
    }

    public final String getSongUrl() {
        return this.songUrl;
    }

    public final boolean isPlaying() {
        return this.isPlaying;
    }

    public final String getPlatform() {
        return this.platform;
    }

    public final String getDeviceName() {
        return this.deviceName;
    }

    public final Long getDuration() {
        return this.duration;
    }
}
