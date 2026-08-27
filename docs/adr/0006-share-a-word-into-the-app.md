---
status: accepted
---

# A word is shared into the app and picks its stack on arrival

Memoization registers as a share target for plain text. A word selected in another app and
shared here opens the app on a stack picker for that word; choosing a stack opens the new-pair
screen with the word already in the word field and the cursor in the explanation field. Saving
the pair closes the app, and the learner is back where the word came from. The picker is the
only new screen; nothing about adding a pair from inside the app changes.

## Why

Words worth learning are met while reading, not while sitting in the app: a word in an article,
a subtitle, a message. Copying it, opening Memoization, finding the stack and pasting is four
steps and the word is usually lost before the third. Sharing is the gesture Android already
has for "take this text somewhere", and it is two taps from the selection.

## Considered Options

**Straight into the last stack used, no picker.** One tap fewer, but the learner keeps stacks
per language: a Serbian word landing in the Spanish stack is a silent mistake to be found and
undone later. The picker costs one tap and is a list already familiar from the library.

**A dialogue over whatever app is in front** (a floating window with the word and a stack list).
Rejected for now: it is a second, smaller add-pair screen to build and keep, and the full
screen already has the translate button and the keyboard language hints.

**ACTION_PROCESS_TEXT**, which would put Memoization straight in the text-selection toolbar
instead of behind the share sheet. It is a small addition on top of this one and can come
later; it also puts an item in every text selection on the device, which is a change worth
choosing deliberately.

**A share target that saves the word with no explanation at all** ("file it now, fill it in
later"). Rejected: a pair with an empty side is due at once and unanswerable, and the learner
would meet it in the next session with nothing to recall.

## Consequences

- `MainActivity` gains a second intent filter and reads the shared text on creation; the share
  opens a second instance of it inside the sharing app's task, which is what makes "save and
  you are back in the browser" work.
- The shared text is tidied before it becomes a word: whitespace collapsed, quotation marks
  around the selection dropped, a long selection cut to 200 characters (`SharedWord`).
- The picker is the start destination of the graph for a share, and it is popped as the pair
  opens, so a back press from the pair hands the learner back to the app they came from.
- A share into an empty library offers to create a stack first; the word goes into the stack
  it creates.
- Translation is not run automatically for a shared word: it costs a network call the learner
  did not ask for. The translate button is where it always was.
