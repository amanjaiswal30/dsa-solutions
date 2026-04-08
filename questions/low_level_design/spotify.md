# Low-Level Design: Spotify (Music Streaming Platform)

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a low-level design for a music streaming platform like Spotify that handles catalog, playback, playlists, recommendations, social features, podcasts, offline listening, and cross-device experience."*

### Clarifying Questions to Ask:

1. **Q:** What scale are we targeting—single-region MVP or global scale?  
   **A:** Design for global scale; assume millions of concurrent streams and a catalog of 100M+ tracks.

2. **Q:** Should we model the full distributed system or focus on client + core domain services?  
   **A:** Emphasize domain model, playback state, recommendation interfaces, and how CDN fits in; acknowledge microservices at boundaries.

3. **Q:** How are subscription tiers modeled?  
   **A:** Free (ad-supported, limited skips), Premium (full quality, offline, no ads), Family (multiple seats under one bill).

4. **Q:** What playback modes must the queue support?  
   **A:** Play next, add to end of queue, shuffle, repeat (off / one / all).

5. **Q:** How deep should recommendations go?  
   **A:** Personalized playlists (Discover Weekly, Daily Mix), collaborative filtering, content-based signals; separate offline batch vs online serving.

6. **Q:** What about DRM and licensing?  
   **A:** Acknowledge encrypted streams and license windows; abstract behind `StreamSession` and rights checks without implementing crypto.

7. **Q:** Collaborative playlists—who can edit?  
   **A:** Owner invites collaborators; collaborators can add/reorder/remove subject to permissions.

8. **Q:** Cross-device behavior?  
   **A:** One active playback session per user (or explicit multi-device rules); handoff transfers playback to another device.

9. **Q:** Podcasts vs music—same pipeline?  
   **A:** Shared catalog abstractions where possible; episode-specific entities and RSS/import for third-party shows.

10. **Q:** Lyrics and analytics for artists?  
    **A:** Lyrics synced or static per track; artist dashboard aggregates streams, listeners, demographics (served by analytics service).

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### User & Subscription Management (FR1-FR8)
1. Users can register, log in, and manage profile (display name, country, email)
2. System should support subscription tiers: FREE, PREMIUM, FAMILY_PRIMARY, FAMILY_MEMBER
3. Family plan owner can invite members; members inherit premium playback benefits with seat limits
4. System should enforce tier-specific limits (e.g., offline device count, max quality, skip limits for free)
5. Users can link multiple devices; each device has a stable device id
6. System should validate active subscription before premium-only actions (offline download, very high quality)
7. Billing state (active, grace, canceled) should gate feature access
8. Users can manage session security (logout all devices, revoke device)

#### Music Catalog (FR9-FR16)
9. Catalog should contain Tracks, Albums, Artists, Genres with stable IDs
10. Track should reference primary artists, featured artists, album, duration, explicit flag, ISRC
11. Album should group tracks with ordering; Artist should have bio, images, verified flag
12. Genre should support hierarchy (parent genre) for browse and recommendations
13. System should support regional availability (track licensed per market)
14. Catalog metadata should be searchable and filterable
15. System should support podcast Shows and Episodes as first-class catalog items
16. Episodes may link to external RSS or be platform-hosted

#### Playlist Management (FR17-FR24)
17. Users can create, rename, delete playlists; playlists have owner and visibility (PUBLIC, PRIVATE, UNLISTED)
18. Users can add, remove, and reorder tracks (and podcast episodes) in playlists
19. Users can share playlists via link; public playlists discoverable in search
20. Collaborative playlists allow invited users to edit track list
21. System should track playlist followers separately from owner
22. System-generated playlists (e.g., Discover Weekly) are read-only to user except hide/save
23. Playlist cover art can be auto-derived or custom image URL
24. Maximum playlist size enforced per product policy

#### Search (FR25-FR29)
25. Users can search across tracks, artists, albums, playlists, podcasts, episodes
26. Search should support type-ahead and full-text queries with ranking
27. Results should respect user market and content availability
28. Search index should be eventually consistent with catalog updates
29. Recent searches and trending queries supported (optional)

#### Playback & Streaming (FR30-FR38)
30. Users can play, pause, seek within a track or episode
31. System should stream audio over HTTPS; client uses adaptive bitrate where applicable
32. Users can select audio quality: LOW, NORMAL, HIGH, VERY_HIGH (tier-gated)
33. Playback should create or resume a `PlaybackSession` with current item, position, device
34. System should issue time-limited stream URLs or tokens after rights check
35. Free tier may insert ads between tracks (modeled as queue items)
36. Crossfade and gapless are optional client features; position sync still server-authoritative for sync
37. Listening history should append completed plays for recommendations and rewind
38. Explicit content filter respects user setting and catalog flags

#### Queue Management (FR39-FR44)
39. Queue holds ordered list of playable items (tracks, episodes, ad slots)
40. User can play next (insert after current) or add to end of queue
41. Shuffle reorders upcoming items; repeat modes: OFF, REPEAT_ONE, REPEAT_ALL
42. Changing context (e.g., play album) can replace queue or append based on UX policy
43. Queue state should sync to server for multi-device resume
44. Local queue overlay for offline-capable items when device is offline

#### Recommendations (FR45-FR52)
45. System should surface personalized playlists (e.g., Discover Weekly, Daily Mix, Release Radar)
46. Recommendation pipeline should combine collaborative filtering, content similarity, and session context
47. Users can thumbs up/down or save recommended items to influence future ranking
48. Radio / "Start Radio" generates infinite queue from seed track or artist
49. New user cold-start should use popularity and genre onboarding selections
50. Recommendations should respect market, explicit filter, and blocked artists
51. Batch jobs compute embeddings or factor models; online service scores candidates in <100ms p99
52. Explainability stub: "Because you listened to X" for interview discussion

#### Social Features (FR53-FR58)
53. Users can follow artists and other users
54. Users can share tracks, albums, playlists to external apps or in-app inbox
55. Public profiles show public playlists and followers count (privacy settings apply)
56. Friend activity feed optional (listening activity visibility)
57. Block and report flows for users and playlists
58. Share tokens or deep links resolve to catalog entities

