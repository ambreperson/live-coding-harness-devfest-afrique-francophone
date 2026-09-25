# Architecture

This document explains how the codebase is structured and, more importantly, *why*. For
build/run/test commands see [README.md](README.md); for terse operational notes aimed at
Claude Code sessions see [CLAUDE.md](CLAUDE.md).

## Hexagonal (ports & adapters), organized by domain

The code is organized by business domain under `conf.live.cfp.<domain>` rather than by
technical layer at the top level. There are two domains so far, `proposal`
(`conf.live.cfp.proposal`) and `event` (`conf.live.cfp.event`), each shaped the same way:

```
proposal/
  domain/
    model/            Proposal, ProposalId, ProposalStatus, Speaker
    exception/        InvalidProposalException, UnknownEventException
  application/
    port/in/          CreateProposalUseCase (interface) + CreateProposalCommand
    port/out/         SaveProposalPort, EventExistsPort (interfaces)
    service/          CreateProposalService (implements CreateProposalUseCase)
  adapter/
    in/web/           ProposalController, CreateProposalRequest/ProposalResponse, ProposalExceptionHandler
    out/persistence/  ProposalJpaEntity, SpringDataProposalRepository, ProposalPersistenceAdapter
    out/event/        EventExistsAdapter (implements EventExistsPort against the event domain)
  config/
    ProposalConfiguration

event/
  domain/
    model/            Event, EventId
    exception/        InvalidEventException
  application/
    port/in/          CreateEventUseCase + CreateEventCommand, ListEventsUseCase, FindEventUseCase
    port/out/         SaveEventPort, ListEventsPort, FindEventPort
    service/          CreateEventService, ListEventsService, FindEventService
  adapter/
    in/web/           EventController, CreateEventRequest/EventResponse, EventExceptionHandler
    out/persistence/  EventJpaEntity, SpringDataEventRepository, EventPersistenceAdapter
  config/
    EventConfiguration
```

The intent: a future domain (e.g. talk review, scheduling) gets its own top-level package
with the same internal shape, rather than domain-specific and future code being merged
into shared `controller`/`service`/`repository` packages.

A `proposal` can optionally reference an `event` it belongs to (`Proposal.eventId`,
nullable): see "Cross-domain communication" below for how that reference is validated
without `proposal` and `event` depending on each other's internals.

## Dependency Inversion: domain and application stay framework-agnostic

`domain` and `application` contain no Spring, JPA, or web imports at all — `Proposal`,
`Speaker`, `CreateProposalUseCase`, `SaveProposalPort`, `CreateProposalService` are plain
Java. `CreateProposalService` isn't even annotated with `@Service`; it's a constructor-injected
plain class.

The wiring lives in `config.ProposalConfiguration`, a `@Configuration` class that
instantiates `CreateProposalService` as a `@Bean`, handed the `SaveProposalPort` (an
interface) and a `Clock` bean:

```java
@Bean
public CreateProposalUseCase createProposalUseCase(SaveProposalPort saveProposalPort, Clock clock) {
    return new CreateProposalService(saveProposalPort, clock);
}
```

Only `adapter` classes carry Spring stereotypes (`@RestController`, `@Component`,
`@RestControllerAdvice`) because they inherently depend on infrastructure (HTTP, JPA).

Why bother: the use case (`application`) defines the port it needs
(`SaveProposalPort`); the persistence adapter (`ProposalPersistenceAdapter`) *implements*
that port. Dependencies point inward, from adapter to application to domain, never the
reverse. This keeps the domain/application testable with plain JUnit (no Spring context
needed) and swappable — a different persistence technology only requires a new adapter, not
a change to the use case or domain model.

## Cross-domain communication: consumer-owned out-port, adapter calls the other domain's in-port

`proposal` optionally references an `event` (`Proposal.eventId`). When a `CreateProposalCommand`
carries an `eventId`, `CreateProposalService` must confirm that event actually exists before
accepting the proposal, and reject it with `UnknownEventException` (mapped to `400` by
`ProposalExceptionHandler`, same as `InvalidProposalException`) otherwise. That means `proposal`
needs something from `event` — the first time two domains in this codebase need to talk to each
other.

The rule that resolves this without breaking domain isolation: **the *consuming* domain defines
the out-port it needs, and its own adapter — not the other domain — implements it by calling the
other domain's in-port.** Concretely:

- `proposal.application.port.out.EventExistsPort` (`boolean existsById(String eventId)`) is
  defined *inside `proposal`*, next to `SaveProposalPort`. `CreateProposalService` depends only
  on this interface, exactly like it depends on `SaveProposalPort` — it has no idea `event`
  exists.
- `proposal.adapter.out.event.EventExistsAdapter` implements `EventExistsPort` and is the only
  class in the codebase that imports both domains: it takes `event.application.port.in.FindEventUseCase`
  as a constructor dependency and implements `existsById` as `findEventUseCase.findById(eventId).isPresent()`.
- `event` imports and knows nothing about `proposal`. The dependency arrow points from
  `proposal`'s adapter layer to `event`'s in-port — never the reverse, and never
  `event`-domain-to-`proposal`-domain or adapter-to-adapter.

