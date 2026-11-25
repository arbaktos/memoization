---
status: accepted
---

# Hard is checked against the answer; Forgot shows it

The card no longer turns over on a tap. **Good** rates the side and moves to the next one at
once, without showing the answer. **Forgot** rates the side at once - leaving then loses
nothing - and turns the card over to the answer, with a single **Next** in place of the three
buttons; the side's time on screen stops at the Forgot. **Hard** turns the card over to the answer and
puts a check in place of the three buttons - "Did you get it right?" with **Yes** and
**Forgot** (ru: «Получилось вспомнить?», «Да» / «Нет ;(»). Yes rates the side Hard; Forgot rates
it Again, exactly as if Again had been pressed first: the lapse is written and the side goes to
the back of the queue.

Nothing is written at the Hard tap. The rating is the answer to the check, so the review log has
one row for it, with the final rating, and its time on screen runs to the check, the answer
included - minutes measure attention (ADR 0005). Leaving the screen or "Enough for today" with
the check open writes nothing for that side; it stays due. A side already forgotten in this
session goes through the same check: yes closes it, no sends it round again.

The log does not mark an Again that came through the check. Nothing reads that yet; a column
can be added when a statistic wants it, and earlier rows stay correct as plain Again and Hard.

The screen follows the design handoff "Переворот карточки на Hard" (variant 1a dark, 2a light):
no close button - Back does that - and "Enough for today" in a row above the bar that is there
only while it is offered; a bar filled by the sides
answered so far, each once in the colour of its worst answer - a Forgot at once, so the tap is
seen to land, even though the side is still owed - with the three counts under it and no "N
left";
buttons in the order Forgot - Hard - Good with their names inside. The handoff's example line on
the back is left out: a pair has no example.

## Why

Hard is the answer a learner gives when unsure, and unsure is when a self-rating is least to be
trusted: a word half-remembered and wrong would otherwise be scheduled as recalled. Good is
meant as "sure", and checking it too would put a step in front of every answer. Forgot needs no
check, but a learner who forgot a word has to see it: the handoff left the answer out after
Forgot, and trying the screen showed that was wrong.

## Considered

**Tap to reveal, rate afterwards** (the classic flow, the handoff's 1c). Checks Good as well, at
the cost of a tap on every side. Rejected in the handoff.

**Check buttons on the back of the card** (1b). A stronger "checking" mode, but the buttons move
away from the thumb. Rejected in the handoff.

**Write Hard at once and correct it on "no".** Two log rows for one answer, a schedule in the
database that was never meant, and a tap counted towards Stamina that the learner took back.