#### Library Management (FR59-FR63)
59. User library contains saved tracks, saved albums, saved playlists (followed), saved podcasts
60. "Liked songs" is a special system playlist or separate index for fast access
61. Library should support offline flag per item where user has downloaded
62. Sorting and filtering (by artist, recently added) in client with server-backed cursors
63. Remove from library should not delete user-owned playlists

#### Offline & Downloads (FR64-FR69) — Premium
64. Premium users can download tracks/episodes for offline playback
65. Downloads encrypted at rest on device; bound to user + device
66. System should enforce max downloaded devices and periodic license refresh
67. Offline playback uses local files; queue may mix online-only items as skipped or grayed out
68. Download job queue supports pause, resume, priority
69. Revoked subscription removes playable offline keys after grace period

#### Lyrics & Display (FR70-FR73)
70. Tracks may have synchronized lyric lines (time-coded) or static lyrics text
71. Client displays lyrics during playback; karaoke-style highlight optional
72. Lyrics provider attribution and regional availability
73. Instrumental tracks have no lyric payload

#### Cross-Device Sync & Handoff (FR74-FR78)
74. Server stores authoritative `PlaybackSession` (device id, item id, position ms, playing state)
75. Other devices poll or subscribe for updates; UI shows "Continue listening"
76. Handoff: user explicitly moves playback to target device; source stops, target starts at same position
77. Conflict policy: last write wins with debounce or explicit "take over" confirmation
78. Volume and local-only settings stay per device

#### Artist Profiles & Analytics (FR79-FR83)
79. Artist profile aggregates releases, top tracks, playlists featuring artist
80. Verified artists get badge; link-out to merchandise or tickets optional
81. Analytics service aggregates stream counts, listener counts, geographic breakdown (artist-only)
82. Real-time "listeners now" optional via streaming analytics pipeline
83. Analytics not exposed to end listeners in LLD scope (privacy)

#### Content Delivery (FR84-FR88)
84. Media stored in object storage; edge POPs cache segments via CDN
85. Playback requests resolve to nearest healthy CDN node
86. Manifests (HLS/DASH) describe quality rungs mapped to `AudioQuality` enum
87. CDN cache keys include tenant, asset id, quality, encryption key id
88. Origin shield and stale-while-revalidate for viral tracks

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many users, streams, and catalog entities?"
- 400M+ MAU scale class; millions of concurrent playback sessions
- Catalog 100M+ tracks; search and recommendation index sharded
- Write-heavy: listening history, analytics events; read-heavy: catalog browse, CDNs

**Deduced NFRs:**
- ✅ Horizontal scaling of stateless API gateways and playback orchestration
- ✅ Sharded user data, playlist storage, and event ingestion
- ✅ CDN absorbs majority of bytes; origin protected by shield layer
- ✅ Recommendation candidate retrieval from ANN index (FAISS, ScaNN class)

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact vs eventual?"
- Playback position for sync: strong per user session with bounded staleness acceptable for UI
- Playlist edits: strong consistency within a playlist partition (user expects immediate reflection)
- Catalog: eventual consistency across regions; regional rights authoritative in edge
- Recommendations: eventual (batch refresh daily/weekly)

**Deduced NFRs:**
- ✅ **Strong consistency** for subscription entitlement at playback token issue time
- ✅ **Linearizable** playlist mutations per playlist id (single-writer or CRDT discussion)
- ✅ **Eventual** search index and recommendation features
- ✅ **Idempotent** stream token and download license issuance

---

#### 3. **Availability Analysis**

**Think:** "What can degrade?"
- CDN miss or POP failure: fail over to alternate POP or origin
- Recommendation service down: fall back to popularity and editorial playlists
- Lyrics service down: hide lyrics; continue audio

**Deduced NFRs:**
- ✅ **99.99%** for CDN-served media; **99.9%+** for control plane APIs
- ✅ **Graceful degradation:** static playlists, offline mode, cached home feed
- ✅ **Multi-region** active-active for session store with conflict resolution
- ✅ **Circuit breakers** around recommendation and third-party lyrics

---

#### 4. **Maintainability Analysis**

**Think:** "How do teams evolve subsystems?"
- Pluggable recommendation scorers and rerankers
- Feature flags for quality tiers and experiments
- Clear boundaries: Catalog, Social, Playback, Reco, Media, Billing

**Deduced NFRs:**
- ✅ **Structured logging** with trace ids across playback and CDN
- ✅ **Schema versioning** for manifests and client protocols
- ✅ **Dark launches** for new ranking models with shadow traffic

---

#### 5. **Performance Analysis**

**Think:** "Latency budgets?"
- Home feed and search p99 < 200ms excluding client rendering
- Stream URL issuance < 50ms after auth + rights
- Recommendation retrieve + rank < 100ms p99 for online path
- CDN TTFB < 100ms for cached segments in major markets

**Deduced NFRs:**
- ✅ **Caching:** catalog fragments, playlist summaries, entitlements
- ✅ **Precomputed** personalized lists where possible (nightly jobs)
- ✅ **Efficient structures:** inverted indexes for search, heaps for queue operations

---

#### 6. **Security Analysis**

**Think:** "What are the risks?"
- Token theft for stream URLs; playlist privacy leaks; family plan abuse
- DRM keys; GDPR for listening history and exports

