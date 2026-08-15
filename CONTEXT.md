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
How well a word pair is known, from Level1 (1 day) through Level2 (2), Level3 (7), Level4 (14)
to Learned (30); it sets the gap, in calendar days, between repetitions.
_Avoid_: score, difficulty, status

**Rating**:
The learner's answer to a card — Again, Hard or Good; Again resets the pair to Level1, Hard
keeps its level, Good moves it one level up.
_Avoid_: easy/wrong (button labels, not the ratings), icon, click

**New**:
A word pair that has never been rated; it is due at once, whatever its level.
_Avoid_: fresh, unseen, just added

**Due**:
A word pair that is New, or whose last repetition was at least its level's frequency in whole
local calendar days ago.
_Avoid_: to learn, toShow, unrepeated

**Session**:
One sitting with a stack: its due word pairs, shuffled once on entry and shown one at a time;
a pair rated Again returns to the back of the queue until it is rated Hard or Good.
_Avoid_: memorization list, wordsToLearn, round

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
rather than importing every word pair as New at Level1.
_Avoid_: include scheduling, preserve state

## Relationships

- A **Library** holds zero or more **Stacks**
- A **Stack** holds zero or more **Word pairs**
- A **Word pair** belongs to exactly one **Stack**
- A **Word pair** has exactly one **Level**, which determines when it becomes **Due**
- A **Session** holds the **Due** word pairs of exactly one **Stack**; each **Rating** given in
  it updates one **Word pair**
- An **Export file** holds one or more **Stacks**, chosen by **Export scope**
- A **Stack** has exactly one **Stack id**, unique within a **Library**
- Importing a **Stack** whose **Stack id** is already present offers replace, **Fork** or skip;
  a **Stack id** not yet present is added and keeps the id from the **Export file**

## Example dialogue

> **Dev:** "If I export one **Stack** and you import it, do you get my **Levels**?"
> **Learner:** "The file carries them, but they're mine, not yours. On import you decide
> whether to **Keep progress** or start the **Word pairs** fresh as **New**."
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
  export already imports, since only the first two columns are required. ADR 0003 (proposed)
  would remove the blocker.
- The five fixed **Levels** are an interim scheduler. The intended replacement is FSRS with a
  **Card** per direction of a **Word pair** (L2→L1, L1→L2 or both, chosen per **Stack**), the
  **Level** derived from stability, and a fixed desired retention of 0.9 — see ADR 0003. A
  retention setting is possible later.
- A **Session** lives in memory. If the process dies mid-session the queue is rebuilt from the
  due pairs; a pair already rated Again that day is Level1 with today's date and so returns
  tomorrow, not later in the same sitting. Accepted for now.
