package com.exteragram.messenger.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public final class BadgeDTO {

    @SerializedName("documentId")
    private final long documentId;

    @SerializedName("text")
    private String text;

    public BadgeDTO(long documentId, String text) {
        this.documentId = documentId;
        this.text = text;
    }

    public long getDocumentId() {
        return this.documentId;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BadgeDTO)) return false;
        BadgeDTO badgeDTO = (BadgeDTO) o;
        return documentId == badgeDTO.documentId && Objects.equals(text, badgeDTO.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, text);
    }

    @Override
    public String toString() {
        return "BadgeDTO(documentId=" + documentId + ", text=" + text + ')';
    }
}