**Deduced NFRs:**
- ✅ **Short-lived** signed URLs for segments; bind to session/device
- ✅ **OAuth2 / OIDC** for user auth; scoped tokens for third-party
- ✅ **Encryption** at rest for PII and offline blobs
- ✅ **Rate limits** on search, token minting, and download APIs
- ✅ **Audit** for collaborative playlist edits and subscription changes

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "User, premium, family" | User, Subscription, SubscriptionTier, FamilyGroup |
| "Track, album, artist" | Track, Album, Artist, Genre |
| "Playlist, collaborative" | Playlist, PlaylistCollaborator, PlaylistVisibility |
| "Search" | SearchQuery, SearchIndex, SearchResult |
| "Stream, quality" | PlaybackSession, StreamToken, AudioQuality, MediaAsset |
| "Queue, shuffle, repeat" | PlayQueue, RepeatMode, ShufflePolicy |
| "Discover Weekly" | RecommendationProfile, PersonalizedPlaylist, RankingModel |
| "Follow, share" | FollowGraph, ShareLink |
| "Podcast" | PodcastShow, Episode |
| "Library, download" | UserLibrary, OfflineDownload, Device |
| "Lyrics" | LyricTrack, LyricLine |
| "CDN" | CdnEdge, MediaManifest, Segment |
| "Artist analytics" | StreamEvent, ArtistAnalyticsAggregate |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| SearchQuery | ❌ NO | Value object / request DTO |
| ShufflePolicy | ❌ NO | Behavior on PlayQueue |
| RankingModel | ✅ YES | Versioned model metadata + deployment pointer |
| StreamToken | ✅ YES | Time-bounded credential for CDN |
| MediaManifest | ✅ YES | Describes segments and quality ladder |
| CdnEdge | ❌ NO | Infrastructure; manifest URL encodes routing |

### Final Entity List

**User & Billing:**
1. **User** - Account, profile, settings (explicit filter, quality preference)
2. **Subscription** - Tier, status, renewal, market
3. **FamilyGroup** - Owner, seats, invited members
4. **Device** - Registered client instance for sync and download caps

**Catalog:**
5. **Artist** - Metadata, verification, profile content
6. **Album** - Ordered collection of tracks
7. **Track** - Audio metadata, rights references, explicit flag
8. **Genre** - Hierarchical classification
9. **PodcastShow** - Show-level metadata
10. **Episode** - Audio episode under a show

**Playlists & Library:**
11. **Playlist** - Owner, visibility, collaborative flag, items
12. **PlaylistItem** - Track or episode reference with position
13. **PlaylistCollaborator** - User id + role (EDITOR, VIEWER)
14. **UserLibrary** - Saved/followed edges to catalog entities
15. **OfflineDownload** - Local copy metadata, license expiry, file key

**Playback:**
16. **PlaybackSession** - Current item, position, device, playing state
17. **PlayQueue** - Ordered playable items + repeat/shuffle state
18. **PlayableItem** - Abstract playable (Track, Episode, AdBreak)
19. **StreamToken** - Signed grant for CDN segment fetch
20. **MediaAsset** - Encoded files per quality / codec fingerprint

**Recommendations:**
21. **RecommendationProfile** - User embedding / taste vector references
22. **PersonalizedPlaylist** - Generated list with schedule (e.g., weekly refresh)
23. **RecommendationCandidateSet** - Precomputed neighbors or scores

**Social & Discovery:**
24. **Follow** - User follows Artist or User
25. **ShareLink** - Resolvable token to catalog or playlist

**Media Delivery:**
26. **MediaManifest** - HLS/DASH manifest descriptor per asset
27. **LyricTrack** - Optional timed lyrics for a track

**Analytics:**
28. **ListeningEvent** - Append-only stream event (track id, ms played, context)
29. **ArtistAnalyticsAggregate** - Rollups for dashboard

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Catalog Relationships

#### Album ↔ Track
**Conclusion:** **Composition** (album owns ordered tracks)
```
Album ◆────→ Track [1..*]
```

#### Track ↔ Artist
**Conclusion:** **Association** (many-to-many via join)
```
Track ──────→ Artist [1..*] (primary + featured)
```

#### Genre ↔ Genre
**Conclusion:** **Self-association** (parent/child)
```
Genre ─────→ Genre [0..1] parent
```

#### PodcastShow ↔ Episode
**Conclusion:** **Composition**
```
PodcastShow ◆────→ Episode [1..*]
```

---

### Pass 2: User, Playlist, Library

#### User ↔ Playlist
**Conclusion:** **Association** (ownership)
```
User ─────→ Playlist [0..*] owned
```

#### Playlist ↔ PlaylistItem
**Conclusion:** **Composition**
```
Playlist ◆────→ PlaylistItem [0..*]
```

#### PlaylistItem → Playable target
**Conclusion:** **Association** (polymorphic)
```
PlaylistItem ─────→ Track | Episode
```

#### User ↔ UserLibrary
**Conclusion:** **Composition**
```
User ◆────→ UserLibrary [1]
```

#### User ↔ Subscription / FamilyGroup
**Conclusion:** **Association**
```
User ─────→ Subscription [0..1]
User ─────→ FamilyGroup [0..1] as member or owner
```

---

### Pass 3: Playback, Recommendations, Delivery

#### User ↔ PlaybackSession
**Conclusion:** **Association** (one active session per policy)
```
User ─────→ PlaybackSession [0..1] active
```

#### PlaybackSession ↔ Device
**Conclusion:** **Association**
```
PlaybackSession ─────→ Device [1]
```

#### PlayQueue ↔ PlaybackSession
**Conclusion:** **Composition** (queue belongs to session context)
```
PlaybackSession ◆────→ PlayQueue [1]
```

#### Track ↔ MediaAsset / MediaManifest
**Conclusion:** **Association**
```
Track ─────→ MediaAsset [1..*] (per encoding)
MediaAsset ─────→ MediaManifest [1]
```

#### User ↔ RecommendationProfile
**Conclusion:** **Association**
```
User ─────→ RecommendationProfile [1]
```

