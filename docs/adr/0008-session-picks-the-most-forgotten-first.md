---
status: accepted
---

# A session picks the most forgotten sides first, by retrievability

When more sides are due than one session takes (37, ADR 0004 and `SessionDefaults`), the
session is filled in order of FSRS **retrievability** - the chance of recalling the side today -
lowest first. Among sides that sit at the same retrievability, the one with the lower stability
goes first. Sides never practised have no retrievability and fill whatever is left, as before.

## Why

Until 2026-09-09 the order was the due date: earliest first. That measures lateness in days,
and a day is not the same thing to every memory. A side a day late on a one-day interval has
had its interval doubled and is about half forgotten; a side three days late on a month-long
interval is barely touched. The learner proposed weighing lateness against the interval - three
days late on one is 300 %, three days late on nine is 33 % - and that ratio is what FSRS's
forgetting curve turns into a probability. Sorting by the probability itself keeps one model of
memory in the app instead of a second formula beside it, and it also puts the sides with very
short stability first on the day they fall due, since a whole day is far past what they hold.

## Considered Options

**Relative overdueness** (days late divided by the interval), as Anki sorted before it had
FSRS. Rejected only because retrievability is the same idea expressed by the scheduler the app
already trusts; the two orders agree wherever they both apply.

**Weakest first** (lowest stability). Rejected: it would keep a strong memory that is a month
late waiting behind a weak one that is not late at all.

**Keep the due date.** Rejected for the reason above; on a real device it spent session slots
on sides at 90 % recall while sides at 60 % waited.

## Consequences

- `Fsrs.retrievability(side, now)` reads a side's chance of recall on a given day, in calendar
  days since its last rating, and is null for a side never rated.
- `dueSessionSides` takes a scheduler; the selection is otherwise unchanged and the session
  still shuffles what it is given, so the order only decides who gets a place.
- Sides due exactly when FSRS asked for them all sit at the desired retention (0.9), so among
  them the order is by stability; this is where "most forgotten" alone could not choose.
