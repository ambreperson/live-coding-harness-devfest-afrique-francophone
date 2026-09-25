# CFP API

A Call for Papers (CFP) API letting conference speakers submit talk proposals.

Built with Java 25 and Spring Boot 4.1.1 (Maven), following a hexagonal (ports & adapters)
architecture and developed with strict TDD. See [ARCHITECTURE.md](ARCHITECTURE.md) for the
design rationale.

## Prerequisites

- Java 25 (the project targets `java.version=25` in `pom.xml`)
- No local Maven install required: the repo ships the Maven Wrapper (`./mvnw`)

## Build, run, test

Dependencies are already resolved locally, so the wrapper can run in offline mode (`-o`).
Drop `-o` if you need to re-resolve dependencies from a remote repository.

```bash
./mvnw -o test                                                   # run the full test suite
./mvnw -o test -Dtest=ProposalTest                                # run a single test class
./mvnw -o test -Dtest=ProposalTest#should_reject_blank_title      # run a single test method
./mvnw -o test -Dtest=ProposalTest,SpeakerTest                    # run several test classes
./mvnw -o spring-boot:run                                         # run the application locally
./mvnw -o clean package                                           # build the executable jar
```

The application exposes these endpoints so far:

```
POST /api/proposals
Content-Type: application/json

{
  "title": "Hexagonal architecture in practice",
  "description": "A talk about ports and adapters",
  "speakerName": "Ada Lovelace",
  "speakerEmail": "ada@example.com",
  "eventId": "..."
}
```

`eventId` is optional; if present it must reference an existing event (`POST /api/events`
below), otherwise the request is rejected with `400`.

```
POST /api/events
Content-Type: application/json

{
  "name": "DevFest Afrique Francophone"
}
```

```
GET /api/events
```

It persists to an in-memory H2 database by default (see `src/main/resources/application.properties`).

## Contributing

Development follows **strict TDD** (red -> green): write a failing test first, confirm it
fails for the expected reason, then implement the minimum to make it pass.

When adding a new domain or extending `proposal`, follow the conventions already in place —
package layout, framework-agnostic domain/application code, per-domain wiring and exception
handling, and the layered test strategy. These are documented with their rationale in
[ARCHITECTURE.md](ARCHITECTURE.md); [CLAUDE.md](CLAUDE.md) has the terse operational
reference (commands, gotchas) for Claude Code sessions.