#### ListeningEvent → User, Track
**Conclusion:** **Association** (fact table)
```
ListeningEvent ─────→ User, Track | Episode
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Album → Track | 1:N | Composition |
| Track → Artist | N:M | Association |
| User → Playlist | 1:N | Association |
| Playlist → PlaylistItem | 1:N | Composition |
| User → PlaybackSession | 1:1 (active) | Association |
| PlaybackSession → PlayQueue | 1:1 | Composition |
| Track → MediaAsset | 1:N | Association |
| User → OfflineDownload | 1:N | Association |
| User → Device | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>>  │
│ SubscriptionTier │  │  AudioQuality    │  │ PlaylistVisibility│
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ FREE             │  │ LOW              │  │ PRIVATE          │
│ PREMIUM          │  │ NORMAL           │  │ PUBLIC           │
│ FAMILY_PRIMARY   │  │ HIGH             │  │ UNLISTED         │
│ FAMILY_MEMBER    │  │ VERY_HIGH        │  └──────────────────┘
└──────────────────┘  └──────────────────┘

┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>>  │
│ SubscriptionStatus│ │ RepeatMode       │  │ CollaboratorRole│
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ ACTIVE           │  │ OFF              │  │ OWNER            │
│ GRACE            │  │ REPEAT_ONE       │  │ EDITOR           │
│ CANCELED         │  │ REPEAT_ALL       │  │ VIEWER           │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

### Class Diagram 2: User, Subscription, Device

```
┌─────────────────────────────────────────────────────────────┐
│                         User                                │
├─────────────────────────────────────────────────────────────┤
│ - userId: String                                            │
│ - email: String                                             │
│ - displayName: String                                       │
│ - country: String                                           │
│ - explicitFilterEnabled: boolean                            │
│ - preferredQuality: AudioQuality                            │
│ - blockedArtistIds: Set<String>                             │
├─────────────────────────────────────────────────────────────┤
│ + canUseQuality(q: AudioQuality): boolean                   │
│ + canDownloadOffline(): boolean                             │
│ + maxOfflineDevices(): int                                  │
└─────────────────────────────────────────────────────────────┘
        │                              │
        │ 1                            │ 1
        ▼                              ▼
┌──────────────────┐          ┌──────────────────┐
│  Subscription    │          │   UserLibrary    │
├──────────────────┤          ├──────────────────┤
│ - tier: Tier     │          │ - savedTrackIds  │
│ - status: Status │          │ - savedAlbumIds  │
│ - renewsAt: Instant         │ - followedPlaylistIds        │
├──────────────────┤          ├──────────────────┤
│ + isPremium(): boolean      │ + saveTrack(id)  │
│ + inGrace(): boolean        │ + removeTrack(id)│
└──────────────────┘          └──────────────────┘
        │
        │ 0..1
        ▼
┌──────────────────┐
│  FamilyGroup     │
├──────────────────┤
│ - ownerUserId    │
│ - maxSeats: int  │
│ - memberIds: Set │
└──────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                        Device                               │
├─────────────────────────────────────────────────────────────┤
│ - deviceId: String                                          │
│ - userId: String                                            │
│ - name: String                                              │
│ - type: (MOBILE, DESKTOP, WEB, SPEAKER)                     │
│ - lastSeenAt: Instant                                       │
├─────────────────────────────────────────────────────────────┤
│ + isAuthorized(): boolean                                   │
└─────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Catalog

```
┌─────────────────────────────────────────────────────────────┐
│                        Artist                               │
├─────────────────────────────────────────────────────────────┤
│ - artistId: String                                          │
│ - name: String                                              │
│ - verified: boolean                                         │
│ - genres: List<Genre>                                       │
└─────────────────────────────────────────────────────────────┘
        △
        │ implements
        │
┌─────────────────────────────────────────────────────────────┐
│                    CatalogEntity                            │
├─────────────────────────────────────────────────────────────┤
│ + getId(): String                                           │
│ + getMarkets(): Set<String>                                 │
└─────────────────────────────────────────────────────────────┘
        △
        │
┌───────┴────────┬────────────────┬──────────────────────────┐
│                │                │                          │
▼                ▼                ▼                          ▼
┌─────────┐  ┌─────────┐  ┌──────────────┐  ┌──────────────────┐
│  Track  │  │  Album  │  │ PodcastShow  │  │     Episode      │
├─────────┤  ├─────────┤  ├──────────────┤  ├──────────────────┤
│-albumId │  │-title   │  │-rssUrl?      │  │-showId           │
│-duration│  │-artistIds│ │-author       │  │-publishDate      │
│-explicit│  │         │  │              │  │-durationMs       │
│-isrc    │  │         │  │              │  │                  │
└─────────┘  └─────────┘  └──────────────┘  └──────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              <<interface>>                                  │
│                   Playable                                  │
├─────────────────────────────────────────────────────────────┤
│ + getPlayableId(): String                                   │
│ + durationMs(): long                                        │
│ + resolveMediaAsset(quality): MediaAsset                    │
└─────────────────────────────────────────────────────────────┘
        △                       △
        │                       │
     Track                   Episode
```

---

### Class Diagram 4: Playlist & Collaborative

```
┌─────────────────────────────────────────────────────────────┐
│                       Playlist                              │
├─────────────────────────────────────────────────────────────┤
│ - playlistId: String                                        │
│ - ownerUserId: String                                       │
│ - title: String                                             │
│ - visibility: PlaylistVisibility                            │
│ - collaborative: boolean                                    │
│ - description: String                                       │
│ - items: List<PlaylistItem>               ◆────────────┐   │
│ - collaborators: List<PlaylistCollaborator>               │   │
├─────────────────────────────────────────────────────────────┤
│ + addItem(playableId, byUserId): void                       │   │
│ + removeItem(index, byUserId): void                         │   │
│ + canEdit(userId): boolean                                  │   │
└─────────────────────────────────────────────────────────────┘
        │                                    │
        │                                    │
        ▼                                    ▼
┌──────────────────────┐        ┌─────────────────────────────┐
│   PlaylistItem       │        │   PlaylistCollaborator      │
├──────────────────────┤        ├─────────────────────────────┤
│ - position: int      │        │ - userId: String              │
│ - playableId: String │        │ - role: CollaboratorRole      │
│ - addedBy: String    │        │ - invitedAt: Instant          │
│ - addedAt: Instant   │        └─────────────────────────────┘
└──────────────────────┘
```

---

### Class Diagram 5: Playback, Queue, Streaming

