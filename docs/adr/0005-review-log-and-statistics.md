---
status: accepted
---

# A review log is the source of the statistics; the log ships before the screen

Every answer the learner gives is recorded in `review_log_table` (session, side, time, how long
the card was on screen, rating, whether the side had come back after Again this session, and the
side's stability, difficulty and due date after the answer), and every sitting in
`session_table` (stack, start, finish, sides offered and waiting). The statistics screen, the
weekly card and the streak are computed from these rows and the sides; nothing derived is
stored. The log is migration 3 to 4, purely additive, and shipped on its own ahead of any UI,
because history cannot be retrofitted.

## Why

The learner asked for statistics whose point is motivation: how quickly words are learned, the
easiest and hardest, and how much effort went in - taps, minutes, streak, mistake rate. The side
rows hold only the present (`reps`, `lapses`, `lastReview`, current stability), so "what is true
now" was available and "what happened" was not. A log is the smallest thing that makes time
visible, and every day it is not there is a day lost.

## What was decided with the learner (2026-08-22)

- **Purpose:** motivation first - speed of learning, easiest and hardest words, effort (taps,
  minutes, streak), mistake rate, progress.
- **Scope:** library-wide first; a stack filter later (the log carries `sideId`, so the filter is
  a join, not a redesign).
- **Start clean:** no backfill; lifetime taps seed from the sides' `reps`, nothing else pretends
  to have a date.
- **Streak** counts a day only if a session was *finished* (queue drained). A day with nothing
  scheduled is neutral - neither kept nor broken. New sides never create an obligation, so adding
  words late in the day cannot cost the streak. Due is judged by local calendar day, and
  scheduled sides become due at midnight only.
- **Minutes** = per-card time (shown to tap), capped at summing time (60 s to start), not session
  wall-clock: honest about attention, survives abandoned sessions and process death.
- **Learning speed** = growth curve of Learned sides over time, plus median days-to-Learned as
  the headline once five words have got there; both read off `stabilityAfter`.
- **Easiest / hardest** = pairs, top five each: hardest by lapses then difficulty, easiest by
  stability among pairs with no lapses; a marker shows which direction is the hard one.
- **Windows:** Today, 7 days, 30 days, All - rolling, default 7 days; streak and the level
  distribution are "now".
- **Every rating is a tap**, including the answer to a side brought back after Forgot; that row is
  flagged `requeued` so the mistake rate can count a Forgot-then-Good as one mistake.
- **Entry points:** a permanent icon in the Library app bar, and a **weekly card** at the top of
  the Library on Monday summarising last week (days practised of obligation days, minutes, taps,
  words learned), shown once a full week of log exists and the week had an obligation or a
  session; its period is a setting - weekly (default), every two weeks, monthly, never.
- **Order:** log first (this ADR), then the screen, then the card and its setting, then the
  stack filter. Visual design goes to the redesign brief.

## Considered Options

**Derive history from the sides.** Each side knows only its last review; the timeline of a
month of practice collapses to one date per side. Rejected: that is how the question arose.

**Backfill from `reps` and `lastReview`.** Would produce a sparse, misleading past. Rejected;
lifetime totals may seed from `reps`, timelines start at the migration.

**Session wall-clock for minutes.** Counts the phone lying on the table. Rejected for per-card
time with a cap.

**Any rating keeps the streak.** More lenient; the learner chose finished sessions, and a
neutral day for "nothing was due" keeps that choice fair.

**Storing computed statistics.** Rejected: definitions will change (the cap, the thresholds,
"hardest"); computing from the raw log means a changed definition re-renders history
consistently.

## Consequences

- `MemorizationSession.Outcome` reports every answer (`Answer`: side, rating, requeued, side
  after); `MemoizationViewModel` writes the session row before the first card and a log row per
  tap, and marks the session finished when the queue drains. An empty sitting is recorded and
  finished at once.
- `shownMs` is stored raw; the cap is a constant applied when summing.
- Hidden pairs keep their log rows.
- `MigrationTest` validates 3 to 4 against the exported schema and the whole chain from 1.
- The statistics definitions will live in pure Kotlin over log rows (`domain/statistics`), so
  the screen and the card are two views of the same functions.
