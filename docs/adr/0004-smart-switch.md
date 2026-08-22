---
status: accepted
---

# Smart switch is the default practice sides

The library-wide practice-sides setting gains a fourth value, **Smart switch**, and it is the
default. Under it every pair is practised word to meaning first; its meaning side joins the
schedule as New once the word side reaches Level3 (stability of seven days, the point at which
the memory holds a week). A meaning side that has been rated stays practised from then on, even
if the word side later lapses below Level3. The three fixed choices - word to meaning, meaning
to word, both - remain for learners who want one of them.

## Why

The default was word to meaning, and a learner who never opened the settings practised only
recognition: a week of daily sessions on a real device had 377 reviews on word sides and not
one on a meaning side, and the learner had not noticed there was a choice. Both sides at once
doubles the load on day one and asks for production of a word before it is even recognised.
Recognition before production, with production arriving as each word settles, is what the
learner wanted and what the vocabulary research behind ADR 0003 suggests.

## Considered Options

**Both sides as the default.** Simplest, but a new stack then asks for 2N New sides at once,
and the session limit of 37 fills with reverse sides of words that are not known yet.

**Alternate directions** (both sides scheduled, one side of a pair per day, the more overdue
one). Rejected: it hides a due side behind its twin and makes the due count a lie.

**Weakest side only** (the lower-stability side of each pair). Rejected: the weaker side is
almost always the meaning side, so it degenerates into meaning to word with extra steps.

**Unlock once, lock again on lapse.** Rejected: a meaning side with its own history would
vanish from practice whenever the word side had a bad day; progress is kept, not hidden.

## Consequences

- `PracticeSides` is pair-aware: it selects from a pair's sides rather than from a set of
  `Shown` values, because Smart switch needs the word side's stability to judge the meaning side.
- Existing installs with no stored setting move to Smart switch on update. On a library where
  the word sides already have history, meaning sides of pairs at Level3 or above become New and
  due at once, within the session limit.
- A pair's level is still that of its weakest practised side, so a pair drops to Level1 on the
  day its meaning side joins, as it would under "both sides".
- The unlock level is a constant (`PracticeSides.UNLOCK_LEVEL`); a setting can come later.
