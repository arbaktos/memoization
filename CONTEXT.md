# Memoization

A spaced-repetition vocabulary trainer. The learner groups word pairs into stacks and
repeats them on a schedule that widens as a word is recalled correctly.

## Language

**Stack**:
A named collection of word pairs the learner studies together, usually one language.
_Avoid_: folder, deck, list

**Word pair**:
One thing to recall and its answer, plus the schedule state that decides when it is next due.
_Avoid_: card, word, translation

**Level**:
How well a word pair is known, from Level1 to Learned; it sets the gap between repetitions.
_Avoid_: score, difficulty, status

**Due**:
A word pair whose gap since its last repetition has exceeded its level's frequency.
_Avoid_: to learn, toShow, unrepeated

**Library**:
Every stack the learner currently has on this device.
_Avoid_: database, collection

**Export file**:
A tab-separated file, one row per word pair, holding one or more stacks with their word
pairs and progress, written for the learner to keep or to hand to someone else.
_Avoid_: backup, dump, deck file, csv

**Export scope**:
How much of the library goes into an export file: a single stack, or all of them.

**Stack id**:
A stable identifier a stack keeps across devices and installs, so the same stack can be
recognised in an export file no matter what it has been renamed to.
_Avoid_: uuid, guid, key

**Fork**:
Importing a stack that already exists as a separate second copy, under a new stack id, so
the original and the imported version can both be kept.
_Avoid_: duplicate, copy, branch

**Import review**:
The step where the learner decides, per incoming stack, whether to replace, fork or skip it.
_Avoid_: merge screen, conflict resolution

**Keep progress**:
The choice, made once per import, to honour the levels and repetition dates in the file
rather than starting every word pair at Level1 and due today.
_Avoid_: include scheduling, preserve state

## Relationships

- A **Library** holds zero or more **Stacks**
- A **Stack** holds zero or more **Word pairs**
- A **Word pair** belongs to exactly one **Stack**
- A **Word pair** has exactly one **Level**, which determines when it becomes **Due**
- An **Export file** holds one or more **Stacks**, chosen by **Export scope**
- A **Stack** has exactly one **Stack id**, unique within a **Library**
- Importing a **Stack** whose **Stack id** is already present offers replace, **Fork** or skip;
  a **Stack id** not yet present is added and keeps the id from the **Export file**

## Example dialogue

> **Dev:** "If I export one **Stack** and you import it, do you get my **Levels**?"
> **Learner:** "The file carries them, but they're mine, not yours. On import you decide
> whether to **Keep progress** or start the **Word pairs** fresh at Level1."
>
> **Dev:** "And if you already have that **Stack**?"
> **Learner:** "Then **Import review** asks me. Replace it if it's my own backup coming home,
> **Fork** it if I want to keep both, skip it if I've changed my mind. It knows it's the same
> one because of the **Stack id**, not the name — I rename things."

## Flagged ambiguities

- "folder" is used throughout the code (`FoldersScreen`, `Default_folder_ID`) for what the
  UI calls a stack — resolved: the concept is **Stack**; the folder naming is legacy.
- "export" was used for both the whole **Library** and a single **Stack** — resolved: those
  are the same artefact at different **Export scope**, not two formats.
- "csv" was used for the **Export file** — resolved: the file is tab-separated, because
  vocabulary is full of commas and Excel picks its delimiter from the system locale. Import
  still accepts comma and semicolon. See ADR 0001.
- "add" means two things during **Import review** — resolved: adding an unseen **Stack** keeps
  the **Stack id** from the file, **Fork** mints a new one. See ADR 0002.

## Open questions

- Anki interoperability (`.apkg`) is deferred, not rejected. The blocker is that Anki
  schedules with a continuous interval and ease per card, while a **Word pair** has one of
  five fixed **Levels** — the mapping is lossy coming back. A plain two-column Anki text
  export already imports, since only the first two columns are required.
