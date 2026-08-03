package com.dhruv.rag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagOrchestratorTest {
    @Test
    void returnsGroundedAnswerWithCitationsAndAuditEvent() {
        DocumentChunk chunk = new DocumentChunk(
                "chunk-1",
                "policy-7",
                "Employees receive twenty days of annual leave.",
                0.94,
                java.util.Map.of("source", "leave-policy.md")
        );
        AtomicReference<RagOrchestrator.AuditEvent> audit = new AtomicReference<>();
        RagOrchestrator orchestrator = new RagOrchestrator(
                (tenant, query) -> query + " site:" + tenant,
                (tenant, query, limit) -> List.of(chunk),
                (query, evidence) -> "The policy provides twenty days of annual leave.",
                audit::set,
                4
        );

        RagOrchestrator.Answer answer = orchestrator.answer("acme", "How much leave?");

        assertTrue(answer.grounded());
        assertEquals(1, answer.citations().size());
        assertEquals("chunk-1", answer.citations().getFirst().id());
        assertEquals("acme", audit.get().tenantId());
        assertEquals(1, audit.get().citationCount());
    }

    @Test
    void refusesToInventAnAnswerWhenRetrievalReturnsNoEvidence() {
        AtomicReference<Boolean> generatorCalled = new AtomicReference<>(false);
        RagOrchestrator orchestrator = new RagOrchestrator(
                (tenant, query) -> query,
                (tenant, query, limit) -> List.of(),
                (query, evidence) -> {
                    generatorCalled.set(true);
                    return "This must not be generated.";
                },
                event -> {
                },
                4
        );

        RagOrchestrator.Answer answer = orchestrator.answer("acme", "Unknown question");

        assertFalse(answer.grounded());
        assertTrue(answer.text().contains("could not find grounded evidence"));
        assertFalse(generatorCalled.get());
    }
}
