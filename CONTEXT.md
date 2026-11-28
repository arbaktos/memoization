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
The learner's choice of which sides to practise: **Smart switch** (the default), word to
meaning, meaning to word, or both. Set for the whole library in the settings; a **Stack** can
choose its own when it is created or edited, and otherwise follows the library's ("As in
settings"). A pair always has both sides; this decides which are scheduled.
_Avoid_: mode, direction setting

**Smart switch**:
The default practice sides: one side of a pair at a time. A pair is practised word to meaning
first; once the word side reaches Level3 - a memory holding a week - from at least two answers
(a first Easy alone gets a new side there), the pair hands over to its meaning side from the
next day on: the meaning side enters as **New**, and the word side is no
longer scheduled. A meaning side
that has been rated keeps the pair even if the word side lapses below that. Both sides at once
is what **Both sides** is for.
_Avoid_: auto mode, adaptive, unlock

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
The stack list folds them into four bars: new (a practised side never answered), learning
(Level1-2), known (Level3-4) and learned. The stack screen shows the same four steps as a dot
beside each side of a pair - the word's dot for the word side, the meaning's for the meaning
side - and no dot for a side the **Practice sides** setting never asks for.
_Avoid_: score, status, stored level

**Rating**:
The learner's answer to a side, one of FSRS's four: Again, Hard, Good, Easy. Three buttons
give them - Forgot, Hard, Easy. Forgot is Again: stability drops sharply but not to nothing and
the side stays **Due** until it is recalled. Easy grows stability most. Hard is not taken at its
word: it turns the card over to the answer, and the learner says whether they had it right -
yes is Good, a full grow; no is Hard, a small one, with no lapse. Nothing is rated until then,
and neither sends the side round again. Recalling a side after Again in the same **Session** is
not a rating: it only lets the lapse's schedule start. Forgot is rated at once and turns the
card over too, so the learner sees what they could not recall; Easy never shows the answer.
_Avoid_: Good for the Easy button (it was once so, and the screen said one thing while the
scheduler got another), icon, click

**New**:
A side that has never been rated; it is due at once, and its first rating sets its stability.
_Avoid_: fresh, unseen, just added

**Due**:
A side that is New, or whose due date's calendar day is today or earlier. A pair is due when
any of its practised sides is.
_Avoid_: to learn, toShow, unrepeated

**Session**:
One sitting with a stack: all of its due practised sides, the most forgotten first (lowest
FSRS retrievability; among ties the weaker memory; never-practised last; equals in random
order), shown one at a time; a side rated Again returns to the back of the queue until it is
rated anything else. A session has no size. It ends one of three ways - the queue drains, the
learner taps **Enough for today**, or they leave the screen - and the **Review log** records
which, with how many sides were closed and how many were not got to. Its progress has a place per
side and one more per Forgot, for the repeat; every answer fills one in the colour of its
button, so the bar is full when the queue is through. It never says how many are left.
_Avoid_: memorization list, wordsToLearn, round, daily limit, cap

**Session summary**:
What the learner is shown when a **Session** ends, however it ended - Back included, once
anything was answered: how many sides were practised, each counted once by its first answer
(Easy, Hard, Forgot), and a message chosen by the mistakes - sides practised before whose first
answer was Forgot - against what FSRS expected for them: within 15 % of that, never narrower
than one either way, "right on track"; more, "a productive session"; fewer, "great recall".
The number of mistakes is never shown; the wording treats them as part of learning.
_Avoid_: report, score, result screen

**Enough for today**:
The learner's way to end a **Session** early. Offered once they have answered as many sides as
the stack's **Stamina** says; the sides not got to simply stay **Due**.
_Avoid_: I'm tired, quit, give up

**Study anyway**:
A **Session** started from the stack list when nothing is **Due**: every practised side, due
or not, the most forgotten first. FSRS rates the early answers for what they are.
_Avoid_: cram, extra practice, review ahead

**Stamina**:
Where the learner tends to stop with a stack: the running mean of answers per **Session** ended
with **Enough for today**, kept per **Stack**. The offer comes at three quarters of that mean -
30 answers until the stack has been stopped once, never earlier than 10 - so a stop can fall
below the mean and pull it either way.
_Avoid_: limit, fatigue score, threshold

**Review log**:
A row per answer the learner gives - every tap, including the answer to a side brought back
after Again - with when, how long the card was on screen, the rating, and the side's schedule
after it; plus a row per **Session** with start, end and what it was given. It is the memory
of the **Statistics**; it starts with the version that introduced it, and nothing in it is
ever deleted.
_Avoid_: history table, analytics, events

**Statistics**:
What the learner sees of their own effort and progress: streak, minutes, taps, mistake rate,
the growth of Learned sides, easiest and hardest pairs - library-wide, later per stack. Read
from the **Review log** and the sides; never stored.
_Avoid_: dashboard, analytics, report

**Streak**:
Consecutive **Obligation days** on each of which a **Session** was finished (its queue drained).
Days with no obligation are skipped, not broken. Shown as current and best.
_Avoid_: combo, chain, daily goal

**Obligation day**:
A local calendar day on which at least one scheduled side - rated before, with a due date on
or before that day - was due. A **New** side never creates an obligation: adding words in the
evening does not cost the streak.
_Avoid_: active day, due day

