# Memoization

A spaced-repetition vocabulary trainer. The learner groups word pairs into stacks and
repeats them on a schedule that widens as a word is recalled correctly.

## Language

**Stack**:
A named collection of word pairs the learner studies together, usually one language.
_Avoid_: folder, deck, list

**Word pair**:
One thing to recall and its answer. The pair holds the words; its sides hold the schedule.
_Avoid_: card, word, translation

**Side**:
One direction of recall of a word pair - shown the word, recall the meaning, or the reverse -
carrying its own stability, difficulty and due date.
_Avoid_: card, direction, face

**Practice sides**:
The learner's choice, for the whole library, of which sides to practise: word to meaning,
meaning to word, or both. A pair always has both sides; this decides which are scheduled.
_Avoid_: mode, direction setting

**Stability**:
How many days a side's memory holds - the point at which recalling it would be a nine-in-ten
chance. It is the schedule: the next repetition falls about that many days later.
_Avoid_: interval, strength, ease

**Difficulty**:
How hard a side is for this learner, from 1 to 10; it slows how fast stability grows.
_Avoid_: level, hardness

**Level**:
A reading of a side's stability, for colour: Level1 under 2 days, Level2 under 7, Level3 under
14, Level4 under 30, Learned beyond. A pair shows the level of its weakest practised side.
_Avoid_: score, status, stored level

**Rating**:
The learner's answer to a side - Again, Hard or Good, which is also what the three buttons say.
Again drops stability sharply but not to nothing, Hard grows it a little, Good grows it fully.
_Avoid_: a fourth rating (FSRS has one, this app does not), icon, click

**New**:
A side that has never been rated; it is due at once, and its first rating sets its stability.
_Avoid_: fresh, unseen, just added

**Due**:
A side that is New, or whose due date's calendar day is today or earlier. A pair is due when
any of its practised sides is.
_Avoid_: to learn, toShow, unrepeated

**Session**:
One sitting with a stack: its due practised sides, shuffled once on entry and shown one at a
time; a side rated Again returns to the back of the queue until it is rated Hard or Good.
_Avoid_: memorization list, wordsToLearn, round

**Hidden**:
A word pair or stack the learner has deleted. Nothing leaves the database - the row and its
progress stay, so an undo, or a restore, can bring it back.
_Avoid_: deleted, removed, trashed

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
rather than importing every side as New.
_Avoid_: include scheduling, preserve state

## Relationships

- A **Library** holds zero or more **Stacks**
- A **Stack** holds zero or more **Word pairs**
- A **Word pair** belongs to exactly one **Stack**
- A **Word pair** has exactly two **Sides**, one per direction of recall
- A **Side** has its own **Stability**, **Difficulty** and due date, and shows a **Level**
- **Practice sides** decides which **Sides** of every pair are scheduled
- A **Session** holds the **Due** practised **Sides** of exactly one **Stack**; each **Rating**
  given in it updates one **Side**
- An **Export file** holds one or more **Stacks**, chosen by **Export scope**
- A **Stack** has exactly one **Stack id**, unique within a **Library**
- Importing a **Stack** whose **Stack id** is already present offers replace, **Fork** or skip;
  a **Stack id** not yet present is added and keeps the id from the **Export file**

## Example dialogue

> **Dev:** "If I export one **Stack** and you import it, do you get my progress?"
> **Learner:** "The file carries it, but it's mine, not yours. On import you decide whether to
> **Keep progress** or start every **Side** fresh as **New**."
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
- "card" is what the learner flips on screen, which is a whole **Word pair** — resolved: the
  half of a pair that carries a schedule is a **Side**, never a card.
- "delete" is what the swipe gesture and its snackbar say — resolved: the concept is
  **Hidden**; nothing is removed from the database. See ADR 0003.

## Open questions

- Anki interoperability (`.apkg`) is deferred, not rejected. Both apps now schedule with
  stability and difficulty per side, so the mapping that ADR 0001 called lossy is no longer
  lossy by construction. A plain two-column Anki text export already imports.
- Desired retention is fixed at 0.9 (ADR 0003). A setting is possible later, as are FSRS
  parameters fitted to this learner's own review history.
- **Hidden** pairs and stacks have no way back in the UI yet; a restore screen is the missing
  half of "nothing is ever deleted".
- A **Session** lives in memory. If the process dies mid-session the queue is rebuilt from the
  due sides; a side already rated Again that day has been rescheduled and returns tomorrow,
  not later in the same sitting. Accepted for now.
