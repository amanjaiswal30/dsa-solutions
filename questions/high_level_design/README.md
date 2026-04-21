# High-Level Design (HLD) — topics to prepare

This folder does **not** contain full HLD write-ups. Use the list below as a **checklist** for what to study and practice on a whiteboard or in mock interviews.

---

## Core building blocks

1. **Rate limiter** — quotas, token bucket vs sliding window, Redis/Lua, hot keys, multi-region.
2. **Consistent hashing** — virtual nodes, ring membership, minimal remapping on node add/remove.
3. **Key-value store** — replication, quorum (R/W), hinted handoff, anti-entropy, CAP trade-offs.
4. **UUID generator** — uniqueness across machines, clock skew, Snowflake-style IDs, collision handling.
5. **URL shortener** — encoding, redirect path, analytics, scale, collision-free short codes.

---

## Systems & pipelines

6. **Web crawler** — politeness (robots, rate limits), URL frontier, dedup, storage of raw HTML.
7. **News feed system** — fan-out on write vs read, ranking, mixed media, pagination.
8. **Chat system** — presence, delivery semantics, history sync, group chats, WebSockets vs polling.
9. **Distributed message queue** — partitions, consumer groups, ordering guarantees, at-least-once delivery.
10. **Metrics, monitoring, and alerting** — collection, time-series storage, dashboards, alert routing, SLOs.
11. **Notification system** — multi-channel delivery, templates, idempotency, user preferences, fan-out.

---

## Domain-style designs

12. **Hotel reservation system** — inventory, overbooking rules, search, payments, concurrency on rooms.
13. **Trending videos** — view counts, time windows, decay, regional vs global trends, cache invalidation.
14. **Gaming leaderboard** — scores, ranks, sharding by game/region, real-time updates.
15. **Payment system** — authorization vs capture, idempotency, reconciliation, failure handling.
16. **Digital wallet** — ledger, balances, transfers, compliance hooks (often overlaps with payment + LLD in this repo).

---

## How to use this list

For each topic, be ready to cover: **requirements**, **APIs or flows**, **data model**, **scaling**, **failure modes**, and **trade-offs**—without needing a long document in this repository.