**Weekly card**:
A dismissible card at the top of the Library on Monday, summarising last week - days
practised of obligation days, minutes, taps, words learned - once a full week of **Review log**
exists and the week had an obligation or a session. Its period is a setting: weekly, every two
weeks, monthly, or never.
_Avoid_: popup, notification, report

**Shared word**:
A word another app hands to Memoization - a selection in a browser, a reader or a chat, sent
through the share sheet or straight from the selection menu. It arrives with no stack of its
own, so the learner picks one and it opens as a new **Word pair** with the word already in the
field - unless the share named a stack itself: the stacks in daily use stand in the share sheet
as targets of their own, and sharing to one goes straight to the pair.
_Avoid_: intent, quick add, import

**Hidden**:
A word pair or stack out of sight: left out of sessions, counts and lists. Nothing leaves the
database - the row and its progress stay. A pair or stack is Hidden either because the learner
**Deleted** it or, for a pair, because a merge folded it into another.
_Avoid_: removed, trashed

**Deleted**:
Hidden by the learner, with the time it happened (`deletedAt`). Deleted stacks and pairs wait at
the end of their list in a collapsible Deleted section, latest first, and Restore brings them
back with their progress, for as long as they wait - there is no expiry. A copy folded away by a
merge is Hidden but not Deleted, so it never shows there. Whatever was Hidden before version 7
of the database counts as Deleted, merge copies included: the learner chose to see them too.
_Avoid_: removed, trashed

**Imported note**:
A note from an Anki package (.apkg/.colpkg), kept whole whether or not it became a word pair:
every field as Anki stored it with its name, the note type, tags, deck, its cards with Anki's
own schedule, and Anki's review history; its media is copied into the app. A note whose type
is a plain one and whose first two fields have text becomes a **Word pair** - the first field
the word, the second the meaning, formatting and media left out - in a **Stack** per Anki deck
("Испанский::Глаголы" reads "Испанский · Глаголы"). Card 1 becomes the word side, card 2 the
meaning side, each carrying on from where Anki had it: FSRS stability and difficulty as they
are, or with only SM-2 the interval as stability and the ease as difficulty; a card on its
learning steps with no FSRS memory starts **New**. Cloze notes, and notes without two fields of
text, are kept but not shown. Anki's note id (guid) makes a second import of the same deck
update the words of the pairs it made, never their progress, and add only what is new.
In the New stack dialog the learner chooses the imported stacks' **Practice sides** and whether
to reset learning progress: with it, the new pairs start **New**, Anki's progress kept only on
the imported cards.
_Avoid_: deck (for a stack), card (for a side)

**Reset progress**:
Every side of every pair in a **Stack** - deleted pairs included, so a restore does not bring the
old schedule back - starts over as **New**. The **Review log** keeps every answer given; an
Undo right after puts the sides back as they were.
_Avoid_: clear, wipe, start over

**Copy**:
A word pair added again to the same stack exactly as before - word and meaning the same up to
case and spacing. Copies are removed all at once: the oldest pair stays, the others are
**Hidden**.
_Avoid_: duplicate (that is the umbrella word), clone

**Spelling variant**:
Two words that differ only in the marks on letters (tražiti / traziti) or in a Cyrillic letter
that looks Latin (ako / akо, typed on the wrong keyboard). They count as the same word; merging
them asks which spelling to keep. A different letter is a different word - trazit is not tražiti.
_Avoid_: typo, near match

**Merge**:
Turning pairs that share a word or a meaning into one pair: the matching side keeps one
spelling, the other side becomes the comma-joined values the learner can edit. The oldest pair
stays; each of its **Sides** takes the weakest schedule among the merged sides of that
direction (a **New** side is the weakest of all), because adding a word again says it was not
remembered. The others are **Hidden** and their answers in the **Review log** move to the kept
sides.
_Avoid_: squish, combine, dedupe

**Dismissed match**:
Two pairs whose **Merge** the learner cancelled: they are never proposed together again. A
pair added later that looks the same is still proposed, against each of them on its own.
_Avoid_: ignored duplicate, false positive

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
- **Practice sides** decides which **Sides** of every pair are scheduled; under **Smart
  switch** that depends on the word side's **Level**
- A **Session** holds the **Due** practised **Sides** of exactly one **Stack**; each **Rating**
  given in it updates one **Side**; a **Stack** has one **Stamina**, which decides when the
  **Session** offers **Enough for today**
- Every **Rating** adds a row to the **Review log**, and every **Session** one; **Statistics**
  and the **Weekly card** are read from the **Review log**; a **Streak** counts **Obligation
  days** with a finished **Session**
- A **Shared word** becomes a **Word pair** in the **Stack** the learner picks for it
- An **Export file** holds one or more **Stacks**, chosen by **Export scope**
- A **Stack** has exactly one **Stack id**, unique within a **Library**
- Within a **Stack**, **Copies** are removed and pairs sharing a word or a meaning are offered
  for a **Merge**, unless they are a **Dismissed match**
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

- Anki import is one way: nothing goes back to Anki, and Anki's review history is kept (see
  **Imported note**) but not turned into **Review log** rows, so the statistics start from the
  first answer given here. What the kept notes could become - more fields on a card, media,
  cloze - is open.
- Desired retention is fixed at 0.9 (ADR 0003). A setting is possible later, as are FSRS
  parameters fitted to this learner's own review history.
- A **Session** lives in memory. If the process dies mid-session the queue is rebuilt from the
  due sides, and a side rated Again and not yet recalled is among them, since a lapse keeps
  the side **Due**; only its place in the queue is lost.
