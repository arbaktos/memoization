---
status: accepted
---

# Merging duplicate pairs: the weakest schedule wins, answers move to the kept pair

A stack can end up holding the same word twice. When the stack has any, a strip above the list
("Duplicates: N to review") opens a review: first one dialog for every **Copy** (same word and
meaning up to case and spacing), then one **Merge** proposal at a time for pairs sharing a word
or a meaning up to marks and look-alike letters (**Spelling variant**). The check is recomputed
from the stack's words on every change; there is no background job.

A merge keeps the **oldest** pair, with the words the learner chose: the matching side in one
spelling, the other side as the comma-joined values, editable. For each direction, the kept
side takes the **weakest** schedule among the merged sides - a side never practised before any
other, then the lowest stability. The other pairs are hidden, not deleted, and their rows in the
review log are re-pointed at the kept sides. Removing copies is the same operation with the
oldest pair's words. Everything a merge writes happens in one transaction.

Cancel on a merge proposal is stored as a **Dismissed match** - the two pair ids, in
`dismissed_match_table` (schema version 6) - and that pair of pairs is never proposed again.
Dismissing the dialog by Back or tapping outside only ends the review. Cancel on the copies
dialog is not remembered: a copy is pure redundancy.

## Why

The learner's view: adding a word again means it was not remembered, so neither copy's progress
can be trusted beyond the weaker one. Keeping the stronger schedule would let a word that was
just looked up again wait a month. Taking the weaker one per side, rather than per pair, keeps
the two directions independent, as they are everywhere else (ADR 0003).

The answers move because statistics, the streak and time-to-Learned are read from the review
log (ADR 0005); leaving them on hidden sides would make past sessions look shorter than they
were.

## Considered Options

**Keep the side with more practice.** Rejected by the learner for the reason above.

**Let the learner choose whose progress to keep.** Rejected: one more decision per merge, and
the answer is almost always "the weaker".

**Remember cancelled matches by word.** Rejected: it would also silence a real duplicate added
later. Ids make a new pair a new question, offered against each existing pair on its own.

**Undo after a merge.** Rejected by the learner. A hidden pair can still come back through the
planned restore, without the kept pair's original progress.

## Consequences

- `Duplicates.find` and `Duplicates.plan` are pure and unit-tested; `MemoDao.applyMerges` writes
  a plan in one transaction.
- A merge loses the kept pair's own stronger schedule for good, and a restored hidden pair
  comes back without the answers that moved away from it.
- Russian й is not folded to и: they are different letters, not a mark.
