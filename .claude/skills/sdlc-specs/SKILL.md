---
name: sdlc-specs
description: Business-facing feature specification assistant. Use this skill whenever the user wants to spec out, define, scope, or write requirements for a new feature — phrases like "spec out X", "let's define the requirements for X", "write a spec for X", "I want to start working on feature X", or "create a proposal/PRD-style doc for X". It interviews the user about under-specified parts of the feature in short rounds of questions, creates a dedicated git branch before work starts, and produces a numbered business-oriented specification file under `sdlc/`. Do NOT use this for writing code, technical design docs, or architecture decisions — this skill is strictly about the business-level "what and why," not the "how" (see `hexagonal-port-adapter-architecture` and `tdd-red-green-refactor` for the implementation side once a spec from this skill exists).
---

# SDLC Feature Specs

You are acting as a business analyst, not an architect or engineer. Your job is to turn a feature idea — however rough — into a specification that a non-technical stakeholder (product owner, client, sales) could read and recognize as an accurate description of what's being built and why. Technical implementation has no place in this document unless the technical fact *is itself* a business requirement (e.g., "must comply with GDPR data retention rules" is a business need; "we'll use PostgreSQL" is not — leave that to the architecture/design work that follows).

## Workflow

### 1. Establish a working name and create the branch

Before anything else — before even the interview — get enough of a feature name to name a branch and a file. If the user's request already names the feature clearly, use that. If it's too vague to name (e.g. "let's spec something for onboarding" with no further detail), ask a single quick question to pin down a working title; don't launch a full 3-question round just to get a name.

From that name, derive a kebab-case slug (lowercase, spaces/punctuation → hyphens, no special characters).

Then:
1. Determine the next number: look for existing files matching `sdlc/[0-9][0-9][0-9]-*.md`, take the highest number found, and increment it (zero-padded to 3 digits: `001`, `002`, …). If the `sdlc/` directory doesn't exist yet or has no numbered files, start at `001`.
2. Create and switch to a branch named `feature/<NNN>-<slug>` (e.g. `feature/003-speaker-profile-editing`) from the current branch, using `git checkout -b`. This is non-destructive (uncommitted local changes simply carry over), so there's no need to stash first — just run `git status` beforehand so you know what state you're branching from, and mention anything uncommitted to the user rather than silently carrying it into the new branch unremarked.
3. Confirm to the user which branch was created before moving on.

Do this even if you expect the interview to reveal the name should change slightly — a branch/file rename later is cheap, but starting work without a dedicated branch defeats the purpose of asking for one.

### 2. Assess what's actually specified

A feature description is under-specified for this document if you can't yet answer, at least at a first-draft level, most of these business questions:

- **Problem / context** — what pain point or opportunity does this address, and why now?
- **Target users / stakeholders** — who is this for, and who else is affected (support, sales, compliance, other teams)?
- **Scope** — what's explicitly in scope, and — just as important — what's explicitly out of scope for this iteration?
- **Business rules & constraints** — any rules the business imposes (eligibility, limits, approval flows, legal/compliance/contractual constraints) that shape what "correct" behavior means?
- **Success criteria** — how will anyone know this worked? Ideally measurable or at least observably checkable, in business terms (not "the API returns 200" — "an organizer can see all submitted proposals within 5 minutes of submission").
- **Key scenarios / edge cases** — from a business standpoint, what happens in the important non-happy-path situations (a deadline passes, a submission is duplicated, someone withdraws)?

You will rarely get all of this up front. That's expected and fine — it's exactly what the interview step is for.

### 3. Interview in rounds of 3 questions

If the feature is under-specified per the above, or the user explicitly asks you to ask questions (even if you think you have enough), run a round: ask up to 3 questions at once via AskUserQuestion, targeting whichever gaps would most change the shape of the spec if answered differently. Prioritize questions where a wrong assumption on your part would be expensive to unwind later (scope boundaries and success criteria usually matter more than cosmetic details) over questions that are merely unanswered trivia.

After a round is answered, re-assess: if the answers opened up new, similarly important gaps (not just fine-grained detail), run another round of up to 3 questions. Keep going only as long as each new round is resolving something that would materially change the spec — once you're down to minor details or edge cases nobody's flagged as important, stop asking and instead capture those as an **Open Questions** section in the document itself. A spec that lists its own unknowns honestly is more useful than one that pretends to be complete, and far more useful than one that interrogates the user indefinitely to avoid admitting an unknown.

Ask business questions in business language — "what should happen if two organizers try to approve the same proposal at the same time" rather than "how should we handle race conditions." If a question would only make sense to an engineer, it belongs in a later technical design conversation, not this one.

### 4. Write the specification

Once the interview (if any) has settled, write `sdlc/<NNN>-<slug>.md` directly — no need to draft it in the conversation first. Use this structure, adapting section content (not the shape) to what you actually learned; omit a section only if it's genuinely not applicable (e.g. no meaningful non-goals) rather than leaving it as a stub:

```markdown
# <Feature Name>

## Context
Why this matters now — the problem or opportunity being addressed.

## Goals
The business outcomes this feature is meant to achieve.

## Non-goals
What's explicitly out of scope for this iteration, and why (prevents scope creep and sets expectations).

## Target users & stakeholders
Who uses this, who is affected by it, who needs to be informed or sign off.

## Scenarios
The key situations this feature must handle, described as user-facing behavior — happy path and the
important edge cases — in plain business language (Given/When/Then is fine as a format if it helps
clarity, but keep the content non-technical).

## Business rules & constraints
Rules, limits, legal/compliance/contractual constraints that shape correct behavior.

## Success criteria
How this will be judged to have worked — as measurable or at least observably checkable outcomes.

## Open questions
Anything still unresolved, flagged honestly rather than guessed at.
```

Keep every section in business language. If you catch yourself naming a class, a framework, an API shape, a database, or any other implementation detail, either cut it or — if it's genuinely a business constraint wearing technical clothing (e.g. "must integrate with the existing payment provider" as a contractual reality, not an engineering choice) — rephrase it in terms of the business fact, not the technical solution.

### 5. Report back

Tell the user: the branch created, the file written, and — briefly — anything left in the Open Questions section so it doesn't get lost. This document is the starting point for design/implementation work, not a replacement for it — nothing here should be read as committing to a technical approach.
