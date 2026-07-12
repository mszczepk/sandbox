---
applyTo: '**/*.sql'
---

# BigQuery SQL Guideline

## Structure

Order:

1. `DECLARE` input params
2. `DECLARE` derived params
3. `WITH` CTEs
4. final `SELECT`

## Parameters

* All tunable inputs as `DECLARE` with defaults and inline comments explaining semantics.
* Derived parameters in a separate `DECLARE` block - never inline in CTEs.
* Prefer `TIMESTAMP` and `INTERVAL` types. Prefer UTC. Use `+` operator for arithmetic.
* Push as many computations as possible into derived parameters so that CTE expressions stay pure and simple.

```
DECLARE analysis_period INTERVAL DEFAULT INTERVAL 30 DAY;
DECLARE start_day TIMESTAMP DEFAULT TIMESTAMP "2026-06-01";
DECLARE end_day TIMESTAMP DEFAULT start_day + analysis_period;
```

## CTE Naming and Layering

Sequential transformations per data source if applicable, each CTE doing one job. The `baseline` (snapshot)
and `events` streams run through the same layers before being merged.

| # | Layer             | Baseline CTE               | Events CTE               | Purpose                                                                     |
|---|-------------------|----------------------------|--------------------------|-----------------------------------------------------------------------------|
| 1 | Partition prune   | `<src>_baseline_partition` | `<src>_events_partition` | Filter `_PARTITIONTIME`, rename columns, no business logic                  |
| 2 | Deduplicate       | `unique_<src>_baseline`    | `unique_<src>_events`    | `QUALIFY ROW_NUMBER() OVER (PARTITION BY <key> ORDER BY <tie-breaker>) = 1` |
| 3 | Time filter       | `<src>_baseline`           | `<src>_events`           | Restrict to the analysis period                                             |
| 4 | Business rules    | `<rule>_<src>_baseline`    | `<rule>_<src>_events`    | Business logic: filtering and transformation                                |
| 5 | Latest by version | -                          | `latest_<src>_events`    | `QUALIFY ROW_NUMBER() OVER (PARTITION BY ... ORDER BY version DESC) = 1`    |

The final `SELECT` is a separate whole-query layer, not a per-source CTE.

In most cases, `<src>` can be derived from both source table names, e.g. `hourly_items_added_snapshot` (baseline),
`com_ecomerce_cart_itemaddedv4` (events) use `items`.
Use `SELECT *` between intermediate layers to avoid repeating column names. However, always prune columns in
the "partition pruning" layer and again in the "final select".

## Column Naming

* Timestamp suffixes: `_at` (not truncated), `_hour` (truncated to hour), `_day` (truncated to day).
* `_PARTITIONTIME` - `ingested_at` / `ingested_hour` / `ingested_day` depending on partition granularity.
* `published_at` - the column used for filtering events by time.
* `version` - the column used for ordering events, regardless of its data type.

## Version Semantics

* Logical ordering uses a domain `version` column, not ingestion time.
* Snapshot and events compete on equal footing (`UNION ALL` then `ROW_NUMBER`).
* `COALESCE(TIMESTAMP(domain_field), _PARTITIONTIME)` as fallback when domain version can be null.

## Style

* Fully qualified table names: `project.dataset.table`.
* `TIMESTAMP()` casts applied at the partition-pruning layer, not repeated downstream.
* Comments only where the intent cannot be implied from naming alone (except parameter descriptions).
* No emojis, arrows (`→`), or symbols outside the standard keyboard. Use `-` (minus) for dashes, plain text for flow
  descriptions.
* Wrap lines and use indent as configured in `.editorconfig` if present. If absent,
  default to line width = 120 chars and indent = 2 spaces.
