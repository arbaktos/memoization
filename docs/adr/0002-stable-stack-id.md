# Stacks carry a stable id that survives export and rename

A stack gets an id that stays with it across devices and installs, stored in a new column on
`stack_entity_table` and written into every export file. Import matches on it: an id already
in the library offers replace, fork or skip; an id not yet present is added and keeps the id
from the file, so a later version of the same shared list is recognised as the same stack.

## Considered Options

**Match on name.** Free, no schema change, and it matches what the import review screen shows
the learner anyway. Rejected because renaming a stack silently turns an old backup into a
stranger, and two stacks can share a name.

**Match on the existing `stackId`.** It is `autoGenerate` and local, so it means nothing in a
file that came from another install.

## Consequences

This is a migration under the rule that the learning database is never wiped
(see `MemoDatabase.MIGRATIONS`), so it has to be written by hand — as the next version bump
after the scheduler migration that took 1 → 2. Existing rows can be backfilled without touching
Kotlin, since SQLite can mint the ids itself:

```sql
ALTER TABLE stack_entity_table ADD COLUMN uuid TEXT NOT NULL DEFAULT '';
UPDATE stack_entity_table SET uuid = lower(hex(randomblob(16)));
```

Word pairs deliberately get no id of their own. Replace, fork and skip all operate on whole
stacks, so word pairs always move wholesale and never need to be matched individually.

"Add" therefore means two things depending on context, and the UI has to say so: adding a
stack the library has never seen keeps the file's id, while forking one it already has mints
a fresh id. The alternative — letting two stacks share an id — would mean every future match
needs a tie-break rule, so the id is unique and the database enforces it.
