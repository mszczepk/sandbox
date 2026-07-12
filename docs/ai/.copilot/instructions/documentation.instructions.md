---
applyTo: "**/*.md"
---

# Documentation Guideline

## General

* Short, concise, simple sentences - all easy and fast to read and grasp the idea by a human.
* Coherent, no contradictions.
* Consistent naming, language and wording along all docs, e.g.
  * If a word "ok" was used for stating something is acceptable, then use it everywhere and do not use synonyms like
    "good", "fine", "acceptable", etc.
* No duplications, everything mentioned only once, even if it's just a link to the other docs or section.
* Some doc types have a defined structure:
  * ADR - according to [ADR](#ADR) template.
  * Designs - according to [Design](#Design) template.
* File tree structure:
  ```
  docs/
    decisions/          - ADR MD files
    designs/
      {design}/         - design MD files
        evaluation/     - (optional) files used during analysis (source code and results): SQL, JSON, CSV, MD
        examples/       - (optional) code examples
  ```
* Use Markdown links:
  * For both docs, headings, subheadings and URLs.
  * Use short notation with numbers, e.g. `[elo.md][1]`.
  * Order numbered links by their appearance order top-down.
  * For headings rename the link to heading name.
  * For files, keep the file name or use file title, which is inside MD.
  * Always reference as precisely as possible - prefer the exact subheading anchor over an upper-heading or whole-file
    link.
  * After renaming a heading, re-verify anchor links - they break silently when the heading text changes.
* For Markdown tables format them properly, align the column widths to fit the widest content, filling others with
  padding spaces if needed.
* Keep a reasonable abstraction level for the doc purpose. For general docs, don't dive deep into impl details but only
  explain the idea, e.g.
  * An architecture doc should state only the concept ("we need a partial index"), and should not show how the concept
    can be implemented or achieved in the technology ("mongo partial index JSONs with fields").
  * An architecture doc should name a concept ("sponsored and complete offers") and not use pseudo code that would
    suggest variable names ("isSponsored && isComplete").
  * When you consciously defer details or further analysis (e.g. physical schema, indexes), leave a comment
    with a next doc reference if it exists ("skipped for now because ... - will be discussed/analyzed later + link to
    other docs if present").
* Back numbers with evidence:
  * Complete the math (e.g. unit cost times rate = monthly cost).
  * Link the SQL and its results if available (from the `evaluation` folder) when a number first appears (or its topic
    is first mentioned).
  * Do not leave "unknown, to estimate" when the data exists, and do not claim a cost is negligible without the math.
  * Flag unverified numbers explicitly (gut-feel, to be analyzed later).
* Give every open topic a disposition - mark accepted risks explicitly with a status line and a "to be analyzed later"
  note. Never leave a described problem with no disposition.
* No hard Markdown formatting, available only (nothing else):
  * Title (heading 1), heading 2, heading 3.
  * Code block, code line.
  * Bullet or dash list.
  * Links.
  * Tables.
* No emojis, arrows (`→`), or symbols outside the standard keyboard. Use `-` (minus) for dashes, plain text for flow
  descriptions.
* Wrap lines and use indent as configured in `.editorconfig` if present. If absent, default to line width = 120 chars
  and indent = 2 spaces.

### ADR

* Prepare only when the team has made the decision. Drafting beforehand is ok.
* The following frontmatter fields are optional: `decision-makers`, `consulted` (two-way communication), and `informed`
  (one-way communication).
* In the `Context and Problem Statement` section, describe in 2-3 sentences or as a question. Link to issue trackers if
  relevant.
* The `More Information` section is optional. Do not add subheadings.
  The section may contain:
  * Additional evidence/confidence for the decision outcome.
  * The team agreement on the decision.
  * If/when/how the decision should be realized or re-visited.
  * Links to other decisions and resources.
* In the `Pros and Cons of the Options` section, label honestly:
  * A weak or non-decisive advantage is "neutral", not "good".
  * Do not inflate.
  * Mirror the same axis across options - if one option lists a trait as "bad", the opposing option's inverse should
    read consistently.
* Decision drivers are not requirements - if a driver is in fact a hard requirement (a knock-out criterion), state that
  explicitly, so it is not weighed like a soft trade-off.

### Design

* Prepare when a solution is being explored or team review is needed.
* The `Dictionary` section is optional. Include only terms that are business-specific or coined for the doc.
* Topics under the `Design` section can represent: a problem, analysis, feature or workflow description, edge case, or
  meeting
  notes.
* Do not contain a bullet list of consequences (bad, good, neutral) - put it into ADR, where decision is
  made, and pros & cons matter. In design only note that something is a problem and browse the possible solutions.
* Update every time the discussion progresses and new ideas come in:
  * Append new issues and problems in a new "design > {topic}".
  * Merge updates to an existing issue with the relevant existing "design > {topic}".
  * For an important meeting or consulting session, a new "design > {meeting}" can be added if not shortly included in
    the affected topics.

## Templates

### ADR

See also:

* [Full ADR template][1]

```
---
status: {proposed | rejected | accepted | deprecated | superseded by ADR-0123}
date: {YYYY-MM-DD}
decision-makers: {list}
consulted: {list}
informed: {list}
---

# {title}

## Context and Problem Statement

{context}

## Decision Drivers

* {driver}
* ...

## Considered Options

* {option 1}
* ...

## Decision Outcome

Chosen option: "{option}", because {justification}.

## Pros and Cons of the Options

### {option 1}

{link to design doc}

Good:
* ...

Neutral:
* ...

Bad:
* ...

## More Information
{info}

[links]
```

### Design

```
# {title}

## Dictionary
* {term} ({abbreviation}) - {description}
* ...

## Design

### Idea

### {topic 1}

### {topic 2}

[links]
```

[1]: https://github.com/adr/madr/blob/develop/template/adr-template.md
