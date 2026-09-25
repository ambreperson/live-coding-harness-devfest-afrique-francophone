---
name: tdd-red-green-refactor
description: Reference guide for the red-green-refactor TDD loop and TDD best practices. Use this skill whenever writing or reviewing code test-first in this repository — starting a new domain, use case, entity, or endpoint, adding a feature via TDD, or checking whether a TDD cycle was actually followed correctly (not just "tests exist"). Also consult it when a test is failing and it's unclear whether the failure is for the right reason, when deciding what kind of test fits a given layer (domain, application service, web adapter, persistence adapter, end-to-end), or when a change feels like it's skipping refactor or batching untested code. This repo's CLAUDE.md mandates strict TDD — use this skill to execute that mandate correctly rather than just going through the motions.
---

# Red-Green-Refactor

TDD is not "write tests." It's a tight loop where the test drives what code gets written, one small behavior at a time. Skipping or rushing any of the three steps below is what turns "TDD" into "write code, then write tests to match it" — which loses most of the value (the design pressure, the safety net for refactoring, and the guarantee that every line of production code is actually exercised by a test that once failed).

## The cycle

**RED — write a failing test, and confirm it fails for the right reason.**
Write the smallest test that describes one new piece of behavior, before writing the code that satisfies it. Run it. It must fail — and the failure must be *because the behavior doesn't exist yet*, not because of a typo, a wrong import, or a test bug. See "Verifying RED is real" below; this step is the one people skip fastest under time pressure, and it's the one that catches tests that would never have failed in the first place.

**GREEN — write the minimum code to pass, then stop.**
Make the test pass with the least code that does it honestly. Resist adding the next feature, an extra parameter, or "while I'm here" handling — that belongs to its own RED step later. If you're tempted to write more than the current test demands, that's a sign there's a missing test for the thing you're about to add; write that test instead.

**REFACTOR — clean up with the safety net in place, then confirm it's still green.**
Now that the behavior is locked in by a passing test, improve the code: remove duplication, rename for clarity, extract a method, simplify. Don't skip this step because "the code already works" — GREEN code is allowed to be ugly precisely because REFACTOR is where it gets fixed, under the protection of a test that will catch a regression immediately. Re-run the test (and the surrounding suite) after refactoring; if it goes red, you broke something the last step didn't cover.

Repeat. Each cycle should be small enough that RED→GREEN takes a couple of minutes, not an hour — if it's taking longer, the test is too big; break it down further.

## Verifying RED is real

A RED that passes for the wrong reason is worse than no test, because it creates false confidence. Before moving to GREEN, check:

- **Read the actual failure message**, don't just glance at red/green. A `NoSuchMethodError` or compile error is a legitimate RED for a class/method that doesn't exist yet. An `AssertionError` should name the *expected* value your test is actually checking, not something incidental.
- **Would this test fail if the feature were simply missing, or could it pass by accident?** E.g. asserting `result != null` when the method under test already returns a non-null default is a RED that never really tested anything.
- **Is the assertion checking behavior, or checking that a mock was configured?** If you're mocking the very thing you're testing, RED can be an artifact of mock setup rather than a real gap. Mock the *dependencies* of the unit under test, never the unit itself.
- **Run only the new test in isolation** the first time, so a green suite elsewhere can't mask this one silently passing (or worse, not running at all — e.g. a typo in the test method name that Maven simply doesn't pick up).

If any of these feel shaky, the fix is almost always to make the assertion more specific, not to trust the red and move on.

## Best practices per layer

This repo's hexagonal architecture gives each layer a natural test shape — matching the test to the layer keeps tests fast, focused, and resistant to unrelated changes. See ARCHITECTURE.md's test-strategy section for how this maps onto this codebase's actual test classes; the reasoning behind each choice is below.

- **Domain (entities, value objects)** — plain JUnit + AssertJ, no framework, no mocks. Domain objects should be constructible and testable with zero setup; if a domain test needs Spring or Mockito, that's usually a sign framework concerns have leaked into the domain. Test invariants (what makes a valid vs. invalid instance) and behavior, not getters.
- **Application services (use cases)** — JUnit + Mockito, mocking the *out-ports* (secondary ports) only — never the use case under test, never the domain objects it manipulates. Inject a fixed `Clock` (or any other real-world dependency) rather than letting the test depend on wall-clock time; a test that's flaky because of timing is a test that isn't really testing your logic. Assert on what the service returns and what it called on its ports (via `ArgumentCaptor` or `verify`), not on internal fields.
- **Web adapters (controllers)** — a slice test (`@WebMvcTest` + `MockMvc` in this repo) with the use case mocked out. This tests HTTP concerns in isolation: routing, status codes, request validation, response shape — it should not spin up a database or the full application context, and it should not need real business logic to pass.
- **Persistence adapters** — a slice test (`@DataJpaTest` here) against a real (in-memory) database, importing just the adapter under test. This is the one place where "real infrastructure" is worth the cost, because mapping bugs (wrong column, wrong cascade, wrong query) are exactly the kind of thing mocks can't catch.
- **End-to-end** — a small number of full-stack tests (`@SpringBootTest` here) that prove the wiring is correct: that the `@Configuration` class actually wires the use case to its port, that a real HTTP request reaches a real (in-memory) database and back. Keep these few and high-value; they're slow and their failures are harder to localize than a focused unit test's, so they shouldn't be where most of your coverage lives.

The shape should follow from what you're actually protecting against at that layer — reach for the cheapest test that would still catch the bug you're worried about.

## Anti-patterns to avoid

- **Skipping refactor.** "It works, ship it" turns TDD into write-once code with tests bolted on. If nothing needs cleaning up after GREEN, fine — but check every time, don't skip the check.
- **Batching multiple tests before implementing any of them.** Writing five failing tests and then implementing all five at once loses the tight feedback loop — you can't tell which test drove which piece of code, and it's harder to keep changes minimal. Go one RED→GREEN→REFACTOR cycle at a time.
- **Testing implementation details instead of behavior.** A test that breaks when you rename a private method or reorder statements — without any observable behavior changing — is coupled to *how*, not *what*. This makes refactoring (the step TDD is supposed to make safe) painful instead. Assert on inputs/outputs and observable interactions (calls to collaborators through ports), not on internals.
- **A RED that passes for the wrong reason.** Covered above — always read the actual failure, don't just trust the red.
- **Writing the implementation first and tests after.** This produces tests that describe what the code already does, not what it should do — bugs the code already has are invisible to tests written to match it. If you catch yourself doing this mid-task, it's fine to backfill correctly: delete or ignore the implementation, write the test against the old behavior first to confirm RED, then restore the implementation for GREEN.
- **A test suite that only ever grows, never gets refactored.** Tests are code too. Duplication in test setup, unclear names, or an assertion that no longer matches its test name are worth cleaning up during REFACTOR just like production code.

## Checklists

**Before writing a test (RED):**
- [ ] Does this test describe exactly one new behavior?
- [ ] Have I run it and seen it fail?
- [ ] Do I understand *why* it failed (compile error vs. assertion, and does that match what I expected)?

**Before writing implementation (GREEN):**
- [ ] Am I writing only enough to make the current failing test pass?
- [ ] Is anything beyond that the concern of a not-yet-written test?

**Before moving to the next test (REFACTOR):**
- [ ] Is there duplication, an unclear name, or awkward structure introduced by the last GREEN step?
- [ ] After cleaning up, does the full relevant test suite still pass?
- [ ] Would a reader unfamiliar with this change understand the resulting code, not just the diff?
