---
name: sdlc-implement
description: Implementation orchestrator that executes a task checklist (produced by the `sdlc-tasks` skill, at `sdlc/NNN-slug-tasks.md`), checking boxes off as work completes. Use this skill whenever the user wants to implement, build, execute, or work through an existing SDLC task list — phrases like "implement the tasks in sdlc/003-...-tasks.md", "let's build this feature now", "start implementing this", "work through the checklist", or "execute the task list for X." It ensures work happens on the feature's dedicated branch, delegates each task (or group of tasks) to a skill-local `implementer` subagent that does the actual TDD work, and uses git worktrees — named after what they're building — to run independent phases in parallel safely. Do NOT use this to write the spec, design, or task list themselves (that's `sdlc-specs`/`sdlc-design`/`sdlc-tasks`) — this skill only executes a task list that already exists.
---

# SDLC Implementation Orchestrator

You are the orchestrator, not the implementer. Your job is to read the task list, figure out what can safely run in parallel versus what must run in order, delegate the actual coding to `implementer` subagents (one per independent stream of work), and keep the checklist file and the feature branch in a consistent, mergeable state throughout. The actual TDD work happens in the subagents — see `agents/implementer.md`, which you'll read and use as their brief.

## Why the orchestrator, not the subagents, owns the checklist

Multiple `implementer` subagents may be working in parallel, each in its own git worktree, each with its own copy of `sdlc/<NNN>-<slug>-tasks.md` on disk. If each one checked its own boxes and those changes were merged later, two subagents editing nearby lines (or the same line, if a task turned out to need splitting) risks a merge conflict on the one file that's supposed to be the calm, readable status report. So: **subagents never edit the checklist.** They report back, in their final message, exactly which checklist line(s) they completed (verbatim) — and you, working on the actual feature branch, are the only one who ever checks a box and commits that change. This keeps the checklist file's history clean and conflict-free no matter how much parallel work happened to produce it.

## 1. Locate the task list and get on the right branch

Find `sdlc/<NNN>-<slug>-tasks.md` (ask if ambiguous or missing — don't invent tasks; if it doesn't exist yet, point the user to `sdlc-tasks`). Read it along with the design and spec it links to, for context you'll hand to subagents.

Check the current branch. The feature should already have a dedicated branch from `sdlc-specs`, named `feature/<NNN>-<slug>`. Run `git status` first (never switch branches blind): if you're not on it, switch to it (`git checkout feature/<NNN>-<slug>`); if it doesn't exist at all, create it from the current branch. If there are uncommitted local changes that don't belong to this feature, stop and ask rather than carrying them along or discarding them.

## 2. Parse the checklist into phases and dependencies

Each phase in the file (as produced by `sdlc-tasks`) looks like:

```markdown
### Phase 2: Persistence adapter
_Depends on: Phase 1 · Can run in parallel with: Phase 3_

- [ ] task...
- [x] task already done...
```

Build a simple picture from this: which phases are already fully checked (skip them), which are ready to start (all their dependencies are fully checked), and which of the ready ones are marked as parallel with each other. Work through the file phase by phase, always picking up the next batch of "ready" phases — this may be one phase alone, or several at once if they're mutually parallel and none blocks another.

## 3. Run a batch: one phase, or several in parallel

**A single ready phase (nothing to parallelize against right now):** run one `implementer` subagent directly against the current working tree (no worktree needed — there's no concurrent stream to isolate it from). Give it that phase's unchecked tasks, in order.

**Several ready, mutually-parallel phases:** give each one its own git worktree, so independent implementer subagents can't step on each other's uncommitted work or half-finished test runs. For each phase in the batch:

1. Derive a short slug from the phase's subject (e.g. "Persistence adapter" → `persistence-adapter`).
2. Create the worktree on a new branch off the current feature branch: `git worktree add .worktrees/<NNN>-<phase-slug> -b feature/<NNN>-<slug>/<phase-slug>`. (Add `.worktrees/` to `.gitignore` first if it isn't already there — these are local working directories, not something to commit.) The branch name and worktree path both name what the agent is building, so `git worktree list` and `git branch` are self-explanatory to anyone watching the parallel work happen.
3. Spawn one `implementer` subagent per worktree, **all in the same message** so they genuinely run concurrently — see `agents/implementer.md` for what to put in each one's brief (the phase's tasks, the worktree path to work in, and pointers to the spec/design for context).

Wait for all subagents in the batch to report back before proceeding — don't start merging one worktree while another in the same batch is still running, since a phase's "independent" status was based on the design's analysis of *port contracts*, not a guarantee that merging early is safe before you've seen every subagent's final state.

## 4. Fold parallel work back in

Once a batch's subagents have all reported:

1. For each worktree, in any order: `git merge feature/<NNN>-<slug>/<phase-slug>` into the feature branch. If a merge conflicts, stop and resolve it deliberately (or ask the user) rather than force-resolving in a way that silently drops one side's work — a conflict here usually means two "independent" phases touched more than expected, which is itself useful signal to surface, not paper over.
2. Remove the worktree and its branch once merged: `git worktree remove .worktrees/<NNN>-<phase-slug>` (and delete the now-merged branch if you want to keep things tidy).
3. On the checklist file, on the feature branch, check the box for every task each subagent confirmed it completed (match its verbatim report against the checklist lines — don't check a box you don't have a direct confirmation for). Commit this checklist update by itself, with a message naming the phase(s) just completed, so the checklist's history reads as a clean log of progress independent of the code commits that did the work.

Then go back to step 2: re-scan for the next batch of now-ready phases (completing a phase may have unblocked others), and repeat until every phase is checked off or you hit something that needs the user's input.

## 5. When something doesn't go cleanly

If a subagent reports a task it couldn't complete as specified — the task turned out to be ambiguous, the codebase has changed underneath it, a test it wrote can't be made to pass without a decision only the user can make — do not guess a resolution that changes scope. Leave that box unchecked, note why in your own summary to the user, and surface it rather than quietly reinterpreting the task list. The task list is a plan the user already reviewed at the design stage; deviating from it silently defeats the point of having reviewed it.

## 6. Wrap up

Once every phase is checked (or you've stopped to report a blocker), run the full test suite once more on the feature branch as a final regression check — parallel merges are exactly the kind of thing that can pass every individual phase's tests while still breaking something at the seams. Report to the user: what's checked, what isn't (and why), and the final test result. Do not push the branch or open a pull request yourself — that stays a separate, explicit action for the user once they've reviewed the result, same as any other git action with effects beyond the local repo.

## The `implementer` subagent

Read `agents/implementer.md` before spawning any implementer subagent — it's the brief every one of them gets. This isn't a project-level agent type (there's no registered `implementer` subagent type to select), so spawn it as a fresh, general-purpose agent and paste that file's content plus the task-specific details (phase, working directory, tasks, context links) into its prompt. Because each one starts with no memory of this conversation, that brief has to be genuinely self-contained — don't assume it can infer anything you haven't written down for it.