```
┌─────────────────────────────────────────────────────────────┐
│                  PlaybackSession                            │
├─────────────────────────────────────────────────────────────┤
│ - sessionId: String                                         │
│ - userId: String                                            │
│ - deviceId: String                                          │
│ - currentItemId: String                                     │
│ - positionMs: long                                          │
│ - playing: boolean                                          │
│ - updatedAt: Instant                                        │
│ - volume: float  // device-local optional mirror            │
├─────────────────────────────────────────────────────────────┤
│ + seek(ms: long): void                                      │
│ + play(): void / pause(): void                              │
│ + handoffTo(deviceId): void                                  │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ 1
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                     PlayQueue                               │
├─────────────────────────────────────────────────────────────┤
│ - items: Deque<String>   // playable ids                    │
│ - currentIndex: int                                         │
│ - shuffleOn: boolean                                        │
│ - repeat: RepeatMode                                        │
│ - shuffleOrder: List<Integer> // perm of upcoming indices   │
├─────────────────────────────────────────────────────────────┤
│ + playNext(playableId): void                                │
│ + enqueue(playableId): void                                 │
│ + next(): String                                            │
│ + toggleShuffle(): void                                     │
│ + setRepeat(mode): void                                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   StreamTokenService                        │
├─────────────────────────────────────────────────────────────┤
│ + issueToken(user, track, quality, device): StreamToken       │
│ + validateToken(token): StreamClaims                        │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                     StreamToken                             │
├─────────────────────────────────────────────────────────────┤
│ - jti: String                                               │
│ - expiresAt: Instant                                        │
│ - cdnBaseUrl: String                                        │
│ - keyId: String                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    MediaManifest                            │
├─────────────────────────────────────────────────────────────┤
│ - assetId: String                                           │
│ - manifestUrl: String                                       │
│ - qualities: Map<AudioQuality, String>                      │
└─────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Recommendations & CDN (Logical)

```
┌─────────────────────────────────────────────────────────────┐
│               RecommendationEngine                          │
├─────────────────────────────────────────────────────────────┤
│ - candidateFetcher: CandidateFetcher                        │
│ - rankers: List<Ranker>                                     │
│ - filters: List<ContentFilter>                              │
├─────────────────────────────────────────────────────────────┤
│ + recommendDailyMix(userId, slot): List<Track>              │
│ + recommendDiscoverWeekly(userId): List<Track>              │
└─────────────────────────────────────────────────────────────┘
            │ uses                    │
            ▼                         ▼
┌──────────────────────┐   ┌──────────────────────────────┐
│ CollaborativeModel   │   │   ContentSimilarityModel       │
│ (user-item factors)  │   │   (audio + metadata embedding) │
└──────────────────────┘   └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    CdnRoutingService                        │
├─────────────────────────────────────────────────────────────┤
│ + selectEdge(userGeo, asn): String                          │
│ + buildSignedUrl(manifest, token): URI                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// SubscriptionTier.java
public enum SubscriptionTier {
    FREE, PREMIUM, FAMILY_PRIMARY, FAMILY_MEMBER;

    public boolean isPremiumPlayback() {
        return this == PREMIUM || this == FAMILY_PRIMARY || this == FAMILY_MEMBER;
    }
}
```

```java
// AudioQuality.java
public enum AudioQuality {
    LOW(96_000),
    NORMAL(160_000),
    HIGH(320_000),
    VERY_HIGH(320_000); // e.g., lossless tier in real product

    private final int approximateBitrateBps;

    AudioQuality(int bps) {
        this.approximateBitrateBps = bps;
    }

    public int getApproximateBitrateBps() {
        return approximateBitrateBps;
    }
}
```

```java
// RepeatMode.java
public enum RepeatMode {
    OFF, REPEAT_ONE, REPEAT_ALL
}
```

```java
// SubscriptionStatus.java
public enum SubscriptionStatus {
    ACTIVE, GRACE, CANCELED
}
```

```java
// CollaboratorRole.java
public enum CollaboratorRole {
    OWNER, EDITOR, VIEWER
}
```

---

### Subscription & Entitlement

```java
// Subscription.java
import java.time.Instant;

public class Subscription {
    private final SubscriptionTier tier;
    private final SubscriptionStatus status;
    private final Instant renewsAt;

    public Subscription(SubscriptionTier tier, SubscriptionStatus status, Instant renewsAt) {
        this.tier = tier;
        this.status = status;
        this.renewsAt = renewsAt;
    }

    public boolean allowsQuality(AudioQuality q) {
        if (!tier.isPremiumPlayback()) {
            return q == AudioQuality.LOW || q == AudioQuality.NORMAL;
        }
        return true;
    }

    public boolean allowsOfflineDownload() {
        return tier.isPremiumPlayback() && status == SubscriptionStatus.ACTIVE;
    }

    public SubscriptionTier getTier() { return tier; }
    public SubscriptionStatus getStatus() { return status; }
}
```

```java
// User.java
import java.util.*;

public class User {
    private final String userId;
    private Subscription subscription;
    private AudioQuality preferredQuality = AudioQuality.HIGH;
    private final Set<String> blockedArtistIds = new HashSet<>();

    public User(String userId) {
        this.userId = userId;
    }

    public boolean canUseQuality(AudioQuality q) {
        return subscription != null && subscription.allowsQuality(q);
    }

    public AudioQuality effectiveQuality() {
        AudioQuality q = preferredQuality;
        if (!canUseQuality(q)) {
            return AudioQuality.NORMAL;
        }
        return q;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getUserId() { return userId; }
    public Set<String> getBlockedArtistIds() { return Collections.unmodifiableSet(blockedArtistIds); }
}
```

---

### PlayQueue

```java
// PlayQueue.java
import java.util.*;

public class PlayQueue {
    private final List<String> items = new ArrayList<>();
    private int currentIndex = 0;
    private boolean shuffleOn = false;
    private RepeatMode repeat = RepeatMode.OFF;
    private final List<Integer> shufflePermutation = new ArrayList<>();

    public void enqueue(String playableId) {
        items.add(playableId);
        rebuildShuffleIfNeeded();
    }

    public void playNext(String playableId) {
        int insertAt = Math.min(currentIndex + 1, items.size());
        items.add(insertAt, playableId);
        rebuildShuffleIfNeeded();
    }

    public void replaceFromPlaylist(List<String> playableIds) {
        items.clear();
        items.addAll(playableIds);
        currentIndex = 0;
        rebuildShuffleIfNeeded();
    }

