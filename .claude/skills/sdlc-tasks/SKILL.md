---
name: sdlc-tasks
description: Task-breakdown assistant that turns a reviewed technical design (produced by the `sdlc-design` skill, at `sdlc/NNN-slug-design.md`) into a checklist of very fine-grained, checkbox-tracked implementation tasks at `sdlc/NNN-slug-tasks.md`. Use this skill whenever the user wants to break a design down into tasks, get a checklist for implementing a feature, plan the exact TDD steps for a design, or turn a design/phase into actionable work items — phrases like "break this design into tasks", "give me a task list for X", "what are the concrete steps to build this", or "turn sdlc/003-...-design.md into tasks." Each task is small enough to be one TDD red/green/refactor step or one single-purpose edit, names the exact file(s)/class(es)/method(s) it touches, and the checklist mirrors the design's phase structure while flagging which tasks can be done in parallel. Do NOT use this to write the design itself (that's `sdlc-design`) or to actually implement the tasks (that's the TDD work this checklist hands off to, per `tdd-red-green-refactor`).
---

# SDLC Task Breakdown

You are turning a technical design into a checklist precise enough that picking up any single unchecked box tells you exactly what to do next — no interpretation required. The test for "precise enough" is simple: if two different people (or two different agents) read the same task, would they touch the same file, in the same way? If not, split it further or name it more concretely.

## 1. Find and read the design (and its spec)

Locate `sdlc/<NNN>-<slug>-design.md`, produced and reviewed via the `sdlc-design` skill. If the user names it, use that; if there's one obvious match, use it; if ambiguous or missing, ask rather than guessing — and if no design exists yet, say so and suggest running `sdlc-design` first. Skim the paired spec (`sdlc/<NNN>-<slug>.md`, linked from the design) too, since a scenario or business rule mentioned there sometimes clarifies what a task actually needs to verify.

## 2. Decompose each phase into TDD-sized tasks

Work through the design's phases **in the same order they appear in the design**, and for each phase, break its capability breakdown (the use cases/ports/adapters it names) into individual tasks at red/green/refactor granularity — see `tdd-red-green-refactor` for what that loop actually means; each task here should correspond to roughly one turn of that loop, not a whole feature's worth of it. See `hexagonal-port-adapter-architecture` for where each kind of class belongs, so file paths are correct rather than guessed.

For each unit of work (one method, one class, one small behavior), the natural task sequence is:
- **Setup**, if needed — e.g. "create package `conf.live.cfp.<domain>.domain.model`", "add dependency X to `pom.xml`" — only when there's a real prerequisite step, not as filler.
- **RED** — "write a failing test `should_<behavior>` in `<ClassName>Test` asserting `<what>`" — name the actual test method and class, not "write tests for X."
- **GREEN** — "implement `<ClassName>.<method>(...)` in `src/.../<ClassName>.java` to make `<ClassName>Test#should_<behavior>` pass" — name the actual file, class, and method.
- **REFACTOR** — only as its own task when there's something concrete to do (extract a method, rename, remove duplication); don't pad the list with empty "refactor if needed" boxes that carry no information.

A task like "implement the use case" is too coarse — it hides an unknown number of RED/GREEN cycles. A task like "add `SubmitProposalCommand` record with `title`, `description` fields to `conf.live.cfp.proposal.application.port.in`" is the right size — one sitting, one clear file, one clear outcome. When in doubt, split rather than merge: an extra checked box costs nothing, but a task that's actually three tasks in a trenchcoat produces false progress signals (checked, but only a third done).

**Example** — decomposing "Phase 1: `SubmitReviewUseCase` and domain model" from a hypothetical design into tasks:

```markdown
### Phase 1: Review domain model & port contracts
_Depends on: none. Blocks all other phases (defines the shared contracts)._

- [ ] Create `conf.live.cfp.review.domain.model` package
- [ ] RED: `ReviewTest#should_reject_blank_comment` — assert `Review.submit(...)` with a blank comment throws `InvalidReviewException`
- [ ] GREEN: implement `Review` constructor validation in `Review.java` to satisfy the above
- [ ] RED: `ReviewTest#should_submit_a_review_with_pending_status` — assert a valid `Review.submit(...)` has status `PENDING`
- [ ] GREEN: implement `Review.submit(...)` factory in `Review.java`
- [ ] Define `SubmitReviewCommand` record (`rating`, `comment`) in `application/port/in/SubmitReviewCommand.java`
- [ ] Define `SubmitReviewUseCase` interface in `application/port/in/SubmitReviewUseCase.java`
- [ ] Define `SaveReviewPort` interface in `application/port/out/SaveReviewPort.java`
```

## 3. Mirror the design's structure, and carry forward parallelism

Use the same phase titles, in the same order, as headers in the task file — someone flipping between the design and the task list should never have to search for where a phase went. Under each phase header, restate the design's dependency/parallelism note (e.g. `_Depends on: Phase 1. Can run in parallel with Phase 3 (both implement adapters against Phase 1's ports)._`) so the parallelism the design identified isn't lost at the task level — this is often the most valuable line in the file for whoever is assigning work.

If, while decomposing, you notice two tasks *within* a phase that are also independent of each other (e.g., two unrelated domain classes with no shared dependency), you may note that too, but don't force it — most within-phase tasks are naturally sequential (you need the class before you can test its second method), and manufacturing false parallelism is worse than not mentioning it.

## 4. Write the task file

Write `sdlc/<NNN>-<slug>-tasks.md` (same number and slug as the spec and design):

```markdown
# <Feature Name> — Tasks

Spec: [sdlc/<NNN>-<slug>.md](<NNN>-<slug>.md) · Design: [sdlc/<NNN>-<slug>-design.md](<NNN>-<slug>-design.md)

### Phase <N>: <Phase title, from the design>
_Depends on: ... · Can run in parallel with: ..._

- [ ] <task>
- [ ] <task>
...

### Phase <N+1>: ...
...
```

Every task is a single `- [ ]` line — flat within its phase, no nested sub-checklists — so that checking one box is an unambiguous, atomic signal of progress. Write each task so it's self-contained: someone reading only that line (not the surrounding conversation) should know the file, the class/method, and the expected behavior, because that's exactly the situation a future implementer (human or agent, possibly not the one who wrote the design) will be in.

## 5. Report back

Tell the user the file path, how many tasks per phase, and which phases can run in parallel — that's usually the detail someone needs immediately to decide how to divide the work.
