---
status: accepted
---

# The session summary judges mistakes against what FSRS expected

Every session that had an answer ends in a summary - drained, "Enough for today", or Back,
which now ends the session and shows it instead of leaving at once. It says how many sides were
practised and how many were new, puts them by level after the session (with names for the
learner instead of Level1..Learned), and judges the mistakes.

A mistake is a side practised before whose first answer in the session was Forgot. Hard, checked
wrong or not, is a recall to FSRS and not a mistake; a repeated Forgot on the same side is not a
second one; new sides count neither way. The expected number is the sum, over those sides, of
FSRS's chance of forgetting each when it was shown (1 - retrievability). Within 15 % of it,
never narrower than one mistake either way, the summary says this is how it should be; fewer,
that the words have stuck; more, that this is fine and they will come up again soon.

## Why

The point is that the learner sees mistakes as part of learning. A fixed target (25 % was
asked for; no source for it was found, and the scheduler aims at about 10 % with retention 0.9)
would disagree with the scheduler: on a well-timed session the learner would nearly always be
told they made too few mistakes. FSRS's own prediction is the honest target, and it moves with
the session - after a long break more mistakes are expected, and they are.

The window has a floor of one mistake because 15 % of a small expectation can hold no whole
number: with 1.4 expected, 1.19..1.61 would leave the learner never "as expected".

## Considered

**A fixed band, 10-15 %.** Simple, but blind to how overdue the session was.

**Retention 0.75, so the scheduler itself aims at 25 %.** Longer intervals for every word; a
change to the schedule, not to a dialog. Not taken.
