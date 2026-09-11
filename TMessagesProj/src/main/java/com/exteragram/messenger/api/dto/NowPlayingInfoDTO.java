package com.exteragram.messenger.api.dto;

import com.exteragram.messenger.api.model.NowPlayingServiceType;
import kotlin.jvm.internal.Intrinsics;

public final class NowPlayingInfoDTO {
    private final NowPlayingServiceType serviceType;
    private String username;

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NowPlayingInfoDTO)) {
            return false;
        }
        NowPlayingInfoDTO nowPlayingInfoDTO = (NowPlayingInfoDTO) obj;
        return this.serviceType == nowPlayingInfoDTO.serviceType && Intrinsics.areEqual(this.username, nowPlayingInfoDTO.username);
    }

    public int hashCode() {
        int iHashCode = this.serviceType != null ? this.serviceType.hashCode() * 31 : 0;
        String str = this.username;
        return iHashCode + (str == null ? 0 : str.hashCode());
    }

    public String toString() {
        return "NowPlayingInfoDTO(serviceType=" + this.serviceType + ", username=" + this.username + ')';
    }

    public NowPlayingInfoDTO(NowPlayingServiceType nowPlayingServiceType, String str) {
        this.serviceType = nowPlayingServiceType;
        this.username = str;
    }

    public final NowPlayingServiceType getServiceType() {
        return this.serviceType;
    }

    public final String getUsername() {
        return this.username;
    }
}
