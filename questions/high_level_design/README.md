# High-Level Design (HLD)

Conversational mock-interview write-ups: **flows first**, then **FR/NFR**, then **small → scale** with component-by-component Q&A.

---

## Full write-ups

| # | Topic | Doc | Focus |
|---|-------|-----|-------|
| 01 | **Rate limiter** | [01_rate_limiter_hld.md](01_rate_limiter_hld.md) | Token bucket, Redis/Lua, hot keys, multi-region |
| 02 | **Consistent hashing** | [02_consistent_hashing_hld.md](02_consistent_hashing_hld.md) | Hash ring, virtual nodes, add/remove, replication |
| 03 | **Key-value store** | [03_key_value_store_hld.md](03_key_value_store_hld.md) | Cassandra-style, quorum W/R, hinted handoff, LSM |
| 04 | **Unique ID generator** | [04_unique_id_generator_hld.md](04_unique_id_generator_hld.md) | Snowflake, clock skew, machine ID leases |
| 05 | **URL shortener** | [05_url_shortener_hld.md](05_url_shortener_hld.md) | Base62 encoding, 302 redirect, read-heavy cache |

---

## Topics to prepare (coming next)

### Core building blocks

1. ~~Rate limiter~~ · ~~Consistent hashing~~ · ~~Key-value store~~ · ~~UUID generator~~ · ~~URL shortener~~

### Systems & pipelines

6. **Web crawler** — politeness (robots, rate limits), URL frontier, dedup, storage of raw HTML.
7. **News feed system** — fan-out on write vs read, ranking, mixed media, pagination.
8. **Chat system** — presence, delivery semantics, history sync, group chats, WebSockets vs polling.
9. **Distributed message queue** — partitions, consumer groups, ordering guarantees, at-least-once delivery.
10. **Metrics, monitoring, and alerting** — collection, time-series storage, dashboards, alert routing, SLOs.
11. **Notification system** — multi-channel delivery, templates, idempotency, user preferences, fan-out.

### Domain-style designs

12. **Hotel reservation system** — inventory, overbooking rules, search, payments, concurrency on rooms.
13. **Trending videos** — view counts, time windows, decay, regional vs global trends, cache invalidation.
14. **Gaming leaderboard** — scores, ranks, sharding by game/region, real-time updates.
15. **Payment system** — authorization vs capture, idempotency, reconciliation, failure handling.
16. **Digital wallet** — ledger, balances, transfers, compliance hooks (often overlaps with payment + LLD in this repo).

---

## Doc format (every HLD topic)

Each write-up follows the same interview arc:

1. **Vague opening question** — interviewer says little; you clarify.
2. **Expected flows** — sequence diagrams for happy path + failure paths.
3. **Functional & non-functional requirements** — explicit table before architecture.
4. **Start small** — one box; interviewer pokes holes.
5. **Scale up** — algorithms, replication, caching, sharding.
6. **Component Q&A** — back-and-forth on each box.
7. **Failures, metrics, trade-offs** — how you close the interview.

**Regenerate diagrams:** `python3 scripts/render_hld_diagrams.py` (requires Node + `@mermaid-js/mermaid-cli` via `npx`).
