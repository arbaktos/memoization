---
status: accepted
---

# Four ratings from three buttons: Hard is checked against the answer, Forgot shows it

The app uses all four FSRS grades now, from three buttons: **Forgot**, **Hard**, **Easy**. The
card no longer turns over on a tap. **Easy** rates the side Easy and moves to the next one at
once, without showing the answer. **Forgot** rates the side at once - leaving then loses
nothing - and turns the card over to the answer, with a single **Next** in place of the three
buttons; the side's time on screen stops at the Forgot. **Hard** turns the card over to the answer and
puts a check in place of the three buttons - "Did you get it right?" with **Yes** and
**Forgot** (ru: «Получилось вспомнить?», «Да» / «Нет ;(»). Yes rates the side Good - hard to get
to, but got. Forgot rates it Hard - something came back, not the answer: a small grow, no lapse,
and the side is done for the session.

Nothing is written at the Hard tap. The rating is the answer to the check, so the review log has
one row for it, with the final rating, and its time on screen runs to the check, the answer
included - minutes measure attention (ADR 0005). Leaving the screen or "Enough for today" with
the check open writes nothing for that side; it stays due. A side already forgotten in this
session goes through the same check: yes closes it, no sends it round again.

The log does not mark an Again that came through the check. Nothing reads that yet; a column
can be added when a statistic wants it, and earlier rows stay correct as plain Again and Hard.

The screen follows the design handoff "Переворот карточки на Hard" (variant 1a dark, 2a light):
no close button - Back does that - and "Enough for today" in a row above the bar that is there
only while it is offered; a bar with a place per side and
one more per Forgot, for the repeat, every answer filling one in the colour of its button - so
a Forgot is seen to land at once and the bar is full only when the queue is through - with the
three counts (taps) under it and no "N left";
buttons in the order Forgot - Hard - Easy with their names inside, coloured by button: both
outcomes of the check count as Hard on the bar. The handoff's example line on
the back is left out: a pair has no example.

## Why

Hard is the answer a learner gives when unsure, and unsure is when a self-rating is least to be
trusted: the check splits it into recalled with effort (Good) and recalled wrong (Hard). Easy is
meant as "sure", and checking it too would put a step in front of every answer. The button says
Easy because that is what it sends: it was once labelled Easy while sending Good, then Good, and
now label and grade agree again.

A first Easy puts a new side at about 8.3 days, past Level3, which under Smart switch would hand
the pair over to its meaning side the next day on one answer. The hand-over now also needs two
answers on the word side (`PracticeSides.HANDOVER_MIN_ANSWERS`). Forgot needs no
check, but a learner who forgot a word has to see it: the handoff left the answer out after
Forgot, and trying the screen showed that was wrong.

## Considered

**Tap to reveal, rate afterwards** (the classic flow, the handoff's 1c). Checks Good as well, at
the cost of a tap on every side. Rejected in the handoff.

**Check buttons on the back of the card** (1b). A stronger "checking" mode, but the buttons move
away from the thumb. Rejected in the handoff.

**"No" on the check as a Forgot** (the first version). Treats a wrong recall as a lapse and
sends the side round again. Replaced: the learner wanted "hard, and then not right" kept apart
from "forgot".

**Write Hard at once and correct it on "no".** Two log rows for one answer, a schedule in the
database that was never meant, and a tap counted towards Stamina that the learner took back.
