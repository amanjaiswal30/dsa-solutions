# High-Level Design: Unique ID Generator

**Difficulty:** Medium ⚡  
**Interview duration:** 35–45 min  
**Style:** Conversational mock interview — requirements, naive approaches, then Snowflake at scale.

**Diagrams:** `assets/images/high_level_design/04_*` (regenerate: `python3 scripts/render_hld_diagrams.py`).

---

## 0. The opening question

> **Interviewer:** *"Design a system that generates unique IDs for every tweet, order, and message in our app — 10k+ IDs per second, sortable by time."*

---

## 1. Clarify scope — expected flows first

### 1.1 Generate ID (happy path)

![](../../assets/images/high_level_design/04_seq_generate_id.svg)

1. Service calls ID API (or embedded library).
2. Generator composes ID from timestamp + machine + sequence.
3. Returns 64-bit integer — no DB round trip per ID.

### 1.2 Clock skew failure

![](../../assets/images/high_level_design/04_seq_clock_skew.svg)

1. NTP adjusts clock backward.
2. Generator detects `now < last_timestamp`.
3. **Reject** or wait until clock catches up — never reuse timestamp+sequence combo.

### 1.3 Clarifying questions

| Question | Typical answer |
|----------|----------------|
| ID type? | 64-bit integer (JSON-safe if &lt; 2^53 for JS — or use string) |
| Sortable by time? | **Yes** — critical for tweets, feeds |
| Globally unique? | Yes across all datacenters |
| Rough QPS? | 10k/sec peak, 1k/sec average |
| Expose to clients? | Internal services only |

> **Interviewer:** *"64-bit, time-sortable, unique cluster-wide, internal API."*

---

## 2. Functional requirements (FR)

| # | Requirement |
|---|-------------|
| FR1 | Generate a **unique** ID per request. |
| FR2 | IDs are **monotonically increasing** with time (roughly sortable). |
| FR3 | Support **batch** generation (e.g. 100 IDs per call). |
| FR4 | **No coordination** per ID at runtime (no central DB increment). |
| FR5 | IDs fit in **64 bits**. |

---

## 3. Non-functional requirements (NFR)

| # | Requirement | Target |
|---|-------------|--------|
| NFR1 | **Latency** | &lt; 1 ms p99 (local generation) |
| NFR2 | **Availability** | 99.99% — ID service down blocks writes |
| NFR3 | **Throughput** | 10k+ IDs/sec per machine |
| NFR4 | **Durability of uniqueness** | No collisions ever — even after restarts |
| NFR5 | **Clock dependency** | Tolerate small NTP drift; fail safe on large skew |

---

## 4. Phase 1 — Naive approaches (and why not)

![](../../assets/images/high_level_design/04_flow_naive_vs_snowflake.svg)

| Approach | Problem |
|----------|---------|
| **DB `AUTO_INCREMENT`** | Single DB bottleneck; not sortable across shards |
| **UUID v4 (random)** | Unique but **not time-sortable**; 128 bits |
| **Redis `INCR`** | Central dependency; network hop per ID |
| **Range per server** | Wastes IDs; painful rebalancing if server dies |

> **Interviewer:** *"So what do you use?"*

> **You:** *"**Twitter Snowflake** — 64-bit, time-ordered, generated locally per machine."*

---

## 5. Phase 2 — Snowflake layout

![](../../assets/images/high_level_design/04_flow_snowflake_layout.svg)

```
| 1 bit sign (0) | 41 bits timestamp ms | 5 bits DC | 5 bits machine | 12 bits sequence |
```

| Field | Bits | Capacity |
|-------|------|----------|
| Timestamp | 41 | ~69 years from epoch |
| Datacenter | 5 | 32 DCs |
| Machine | 5 | 32 machines per DC (1024 total) |
| Sequence | 12 | 4096 IDs per ms per machine |

**Per-machine max:** 4096 × 1000 = **~4M IDs/sec** — plenty for 10k/sec.

**Generation logic (pseudocode):**

```
lock:
  now = current_time_ms()
  if now < last_ts: throw ClockMovedBackwards
  if now == last_ts:
    sequence = (sequence + 1) & 4095
    if sequence == 0: wait next millisecond
  else:
    sequence = 0
  last_ts = now
  return (now - EPOCH) << 22 | dc << 17 | machine << 12 | sequence
```

---

## 6. Phase 3 — Machine ID assignment

> **Interviewer:** *"How does machine 7 know it's machine 7?"*

> **You:** *"On startup, acquire a **lease** from ZooKeeper / etcd for `(datacenter_id, worker_id)` pair. Heartbeat renews lease. If process dies, lease expires — ID slot recycled after TTL."*

**Alternative:** Static config in K8s StatefulSet ordinal — `pod-3` → worker_id=3.

> **Interviewer:** *"Two machines get same ID?"*

> **You:** *"Lease is exclusive — second claimant blocks or gets different slot. Collision detection on startup is mandatory."*

---

## 7. Phase 4 — Scale & deployment

![](../../assets/images/high_level_design/04_flow_final_architecture.svg)

- **Embedded library** in each service (fastest) OR thin **ID API** for non-JVM languages.
- **Stateless API** — each instance runs its own Snowflake with unique machine ID.
- **Batch endpoint:** `POST /ids?count=100` — amortize HTTP overhead.

---

## 8. Clock skew deep dive

| Scenario | Handling |
|----------|----------|
| Small drift forward | OK — gaps in sequence are fine |
| Clock backward (NTP) | Reject requests until `now ≥ last_ts` |
| DC clock skew | Use **logical clock** bound to NTP; monitor skew across DCs |
| Leap second | Treat as same millisecond; sequence handles burst |

> **Interviewer:** *"Can we use UUID v7 instead?"*

> **You:** *"UUID v7 (2024 RFC) is time-sortable 128-bit — good alternative if 64 bits tight or you want standard format. Snowflake still wins on compactness and bit budget control."*

---

## 9. Component Q&A

| Question | Answer |
|----------|--------|
| JS `Number` precision? | Snowflake &gt; 2^53 — return **string** to browsers |
| Multi-region ordering? | Rough time order — not strict global order across DC clock skew |
| ID exhaustion? | 41-bit timestamp — plan epoch rollover in 2078 or migrate |
| Security? | IDs guessable (sequential) — don't use as auth tokens |

---

## 10. Trade-offs

| Decision | Snowflake | DB sequence | UUID v4 |
|----------|-----------|-------------|---------|
| Sortable | ✅ | ✅ per shard | ❌ |
| Coordination | Startup only | Every ID | None |
| Size | 64 bit | 64 bit | 128 bit |
| Hotspot | None | DB hotspot | None |

**Close:**

> *"Snowflake: 64-bit IDs from timestamp + DC + machine + sequence; generated locally at 4M/sec per node; machine IDs leased at startup; clock backward is a hard error; expose as internal API or library."*

---

## 11. Follow-ups

1. **Instagram / Uber ID** — custom base62 encoding on top of Snowflake.
2. **Flickr ticket servers** — DB block allocation (older pattern).
3. **Distributed UUID without clocks** — name-based UUID v5 + counter (not time-sortable).

---

*Prev: [Key-Value Store](03_key_value_store_hld.md) · Next: [URL Shortener](05_url_shortener_hld.md)*
