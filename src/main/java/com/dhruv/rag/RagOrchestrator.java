package com.dhruv.rag;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class RagOrchestrator {
    public interface QueryRewriter {
        String rewrite(String tenantId, String rawQuery);
    }

    public interface Retriever {
        List<DocumentChunk> retrieve(String tenantId, String rewrittenQuery, int limit);
    }

    public interface AnswerGenerator {
        String generate(String rewrittenQuery, List<DocumentChunk> evidence);
    }

    public interface AuditSink {
        void record(AuditEvent event);
    }

    public record AuditEvent(
            Instant occurredAt,
            String tenantId,
            String query,
            boolean grounded,
            int citationCount
    ) {
    }

    public record Answer(
            String text,
            String query,
            List<DocumentChunk> citations,
            boolean grounded
    ) {
        public Answer {
            text = Objects.requireNonNull(text);
            query = Objects.requireNonNull(query);
            citations = List.copyOf(citations);
        }
    }

    private final QueryRewriter queryRewriter;
    private final Retriever retriever;
    private final AnswerGenerator answerGenerator;
    private final AuditSink auditSink;
    private final int maxChunks;

    public RagOrchestrator(
            QueryRewriter queryRewriter,
            Retriever retriever,
            AnswerGenerator answerGenerator,
            AuditSink auditSink,
            int maxChunks
    ) {
        this.queryRewriter = Objects.requireNonNull(queryRewriter);
        this.retriever = Objects.requireNonNull(retriever);
        this.answerGenerator = Objects.requireNonNull(answerGenerator);
        this.auditSink = Objects.requireNonNull(auditSink);
        if (maxChunks < 1) {
            throw new IllegalArgumentException("maxChunks must be positive");
        }
        this.maxChunks = maxChunks;
    }

    public Answer answer(String tenantId, String rawQuery) {
        requireText(tenantId, "tenantId");
        requireText(rawQuery, "rawQuery");

        String rewrittenQuery = requireText(
                queryRewriter.rewrite(tenantId, rawQuery),
                "rewrittenQuery"
        );
        List<DocumentChunk> retrieved = retriever.retrieve(tenantId, rewrittenQuery, maxChunks);
        List<DocumentChunk> evidence = retrieved == null
                ? List.of()
                : retrieved.stream().limit(maxChunks).toList();

        Answer answer;
        if (evidence.isEmpty()) {
            answer = new Answer(
                    "I could not find grounded evidence for that request.",
                    rewrittenQuery,
                    List.of(),
                    false
            );
        } else {
            String generated = requireText(
                    answerGenerator.generate(rewrittenQuery, evidence),
                    "generatedAnswer"
            );
            answer = new Answer(generated, rewrittenQuery, evidence, true);
        }

        auditSink.record(new AuditEvent(
                Instant.now(),
                tenantId,
                rewrittenQuery,
                answer.grounded(),
                answer.citations().size()
        ));
        return answer;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
