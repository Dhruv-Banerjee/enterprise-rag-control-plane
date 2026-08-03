# Enterprise RAG Control Plane

Architecture-first reference implementation for a grounded, auditable retrieval-augmented generation platform.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![AI](https://img.shields.io/badge/AI-RAG%20%7C%20Evaluation%20%7C%20Guardrails-8250DF?style=for-the-badge)
![Data](https://img.shields.io/badge/Data-PostgreSQL%20%7C%20pgvector-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Quality](https://img.shields.io/badge/Engineering-Tests%20%7C%20Auditability%20%7C%20Tenant%20Isolation-1F6FEB?style=for-the-badge)

## Why this project

Most RAG demos stop at “send a prompt to a model.” Enterprise systems need stronger guarantees: tenant isolation, traceable citations, predictable retrieval budgets, provider portability, and a safe response when evidence is missing.

This repository models those concerns in a small, testable Java core. Infrastructure adapters can be connected to Spring Boot, Spring AI, PostgreSQL/pgvector, OpenTelemetry, and a production model gateway without changing the orchestration contract.

## Core capabilities

- **Grounded answers:** generation only runs when retrievable evidence exists.
- **Tenant-aware retrieval:** every retrieval request carries an explicit tenant boundary.
- **Citation-first responses:** returned answers keep the evidence chunks that support them.
- **Audit events:** every request records grounding status and citation count.
- **Provider-agnostic design:** query rewriting, retrieval, generation, and audit sinks are replaceable ports.
- **Bounded context:** retrieval budgets prevent accidental context-window blowups.

## Architecture

~~~text
Client
  │
  ▼
Query policy ──► Query rewriting ──► Tenant-scoped retrieval
                                        │
                                        ▼
                              Rerank + context budget
                                        │
                                        ▼
                               Guarded answer generation
                                        │
                          Answer + citations + audit event
~~~

## Reference projects

The design is independently implemented and uses public projects as architectural references:

- [Spring AI](https://github.com/spring-projects/spring-ai) for Java-friendly AI application boundaries.
- [Microsoft GraphRAG](https://github.com/microsoft/graphrag) for structured retrieval and knowledge extraction ideas.
- [LangGraph](https://github.com/langchain-ai/langgraph) for explicit, stateful workflow thinking.

No source code from those repositories is copied into this project.

## Run the tests

Requirements: Java 21 and Maven 3.9+.

~~~bash
mvn test
~~~

Start the local vector-capable database:

~~~bash
docker compose up -d
~~~

## Production roadmap

- Add Spring Boot REST endpoints and OpenAPI contracts.
- Add document ingestion with checksum-based deduplication.
- Add PostgreSQL/pgvector retrieval and hybrid keyword search.
- Add offline evaluation sets for faithfulness, relevance, and citation coverage.
- Add model gateway timeouts, retries, budget enforcement, and prompt-injection filters.
- Add OpenTelemetry traces and a deployment profile for Kubernetes.

## Portfolio signal

This project demonstrates backend architecture, Java 21, AI system design, retrieval engineering, data boundaries, testing, and production-minded safety—not just a chatbot UI.
