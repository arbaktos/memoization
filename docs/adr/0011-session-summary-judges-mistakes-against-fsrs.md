---
status: accepted
---

# The session summary judges mistakes against what FSRS expected

Every session that had an answer ends in a summary - drained, "Enough for today", or Back,
which now ends the session and shows it instead of leaving at once. It says how many sides were
practised, how each was first answered (Easy, Hard or Forgot, a bar and a legend that add up to
the sides practised - a repeat after Forgot is not counted again), and judges the mistakes.
The judgement is a message, never a count: a count reads as a score.

A mistake is a side practised before whose first answer in the session was Forgot. Hard, checked
wrong or not, is a recall to FSRS and not a mistake; a repeated Forgot on the same side is not a
second one; new sides count neither way. The expected number is the sum, over those sides, of
FSRS's chance of forgetting each when it was shown (1 - retrievability). Within 15 % of it,
never narrower than one mistake either way, the summary says the learner is right on track;
more, that it was a productive session and mistakes are where the learning happens; fewer,
that recall was great. A session of only new sides has nothing to judge and shows no message.
None of it is in red.

## Why

The point is that the learner sees mistakes as part of learning. A fixed target (25 % was
asked for; no source for it was found, and the scheduler aims at about 10 % with retention 0.9)
would disagree with the scheduler: on a well-timed session the learner would nearly always be
told they made too few mistakes. FSRS's own prediction is the honest target, and it moves with
the session - after a long break more mistakes are expected, and they are.

The window has a floor of one mistake because 15 % of a small expectation can hold no whole
number: with 1.4 expected, 1.19..1.61 would leave the learner never "as expected".

## Considered

**The design's rule: Hard and Forgot are mistakes, 20-30 % is the norm.** Kept FSRS instead,
for the reasons above; the design's layout and texts were taken as they are.

**Sides by level after the session.** In the first version; dropped with the design, which
puts how the session went before where the words stand.

**A fixed band, 10-15 %.** Simple, but blind to how overdue the session was.

**Retention 0.75, so the scheduler itself aims at 25 %.** Longer intervals for every word; a
change to the schedule, not to a dialog. Not taken.
