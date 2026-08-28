---
status: accepted
---

# A word handed over from another app picks its stack on arrival

Memoization registers as a target for plain text twice over: as a share target
(`ACTION_SEND`), and in the text-selection menu itself (`ACTION_PROCESS_TEXT`), where it stands
next to Copy and Share. Either way a word selected in another app opens Memoization on a stack
picker for that word; choosing a stack opens the new-pair screen with the word already in the
word field and the cursor in the explanation field. Saving the pair closes the app, and the
learner is back where the word came from. The picker is the only new screen; nothing about
adding a pair from inside the app changes.

## Why

Words worth learning are met while reading, not while sitting in the app: a word in an article,
a subtitle, a message. Copying it, opening Memoization, finding the stack and pasting is four
steps and the word is usually lost before the third. Sharing is the gesture Android already
has for "take this text somewhere", and it is two taps from the selection; the selection menu
is one tap, and a word met mid-sentence is worth exactly that much attention.

## Considered Options

**Straight into the last stack used, no picker.** One tap fewer, but the learner keeps stacks
per language: a Serbian word landing in the Spanish stack is a silent mistake to be found and
undone later. The picker costs one tap and is a list already familiar from the library.

**A dialogue over whatever app is in front** (a floating window with the word and a stack list).
Rejected for now: it is a second, smaller add-pair screen to build and keep, and the full
screen already has the translate button and the keyboard language hints.

**The share sheet alone**, without the selection menu. Rejected: the share sheet is two taps
and a list of every app on the phone, and the word is met mid-sentence. Both entries read the
same intent extra, so the second one costs a filter and a branch.

**A share target that saves the word with no explanation at all** ("file it now, fill it in
later"). Rejected: a pair with an empty side is due at once and unanswerable, and the learner
would meet it in the next session with nothing to recall.

## Consequences

- `MainActivity` gains two intent filters and reads the handed-over text on creation; either
  entry opens a second instance of it inside the other app's task, which is what makes "save
  and you are back in the browser" work.
- Memoization appears by name in the text-selection menu of every app on the device. That is
  the point of `ACTION_PROCESS_TEXT`, and it is also its cost: the menu is a shared space, and
  the item is there whether or not a word is worth learning. The label is the app name; the
  selection is never sent back, so nothing the learner reads is changed by us.
- The shared text is tidied before it becomes a word: whitespace collapsed, quotation marks
  around the selection dropped, a long selection cut to 200 characters (`SharedWord`).
- The picker is the start destination of the graph for a share, and it is popped as the pair
  opens, so a back press from the pair hands the learner back to the app they came from.
- A share into an empty library offers to create a stack first; the word goes into the stack
  it creates.
- Translation is not run automatically for a shared word: it costs a network call the learner
  did not ask for. The translate button is where it always was.
