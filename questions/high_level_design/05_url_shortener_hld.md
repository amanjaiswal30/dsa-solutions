# High-Level Design: URL Shortener

**Difficulty:** Medium ⚡  
**Interview duration:** 45–60 min  
**Style:** Conversational mock interview — shorten + redirect flows, read-heavy scale, analytics optional.

**Diagrams:** `assets/images/high_level_design/05_*` (regenerate: `python3 scripts/render_hld_diagrams.py`).

---

## 0. The opening question

> **Interviewer:** *"Design a URL shortener like Bitly — shorten long URLs and redirect when users click the short link."*

---

## 1. Clarify scope — expected flows first

### 1.1 Shorten URL (write path)

![](../../assets/images/high_level_design/05_seq_shorten_url.svg)

1. User submits long URL.
2. System generates unique `short_code` (e.g. `abc12X`).
3. Persist `(short_code → long_url, created_at, user_id)`.
4. Return `https://short.domain/abc12X`.

### 1.2 Redirect (read path — 100:1 read/write)

![](../../assets/images/high_level_design/05_seq_redirect.svg)

1. Browser hits `GET /abc12X`.
2. Lookup `short_code` → `long_url` (cache first).
3. Return **302 Found** (or 301 if permanent).
4. Optionally record click event async.

### 1.3 Clarifying questions

| Question | Typical answer |
|----------|----------------|
| Short code length? | 6–8 chars Base62 → 56B–218T URLs |
| Custom aliases? | Optional premium feature |
| Expiry? | Default never; optional TTL |
| Analytics? | Async click stream — mention, don't over-design |
| Read/write ratio? | **100:1** — optimize redirect |

> **Interviewer:** *"100M shortened URLs, 1B redirects/month, 6-char codes, 302 redirect."*

---

## 2. Functional requirements (FR)

| # | Requirement |
|---|-------------|
| FR1 | `shorten(long_url)` → unique short URL. |
| FR2 | `redirect(short_code)` → HTTP 302 to long URL. |
| FR3 | Short codes are **unique** — collision-free. |
| FR4 | Optional **custom alias** per user. |
| FR5 | Optional **expiration** on links. |
| FR6 | **Analytics** — click count, referrer (async). |

---

## 3. Non-functional requirements (NFR)

| # | Requirement | Target |
|---|-------------|--------|
| NFR1 | **Redirect latency** | p99 &lt; 50 ms |
| NFR2 | **Availability** | 99.99% on read path |
| NFR3 | **Uniqueness** | Zero collisions on short codes |
| NFR4 | **Scale reads** | 400+ redirects/sec sustained |
| NFR5 | **Durability** | Mappings never lost once created |

---

## 4. Back-of-the-envelope

- **Writes:** 100M URLs over 5 years → ~600 writes/day average (low).
- **Reads:** 1B/month → ~400 reads/sec average, **10k/sec** peak.
- **Storage:** 100M rows × ~500 bytes ≈ **50 GB** — fits sharded SQL.
- **Cache:** 20% hot links → 20M × 500B ≈ 10 GB Redis — feasible.

---

## 5. Phase 1 — Single server

> **Interviewer:** *"MVP?"*

> **You:** *"One app server + PostgreSQL table `(short_code PK, long_url, created_at)`. Redirect is `SELECT` + 302. Works until read QPS exceeds one box."*

---

## 6. Phase 2 — Encoding strategy

![](../../assets/images/high_level_design/05_flow_encoding_strategies.svg)

| Strategy | How | Pros / cons |
|----------|-----|-------------|
| **Counter + Base62** | Global counter → encode to `a-zA-Z0-9` | No collision; need distributed counter |
| **Hash + truncate** | MD5(url) first 7 chars | Collision → check DB + retry |
| **Pre-generated blocks** | Each server gets ID range | No hot counter DB |

**Recommended in interview:** Counter + Base62 (clean story).

```
id = 123456789 → base62 → "8M0kX"
```

6 chars Base62 = 62^6 ≈ **56 billion** codes.

> **Interviewer:** *"Distributed counter without single point?"*

> **You:** *"DB row per shard range, or Snowflake ID → Base62 ([see doc 04](04_unique_id_generator_hld.md))."*

---

## 7. Phase 3 — Read-heavy path (critical)

![](../../assets/images/high_level_design/05_flow_read_heavy_cache.svg)

**Cache-aside:**

1. Redirect service checks **Redis** `GET short_code`.
2. Miss → DB → `SET` with TTL (e.g. 24h).
3. **CDN** caches 302 responses for hottest links (short TTL — URLs can change).

> **Interviewer:** *"Cache stampede on viral link?"*

> **You:** *"Singleflight / request coalescing — one DB fetch per hot key, others wait."*

---

## 8. Phase 4 — Database & sharding

**Schema:**

```sql
short_urls (
  short_code   VARCHAR(8) PRIMARY KEY,
  long_url     TEXT NOT NULL,
  user_id      BIGINT,
  created_at   TIMESTAMP,
  expires_at   TIMESTAMP NULL
)
```

**Shard key:** `short_code` — redirects always know the code. Consistent hash or prefix sharding.

**Indexes:** PK on `short_code` is enough for redirect. Secondary index on `long_url` if dedup same long URL (optional).

---

## 9. Phase 5 — Analytics (async)

> **Interviewer:** *"Track clicks?"*

> **You:** *"Don't block redirect. Publish `{short_code, ts, referrer, ip}` to **Kafka** → Flink/Batch for aggregates. Separate read path entirely."*

---

## 10. Final architecture

![](../../assets/images/high_level_design/05_flow_final_architecture.svg)

**APIs:**

```
POST /api/v1/shorten   { "long_url": "https://..." }  → { "short_url": "..." }
GET  /{short_code}                              → 302 Location: long_url
GET  /api/v1/stats/{short_code}               → { "clicks": 42000 }
```

---

## 11. Component Q&A

| Question | Answer |
|----------|--------|
| 301 vs 302? | 302 default (stats, editable); 301 if permanent |
| Malicious URLs? | Scan with Safe Browsing API on shorten |
| Custom alias collision? | `INSERT` unique constraint → 409 Conflict |
| Delete / expire? | Tombstone in DB; cache invalidation pub/sub |

---

## 12. Trade-offs

| Decision | Option A | Option B |
|----------|----------|----------|
| Code gen | Counter + Base62 | Hash + retry |
| Cache | Redis cluster | Memcached |
| DB | Sharded MySQL | DynamoDB (code as key) |
| Redirect | 302 + async analytics | Sync write click (slow) |

**Close:**

> *"Write-light shorten API with Snowflake→Base62 codes in sharded DB; read-heavy redirect through CDN → Redis → DB with 302; analytics async via Kafka; optimize for 100:1 read ratio."*

---

## 13. Follow-ups

1. **Custom domains** — `user.com/abc` CNAME to your redirect tier.
2. **QR codes** — render service, same backend.
3. **Rate limit** — shorten API per user ([doc 01](01_rate_limiter_hld.md)).

---

*Prev: [Unique ID Generator](04_unique_id_generator_hld.md)*
