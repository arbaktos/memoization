---
status: accepted
---

# Study anyway takes every practised side ahead of time

The stack list's redesign (handoff "Список колод") gives every stack a Study button. When
nothing is due it does not disappear: it turns grey and says "Study anyway". Pressed, it starts
an ordinary session over every practised side of the stack, due or not, in the same order as a
due session - the most forgotten first (ADR 0008) - with no size, ending the same three ways.

Answers are rated as usual. FSRS already handles an early answer: on the same day it moves
stability only a little (short-term stability), on a later day a recall at high retrievability
earns little growth, and a Forgot is a lapse like any other. So studying ahead cannot push a
word further out than it has earned, and nothing in the scheduler changes.

## Why

A learner who opens the app wanting to study and finds nothing due should still be able to.
Hiding the button (as before) reads as "this stack is broken"; a disabled one as "go away".

## Considered

**A disabled button.** No new mode, but no way to study either.

**Only the sides nearest their due date.** A cap is a session size, which sessions do not have;
the order already puts the most forgotten first and "Enough for today" ends it.
