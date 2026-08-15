---
status: proposed
---

# Scheduling moves from five fixed levels to FSRS, with a card per direction

The five-level scheme (1 / 2 / 7 / 14 / 30 days, one step up on Good, reset on Again) is kept
working as an interim, but the intended scheduler is FSRS via the MIT-licensed `FSRS-Kotlin`.
Each **card** — one direction of one word pair — carries its own FSRS state in a new table
(stability, difficulty, due, last review, reps, lapses, state), and the **level** shown in the
UI and written to export files becomes a reading of stability rather than a stored fact.

## Why

FSRS is fitted to some 700 million real reviews, predicts recall better than SM-2 for over 99 %
of users on the open benchmark, and in simulation reaches the same retention with 20–30 % fewer
reviews. Fixed steps cannot adapt to a pair being easy or hard for this particular learner.
Research on second-language vocabulary also says that recalling the meaning (L2→L1) and
recalling the word (L1→L2) are different skills that need separate practice, which is why a
pair can have more than one card and why the state lives on the card, not on the pair.

## Considered Options

**Stay on fixed levels, tuned.** Zero migration, but the gain of a good schedule is precisely
that it stops being uniform across pairs — tuning the ladder cannot deliver that.

**Duolingo-style half-life regression.** The same idea (a per-item memory half-life), but it
needs a trained model per deployment; FSRS ships with usable default parameters.

**FSRS fields on the word pair row.** No new table, but "both directions" then means two sets
of the same seven columns, the due query gets awkward, and a third kind of card (listening,
say) could not be added.

**Show stability directly instead of a level.** More honest, but it changes the language, the
colours in the stack list and the export columns at once; deriving a level from stability
(< 2 d Level1, < 7 Level2, < 14 Level3, < 30 Level4, ≥ 30 Learned) keeps all three stable.

## Consequences

- Desired retention is a constant 0.9 with FSRS-6 default parameters; a setting can come later.
- Ratings stay three: Again / Hard / Good. FSRS's Easy is not exposed — its authors report it
  is over-used, and a fourth button costs attention on every card.
- The migration creates one L2→L1 card per existing pair, seeding stability from the level
  (1 / 2 / 7 / 14 / 30) and due from `lastRep`; a stack setting then adds or hides the L1→L2
  card. Old export files without FSRS columns import the same way.
- The Anki blocker recorded in ADR 0001 largely goes away: both apps then model a card as
  stability-and-difficulty, so a mapping in either direction is no longer lossy by construction.
