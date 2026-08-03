package com.dhruv.rag;

import java.util.Map;
import java.util.Objects;

public record DocumentChunk(
        String id,
        String documentId,
        String text,
        double relevanceScore,
        Map<String, String> metadata
) {
    public DocumentChunk {
        id = requireText(id, "id");
        documentId = requireText(documentId, "documentId");
        text = requireText(text, "text");
        if (Double.isNaN(relevanceScore) || relevanceScore < 0 || relevanceScore > 1) {
            throw new IllegalArgumentException("relevanceScore must be between 0 and 1");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