Why this shape rather than the alternatives: calling `event`'s REST endpoint or repository
directly from `proposal` would leak infrastructure/transport concerns across a domain boundary;
letting `event` define and export an `EventQueryPort` for others to depend on would make `event`
aware of, and responsible for, its consumers' needs. Owning the port in the consumer keeps each
domain in charge of the contract *it* requires, and the in-port (`FindEventUseCase`) is exactly
the same seam `event`'s own controller uses — no bespoke integration API is added just to satisfy
`proposal`. As more domains are added, this is the pattern to reach for whenever one domain needs
a fact that another domain owns: a new out-port on the consumer's side, backed by an adapter that
calls the provider's existing in-port.

## Conventions and their rationale

**Domain entities validate their own invariants in the constructor.** `Proposal`'s private
constructor rejects a blank title/description or a null speaker by throwing
`InvalidProposalException`; `Speaker` is a record with a compact constructor that rejects a
blank name or malformed email. This means the domain can never be instantiated in an invalid
state regardless of which adapter or test constructs it, rather than relying solely on
adapter-level validation which only guards the one path that goes through the HTTP layer.

**Adapter-level DTOs still carry `jakarta.validation` annotations.** `CreateProposalRequest`
uses `@NotBlank`/`@Email` for a fast 400 on malformed HTTP input, in addition to the domain
invariant checks. This is defense in depth: HTTP-level validation exists to fail fast with
a proper HTTP status before invoking the use case; domain validation is what actually
guarantees the invariant, including for any future adapter (CLI, message consumer, etc.)
that bypasses the web layer.

**Use cases receive time via an injected `Clock`.** `CreateProposalService` takes a
`java.time.Clock` in its constructor and calls `clock.instant()` rather than
`Instant.now()`. `ProposalConfiguration` wires `Clock.systemUTC()` in production;
`CreateProposalServiceTest` wires a `Clock.fixed(...)`. This makes the submission timestamp
deterministic and assertable in tests without sleeping or tolerating time windows.

**Persistence adapters map explicitly between JPA entity and domain model.**
`ProposalPersistenceAdapter` has private `toEntity`/`toDomain` methods converting between
`Proposal` (domain) and `ProposalJpaEntity` (JPA, package-private to `adapter.out.persistence`).
The JPA entity is never exposed outside the adapter package. This keeps JPA annotations and
persistence concerns (column mappings, ID as a raw `String`, etc.) out of the domain model,
and means a change to the persistence schema can't silently change domain behavior.

**Domain exceptions are mapped to HTTP status per-domain.** `ProposalExceptionHandler` is a
`@RestControllerAdvice(assignableTypes = ProposalController.class)` — scoped to a single
controller — that maps `InvalidProposalException` to `400 Bad Request`. Scoping the advice
to one controller (rather than one global `@RestControllerAdvice` for the whole application)
keeps exception-to-status mapping colocated with the domain that owns the exception, so a
new domain can define its own mapping without touching or risking regressions in existing
ones. `event`'s `EventExceptionHandler` follows the same pattern for `InvalidEventException`;
`UnknownEventException` — raised by `proposal` when it validates an `eventId` — is mapped by
`ProposalExceptionHandler`, not `EventExceptionHandler`, because it's `proposal`'s exception
to own even though it's about a missing `event`.

## Test strategy

Each architectural layer has a matching test style, chosen to test each layer at the
right level of isolation:

| Layer | Example | Style |
|---|---|---|
| Domain (`domain/model/*Test`) | `ProposalTest`, `SpeakerTest` | Plain JUnit + AssertJ, no Spring context — verifies invariants and behavior directly. |
| Application service (`application/service/*Test`) | `CreateProposalServiceTest` | JUnit + Mockito (`@ExtendWith(MockitoExtension.class)`), mocks the out-port (`SaveProposalPort`), uses a fixed `Clock`. |
| Web adapter (`adapter/in/web/*Test`) | `ProposalControllerTest` | `@WebMvcTest(ProposalController.class)` + `MockMvc`, mocks the in-port (`CreateProposalUseCase`) with `@MockitoBean`. |
| Persistence adapter (`adapter/out/persistence/*Test`) | `ProposalPersistenceAdapterTest` | `@DataJpaTest` with `@Import(ProposalPersistenceAdapter.class)` to bring the adapter into the slice context alongside the Spring Data repository, backed by H2. |
| End-to-end (domain root) | `ProposalCreationIntegrationTest` | `@SpringBootTest` + `@AutoConfigureMockMvc`, exercising the full wired stack (HTTP -> use case -> persistence) against H2 — the only test that actually confirms the `config` wiring is correct. |

The narrower slice tests (domain, service, web, persistence) exist so most failures are
caught fast and precisely, without booting a full Spring context; the single end-to-end test
per feature exists specifically to catch wiring mistakes (e.g. a missing `@Bean`, a
misconfigured `@Import`) that the slice tests can't see because they mock across the
boundary being wired.

`EventProposalIntegrationTest` (test root, `conf.live.cfp`) is the same style of end-to-end
test but spans both domains: it drives event creation/listing and proposal submission through
HTTP, including submitting a proposal with a valid `eventId`, one with an unknown `eventId`
(asserting the `400`), and one with no `eventId` at all. It's the test that actually exercises
the `EventExistsAdapter` -> `FindEventUseCase` wiring described above, which no single-domain
slice test can see.
