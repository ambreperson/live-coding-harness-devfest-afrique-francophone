---
name: hexagonal-port-adapter-architecture
description: Reference guide for structuring code with hexagonal (ports & adapters) architecture and the Dependency Inversion Principle. Use this skill whenever creating a new domain, feature slice, use case, or adapter in this repository, when deciding which package a new class belongs in, when adding a new adapter (web, persistence, messaging, CLI) for an existing use case, or when reviewing whether domain/application code has stayed framework-agnostic. Also consult it when something feels architecturally off — a Spring/JPA import creeping into domain code, a use case calling Instant.now() or reaching into infrastructure directly, or a JPA entity leaking outside its adapter package. This repo's CLAUDE.md and ARCHITECTURE.md describe the concrete result of applying this pattern to the `proposal` domain — this skill is the reusable playbook for extending it to every new domain the same way.
---

# Hexagonal (Ports & Adapters) Architecture

The core idea: business logic (the "hexagon" — domain + application) should not know that Spring, a database, or HTTP exist. It defines *ports* (interfaces) describing what it needs from the outside world and what it offers to it; *adapters* on the outside implement or call those ports. Dependencies always point inward — adapter → application → domain — never the reverse.

This isn't architecture for its own sake. It buys two concrete things:
- **Testability without a framework.** Domain and application code can be unit-tested with plain JUnit — no Spring context, no database, no HTTP server — because nothing in that code depends on them.
- **Swappability.** Replacing a persistence technology, adding a second delivery mechanism (a CLI, a message consumer) alongside the REST API, or mocking a dependency in a test all become a matter of writing a new adapter, not touching the use case or domain model.

If a change to "how we store data" or "how a request arrives" forces you to edit domain or application code, the inversion has leaked somewhere — that's the signal to look for a stray framework import.

## Package layout

Organize by business domain first, technical layer second — a new domain gets its own top-level package with this same internal shape, rather than everything funneling into shared `controller`/`service`/`repository` packages:

```
<domain>/
  domain/
    model/            entities, value objects
    exception/        domain-specific exceptions
  application/
    port/in/          use case interfaces + their command/query DTOs
    port/out/         interfaces the use case needs from infrastructure
    service/          use case implementations — depend only on ports + domain
  adapter/
    in/<mechanism>/    e.g. web: controllers, request/response DTOs, exception mapping
    out/<mechanism>/   e.g. persistence: entities, repositories, adapters implementing out-ports
  config/
    wiring — instantiates application services as beans, injected with their ports
```

`domain` and `application` contain **zero** framework imports — no `@Service`, no `@Component`, no JPA annotations, no web types. This is the whole mechanism that makes the inversion real, not just conceptual: if you can `import org.springframework.*` in a domain or application class, something has gone wrong.

The wiring lives in `config`: a `@Configuration` class instantiates the plain application service as a `@Bean`, handing it its out-port(s) (also just interfaces — Spring supplies whichever adapter implements them) and any other collaborators (see "inject what varies," below). Only `adapter` classes carry framework stereotypes (`@RestController`, `@Component`, `@RestControllerAdvice`, `@Repository`) because they inherently depend on infrastructure — that dependency is their entire job.

For this repo's live, concrete instance of this layout (the `proposal` domain), see [ARCHITECTURE.md](../../../ARCHITECTURE.md) — it documents the exact classes and is kept current as domains are added; don't treat this skill's example structure as more authoritative than that file.

## Scaffolding a new domain, in order

When adding a new domain or feature slice, this order keeps every piece testable in isolation before it's wired together (and pairs naturally with a red-green-refactor loop — see the `tdd-red-green-refactor` skill for that discipline):

1. **Domain model** — entities/value objects that validate their own invariants (see below), plus any domain exceptions they throw. Test with plain JUnit, no mocks needed.
2. **Application ports** — the in-port (use case interface + its command/query type) and any out-ports (interfaces for what the use case needs from infrastructure, e.g. "save this," "find that").
3. **Application service** — the use case implementation, depending only on the ports and domain types from steps 1-2. Test with the out-ports mocked (Mockito or similar) — never mock the domain objects themselves, and never mock the use case under test.
4. **Adapters** — one per delivery/infrastructure mechanism (a web controller, a persistence adapter, etc.), each implementing or calling the relevant port. Test each with a slice test appropriate to that mechanism (see "Test strategy" below).
5. **Config wiring** — the `@Bean` that ties the service to its adapters. Prove it with one end-to-end test that exercises the real wiring, not mocks.

