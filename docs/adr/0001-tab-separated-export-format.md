# Export files are flat tab-separated text, one row per word pair

An export has to serve two jobs at once: a faithful backup of the learner's own progress,
and a vocabulary list that can be handed to someone else. We write a single flat file,
one row per word pair with the stack's columns repeated on each row, tab-separated, UTF-8
with a BOM, and we read comma, semicolon or tab on import.

## Considered Options

**Pretty JSON.** Nests words under stacks, represents an empty stack, no repetition. Rejected
because it cannot be opened in a spreadsheet, which is the one thing a learner actually wants
to do with a vocabulary list.

**Two normalised files** (stacks + words). Correct, but sharing means keeping two files
together or zipping them, and half of the pair is useless on its own.

**Anki `.apkg`.** Real interoperability, including AnkiWeb shared decks. Rejected for now
because Anki schedules with a continuous interval-plus-ease per card while this app has five
fixed levels: `Level3 → 7 days` maps cleanly, `interval 37, ease 2350 → ?` does not. Deferred
as separate work, not ruled out.

## Consequences

Tab rather than comma is deliberate. Vocabulary contains commas constantly (`¿qué tal?`) and
tabs essentially never, so almost nothing needs quoting and the file stays hand-editable. It
also sidesteps Excel taking its delimiter from the system locale, which in a semicolon locale
drops a comma-separated file into a single column.

Only the first two columns are required. A file with no header and two columns is read as
word and meaning, with a generated stack id, `Level1` and a `last_rep` of now — so a plain
Anki text export imports without any Anki-specific code. The cost is that a malformed file
imports as nonsense rather than failing, which is why bad rows are skipped and then reported
in a summary the learner has to dismiss.

An empty stack cannot be represented, and rows disagreeing about a stack's name resolve to
the first one seen.
