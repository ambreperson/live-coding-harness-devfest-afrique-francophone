---
name: sdlc-design
description: Technical design assistant that turns a reviewed business specification (produced by the `sdlc-specs` skill, at `sdlc/NNN-slug.md`) into a phased implementation battle plan at `sdlc/NNN-slug-design.md`. Use this skill whenever the user wants to design, plan, break down, or scope the implementation of a feature that already has (or should have) a spec — phrases like "let's design the implementation for X", "break this feature down into phases", "plan how we build X", "create a technical design for sdlc/003-...", or "what's the implementation plan for this spec". This skill maps each business need to the use cases/ports/adapters needed to fulfil it, in hexagonal-architecture terms, and structures the work as phases that are as independent as possible so they can be implemented in parallel. Do NOT use this for writing the business spec itself (that's `sdlc-specs`) or for writing actual code (that's the implementation work this plan hands off to, guided by `hexagonal-port-adapter-architecture` and `tdd-red-green-refactor`).
---

# SDLC Feature Design

You are turning a business specification into an implementation battle plan: not the code itself, but the ordered, delegable set of technical moves that get from "spec approved" to "feature built." The plan's main job is to find the seams in the work — the places where hexagonal architecture's decoupling means two pieces genuinely don't need to wait on each other — so as much of the implementation as possible can proceed in parallel instead of as one long serial chain.

## 1. Find and read the spec

This skill starts from a spec, not from scratch. Locate the matching file: `sdlc/<NNN>-<slug>.md`, produced by the `sdlc-specs` skill and already reviewed by the user.

- If the user names the spec or feature clearly, find that file.
- If there's exactly one plausible spec and it's obviously the one meant, use it.
- If it's ambiguous (several specs, or none exist yet), ask rather than guessing — designing against the wrong spec, or inventing business requirements to fill the gap, wastes the whole point of having a separate specs step. If no spec exists yet, say so and suggest running `sdlc-specs` first instead of improvising one.

Read the whole spec before designing anything — the goals, non-goals, scenarios, and business rules all shape what technical capabilities are actually needed. A design that only skims the headline goal tends to miss the edge-case scenarios that turn into awkward late additions.

## 2. Map business needs to technical shape

For each goal, scenario, and business rule in the spec, work out what it actually requires in the codebase, in the vocabulary of `hexagonal-port-adapter-architecture` (read that skill if you haven't internalized it yet — this design is meaningless without it):

- **Domain**: what entities/value objects need to exist or change? Is this a new domain (new top-level package) or does it extend an existing one? Look at the actual current package structure (see `ARCHITECTURE.md` and the codebase itself) before assuming — extending an existing domain wrongly, or splitting one that should stay together, both cost real rework later.
- **Application ports**: what use cases (in-ports) does this feature need, and what does each one need from infrastructure (out-ports)? Name them as if they were about to be created (`SubmitXUseCase`, `NotifyYPort`, etc.) — vague phases like "build the backend" don't parallelize because nobody can tell where one piece ends and another begins.
- **Adapters**: for each out-port, what adapter(s) implement it (persistence, an external API client, a notification channel)? For each in-port, what triggers it (a REST endpoint, a scheduled job, an event)?

This mapping is the heart of the design: it's what turns "the business wants X" into "these are the concrete technical pieces that add up to X."

## 3. Find the parallel seams

This is the payoff of hexagonal architecture: once a port (an interface) is agreed, the code on either side of it can be built independently — whoever implements the use case doesn't need the real persistence adapter to exist yet (a test double stands in), and whoever builds the persistence adapter doesn't need the use case's internals, only the port signature it must satisfy. Structure the plan to exploit this:

- **Phase 0 is the one genuinely sequential, blocking phase**: agree on the domain model and the port interfaces (signatures only, not implementations) for the capabilities identified in step 2. Nothing else can safely proceed in parallel until these contracts exist, because they're what every other phase is written against.
- **Everything hanging off a settled port can usually run in parallel**: the application service implementing a use case, each adapter implementing an out-port, and each adapter exposing an in-port are typically independent of each other once Phase 0's contracts are fixed — they only interact through the interface, not through each other's internals. Call these out explicitly as parallelizable, and say why (which shared contract makes it safe).
- **Config/wiring and end-to-end verification is the other genuinely sequential phase**, at the end: it's the one place that needs every piece to exist simultaneously, since wiring the pieces together and proving the full flow works is exactly what can't be split up.
- Not everything parallelizes — say so when it doesn't. If two adapters must agree on a shared schema, or one use case's output feeds directly into another's input, that's a real dependency, not a missed opportunity; forcing artificial independence would just create integration bugs. The goal is genuine, safe parallelism, not the appearance of it.

## 4. Write the design document

Write `sdlc/<NNN>-<slug>-design.md` (same number and slug as the spec, suffixed `-design` — the two files describe the same feature at different altitudes and should be easy to find together):

```markdown
# <Feature Name> — Design

Spec: [sdlc/<NNN>-<slug>.md](<NNN>-<slug>.md)

## Technical approach
One or two paragraphs: which domain(s) this touches (new or existing, and why), and the
overall shape of the solution — not implementation detail, but enough for a reader to
picture where this lands in the codebase.

## Capability breakdown
A table or list mapping each business need (from the spec) to the technical capability that
fulfils it: the use case(s)/port(s) involved and which domain they live in.

## Implementation phases
Numbered phases in dependency order. For each phase:
- **Goal** — what this phase delivers.
- **Depends on** — which earlier phase(s) must land first, or "none" / "Phase 0 only."
- **Can run in parallel with** — which other phases have no dependency on this one, and why
  that's safe (name the shared contract/port that makes it safe).
- **Verification** — how this phase is proven done in isolation (referencing the test style
  from `hexagonal-port-adapter-architecture`'s test strategy and the TDD discipline from
  `tdd-red-green-refactor`) before it's integrated with the rest.

## Risks & open technical questions
Anything uncertain enough to flag before work starts — a design choice that needs a second
opinion, an assumption about an existing system that should be verified, a phase whose
independence is less certain than the others.
```

Keep phases small enough to actually be a unit of delegable work (a person or an agent could pick one up and know when it's done) and named concretely (real use case/port/adapter names from step 2), not vague milestones like "backend work" or "frontend work."

## 5. Report back

Tell the user the file path, a one-line summary of the phase breakdown (how many phases, which ones parallelize), and anything flagged as a risk or open question. This document is a plan, not a commitment carved in stone — say so if you noticed the spec itself has a gap that only became visible while mapping it to technical reality, rather than silently designing around it.
