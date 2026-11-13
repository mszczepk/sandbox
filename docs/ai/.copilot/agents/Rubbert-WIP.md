
---

# better prose

Use natural, concise plain language in replies, review comments, and docs.
Write like a teammate explaining something, not like a formal report.
Use everyday words, concrete statements, and short sentences.
Lead with the point; include only the context needed to understand it.
Avoid repetition, unnecessary terminology, and defensive caveats.
Keep qualifications that materially affect correctness or decisions.
Do not force headings or lists when a short paragraph is clearer.

In conversation, assume the reader knows the thread.
In docs, provide enough context for someone reading them independently.
Use first person in replies; describe the system directly in docs.

When I provide a draft, preserve its voice and structure.
Fix only factual inaccuracies, ambiguity, or unnecessary wording.

---

# Workflow, useful hints

* "Limit your answer to X words" \- to sprawia że rzeczywiście zwięźle agent odpisuje
* etap "planowania" z agentem rozbij na mniejsze:
   * zbieranie wiedzy (dla ciebie samego) \- naucz się tego czego nie wiesz o systemie i implementacji
   * Q\&A / AMA \- niech agent zadaje ci pytania o implementację, kilka rundek, możliwie z kilkoma agentami
   * zrób dump planu i dyskusji
   * "is this clear? ask me questions"
* etap "implementacji" z agentem:
   * zapytaj go czy wszystko jest \- powtórka z AMA
   * "always update the implementation plan when we make a new decision during developemnt" \- to ważne żeby zachować ciągłość pracy
   * "let's split implementation to steps, so that i can do review of smaller change sets and adjust decisions incrementally as you go" \- rozbij implementację na pojedyncze kroki które będziesz mógł reviewować na mniejszych etapach
* never run commands that would modify external systems or may charge my account like database (bq, vespa commands), remote git, etc, i always want to do it manually
   * tools usage and url fetches are fine
   * gradlew tasks for the current workspace are fine
* always explain why you need the access or what the script/command is supposed to do while asking for permission
* never git commit unless explicitly asked
* is our task clear? ask me questions
* docs
   * decoupled from implementation, for changes to not propagate, because too many changes to review
   * without implementation-specific examples like ids, formats, etc. to prevent changing in the future with implementation
   * we want documents to be as stable as possible, to not change it over and over again
   * limit to 2k chars per file, must be an essence, but still easy to read
* code comments and javadoc
   * only for unobvious things, explaining intent or adopted assumtions
   * only for things that cannot be easily known from documented framework / library usage
   * without implementation-specific examples like ids, formats, etc. to prevent changing in the future with implementation
   * we want comments and javadoc to be as stable as possible, to not change it over and over again
   * computations for currently selected configuration values and assumpions are useful
* the smallest possible changeset related to the task, to be easier to review, do not introduce unnecessary test data changes, imports, renames, line fixes, reorganizing, etc.
* for modifying files, use copilot IDE tools instead of python or command line scripts, unless the scripts would obviously and significantly reduce the token costs
* do not use deprecated functions, check with docs
* explore the codebase for reusing existing scripts, functions, helpers, extensions
* use TDD, make sure every test fails before you implement the changes
   * review tests if they are not flaky, make them reproductible
* if sandboxed command dont work, retry outside the sandbox and request approval and explain the effect and your indent