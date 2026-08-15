---
status: accepted
---

# Scheduling is FSRS-6, with a side of a word pair as the unit that is scheduled

The five fixed levels are gone. Each **side** of a word pair - shown the word and recall the
meaning, or the reverse - carries its own FSRS state (stability, difficulty, due, last review,
reps, lapses) in a new `side_entity_table`. The **level** shown in the UI is a reading of
stability rather than a stored fact, and a pair shows the level of its weakest practised side.
Which sides are practised is one setting for the whole library: word to meaning (the default),
meaning to word, or both.

## Why

FSRS is fitted to some 700 million real reviews, predicts recall better than SM-2 for over 99 %
of users on the open benchmark, and in simulation reaches the same retention with 20-30 % fewer
reviews. Fixed steps cannot tell that a particular pair is easy or hard for this learner, and
they threw away everything a lapsed pair had earned. Research on second-language vocabulary also
says that recalling the meaning and recalling the word are different skills, which is why the
schedule belongs on the side, not on the pair.

## Considered Options

**The `FSRS-Kotlin` repository.** Not a library: three files in the repository root, no build
script and no publication, importing `app.arteh.flashcard.*`, `R.color`, `Log` and Compose from
the application it was cut out of, and using `java.time` (API 26) against our minSdk 23. Its
`calculate()` returns button labels and colours rather than a new card state. Rejected in favour
of ~150 lines of pure Kotlin transcribed from the reference implementation (py-fsrs, MIT) and
pinned by test vectors generated with an independent port (ts-fsrs 5.4.1, FSRS-6.0).

**Learning steps** (a lapsed card returning after one and ten minutes, as Anki does). Rejected:
the session already brings a lapsed side round again before it ends, and steps would turn the
queue into a clock and record several reviews per sitting.

**Per-stack direction.** Rejected as a setting nobody would want to keep re-answering; one
library-wide choice is enough, and every pair carries both sides regardless, so switching costs
nothing.

**FSRS fields on the word pair row.** No new table, but "both directions" then means two sets of
the same seven columns, the due query gets awkward, and a third kind of side could not be added.

**Showing stability directly instead of a level.** More honest, but it would change the
language, the colours in the stack list and the export columns at once; deriving a level from
stability keeps all three stable.

## Consequences

- Desired retention is a constant 0.9 with FSRS-6 default parameters; a setting can come later.
- Ratings stay three: Again, Hard, Good, mapped to FSRS grades 1-3. FSRS's Easy is implemented
  but never sent - its authors report it is over-used, and a fourth button costs attention on
  every word. The ceiling on a first interval is therefore about two days rather than eight.
- Intervals are fuzzed as in Anki, so a batch of pairs added on one day does not keep returning
  as one lump. Due-ness is judged by calendar day, so a side rated at 23:59 is available again
  the moment the next day starts.
- The 2 to 3 migration gives every pair two sides. The word side inherits the old schedule - its
  level becomes the stability in days it stood for, its due date lands where the five-level
  schedule would have put it - and the meaning side starts New. An instrumented `MigrationTest`
  covers it, and it was verified against a copy of a real device database.
- Nothing is deleted any more: removing a word pair or a stack only hides it, and the FSRS state
  survives for an undo or a future restore screen. The WorkManager job that used to delete a
  stack three seconds after the snackbar is gone, along with WorkManager itself.
- The Anki blocker recorded in ADR 0001 largely goes away: both apps now model a side as
  stability and difficulty, so a mapping in either direction is no longer lossy by construction.
