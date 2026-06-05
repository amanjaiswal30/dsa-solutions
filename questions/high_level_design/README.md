# High-Level Design (HLD)

Conversational mock-interview write-ups: **flows first**, then **FR/NFR**, then **small → scale** with component-by-component Q&A.

---

## Full write-ups

| # | Topic | Doc | Focus |
|---|-------|-----|-------|
| 01 | **Rate limiter** | [01_rate_limiter_hld.md](01_rate_limiter_hld.md) | Token bucket, Redis/Lua, hot keys, multi-region, fail-open vs closed |

---

## Topics to prepare (coming next)

Use the list below as a **checklist** for whiteboard practice until a full doc exists.

### Core building blocks

1. ~~**Rate limiter**~~ — [done](01_rate_limiter_hld.md)
2. **Consistent hashing** — virtual nodes, ring membership, minimal remapping on node add/remove.
3. **Key-value store** — replication, quorum (R/W), hinted handoff, anti-entropy, CAP trade-offs.
4. **UUID generator** — uniqueness across machines, clock skew, Snowflake-style IDs, collision handling.
5. **URL shortener** — encoding, redirect path, analytics, scale, collision-free short codes.

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
2. **Expected flows** — allow path, deny path, admin/config (sequence diagrams).
3. **Functional & non-functional requirements** — explicit table before architecture.
4. **Start small** — one box, in-memory; interviewer pokes holes.
5. **Scale up** — shared store → algorithms → control plane → sharding → multi-region.
6. **Component Q&A** — back-and-forth on gateway, Redis, control plane.
7. **Failures, metrics, trade-offs** — how you close the interview.

For any topic, be ready to cover: **requirements**, **APIs or flows**, **data model**, **scaling**, **failure modes**, and **trade-offs**.

**Regenerate diagrams:** `python3 scripts/render_hld_diagrams.py` (requires Node + `@mermaid-js/mermaid-cli` via `npx`).