    public String current() {
        if (items.isEmpty()) return null;
        return items.get(currentIndex);
    }

    public String next() {
        if (items.isEmpty()) return null;
        if (repeat == RepeatMode.REPEAT_ONE) {
            return items.get(currentIndex);
        }
        if (shuffleOn) {
            int pos = indexInShuffle(currentIndex);
            if (pos + 1 < shufflePermutation.size()) {
                currentIndex = shufflePermutation.get(pos + 1);
            } else if (repeat == RepeatMode.REPEAT_ALL) {
                reshuffleUpcoming();
                currentIndex = shufflePermutation.isEmpty() ? currentIndex : shufflePermutation.get(0);
            } else {
                return null;
            }
        } else {
            if (currentIndex + 1 < items.size()) {
                currentIndex++;
            } else if (repeat == RepeatMode.REPEAT_ALL) {
                currentIndex = 0;
            } else {
                return null;
            }
        }
        return items.get(currentIndex);
    }

    public void toggleShuffle() {
        shuffleOn = !shuffleOn;
        rebuildShuffleIfNeeded();
    }

    public void setRepeat(RepeatMode mode) {
        this.repeat = mode;
    }

    private void rebuildShuffleIfNeeded() {
        shufflePermutation.clear();
        if (!shuffleOn) return;
        for (int i = 0; i < items.size(); i++) {
            shufflePermutation.add(i);
        }
        Collections.shuffle(shufflePermutation);
    }

    private void reshuffleUpcoming() {
        rebuildShuffleIfNeeded();
    }

    private int indexInShuffle(int idx) {
        return shufflePermutation.indexOf(idx);
    }

    public List<String> snapshot() {
        return List.copyOf(items);
    }
}
```

---

### PlaybackSession

```java
// PlaybackSession.java
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PlaybackSession {
    private final String sessionId;
    private final String userId;
    private final AtomicLong positionMs = new AtomicLong(0);
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private volatile String deviceId;
    private volatile String currentPlayableId;
    private volatile Instant updatedAt = Instant.now();
    private final PlayQueue queue = new PlayQueue();

    public PlaybackSession(String sessionId, String userId, String deviceId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public synchronized void handoffTo(String newDeviceId) {
        this.deviceId = newDeviceId;
        touch();
    }

    public void seek(long ms) {
        positionMs.set(ms);
        touch();
    }

    public void play() {
        playing.set(true);
        touch();
    }

    public void pause() {
        playing.set(false);
        touch();
    }

    public void advanceTo(String playableId, long startMs) {
        this.currentPlayableId = playableId;
        this.positionMs.set(startMs);
        touch();
    }

    public PlayQueue getQueue() {
        return queue;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public long getPositionMs() { return positionMs.get(); }
    public boolean isPlaying() { return playing.get(); }
    public String getCurrentPlayableId() { return currentPlayableId; }
}
```

---

### Playlist (Collaborative)

```java
// Playlist.java
import java.time.Instant;
import java.util.*;

public class Playlist {
    private final String playlistId;
    private final String ownerUserId;
    private String title;
    private PlaylistVisibility visibility = PlaylistVisibility.PRIVATE;
    private boolean collaborative;
    private final List<PlaylistItem> items = new ArrayList<>();
    private final Map<String, CollaboratorRole> collaborators = new HashMap<>();

    public Playlist(String playlistId, String ownerUserId, String title) {
        this.playlistId = playlistId;
        this.ownerUserId = ownerUserId;
        this.title = title;
        collaborators.put(ownerUserId, CollaboratorRole.OWNER);
    }

    public void addCollaborator(String userId, CollaboratorRole role) {
        if (!collaborative) throw new IllegalStateException("Not collaborative");
        collaborators.put(userId, role);
    }

    public boolean canEdit(String userId) {
        CollaboratorRole r = collaborators.get(userId);
        return r == CollaboratorRole.OWNER || r == CollaboratorRole.EDITOR;
    }

    public synchronized void addItem(String playableId, String byUserId) {
        if (!canEdit(byUserId)) throw new SecurityException("Cannot edit");
        items.add(new PlaylistItem(items.size(), playableId, byUserId, Instant.now()));
    }

    public synchronized void removeItem(int index, String byUserId) {
        if (!canEdit(byUserId)) throw new SecurityException("Cannot edit");
        items.remove(index);
        reindex();
    }

    public void setCollaborative(boolean collaborative) {
        this.collaborative = collaborative;
    }

    private void reindex() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i);
        }
    }

    public List<PlaylistItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
```

```java
// PlaylistItem.java
import java.time.Instant;

public class PlaylistItem {
    private int position;
    private final String playableId;
    private final String addedByUserId;
    private final Instant addedAt;

    public PlaylistItem(int position, String playableId, String addedByUserId, Instant addedAt) {
        this.position = position;
        this.playableId = playableId;
        this.addedByUserId = addedByUserId;
        this.addedAt = addedAt;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPlayableId() { return playableId; }
}
```

---

### Stream Token + CDN Path (Illustrative)

```java
// StreamToken.java
import java.time.Instant;

public class StreamToken {
    private final String jti;
    private final String userId;
    private final String trackId;
    private final AudioQuality quality;
    private final Instant expiresAt;
    private final String cdnHost;

    public StreamToken(String jti, String userId, String trackId,
                       AudioQuality quality, Instant expiresAt, String cdnHost) {
        this.jti = jti;
        this.userId = userId;
        this.trackId = trackId;
        this.quality = quality;
        this.expiresAt = expiresAt;
        this.cdnHost = cdnHost;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getCdnHost() { return cdnHost; }
    public String getTrackId() { return trackId; }
}
```

```java
// StreamTokenService.java
import java.time.*;
import java.util.UUID;

public class StreamTokenService {
    private final RightsService rightsService;
    private final CdnRoutingService cdnRoutingService;

    public StreamTokenService(RightsService rightsService, CdnRoutingService cdnRoutingService) {
        this.rightsService = rightsService;
        this.cdnRoutingService = cdnRoutingService;
    }

