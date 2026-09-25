# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A Call for Papers (CFP) API for conference speakers to submit talk proposals. Java 25 / Spring Boot 4.1.1, built with Maven. Base package: `conf.live.cfp`.

Note: `live-coding.md` at the repo root is off-limits (read/edit denied by `.claude/settings.json`, and a hook blocks Bash commands referencing it). Do not attempt to read or work around this — it is intentionally excluded.

## Commands

Use the Maven wrapper, offline mode (`-o`) works since dependencies are already resolved locally:

```bash
./mvnw -o test                                          # run the full test suite
./mvnw -o test -Dtest=ProposalTest                       # run a single test class
./mvnw -o test -Dtest=ProposalTest#should_reject_blank_title  # run a single test method
./mvnw -o test -Dtest=ProposalTest,SpeakerTest           # run several test classes
./mvnw -o spring-boot:run                                # run the application locally
./mvnw -o clean package                                  # build the jar
```

Drop `-o` if dependencies need to be re-resolved from a remote repo.

## Architecture

The codebase follows **hexagonal (ports & adapters) architecture**, organized by business domain under `conf.live.cfp.<domain>` rather than by technical layer at the top level. There are two domains so far, `proposal` and `event` (a `proposal` can optionally reference an `event` it belongs to). Future features/domains should follow the same package shape:

```
<domain>/
  domain/            # entities, value objects, domain exceptions — NO Spring/framework imports
    model/
    exception/
  application/        # use cases — NO Spring/framework imports either
    port/in/           # incoming ports: use case interfaces + command DTOs
    port/out/          # outgoing ports: interfaces the domain needs from infrastructure
    service/           # use case implementations, depend only on ports + domain
  adapter/
    in/web/            # REST controllers, request/response DTOs, exception handlers
    out/persistence/   # JPA entities, Spring Data repositories, adapters implementing out-ports
  config/              # @Configuration classes wiring application services to ports as @Bean
```

Key design rule enforced in this codebase: **`domain` and `application` packages must stay framework-agnostic** (no Spring annotations, no JPA, no web types); `config` wires application services to their ports as `@Bean`s. This is a deliberate application of the Dependency Inversion Principle, and it plus the other conventions (invariant validation in constructors, injected `Clock`, explicit entity/domain mapping, per-domain exception handling, and the pattern for cross-domain calls — a consumer-owned out-port implemented by an adapter that calls the other domain's in-port, see `EventExistsPort`/`EventExistsAdapter`) are explained with their rationale in [ARCHITECTURE.md](ARCHITECTURE.md) — follow them when adding new domains.

## Testing

Development follows **strict TDD** (red → green loop): write a failing test first, confirm it fails for the expected reason (usually a compile error against a not-yet-existing class), then implement the minimum to make it pass. Preserve this workflow when adding features.

The test stack (JUnit 5, Mockito, AssertJ, MockMvc, DataJpaTest) is already on the classpath via Spring Boot's modular test starters — no extra dependencies needed. Which test style to use for which layer (domain, application service, web adapter, persistence adapter, end-to-end) is documented in [ARCHITECTURE.md](ARCHITECTURE.md#test-strategy); follow the same pattern for new domains.

### Spring Boot 4 / Jackson 3 package locations

This project pulls in Spring Boot 4.1.1's modularized test starters and Jackson 3, which move several classes to new packages compared to Spring Boot 3 / Jackson 2. Don't assume the old locations — these are the correct ones in this codebase:
- `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure`
- `@WebMvcTest`, `@AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure`
- `ObjectMapper`, `JsonNode` → `tools.jackson.databind` (Jackson 3, not `com.fasterxml.jackson.databind`); `JsonNode#asString()` replaces the old `asText()` pattern in new code, though `asText()` still exists.
- `MockitoBean` → `org.springframework.test.context.bean.override.mockito.MockitoBean` (unchanged from Spring Boot 3).

If a symbol can't be found where expected, check the actual jar under `~/.m2/repository/org/springframework/boot/` (`unzip -l <jar> | grep -i <ClassName>`) rather than guessing.
