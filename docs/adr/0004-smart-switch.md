---
status: accepted
---

# Smart switch is the default practice sides

The library-wide practice-sides setting gains a fourth value, **Smart switch**, and it is the
default. Under it a pair is practised one side at a time: word to meaning first, and once the
word side reaches Level3 (stability of seven days, the point at which the memory holds a week)
the pair hands over to its meaning side, which enters the schedule as New while the word side
leaves it. A pair whose meaning side has been rated stays on that side from then on, even if
the word side later lapses below Level3. The three fixed choices - word to meaning, meaning to
word, both - remain for learners who want one of them; **Both sides** is what schedules the two
sides of a pair together.

## Why

The default was word to meaning, and a learner who never opened the settings practised only
recognition: a week of daily sessions on a real device had 377 reviews on word sides and not
one on a meaning side, and the learner had not noticed there was a choice. Both sides at once
doubles the load on day one and asks for production of a word before it is even recognised.
Recognition before production, with production arriving as each word settles, is what the
learner wanted and what the vocabulary research behind ADR 0003 suggests.

The hand-over, rather than an addition, is the same argument carried through: a word that is
recognised well enough to be produced does not need to be recognised on a schedule of its own,
and asking for both is the daily load Smart switch exists to avoid. Producing a word implies
recognising it, so the meaning side alone keeps the pair honest.

## Considered Options

**Both sides as the default.** Simplest, but a new stack then asks for 2N New sides at once,
and the session limit of 37 fills with reverse sides of words that are not known yet.

**Alternate directions** (both sides scheduled, one side of a pair per day, the more overdue
one). Rejected: it hides a due side behind its twin and makes the due count a lie.

**Weakest side only** (the lower-stability side of each pair). Rejected: the weaker side is
almost always the meaning side, so it degenerates into meaning to word with extra steps.

**Unlock once, lock again on lapse.** Rejected: a meaning side with its own history would
vanish from practice whenever the word side had a bad day; progress is kept, not hidden.

**Both sides from the hand-over on** (the meaning side joins, the word side stays). This was
the first cut of Smart switch, and living with it showed the cost: from the day a word settles
the pair asks for two answers a sitting instead of one, and the word side - the easier of the
two by then - takes session slots from words that need them. Replaced by the hand-over.

## Consequences

- `PracticeSides` is pair-aware: it selects from a pair's sides rather than from a set of
  `Shown` values, because Smart switch needs the word side's stability to judge the meaning side.
- Existing installs with no stored setting move to Smart switch on update. On a library where
  the word sides already have history, meaning sides of pairs at Level3 or above become New and
  due at once, within the session limit.
- The hand-over takes effect the day after the word side reached Level3, not the moment it did
  (since 2026-09-09). Before, a word settled in a sitting made its meaning side due at once, so
  a session's "N more waiting" was short by the number of pairs that settled during it - the
  stack list, counted afterwards, showed more - and a word could be asked for from the other
  side in the same sitting. On the day it settles the pair is done for the day at Level3; the
  next day it is the meaning side, New and due.
- A pair's level is still that of its weakest practised side, so a pair drops to Level1 on the
  day the meaning side takes over: it is New and it is now the only side practised.
- The word side keeps its schedule in the database, untouched. It is only unscheduled: a
  learner who moves to Word to meaning or Both sides gets its history back as it was.
- The hand-over level is a constant (`PracticeSides.HANDOVER_LEVEL`); a setting can come later.