    public StreamToken issue(User user, Track track, AudioQuality quality, Device device, String market) {
        if (!user.canUseQuality(quality)) {
            throw new IllegalStateException("Quality not allowed for user tier");
        }
        if (!rightsService.isStreamableInMarket(track, market)) {
            throw new IllegalStateException("Not licensed in market");
        }
        String edge = cdnRoutingService.selectEdge(device);
        Instant exp = Instant.now().plus(Duration.ofMinutes(15));
        return new StreamToken(
                UUID.randomUUID().toString(),
                user.getUserId(),
                track.getId(),
                quality,
                exp,
                edge
        );
    }
}

// RightsService, CdnRoutingService: interfaces in interview
interface RightsService {
    boolean isStreamableInMarket(Track track, String market);
}

interface CdnRoutingService {
    String selectEdge(Device device);
}
```

```java
// Device.java (minimal)
public class Device {
    private final String deviceId;

    public Device(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
```

```java
// Track.java (minimal for streaming path)
public class Track {
    private final String id;
    private final boolean explicit;

    public Track(String id, boolean explicit) {
        this.id = id;
        this.explicit = explicit;
    }

    public String getId() {
        return id;
    }

    public boolean isExplicit() {
        return explicit;
    }
}
```

---

### Recommendation Engine (Collaborative + Rerank)

```java
// RecommendationEngine.java
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private final CollaborativeScorer collabScorer;
    private final ContentSimilarityScorer contentScorer;
    private final PopularityFallback popFallback;

    public RecommendationEngine(CollaborativeScorer collabScorer,
                                ContentSimilarityScorer contentScorer,
                                PopularityFallback popFallback) {
        this.collabScorer = collabScorer;
        this.contentScorer = contentScorer;
        this.popFallback = popFallback;
    }

    /**
     * Online path: retrieve candidates from ANN + light features, rerank.
     */
    public List<String> buildDiscoverWeekly(String userId, UserPreferences prefs, int limit) {
        List<Candidate> candidates = collabScorer.topCandidates(userId, 500);
        if (candidates.isEmpty()) {
            return popFallback.globalChart(prefs.getMarket(), limit);
        }
        for (Candidate c : candidates) {
            double score = collabScorer.score(userId, c.getTrackId()) * 0.6
                    + contentScorer.similarToRecentHistory(userId, c.getTrackId()) * 0.4;
            c.setScore(score);
        }
        return candidates.stream()
                .sorted(Comparator.comparingDouble(Candidate::getScore).reversed())
                .map(Candidate::getTrackId)
                .filter(prefs::allowed)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static class UserPreferences {
        private final String market;
        private final boolean explicitAllowed;
        private final Set<String> blockedArtists;

        public UserPreferences(String market, boolean explicitAllowed, Set<String> blockedArtists) {
            this.market = market;
            this.explicitAllowed = explicitAllowed;
            this.blockedArtists = blockedArtists;
        }

        public boolean allowed(String trackId) {
            // In real system: join track metadata for explicit + blocked artists
            return true;
        }

        public String getMarket() { return market; }
    }

    public static class Candidate {
        private final String trackId;
        private double score;

        public Candidate(String trackId) {
            this.trackId = trackId;
        }

        public String getTrackId() { return trackId; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}

interface CollaborativeScorer {
    List<RecommendationEngine.Candidate> topCandidates(String userId, int k);
    double score(String userId, String trackId);
}

interface ContentSimilarityScorer {
    double similarToRecentHistory(String userId, String trackId);
}

interface PopularityFallback {
    List<String> globalChart(String market, int limit);
}
```

---

### Offline Download

```java
// OfflineDownload.java
import java.time.Instant;

public class OfflineDownload {
    private final String downloadId;
    private final String userId;
    private final String deviceId;
    private final String playableId;
    private final AudioQuality quality;
    private Instant licenseExpiresAt;
    private DownloadState state;

    public OfflineDownload(String downloadId, String userId, String deviceId,
                           String playableId, AudioQuality quality, Instant licenseExpiresAt) {
        this.downloadId = downloadId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.playableId = playableId;
        this.quality = quality;
        this.licenseExpiresAt = licenseExpiresAt;
        this.state = DownloadState.PENDING;
    }

    public void markCompleted() {
        this.state = DownloadState.COMPLETED;
    }

    public boolean isPlayable(Instant now, Subscription sub) {
        return sub.allowsOfflineDownload()
                && state == DownloadState.COMPLETED
                && now.isBefore(licenseExpiresAt);
    }
}

enum DownloadState { PENDING, COMPLETED, FAILED, REVOKED }
```

---

### Demo

```java
// SpotifyLldDemo.java
import java.util.*;

public class SpotifyLldDemo {
    public static void main(String[] args) {
        User user = new User("u1");
        user.setSubscription(new Subscription(SubscriptionTier.PREMIUM, SubscriptionStatus.ACTIVE,
                java.time.Instant.now().plusSeconds(86400)));

        PlaybackSession session = new PlaybackSession("s1", user.getUserId(), "d-phone");
        session.getQueue().replaceFromPlaylist(List.of("t1", "t2", "t3"));
        session.advanceTo(session.getQueue().current(), 0);
        session.play();

        session.getQueue().playNext("t-insert");
        System.out.println("Queue after play-next: " + session.getQueue().snapshot());

        Playlist pl = new Playlist("p1", user.getUserId(), "Road trip");
        pl.setCollaborative(true);
        pl.addCollaborator("friend", CollaboratorRole.EDITOR);
        pl.addItem("t1", user.getUserId());
        pl.addItem("t2", "friend");

        System.out.println("Playlist size: " + pl.getItems().size());
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `Ranker`, `CollaborativeScorer` vs `ContentSimilarityScorer`, CDN selection policies  
**Why:** Swap ranking and routing without changing orchestration  
**Interview Justification:** "Recommendation teams ship models weekly; strategy interfaces let us plug new scorers and shadow-test."

---

### Pattern 2: State Pattern
**Where:** `PlaybackSession` playing/paused; `DownloadState`  
**Why:** Clear transitions for playback and offline license lifecycle  
**Interview Justification:** "Invalid transitions (e.g., issue token after subscription canceled) are blocked by state checks."

---

### Pattern 3: Facade Pattern
**Where:** `StreamTokenService` hiding rights + CDN + signing  
**Why:** Single entry for clients to start playback  
**Interview Justification:** "Clients should not know license rules or edge selection details."

---

### Pattern 4: Observer Pattern
**Where:** Session sync to secondary devices via pub/sub or WebSocket  
**Why:** Decouple playback core from UI on all devices  
**Interview Justification:** "When position updates every few seconds, observers update UI without polling tight loops."

---

### Pattern 5: Proxy Pattern
**Where:** CDN as transparent cache proxy to origin object storage  
**Why:** Scale read path for identical segments  
**Interview Justification:** "Same manifest URL hits edge first; origin only on miss—classic caching proxy."

---

## 💡 Step 8: Interview Discussion Points

### 1. Streaming Architecture: CDN + Signed URLs

**Interviewer:** "Walk through what happens when a user presses play."

**Answer:**
"**Control plane:** Client asks Playback API for a session update and stream entitlement. Server checks subscription, market rights, explicit settings, then calls `StreamTokenService` which returns short-lived credentials and a manifest URL scoped to `AudioQuality`.

**Data plane:** Client player fetches HLS/DASH manifest from CDN hostname chosen by GeoDNS/Anycast. Segments are static objects in object storage; CDN caches by `(assetId, quality, segmentIndex)`. On cache miss, shield tier reduces origin load.

**Security:** URLs carry signed query params or cookies bound to `jti`; keys rotate. Offline uses different packaging (local encrypted files + device-bound keys).

**Failure:** If POP is bad, client retries alternate edge from manifest redirect list; player downgrades quality via ABR."

---

### 2. Recommendation: Collaborative Filtering + Two-Tower Retrieval

**Interviewer:** "How does Discover Weekly scale?"

**Answer:**
"**Offline:** Batch jobs compute user embeddings and track embeddings from implicit feedback (completes, skips, saves). Matrix factorization or neural two-tower models produce vectors.

**Approximate nearest neighbors:** For each user, retrieve top-K neighbor tracks from FAISS/ScaNN index—sub-millisecond per query at scale.

**Online rerank:** Lightweight model scores the 500–2000 candidates using context (time of day, device, recent session). Apply business rules: diversity, artist caps, freshness for Release Radar.

**Cold start:** Fall back to editorial and regional charts; collect onboarding genre picks on first run.

**Privacy:** Aggregate training; optional differential noise for small cohorts in discussion."

---

### 3. Queue + Cross-Device Sync Conflicts

**Interviewer:** "Two devices update the queue—what happens?"

**Answer:**
"**Authoritative session store** keyed by `userId` with version vector or `updatedAt` timestamp.

**Policies:**
- **Last-write-wins** with debounce window (e.g., 2s) to absorb bursty seeks.
- **Explicit take-over:** 'Play on this device' sends `handoffTo` and clears other device's playing flag.

**Queue sync:** Send operations as CRDT-style op log (add, remove, move) for collaborative feel, or periodic full snapshot for simplicity.

**Offline:** Device applies local queue overlay; on reconnect, merge with conflict rules (server wins for subscription, client wins for transient UI state optional)."

---

### 4. Family Plan Abuse & Entitlement

**Interviewer:** "How do you stop family sharing outside household?"

**Answer:**
"**Policy layer:** Periodic address verification hints, GPS fuzzy checks (controversial), device limit, invitation cooldown.

**Technical:** `FamilyGroup` binds seats; each `Device` must refresh entitlement tokens. Anomaly detection on simultaneous streams from distant geos.

**Interview balance:** Acknowledge privacy trade-offs; emphasize rate limits and account-level flags rather than storing exact addresses in LLD."

---

### 5. Podcasts vs Music in One Player

**Interviewer:** "Same queue for podcasts and music?"

**Answer:**
"**Polymorphic `Playable`:** `Track` and `Episode` implement common interface for duration, resume position, skip rules (mid-roll ads for podcasts modeled as queue markers).

**Progress:** Podcast resume uses longer retention; music scrobbles fire at 30s threshold policy.

**CDN:** Same edge stack; different manifests and ad insertion strategy (server-side ad insertion optional)."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `StreamTokenService`: entitlement issuance only
- `PlayQueue`: ordering and repeat/shuffle only
- `RecommendationEngine`: orchestrates retrieval + rank; individual scorers own math

### Open/Closed ✅
```java
public interface Ranker {
    double score(User u, Candidate c);
}
// Add new Ranker without modifying RecommendationEngine core loop
```

### Liskov Substitution ✅
Any `Playable` implementation can sit in `PlaylistItem` and `PlayQueue` without special cases in client contract.

### Interface Segregation ✅
```java
interface OfflineLicenseIssuer { License issue(...); }
interface StreamUrlIssuer { StreamToken issue(...); }
// Premium-only service implements both; free-only client depends on Stream only
```

### Dependency Inversion ✅
```java
public class StreamTokenService {
    private final RightsService rights;
    private final CdnRoutingService cdn;
    // Depends on abstractions for test doubles
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **CDN-first** media delivery with signed, short-lived access and multi-bitrate manifests
- ✅ **Separation** of catalog, playback session, entitlements, and recommendation tiers (batch vs online)
- ✅ **Collaborative filtering + content** signals with ANN retrieval for scale
- ✅ **PlayQueue** with explicit repeat/shuffle semantics; **PlaybackSession** for sync and handoff
- ✅ **Playlist** permissions model for public/private/collaborative sharing
- ✅ **Premium gates** for offline, very high quality, and download device caps

### Streaming & Scale
- ✅ Origin shield, cache keys per quality, regional rights at token time
- ✅ Event pipeline for listening history feeding batch recommendation jobs

### Product Features Covered
- ✅ Subscriptions (free/premium/family), catalog, playlists, search (indexed), playback, queue, reco, social, podcasts, library, offline, lyrics hook, quality settings, cross-device sync, artist analytics (aggregates), collaborative filtering, CDN

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
