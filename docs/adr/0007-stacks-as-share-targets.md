---
status: accepted
---

# The stacks themselves are what a word is shared to

The four stacks a word is most likely to belong to are published as Android sharing shortcuts,
so the share sheet offers them by name in its row of direct targets - the row a chat app fills
with the people written to most. Sharing to one of them skips the picker: the word opens as a
new pair in that stack. The stacks are ranked pinned first, then by when each was last
practised, then by size; the system reorders them from then on by what it is told is used.

## Why

The share sheet already knows how to be the fast path. With the app as a single target, a word
costs two taps in the sheet and one on the picker; with the stacks in the sheet, it costs one,
and the picker is left for the times the answer is not the usual stack. A learner keeps one or
two live stacks and shares into those over and over, which is the case the direct-target row
exists for.

## Considered Options

**One target per stack, all of them.** Rejected: the row holds a handful, the system would cut
the list anyway, and a library of a dozen stacks would publish twelve shortcuts to have four
shown. Four is what is published, and the ranking decides which four.

**Ranking by how many words were added to each stack lately.** The truer measure of where a
shared word belongs, and the app does not record it: word pairs carry no created-at. Sessions
do, so "practised most recently" stands in for it until the review log can answer the real
question.

**Letting the system rank alone, publishing in library order.** Rejected as the starting state:
the system learns from use, and on the first share it knows nothing. Pinned-then-recent is the
better first guess, and reporting each use hands the ordering over to the system afterwards.

## Consequences

- `StackShortcuts` republishes the whole set whenever the library changes, so a renamed stack
  is renamed in the share sheet and a hidden one leaves it.
- A shared word that names a stack passes straight through the picker screen. If that stack has
  since been hidden, the picker stands and the learner chooses again - the share sheet caches
  targets, so this happens.
- The shortcuts are also what the launcher shows on a long press of the app icon; tapped there,
  a stack shortcut opens that stack.
- Direct targets are the platform's own from Android 10; on Android 9 and older the app is
  still a share target, only without the row of stacks. The backport
  (`androidx.sharetarget:sharetarget`) is a dependency away if those devices ever matter here.
- The system is told which stack a word went to (`reportShortcutUsed`); that, not our ranking,
  is what makes the row settle on the stacks in daily use.