Building bottom-up like this means each layer's tests fail for the right reason (missing behavior) rather than for wiring reasons, and the use case's design gets pressure-tested by its own tests before any adapter exists to hide gaps behind.

## Conventions worth carrying into every domain

These aren't arbitrary style — each solves a specific problem that shows up once you have more than one domain or more than one adapter per port:

**Validate domain invariants in the constructor, not just at the edge.** If an entity's constructor rejects invalid state, it *cannot* exist invalid, regardless of which adapter, test, or future caller constructs it. Relying only on adapter-level validation (e.g. a web DTO's `@NotBlank`) only guards the one path that happens to go through that adapter — a second adapter (a CLI, a batch import, a future message consumer) would bypass it entirely.

**Keep adapter-level input validation anyway — it's defense in depth, not redundant.** A `@NotBlank`/`@Email` on a web request DTO exists to fail fast with a proper HTTP status *before* the use case even runs; the domain constructor is what actually guarantees the invariant everywhere else. Different jobs, both worth doing.

**Inject what varies — especially time.** A use case that calls `Instant.now()` or `LocalDate.now()` directly is untestable without either sleeping or tolerating a fuzzy time window. Take a `java.time.Clock` in the constructor instead, wire `Clock.systemUTC()` in production config, and pass `Clock.fixed(...)` in tests — the same principle applies to anything else that's nondeterministic or environment-dependent (random IDs, feature flags, external config).

**Map explicitly between a persistence entity and the domain model — never let the entity escape its adapter package.** A JPA entity (or any other storage-shaped class) carries concerns — column mappings, ORM annotations, an ID shaped for the database — that have nothing to do with the domain. An explicit `toEntity`/`toDomain` mapping keeps schema changes from silently changing domain behavior, and keeps the domain importable without pulling in a persistence framework.

**Scope exception-to-HTTP mapping per domain, not with one global handler.** A `@RestControllerAdvice` scoped to a single controller (`assignableTypes = ...`) keeps a domain's exception mapping colocated with the domain that owns it — a new domain can define its own mapping without touching, or risking a regression in, anyone else's.

## Test strategy

Match the test to what a layer actually needs, from cheapest/most-isolated to most expensive/most-realistic:

| Layer | Style | Why this level |
|---|---|---|
| Domain (entities, value objects) | Plain unit tests, no framework, no mocks | Nothing here should require infrastructure to test — if it does, something leaked in. |
| Application service (use case) | Unit tests mocking only the out-ports | Verifies orchestration logic in isolation; mocking the domain or the use case itself would test nothing real. |
| Adapter (web, persistence, etc.) | A framework "slice" test for that mechanism, with the port on the other side mocked or (for persistence) a real in-memory database | Tests the adapter's actual job — HTTP wiring, request validation, or SQL/mapping correctness — without paying for a full application context. |
| End-to-end (per feature, sparingly) | Full application context, real (in-memory) infrastructure, no mocks | The only place that would actually catch a wiring mistake (a missing `@Bean`, a misconfigured import) — everything below this mocks across the exact boundary a wiring bug would break. |

Most coverage should live in the first three rows — they're fast and pinpoint failures precisely. Keep the last row to a handful of tests per feature; it exists specifically to catch what the others structurally can't see, not to re-verify business logic already covered below it.

## Signs the inversion has leaked

Watch for these — each means a dependency is pointing the wrong way:
- A `domain` or `application` class imports anything Spring, JPA, or web-related.
- An application service instantiates an adapter directly instead of depending on a port interface.
- A persistence entity (or its annotations) is referenced from domain or application code.
- Business logic lives inside a controller or repository class instead of the use case.
- A new adapter for an existing capability requires changing the use case, rather than just implementing the existing port.

Any of these is a reason to pause and move the offending code across the boundary, not a reason to make an exception "just this once."

## Automated enforcement

These rules aren't just documentation — `src/test/java/conf/live/cfp/architecture/HexagonalArchitectureTest.java` (ArchUnit, run with `./mvnw -o test -Dtest=HexagonalArchitectureTest`, or as part of the full `./mvnw -o test`) checks the framework-agnostic boundary, the inward-only layer dependencies, JPA entity encapsulation, controller/entity/config package placement, and freedom from cycles between domains — written against the generic `conf.live.cfp.(*)..` package pattern so it applies to every domain, not just `proposal`.
