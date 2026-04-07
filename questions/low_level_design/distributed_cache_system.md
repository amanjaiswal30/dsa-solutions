# Low-Level Design: Distributed Cache System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a distributed caching system like Redis or Memcached that provides fast in-memory storage with support for multiple data structures, eviction policies, and distributed architecture."*

### Clarifying Questions to Ask:

1. **Q:** What are the core operations we need to support?  
   **A:** GET, PUT, DELETE with O(1) time complexity. Support for expiration (TTL).

2. **Q:** What data structures should we support?  
   **A:** Start with key-value (string), then support lists, sets, sorted sets, hashes.

3. **Q:** What eviction policies are needed?  
   **A:** LRU (Least Recently Used), LFU (Least Frequently Used), FIFO, TTL-based.

4. **Q:** Should it be distributed?  
   **A:** Yes, support horizontal scaling across multiple cache nodes.

5. **Q:** How do we handle cache consistency?  
   **A:** Support cache invalidation, write-through, write-behind strategies.

6. **Q:** What about persistence?  
   **A:** Optional persistence to disk (snapshots, append-only file).

7. **Q:** Should we support pub/sub?  
   **A:** Yes, basic pub/sub for real-time messaging.

8. **Q:** What about replication?  
   **A:** Master-slave replication for high availability.

9. **Q:** What are the scale requirements?  
   **A:** Support millions of keys, handle 100K+ requests/sec per node.

10. **Q:** Should we support transactions?  
    **A:** Basic multi-key operations with atomicity.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Core Cache Operations (FR1-FR5)
1. System should support GET, PUT, DELETE operations in O(1) time
2. Keys should expire automatically based on TTL (Time-To-Live)
3. System should support multiple data types:
   - String (simple key-value)
   - List (ordered collection)
   - Set (unique elements)
   - Sorted Set (sorted by score)
   - Hash (field-value pairs)
4. System should return appropriate error codes:
   - Key not found
   - Memory limit exceeded
   - Invalid data type
5. System should support atomic operations on single keys

#### Eviction Policies (FR6-FR10)
6. When memory is full, system should evict keys based on policy:
   - **LRU** - Least Recently Used
   - **LFU** - Least Frequently Used  
   - **FIFO** - First In First Out
   - **Random** - Random selection
   - **TTL** - Evict expired keys first
7. Eviction should happen automatically when memory threshold reached
8. Users should configure eviction policy per cache instance
9. System should track access frequency and recency
10. Eviction should be fast (< 1ms per operation)

#### Distributed Architecture (FR11-FR15)
11. Cache should be horizontally scalable (add/remove nodes)
12. Keys should be distributed using consistent hashing
13. System should support replication (master-slave)
14. Failover should be automatic when master node fails
15. Clients should discover cache nodes dynamically

#### Advanced Features (FR16-FR22)
16. System should support batch operations (MGET, MSET)
17. Cache should support transactions (MULTI/EXEC)
18. System should provide pub/sub messaging
19. Subscribers should receive messages in real-time
20. Cache should support persistence:
    - RDB snapshots (point-in-time backup)
    - AOF (Append-Only File) for durability
21. System should support cache warming on startup
22. Cache should provide monitoring metrics:
    - Hit rate, miss rate
    - Memory usage
    - Request latency (P50, P95, P99)

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How much data? How many requests?"
- Store 10 million keys (avg 1KB each = 10GB per node)
- Handle 100,000 requests/sec per node
- Support 100+ cache nodes (1TB total cache, 10M req/sec)
- Peak load: 5× average during traffic spikes

**Deduced NFRs:**
- ✅ Horizontal scaling with consistent hashing
- ✅ Memory-efficient data structures (compressed storage)
- ✅ Connection pooling (1000 concurrent connections per node)
- ✅ Partitioning by key (shard across nodes)
- ✅ Load balancer with health checks

---

#### 2. **Consistency Analysis**

**Think:** "What consistency guarantees?"
- Cache is inherently eventually consistent
- Reads should reflect recent writes (within 100ms)
- Replication lag acceptable (< 1 second)
- Some data loss acceptable (cache is not primary storage)

**Deduced NFRs:**
- ✅ **Eventual consistency** for replicas
- ✅ **Synchronous writes** to master (strong consistency on master)
- ✅ **Asynchronous replication** to slaves
- ✅ Read-your-writes consistency (read from master after write)
- ✅ Idempotent operations (duplicate requests safe)

---

#### 3. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Cache downtime degrades performance but doesn't break system
- High availability desired but not critical
- Graceful degradation acceptable

**Deduced NFRs:**
- ✅ **99.9% availability** (8.7 hours downtime/year)
- ✅ Master-slave replication (automatic failover)
- ✅ Health checks every 1 second
- ✅ Automatic slave promotion on master failure
- ✅ Circuit breaker pattern for client libraries
- ✅ Graceful degradation:
  - If cache unavailable → read from database
  - If memory full → aggressive eviction

---

#### 4. **Maintainability Analysis**

**Think:** "How to operate and debug?"
- Need to monitor cache performance
- Need to debug slow operations
- Need to add features without downtime

**Deduced NFRs:**
- ✅ Comprehensive metrics:
  - Request latency (P50, P95, P99)
  - Hit/miss ratio
  - Eviction rate
  - Memory usage
  - Network bandwidth
- ✅ Distributed tracing (trace key through system)
- ✅ Rolling updates (upgrade without downtime)
- ✅ Configuration hot-reload
- ✅ Detailed logging (slow operations, errors)

---

#### 5. **Performance Analysis**

**Think:** "Latency requirements?"
- GET/PUT: < 1ms (P99)
- Batch operations: < 5ms for 100 keys
- Eviction: < 100μs per key
- Replication lag: < 500ms

**Deduced NFRs:**
- ✅ **In-memory storage** (no disk I/O on read path)
- ✅ **Lock-free data structures** where possible
- ✅ **Connection pooling** (avoid connection overhead)
- ✅ **Binary protocol** (more efficient than text)
- ✅ **Zero-copy operations** (minimize memory allocations)
- ✅ **Pipelining** (batch multiple commands)
- ✅ **Non-blocking I/O** (async networking)

---

#### 6. **Security Analysis**

**Think:** "Security risks?"
- Unauthorized access to cache data
- Cache poisoning (malicious data injection)
- DDoS attacks
- Memory exhaustion attacks

**Deduced NFRs:**
- ✅ **Authentication** (password-based or token-based)
- ✅ **Access control** (per-key permissions)
- ✅ **Rate limiting** (per-client request limits)
- ✅ **Memory limits** (max memory per client)
- ✅ **SSL/TLS** for client-server communication
- ✅ **Command blacklisting** (disable dangerous commands)
- ✅ **Network isolation** (cache in private subnet)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "GET, PUT, DELETE" | Cache, Key, Value |
| "TTL expiration" | TTL, ExpirationPolicy |
| "String, List, Set" | DataType, StringValue, ListValue |
| "LRU, LFU eviction" | EvictionPolicy, AccessMetadata |
| "Distributed nodes" | CacheNode, Cluster |
| "Consistent hashing" | HashRing, VirtualNode |
| "Replication" | Master, Slave, ReplicationLog |
| "Pub/sub" | Channel, Subscriber, Message |
| "Snapshots, AOF" | Snapshot, AOFLog |
| "Transactions" | Transaction, Command |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Cache | ✅ YES | Main entity with state and behavior |
| Key | ❌ NO | Simple attribute (String) |
| Value | ✅ YES | Abstract entity with multiple types |
| TTL | ❌ NO | Attribute of cache entry |
| ExpirationPolicy | ✅ YES | Strategy pattern entity |
| DataType | ✅ YES | Enum for supported types |
| StringValue | ✅ YES | Concrete Value implementation |
| ListValue | ✅ YES | Concrete Value implementation |
| EvictionPolicy | ✅ YES | Strategy pattern entity |
| AccessMetadata | ✅ YES | Tracks access patterns |
| CacheNode | ✅ YES | Physical cache instance |
| Cluster | ✅ YES | Collection of nodes with state |
| HashRing | ✅ YES | Consistent hashing implementation |
| VirtualNode | ✅ YES | Logical node on hash ring |
| Master | ❌ NO | Role, not separate entity |
| Slave | ❌ NO | Role, not separate entity |
| ReplicationLog | ✅ YES | Log of write operations |
| Channel | ✅ YES | Pub/sub channel entity |
| Subscriber | ✅ YES | Client subscribed to channel |
| Message | ✅ YES | Pub/sub message entity |
| Snapshot | ✅ YES | Persistence entity |
| AOFLog | ✅ YES | Append-only file entity |
| Transaction | ✅ YES | Multi-command atomic operation |
| Command | ✅ YES | Single cache operation |

### Final Entity List

**Core Cache Entities:**
1. **CacheEntry** - Key-value pair with metadata
2. **CacheValue** - Abstract value (interface)
3. **StringValue** - String data type
4. **ListValue** - List data type
5. **SetValue** - Set data type
6. **SortedSetValue** - Sorted set data type
7. **HashValue** - Hash map data type
8. **DataType** - Enum (STRING, LIST, SET, SORTED_SET, HASH)

**Eviction & Expiration:**
9. **EvictionPolicy** - Interface (LRU, LFU, FIFO, Random)
10. **LRUPolicy** - Least Recently Used implementation
11. **LFUPolicy** - Least Frequently Used implementation
12. **AccessMetadata** - Tracks access time and frequency
13. **ExpirationManager** - Manages TTL-based expiration

**Distributed Architecture:**
14. **CacheNode** - Single cache instance
15. **Cluster** - Collection of cache nodes
16. **HashRing** - Consistent hashing ring
17. **VirtualNode** - Logical node on hash ring
18. **NodeRole** - Enum (MASTER, SLAVE, STANDALONE)

**Replication:**
19. **ReplicationLog** - Log of write operations
20. **ReplicationOffset** - Position in replication log
21. **ReplicationManager** - Handles master-slave sync

**Pub/Sub:**
22. **Channel** - Pub/sub channel
23. **Subscriber** - Subscribed client
24. **Message** - Published message

**Persistence:**
25. **Snapshot** - RDB snapshot
26. **AOFEntry** - Single AOF log entry
27. **PersistenceManager** - Handles persistence

**Client Operations:**
28. **Command** - Single cache operation
29. **Transaction** - Multi-command batch
30. **CommandType** - Enum (GET, SET, DELETE, EXPIRE, etc.)

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Cache Relationships

#### CacheNode ↔ CacheEntry
**Conclusion:** **Composition** (node owns entries)
```
CacheNode ◆────→ CacheEntry [0..*]
```

#### CacheEntry ↔ CacheValue
**Conclusion:** **Composition** (entry owns value)
```
CacheEntry ◆────→ CacheValue [1]
```

#### CacheValue → (StringValue, ListValue, SetValue, etc.)
**Conclusion:** **Inheritance** (polymorphic value types)
```
CacheValue ←──── StringValue
           ←──── ListValue
           ←──── SetValue
           ←──── SortedSetValue
           ←──── HashValue
```

---

### Pass 2: Eviction Relationships

#### CacheNode ↔ EvictionPolicy
**Conclusion:** **Association** (strategy pattern)
```
CacheNode ─────→ EvictionPolicy [1]
```

#### EvictionPolicy → (LRUPolicy, LFUPolicy, etc.)
**Conclusion:** **Inheritance** (strategy implementations)
```
EvictionPolicy ←──── LRUPolicy
               ←──── LFUPolicy
               ←──── FIFOPolicy
```

#### CacheEntry ↔ AccessMetadata
**Conclusion:** **Composition** (entry tracks access)
```
CacheEntry ◆────→ AccessMetadata [1]
```

---

### Pass 3: Distributed Architecture

#### Cluster ↔ CacheNode
**Conclusion:** **Aggregation** (cluster manages nodes)
```
Cluster ◇────→ CacheNode [1..*]
```

#### Cluster ↔ HashRing
**Conclusion:** **Composition** (cluster owns hash ring)
```
Cluster ◆────→ HashRing [1]
```

#### HashRing ↔ VirtualNode
**Conclusion:** **Composition** (ring owns virtual nodes)
```
HashRing ◆────→ VirtualNode [128..*]  // 128 per physical node
```

---

### Pass 4: Replication

#### CacheNode ↔ ReplicationLog
**Conclusion:** **Composition** (master owns log)
```
CacheNode (master) ◆────→ ReplicationLog [1]
```

#### CacheNode ↔ CacheNode (replication)
**Conclusion:** **Association** (master-slave relationship)
```
CacheNode (master) ─────→ CacheNode (slave) [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| CacheNode → CacheEntry | 1:N | Composition |
| CacheEntry → CacheValue | 1:1 | Composition |
| CacheValue → Implementations | 1:1 | Inheritance |
| CacheNode → EvictionPolicy | 1:1 | Association |
| CacheEntry → AccessMetadata | 1:1 | Composition |
| Cluster → CacheNode | 1:N | Aggregation |
| Cluster → HashRing | 1:1 | Composition |
| HashRing → VirtualNode | 1:N | Composition |
| CacheNode → ReplicationLog | 1:1 | Composition |
| Master → Slaves | 1:N | Association |
| Channel → Subscriber | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│    DataType     │  │   CommandType    │  │    NodeRole     │
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ STRING          │  │ GET              │  │ MASTER          │
│ LIST            │  │ SET              │  │ SLAVE           │
│ SET             │  │ DELETE           │  │ STANDALONE      │
│ SORTED_SET      │  │ EXPIRE           │  └─────────────────┘
│ HASH            │  │ TTL              │
└─────────────────┘  │ EXISTS           │  ┌─────────────────┐
                     │ MGET             │  │ <<enumeration>> │
┌─────────────────┐  │ MSET             │  │ EvictionType    │
│ <<enumeration>> │  │ PUBLISH          │  ├─────────────────┤
│ ErrorCode       │  │ SUBSCRIBE        │  │ LRU             │
├─────────────────┤  └──────────────────┘  │ LFU             │
│ SUCCESS         │                        │ FIFO            │
│ KEY_NOT_FOUND   │                        │ RANDOM          │
│ MEMORY_FULL     │                        │ TTL_ONLY        │
│ TYPE_MISMATCH   │                        └─────────────────┘
│ EXPIRED         │
│ INVALID_COMMAND │
└─────────────────┘
```

---

### Class Diagram 2: Cache Entry & Values

```
┌───────────────────────────────────────────────────────────┐
│                     CacheEntry                            │
├───────────────────────────────────────────────────────────┤
│ - key: String                                             │
│ - value: CacheValue                 ◆───────────────┐     │
│ - metadata: AccessMetadata          ◆───────────────┼──┐  │
│ - expiresAt: long (timestamp in ms)                 │  │  │
│ - createdAt: long                                   │  │  │
│ - dataType: DataType                                │  │  │
├───────────────────────────────────────────────────────────┤
│ + CacheEntry(key, value, ttl)                             │
│ + isExpired(): boolean                                    │
│ + getValue(): CacheValue                                  │
│ + updateValue(value: CacheValue): void                    │
│ + touch(): void  // Update access time                    │
│ + getRemainingTTL(): long                                 │
└───────────────────────────────────────────────────────────┘
                    │                              │
                    │                              │
                    ▼                              ▼
┌─────────────────────────────┐    ┌──────────────────────────┐
│   <<interface>>             │    │   AccessMetadata         │
│     CacheValue              │    ├──────────────────────────┤
├─────────────────────────────┤    │ - lastAccessTime: long   │
│ + getType(): DataType       │    │ - accessCount: long      │
│ + serialize(): byte[]       │    │ - frequency: int         │
│ + getSizeInBytes(): long    │    │ - insertionTime: long    │
│ + clone(): CacheValue       │    ├──────────────────────────┤
└─────────────────────────────┘    │ + recordAccess(): void   │
         △                          │ + getScore(policy:       │
         │                          │   EvictionType): double  │
    ┌────┴────┬────────┬────────┐  └──────────────────────────┘
    │         │        │        │
    │         │        │        │
    ▼         ▼        ▼        ▼
┌─────────┐ ┌────────┐ ┌─────────┐ ┌───────────────┐
│ String  │ │  List  │ │   Set   │ │  SortedSet    │
│ Value   │ │ Value  │ │  Value  │ │    Value      │
├─────────┤ ├────────┤ ├─────────┤ ├───────────────┤
│ - data: │ │ - list:│ │ - set:  │ │ - sortedMap:  │
│   String│ │   List │ │   Set   │ │   TreeMap     │
│         │ │   <Str>│ │   <Str> │ │   <Double,Str>│
├─────────┤ ├────────┤ ├─────────┤ ├───────────────┤
│ + get():│ │ + push │ │ + add   │ │ + add(score,  │
│   String│ │ + pop  │ │ + remove│ │       member) │
│ + set(s)│ │ + get  │ │ +contains│ │ + range(min, │
└─────────┘ │ + size │ │ + size  │ │        max)   │
            └────────┘ └─────────┘ │ + rank        │
                                   └───────────────┘

┌──────────────────────────┐
│      HashValue           │
├──────────────────────────┤
│ - fields: Map<String,    │
│           String>        │
├──────────────────────────┤
│ + hset(field, value)     │
│ + hget(field): String    │
│ + hdel(field): boolean   │
│ + hgetall(): Map         │
└──────────────────────────┘
```

---

### Class Diagram 3: Eviction Policies

```
┌───────────────────────────────────────────────────────────┐
│                <<interface>>                              │
│              EvictionPolicy                               │
├───────────────────────────────────────────────────────────┤
│ + evict(cache: Map<String, CacheEntry>,                  │
│         count: int): List<String>                         │
│ + recordAccess(entry: CacheEntry): void                   │
│ + recordInsertion(entry: CacheEntry): void                │
│ + getName(): String                                       │
└───────────────────────────────────────────────────────────┘
                         △
         ┌───────────────┼───────────────┬────────────┐
         │               │               │            │
         │               │               │            │
         ▼               ▼               ▼            ▼
┌──────────────┐  ┌──────────────┐  ┌─────────┐  ┌──────────┐
│  LRUPolicy   │  │  LFUPolicy   │  │  FIFO   │  │  Random  │
├──────────────┤  ├──────────────┤  │  Policy │  │  Policy  │
│ - accessOrder│  │ - freqMap:   │  ├─────────┤  ├──────────┤
│   : LinkedHS │  │   TreeMap    │  │ - queue:│  │ - random:│
│     Map      │  │   <Freq,Set> │  │   Queue │  │   Random │
├──────────────┤  ├──────────────┤  ├─────────┤  ├──────────┤
│ + evict():   │  │ + evict():   │  │ + evict │  │ + evict()│
│   List<Key>  │  │   List<Key>  │  └─────────┘  └──────────┘
│ + record     │  │ + record     │
│   Access()   │  │   Access()   │
└──────────────┘  └──────────────┘

// LRU uses LinkedHashMap for O(1) access + LRU ordering
// LFU uses TreeMap<Frequency, Set<Key>> + HashMap<Key, Frequency>
// FIFO uses simple Queue
// Random uses Random.nextInt()
```

---

### Class Diagram 4: Cache Node

```
┌───────────────────────────────────────────────────────────┐
│                      CacheNode                            │
├───────────────────────────────────────────────────────────┤
│ - nodeId: String                                          │
│ - role: NodeRole                                          │
│ - cache: ConcurrentHashMap<String, CacheEntry>           │
│ - evictionPolicy: EvictionPolicy     ─────────────┐       │
│ - expirationManager: ExpirationManager ────────┐  │       │
│ - replicationLog: ReplicationLog (if master) ──┼──┼──┐    │
│ - slaves: List<CacheNode> (if master)          │  │  │    │
│ - master: CacheNode (if slave)                 │  │  │    │
│ - maxMemory: long                              │  │  │    │
│ - currentMemory: AtomicLong                    │  │  │    │
│ - channels: Map<String, Channel>  (pub/sub)    │  │  │    │
│ - metrics: NodeMetrics                         │  │  │    │
├───────────────────────────────────────────────────────────┤
│ + CacheNode(nodeId, maxMemory, evictionPolicy)           │
│ + get(key: String): CacheValue                            │
│ + set(key: String, value: CacheValue, ttl: long): void    │
│ + delete(key: String): boolean                            │
│ + exists(key: String): boolean                            │
│ + expire(key: String, ttl: long): boolean                 │
│ + mget(keys: List<String>): Map<String, CacheValue>       │
│ + mset(entries: Map<String, CacheValue>): void            │
│ + evictIfNeeded(): void                                   │
│ + cleanExpired(): void                                    │
│ + publish(channel: String, message: Message): void        │
│ + subscribe(channel: String, subscriber: Subscriber)      │
│ + replicate(command: Command): void  // To slaves         │
│ + getMetrics(): NodeMetrics                               │
└───────────────────────────────────────────────────────────┘
         │                    │                  │
         │                    │                  │
         ▼                    ▼                  ▼
┌────────────────┐  ┌──────────────────┐  ┌──────────────┐
│ Expiration     │  │ ReplicationLog   │  │ NodeMetrics  │
│ Manager        │  ├──────────────────┤  ├──────────────┤
├────────────────┤  │ - offset: long   │  │ - hitCount:  │
│ - heap: Min    │  │ - entries: List  │  │   AtomicLong │
│   Heap<Entry>  │  │   <AOFEntry>     │  │ - missCount: │
│ - daemon:      │  │ - maxSize: int   │  │   AtomicLong │
│   Thread       │  ├──────────────────┤  │ - evictions: │
├────────────────┤  │ + append(cmd):   │  │   long       │
│ + schedule     │  │   void           │  │ - memory:    │
│   Expiration() │  │ + getFrom        │  │   long       │
│ + clean        │  │   Offset():List  │  │ - avgLatency │
│   Expired()    │  │ + trim(): void   │  ├──────────────┤
│ + cancel(key)  │  └──────────────────┘  │ + recordHit()│
└────────────────┘                        │ + recordMiss │
                                          │ + getHitRate │
                                          └──────────────┘
```

---

### Class Diagram 5: Distributed Architecture

```
┌───────────────────────────────────────────────────────────┐
│                        Cluster                            │
├───────────────────────────────────────────────────────────┤
│ - clusterId: String                                       │
│ - nodes: List<CacheNode>                                  │
│ - hashRing: HashRing              ◆─────────────┐         │
│ - replicationManager: ReplicationManager ───────┼───┐     │
│ - clusterConfig: ClusterConfig                  │   │     │
├───────────────────────────────────────────────────────────┤
│ + Cluster(config: ClusterConfig)                          │
│ + addNode(node: CacheNode): void                          │
│ + removeNode(nodeId: String): void                        │
│ + get(key: String): CacheValue                            │
│ + set(key: String, value: CacheValue): void               │
│ + getNodeForKey(key: String): CacheNode                   │
│ + rebalance(): void  // After node add/remove             │
│ + handleFailover(failedNode: CacheNode): void             │
└───────────────────────────────────────────────────────────┘
                    │                              │
                    │                              │
                    ▼                              ▼
┌───────────────────────────────┐    ┌──────────────────────┐
│         HashRing              │    │  ReplicationManager  │
├───────────────────────────────┤    ├──────────────────────┤
│ - ring: TreeMap<Long,         │    │ - masters: List<Node>│
│         VirtualNode>          │    │ - topology: Map<     │
│ - virtualNodesPerPhysical:int │    │   Master, List<Slave│
│   (default: 128)              │    ├──────────────────────┤
├───────────────────────────────┤    │ + setupReplication(  │
│ + HashRing(nodes, vnCount)    │    │   master, slaves)    │
│ + addNode(node): void          │    │ + promoteSlaveToM   │
│ + removeNode(node): void       │    │   aster(slave):void │
│ + getNode(key: String):        │    │ + syncSlave(slave): │
│   CacheNode                    │    │   void              │
│ + getReplicaNodes(key, count):│    │ + handleFailover():  │
│   List<CacheNode>              │    │   void              │
└───────────────────────────────┘    └──────────────────────┘
            │
            │ contains
            ▼
┌───────────────────────────────┐
│       VirtualNode             │
├───────────────────────────────┤
│ - hash: long                  │
│ - physicalNode: CacheNode     │
│ - vnodeId: int                │
├───────────────────────────────┤
│ + VirtualNode(node, vnodeId)  │
│ + getHash(): long              │
│ + getPhysicalNode(): CacheNode│
└───────────────────────────────┘

// Hash function: MurmurHash3 or MD5
// Formula: hash(nodeId + ":" + vnodeId)
```

---

### Class Diagram 6: Pub/Sub

```
┌───────────────────────────────────────────────────────────┐
│                       Channel                             │
├───────────────────────────────────────────────────────────┤
│ - name: String                                            │
│ - subscribers: CopyOnWriteArrayList<Subscriber>           │
│ - messageHistory: CircularBuffer<Message> (last 100)      │
├───────────────────────────────────────────────────────────┤
│ + Channel(name)                                           │
│ + subscribe(subscriber: Subscriber): void                 │
│ + unsubscribe(subscriber: Subscriber): void               │
│ + publish(message: Message): void                         │
│ + getSubscriberCount(): int                               │
└───────────────────────────────────────────────────────────┘
            │
            │ contains
            ▼
┌───────────────────────────────────────────────────────────┐
│                     Subscriber                            │
├───────────────────────────────────────────────────────────┤
│ - subscriberId: String                                    │
│ - channels: Set<String>                                   │
│ - callback: Consumer<Message>                             │
│ - lastMessageTime: long                                   │
├───────────────────────────────────────────────────────────┤
│ + Subscriber(id, callback)                                │
│ + onMessage(message: Message): void                       │
│ + isAlive(): boolean  // Heartbeat check                  │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                      Message                              │
├───────────────────────────────────────────────────────────┤
│ - messageId: String                                       │
│ - channel: String                                         │
│ - payload: String                                         │
│ - timestamp: long                                         │
│ - publisherId: String                                     │
├───────────────────────────────────────────────────────────┤
│ + Message(channel, payload, publisherId)                  │
│ + serialize(): byte[]                                     │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 7: Persistence

```
┌───────────────────────────────────────────────────────────┐
│               PersistenceManager                          │
├───────────────────────────────────────────────────────────┤
│ - snapshotScheduler: ScheduledExecutorService             │
│ - aofWriter: BufferedWriter                               │
│ - snapshotPath: String                                    │
│ - aofPath: String                                         │
│ - snapshotInterval: Duration                              │
│ - syncPolicy: SyncPolicy (ALWAYS, EVERYSEC, NO)           │
├───────────────────────────────────────────────────────────┤
│ + PersistenceManager(config)                              │
│ + enableSnapshot(interval: Duration): void                │
│ + saveSnapshot(cache: Map<String, CacheEntry>): void      │
│ + loadSnapshot(): Map<String, CacheEntry>                 │
│ + appendToAOF(command: Command): void                     │
│ + replayAOF(): List<Command>                              │
│ + rewriteAOF(cache: Map): void  // Compact AOF            │
└───────────────────────────────────────────────────────────┘
            │                          │
            │                          │
            ▼                          ▼
┌─────────────────────┐    ┌──────────────────────────┐
│     Snapshot        │    │       AOFEntry           │
├─────────────────────┤    ├──────────────────────────┤
│ - version: int      │    │ - command: CommandType   │
│ - timestamp: long   │    │ - key: String            │
│ - entries: Map      │    │ - value: CacheValue      │
│ - checksum: long    │    │ - ttl: long              │
├─────────────────────┤    │ - timestamp: long        │
│ + save(path): void  │    ├──────────────────────────┤
│ + load(path): Map   │    │ + serialize(): String    │
│ + verify(): boolean │    │ + deserialize(s): AOF    │
└─────────────────────┘    └──────────────────────────┘

// RDB format: Binary snapshot (compressed)
// AOF format: Text log of commands (e.g., "SET key value 3600")
```

---

### Class Diagram 8: Client & Protocol

```
┌───────────────────────────────────────────────────────────┐
│                    CacheClient                            │
├───────────────────────────────────────────────────────────┤
│ - cluster: Cluster                                        │
│ - connectionPool: ConnectionPool                          │
│ - retryPolicy: RetryPolicy                                │
│ - timeout: Duration                                       │
├───────────────────────────────────────────────────────────┤
│ + CacheClient(config: ClientConfig)                       │
│ + get(key: String): Optional<CacheValue>                  │
│ + set(key: String, value: CacheValue, ttl: long): void    │
│ + delete(key: String): boolean                            │
│ + mget(keys: List<String>): Map<String, CacheValue>       │
│ + executeTransaction(commands: List<Command>): List<Resp> │
│ + pipeline(commands: List<Command>): List<Response>       │
│ + subscribe(channel: String, callback: Consumer): void    │
│ + close(): void                                           │
└───────────────────────────────────────────────────────────┘
            │
            │ uses
            ▼
┌───────────────────────────────────────────────────────────┐
│                     Command                               │
├───────────────────────────────────────────────────────────┤
│ - type: CommandType                                       │
│ - key: String                                             │
│ - value: CacheValue                                       │
│ - args: List<String>                                      │
│ - timestamp: long                                         │
├───────────────────────────────────────────────────────────┤
│ + Command(type, key, value, args)                         │
│ + execute(node: CacheNode): Response                      │
│ + serialize(): byte[]  // Binary protocol                 │
│ + deserialize(bytes): Command                             │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                    Response                               │
├───────────────────────────────────────────────────────────┤
│ - status: ErrorCode                                       │
│ - value: CacheValue                                       │
│ - message: String                                         │
│ - latency: long                                           │
├───────────────────────────────────────────────────────────┤
│ + Response(status, value)                                 │
│ + isSuccess(): boolean                                    │
│ + getValue(): Optional<CacheValue>                        │
└───────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### CacheEntry & AccessMetadata

```java
// CacheEntry.java
import java.util.concurrent.atomic.AtomicLong;

public class CacheEntry {
    private final String key;
    private CacheValue value;
    private final AccessMetadata metadata;
    private long expiresAt;  // 0 = no expiration
    private final long createdAt;
    private final DataType dataType;
    private final AtomicLong sizeInBytes;
    
    public CacheEntry(String key, CacheValue value, long ttlMillis) {
        this.key = key;
        this.value = value;
        this.metadata = new AccessMetadata();
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = ttlMillis > 0 ? createdAt + ttlMillis : 0;
        this.dataType = value.getType();
        this.sizeInBytes = new AtomicLong(calculateSize());
    }
    
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
    
    public void touch() {
        metadata.recordAccess();
    }
    
    public void updateValue(CacheValue newValue) {
        if (newValue.getType() != this.dataType) {
            throw new IllegalArgumentException("Type mismatch");
        }
        this.value = newValue;
        this.sizeInBytes.set(calculateSize());
    }
    
    public long getRemainingTTL() {
        if (expiresAt == 0) return -1;  // No expiration
        long remaining = expiresAt - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }
    
    public void updateExpiration(long newTtlMillis) {
        this.expiresAt = newTtlMillis > 0 
            ? System.currentTimeMillis() + newTtlMillis 
            : 0;
    }
    
    private long calculateSize() {
        return key.length() * 2 + value.getSizeInBytes() + 64; // Overhead
    }
    
    public String getKey() { return key; }
    public CacheValue getValue() { return value; }
    public AccessMetadata getMetadata() { return metadata; }
    public long getSizeInBytes() { return sizeInBytes.get(); }
    public DataType getType() { return dataType; }
}
```

```java
// AccessMetadata.java
import java.util.concurrent.atomic.AtomicLong;

public class AccessMetadata {
    private final AtomicLong lastAccessTime;
    private final AtomicLong accessCount;
    private final AtomicLong frequency;  // For LFU
    private final long insertionTime;
    
    public AccessMetadata() {
        long now = System.currentTimeMillis();
        this.lastAccessTime = new AtomicLong(now);
        this.accessCount = new AtomicLong(0);
        this.frequency = new AtomicLong(0);
        this.insertionTime = now;
    }
    
    public void recordAccess() {
        lastAccessTime.set(System.currentTimeMillis());
        accessCount.incrementAndGet();
        frequency.incrementAndGet();
    }
    
    /**
     * Calculate eviction score based on policy
     * Lower score = higher eviction priority
     */
    public double getScore(EvictionType policy) {
        long now = System.currentTimeMillis();
        
        switch (policy) {
            case LRU:
                // Most recently used = highest score
                return (double) lastAccessTime.get();
                
            case LFU:
                // Most frequently used = highest score
                return (double) frequency.get();
                
            case FIFO:
                // First in = lowest score
                return (double) insertionTime;
                
            default:
                return 0.0;
        }
    }
    
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
    
    public long getAccessCount() {
        return accessCount.get();
    }
    
    public long getFrequency() {
        return frequency.get();
    }
    
    public long getIdleTime() {
        return System.currentTimeMillis() - lastAccessTime.get();
    }
}
```

---

### Cache Value Implementations

```java
// CacheValue.java (Interface)
public interface CacheValue {
    DataType getType();
    byte[] serialize();
    long getSizeInBytes();
    CacheValue clone();
}
```

```java
// StringValue.java
import java.nio.charset.StandardCharsets;

public class StringValue implements CacheValue {
    private String data;
    
    public StringValue(String data) {
        this.data = data;
    }
    
    public String get() {
        return data;
    }
    
    public void set(String value) {
        this.data = value;
    }
    
    @Override
    public DataType getType() {
        return DataType.STRING;
    }
    
    @Override
    public byte[] serialize() {
        return data.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public long getSizeInBytes() {
        return data.length() * 2L; // Approx (Java uses UTF-16 internally)
    }
    
    @Override
    public CacheValue clone() {
        return new StringValue(this.data);
    }
}
```

```java
// ListValue.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListValue implements CacheValue {
    private final List<String> list;
    
    public ListValue() {
        this.list = new CopyOnWriteArrayList<>();
    }
    
    public void lpush(String value) {
        list.add(0, value);
    }
    
    public void rpush(String value) {
        list.add(value);
    }
    
    public String lpop() {
        return list.isEmpty() ? null : list.remove(0);
    }
    
    public String rpop() {
        return list.isEmpty() ? null : list.remove(list.size() - 1);
    }
    
    public String lindex(int index) {
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }
    
    public List<String> lrange(int start, int stop) {
        if (start < 0) start = 0;
        if (stop >= list.size()) stop = list.size() - 1;
        if (start > stop) return Collections.emptyList();
        return new ArrayList<>(list.subList(start, stop + 1));
    }
    
    public int llen() {
        return list.size();
    }
    
    @Override
    public DataType getType() {
        return DataType.LIST;
    }
    
    @Override
    public byte[] serialize() {
        return String.join(",", list).getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public long getSizeInBytes() {
        return list.stream()
            .mapToLong(s -> s.length() * 2L)
            .sum() + list.size() * 8L; // List overhead
    }
    
    @Override
    public CacheValue clone() {
        ListValue copy = new ListValue();
        copy.list.addAll(this.list);
        return copy;
    }
}
```

```java
// SetValue.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SetValue implements CacheValue {
    private final Set<String> set;
    
    public SetValue() {
        this.set = ConcurrentHashMap.newKeySet();
    }
    
    public boolean sadd(String member) {
        return set.add(member);
    }
    
    public boolean srem(String member) {
        return set.remove(member);
    }
    
    public boolean sismember(String member) {
        return set.contains(member);
    }
    
    public Set<String> smembers() {
        return new HashSet<>(set);
    }
    
    public int scard() {
        return set.size();
    }
    
    public Set<String> sinter(SetValue other) {
        Set<String> result = new HashSet<>(this.set);
        result.retainAll(other.set);
        return result;
    }
    
    public Set<String> sunion(SetValue other) {
        Set<String> result = new HashSet<>(this.set);
        result.addAll(other.set);
        return result;
    }
    
    @Override
    public DataType getType() {
        return DataType.SET;
    }
    
    @Override
    public byte[] serialize() {
        return String.join(",", set).getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public long getSizeInBytes() {
        return set.stream()
            .mapToLong(s -> s.length() * 2L)
            .sum() + set.size() * 16L; // Set overhead
    }
    
    @Override
    public CacheValue clone() {
        SetValue copy = new SetValue();
        copy.set.addAll(this.set);
        return copy;
    }
}
```

---

### Eviction Policies

```java
// EvictionPolicy.java (Interface)
import java.util.*;

public interface EvictionPolicy {
    /**
     * Evict entries from cache
     * @param cache The cache map
     * @param count Number of entries to evict
     * @return List of evicted keys
     */
    List<String> evict(Map<String, CacheEntry> cache, int count);
    
    /**
     * Record access for eviction tracking
     */
    void recordAccess(CacheEntry entry);
    
    /**
     * Record insertion for eviction tracking
     */
    void recordInsertion(CacheEntry entry);
    
    String getName();
}
```

```java
// LRUPolicy.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LRUPolicy implements EvictionPolicy {
    
    @Override
    public List<String> evict(Map<String, CacheEntry> cache, int count) {
        // Sort by last access time (ascending)
        return cache.values().stream()
            .sorted(Comparator.comparingLong(
                e -> e.getMetadata().getLastAccessTime()
            ))
            .limit(count)
            .map(CacheEntry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public void recordAccess(CacheEntry entry) {
        entry.getMetadata().recordAccess();
    }
    
    @Override
    public void recordInsertion(CacheEntry entry) {
        // No special action needed for LRU
    }
    
    @Override
    public String getName() {
        return "LRU";
    }
}
```

```java
// LFUPolicy.java
import java.util.*;
import java.util.stream.Collectors;

public class LFUPolicy implements EvictionPolicy {
    
    @Override
    public List<String> evict(Map<String, CacheEntry> cache, int count) {
        // Sort by frequency (ascending), then by last access time
        return cache.values().stream()
            .sorted(Comparator
                .comparingLong((CacheEntry e) -> e.getMetadata().getFrequency())
                .thenComparingLong(e -> e.getMetadata().getLastAccessTime())
            )
            .limit(count)
            .map(CacheEntry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public void recordAccess(CacheEntry entry) {
        entry.getMetadata().recordAccess();
    }
    
    @Override
    public void recordInsertion(CacheEntry entry) {
        // No special action needed
    }
    
    @Override
    public String getName() {
        return "LFU";
    }
}
```

---

### Cache Node Implementation

```java
// CacheNode.java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CacheNode {
    private final String nodeId;
    private NodeRole role;
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final EvictionPolicy evictionPolicy;
    private final ExpirationManager expirationManager;
    private final long maxMemory;
    private final AtomicLong currentMemory;
    private final double evictionThreshold = 0.95; // Evict at 95% memory
    
    // Replication (if master)
    private final ReplicationLog replicationLog;
    private final List<CacheNode> slaves;
    
    // Replication (if slave)
    private CacheNode master;
    
    // Pub/Sub
    private final ConcurrentHashMap<String, Channel> channels;
    
    // Metrics
    private final NodeMetrics metrics;
    
    public CacheNode(String nodeId, long maxMemory, EvictionPolicy policy) {
        this.nodeId = nodeId;
        this.role = NodeRole.STANDALONE;
        this.cache = new ConcurrentHashMap<>();
        this.evictionPolicy = policy;
        this.expirationManager = new ExpirationManager(this);
        this.maxMemory = maxMemory;
        this.currentMemory = new AtomicLong(0);
        this.replicationLog = new ReplicationLog();
        this.slaves = new CopyOnWriteArrayList<>();
        this.channels = new ConcurrentHashMap<>();
        this.metrics = new NodeMetrics();
        
        // Start expiration cleanup thread
        expirationManager.start();
    }
    
    // Core Operations
    
    public Optional<CacheValue> get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            metrics.recordMiss();
            return Optional.empty();
        }
        
        if (entry.isExpired()) {
            delete(key);
            metrics.recordMiss();
            return Optional.empty();
        }
        
        // Update access metadata
        entry.touch();
        evictionPolicy.recordAccess(entry);
        
        metrics.recordHit();
        return Optional.of(entry.getValue());
    }
    
    public void set(String key, CacheValue value, long ttlMillis) {
        // Check memory before insertion
        evictIfNeeded();
        
        CacheEntry entry = new CacheEntry(key, value, ttlMillis);
        CacheEntry oldEntry = cache.put(key, entry);
        
        // Update memory usage
        if (oldEntry != null) {
            currentMemory.addAndGet(-oldEntry.getSizeInBytes());
        }
        currentMemory.addAndGet(entry.getSizeInBytes());
        
        // Track for eviction
        evictionPolicy.recordInsertion(entry);
        
        // Schedule expiration
        if (ttlMillis > 0) {
            expirationManager.scheduleExpiration(key, ttlMillis);
        }
        
        // Replicate to slaves
        if (role == NodeRole.MASTER) {
            Command cmd = new Command(CommandType.SET, key, value, ttlMillis);
            replicate(cmd);
        }
    }
    
    public boolean delete(String key) {
        CacheEntry entry = cache.remove(key);
        
        if (entry != null) {
            currentMemory.addAndGet(-entry.getSizeInBytes());
            expirationManager.cancelExpiration(key);
            
            // Replicate to slaves
            if (role == NodeRole.MASTER) {
                Command cmd = new Command(CommandType.DELETE, key, null, 0);
                replicate(cmd);
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean exists(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return false;
        
        if (entry.isExpired()) {
            delete(key);
            return false;
        }
        
        return true;
    }
    
    public boolean expire(String key, long ttlMillis) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            return false;
        }
        
        entry.updateExpiration(ttlMillis);
        expirationManager.scheduleExpiration(key, ttlMillis);
        
        return true;
    }
    
    // Batch Operations
    
    public Map<String, CacheValue> mget(List<String> keys) {
        Map<String, CacheValue> result = new HashMap<>();
        
        for (String key : keys) {
            get(key).ifPresent(value -> result.put(key, value));
        }
        
        return result;
    }
    
    public void mset(Map<String, CacheValue> entries, long ttlMillis) {
        for (Map.Entry<String, CacheValue> entry : entries.entrySet()) {
            set(entry.getKey(), entry.getValue(), ttlMillis);
        }
    }
    
    // Eviction
    
    private void evictIfNeeded() {
        double memoryUsage = (double) currentMemory.get() / maxMemory;
        
        if (memoryUsage >= evictionThreshold) {
            // Evict 10% of keys
            int evictCount = Math.max(1, cache.size() / 10);
            
            List<String> keysToEvict = evictionPolicy.evict(cache, evictCount);
            
            for (String key : keysToEvict) {
                delete(key);
                metrics.recordEviction();
            }
        }
    }
    
    public void cleanExpired() {
        // Called periodically by ExpirationManager
        List<String> expiredKeys = new ArrayList<>();
        
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredKeys.add(entry.getKey());
            }
        }
        
        for (String key : expiredKeys) {
            delete(key);
        }
    }
    
    // Replication
    
    private void replicate(Command command) {
        replicationLog.append(command);
        
        for (CacheNode slave : slaves) {
            // Async replication to slaves
            CompletableFuture.runAsync(() -> {
                slave.applyCommand(command);
            });
        }
    }
    
    public void applyCommand(Command command) {
        // Slave applies command from master
        switch (command.getType()) {
            case SET:
                set(command.getKey(), command.getValue(), command.getTtl());
                break;
            case DELETE:
                delete(command.getKey());
                break;
            case EXPIRE:
                expire(command.getKey(), command.getTtl());
                break;
            default:
                // Ignore read commands
        }
    }
    
    public void addSlave(CacheNode slave) {
        slave.setMaster(this);
        slave.setRole(NodeRole.SLAVE);
        this.slaves.add(slave);
        
        // Full sync: copy all data to slave
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            slave.set(entry.getKey(), entry.getValue().getValue(), 
                     entry.getValue().getRemainingTTL());
        }
    }
    
    private void setMaster(CacheNode master) {
        this.master = master;
    }
    
    private void setRole(NodeRole role) {
        this.role = role;
    }
    
    // Pub/Sub
    
    public void publish(String channelName, Message message) {
        Channel channel = channels.computeIfAbsent(channelName, Channel::new);
        channel.publish(message);
    }
    
    public void subscribe(String channelName, Subscriber subscriber) {
        Channel channel = channels.computeIfAbsent(channelName, Channel::new);
        channel.subscribe(subscriber);
    }
    
    public void unsubscribe(String channelName, Subscriber subscriber) {
        Channel channel = channels.get(channelName);
        if (channel != null) {
            channel.unsubscribe(subscriber);
        }
    }
    
    // Getters
    
    public String getNodeId() {
        return nodeId;
    }
    
    public NodeRole getRole() {
        return role;
    }
    
    public NodeMetrics getMetrics() {
        return metrics;
    }
    
    public long getCurrentMemory() {
        return currentMemory.get();
    }
    
    public long getMaxMemory() {
        return maxMemory;
    }
    
    public int getKeyCount() {
        return cache.size();
    }
}
```

---

### Expiration Manager

```java
// ExpirationManager.java
import java.util.*;
import java.util.concurrent.*;

public class ExpirationManager {
    private final CacheNode node;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> expirationTasks;
    
    public ExpirationManager(CacheNode node) {
        this.node = node;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.expirationTasks = new ConcurrentHashMap<>();
    }
    
    public void start() {
        // Periodic cleanup of expired keys (every 1 second)
        scheduler.scheduleAtFixedRate(
            () -> node.cleanExpired(),
            1, 1, TimeUnit.SECONDS
        );
    }
    
    public void scheduleExpiration(String key, long ttlMillis) {
        // Cancel existing expiration task
        cancelExpiration(key);
        
        // Schedule new expiration
        ScheduledFuture<?> future = scheduler.schedule(
            () -> node.delete(key),
            ttlMillis,
            TimeUnit.MILLISECONDS
        );
        
        expirationTasks.put(key, future);
    }
    
    public void cancelExpiration(String key) {
        ScheduledFuture<?> future = expirationTasks.remove(key);
        if (future != null) {
            future.cancel(false);
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}
```

---

### Distributed Cluster with Consistent Hashing

```java
// Cluster.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cluster {
    private final String clusterId;
    private final List<CacheNode> nodes;
    private final HashRing hashRing;
    private final ReplicationManager replicationManager;
    
    public Cluster(String clusterId) {
        this.clusterId = clusterId;
        this.nodes = new CopyOnWriteArrayList<>();
        this.hashRing = new HashRing(128); // 128 virtual nodes per physical node
        this.replicationManager = new ReplicationManager();
    }
    
    public void addNode(CacheNode node) {
        nodes.add(node);
        hashRing.addNode(node);
    }
    
    public void removeNode(String nodeId) {
        CacheNode node = nodes.stream()
            .filter(n -> n.getNodeId().equals(nodeId))
            .findFirst()
            .orElse(null);
        
        if (node != null) {
            nodes.remove(node);
            hashRing.removeNode(node);
            
            // Trigger rebalancing (keys from removed node migrate to others)
            rebalance();
        }
    }
    
    public Optional<CacheValue> get(String key) {
        CacheNode node = hashRing.getNode(key);
        return node.get(key);
    }
    
    public void set(String key, CacheValue value, long ttlMillis) {
        CacheNode node = hashRing.getNode(key);
        node.set(key, value, ttlMillis);
    }
    
    public boolean delete(String key) {
        CacheNode node = hashRing.getNode(key);
        return node.delete(key);
    }
    
    public CacheNode getNodeForKey(String key) {
        return hashRing.getNode(key);
    }
    
    private void rebalance() {
        // After node add/remove, some keys may need to move
        // This is handled automatically by consistent hashing
        // (keys map to new nodes based on updated hash ring)
    }
    
    public void setupReplication(int replicationFactor) {
        // For each key, replicate to N nodes
        // Use hash ring to find replica nodes
        
        for (CacheNode master : nodes) {
            List<CacheNode> replicas = hashRing.getReplicaNodes(
                master.getNodeId(), 
                replicationFactor - 1
            );
            
            for (CacheNode replica : replicas) {
                master.addSlave(replica);
            }
        }
    }
    
    public void handleFailover(CacheNode failedNode) {
        // Promote one of the slaves to master
        replicationManager.promoteSlaveToMaster(failedNode);
    }
}
```

```java
// HashRing.java
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class HashRing {
    private final TreeMap<Long, VirtualNode> ring;
    private final int virtualNodesPerPhysical;
    
    public HashRing(int virtualNodesPerPhysical) {
        this.ring = new TreeMap<>();
        this.virtualNodesPerPhysical = virtualNodesPerPhysical;
    }
    
    public void addNode(CacheNode node) {
        // Add virtual nodes to ring
        for (int i = 0; i < virtualNodesPerPhysical; i++) {
            String vnodeKey = node.getNodeId() + ":" + i;
            long hash = hash(vnodeKey);
            ring.put(hash, new VirtualNode(node, i));
        }
    }
    
    public void removeNode(CacheNode node) {
        // Remove virtual nodes from ring
        for (int i = 0; i < virtualNodesPerPhysical; i++) {
            String vnodeKey = node.getNodeId() + ":" + i;
            long hash = hash(vnodeKey);
            ring.remove(hash);
        }
    }
    
    public CacheNode getNode(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No nodes in cluster");
        }
        
        long hash = hash(key);
        
        // Find first node with hash >= key hash (clockwise on ring)
        Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);
        
        if (entry == null) {
            // Wrap around to first node
            entry = ring.firstEntry();
        }
        
        return entry.getValue().getPhysicalNode();
    }
    
    public List<CacheNode> getReplicaNodes(String key, int count) {
        // Get next N nodes clockwise on ring (for replication)
        if (ring.isEmpty()) {
            return Collections.emptyList();
        }
        
        long hash = hash(key);
        Set<CacheNode> replicas = new LinkedHashSet<>();
        
        // Start from current position
        Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        
        // Traverse ring clockwise
        Iterator<Map.Entry<Long, VirtualNode>> iterator = 
            ring.tailMap(entry.getKey(), true).entrySet().iterator();
        
        while (replicas.size() < count && iterator.hasNext()) {
            replicas.add(iterator.next().getValue().getPhysicalNode());
            
            // Wrap around if needed
            if (!iterator.hasNext() && replicas.size() < count) {
                iterator = ring.entrySet().iterator();
            }
        }
        
        return new ArrayList<>(replicas);
    }
    
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            
            // Use first 8 bytes as long
            long hash = 0;
            for (int i = 0; i < 8; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }
            
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Hash function failed", e);
        }
    }
}
```

```java
// VirtualNode.java
public class VirtualNode {
    private final CacheNode physicalNode;
    private final int vnodeId;
    
    public VirtualNode(CacheNode physicalNode, int vnodeId) {
        this.physicalNode = physicalNode;
        this.vnodeId = vnodeId;
    }
    
    public CacheNode getPhysicalNode() {
        return physicalNode;
    }
    
    public int getVnodeId() {
        return vnodeId;
    }
}
```

---

### Client & Demo

```java
// CacheClient.java
import java.util.*;

public class CacheClient {
    private final Cluster cluster;
    
    public CacheClient(Cluster cluster) {
        this.cluster = cluster;
    }
    
    public Optional<String> get(String key) {
        Optional<CacheValue> value = cluster.get(key);
        
        if (value.isPresent() && value.get() instanceof StringValue) {
            return Optional.of(((StringValue) value.get()).get());
        }
        
        return Optional.empty();
    }
    
    public void set(String key, String value, long ttlSeconds) {
        cluster.set(key, new StringValue(value), ttlSeconds * 1000);
    }
    
    public void set(String key, String value) {
        set(key, value, 0); // No expiration
    }
    
    public boolean delete(String key) {
        return cluster.delete(key);
    }
    
    public Map<String, String> mget(List<String> keys) {
        Map<String, String> result = new HashMap<>();
        
        for (String key : keys) {
            get(key).ifPresent(value -> result.put(key, value));
        }
        
        return result;
    }
}
```

```java
// DistributedCacheDemo.java
public class DistributedCacheDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Distributed Cache System Demo ===\n");
        
        // Create cluster with 3 nodes
        Cluster cluster = new Cluster("cluster-1");
        
        CacheNode node1 = new CacheNode("node-1", 10 * 1024 * 1024, new LRUPolicy());
        CacheNode node2 = new CacheNode("node-2", 10 * 1024 * 1024, new LRUPolicy());
        CacheNode node3 = new CacheNode("node-3", 10 * 1024 * 1024, new LFUPolicy());
        
        cluster.addNode(node1);
        cluster.addNode(node2);
        cluster.addNode(node3);
        
        // Setup replication (each key replicated to 2 nodes)
        cluster.setupReplication(2);
        
        System.out.println("Cluster initialized with 3 nodes");
        System.out.println("Replication factor: 2\n");
        
        // Create client
        CacheClient client = new CacheClient(cluster);
        
        // Demo 1: Basic SET/GET
        System.out.println("--- Demo 1: Basic Operations ---");
        client.set("user:1001", "Alice");
        client.set("user:1002", "Bob");
        client.set("user:1003", "Charlie");
        
        System.out.println("GET user:1001 = " + client.get("user:1001").orElse("null"));
        System.out.println("GET user:1002 = " + client.get("user:1002").orElse("null"));
        
        CacheNode nodeForUser1001 = cluster.getNodeForKey("user:1001");
        System.out.println("user:1001 stored on: " + nodeForUser1001.getNodeId());
        
        // Demo 2: TTL Expiration
        System.out.println("\n--- Demo 2: TTL Expiration ---");
        client.set("session:abc123", "temp-token", 2); // 2 seconds TTL
        System.out.println("SET session with 2s TTL");
        System.out.println("GET session (immediate) = " + 
                         client.get("session:abc123").orElse("null"));
        
        Thread.sleep(2500);
        System.out.println("GET session (after 2.5s) = " + 
                         client.get("session:abc123").orElse("null"));
        
        // Demo 3: Batch Operations
        System.out.println("\n--- Demo 3: Batch Operations ---");
        List<String> keys = Arrays.asList("user:1001", "user:1002", "user:1003");
        Map<String, String> results = client.mget(keys);
        System.out.println("MGET results: " + results);
        
        // Demo 4: Metrics
        System.out.println("\n--- Demo 4: Node Metrics ---");
        for (int i = 0; i < 100; i++) {
            client.get("user:" + (i % 3 + 1001));
        }
        
        System.out.println("Node 1 Metrics:");
        System.out.println("  Hit Rate: " + 
                         String.format("%.2f%%", node1.getMetrics().getHitRate() * 100));
        System.out.println("  Keys: " + node1.getKeyCount());
        System.out.println("  Memory: " + node1.getCurrentMemory() + " bytes");
        
        System.out.println("\n=== Demo Complete ===");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** Eviction policies (LRU, LFU, FIFO, Random)  
**Why:** Pluggable eviction algorithms  
**Interview Justification:** "Different use cases need different eviction strategies. Strategy pattern allows swapping policies without changing cache logic."

---

### Pattern 2: Consistent Hashing
**Where:** Key distribution across nodes (`HashRing`)  
**Why:** Minimize key redistribution on node add/remove  
**Interview Justification:** "With naive hashing (hash % N), adding/removing node moves ~50% of keys. Consistent hashing moves only K/N keys (where K = total keys, N = nodes)."

---

### Pattern 3: Observer Pattern
**Where:** Pub/Sub (`Channel`, `Subscriber`)  
**Why:** Decouple publishers from subscribers  
**Interview Justification:** "Pub/sub enables real-time messaging. Observer pattern allows multiple subscribers to react to published messages independently."

---

### Pattern 4: Singleton Pattern
**Where:** `Cluster`, `ExpirationManager`  
**Why:** Single instance manages global state  
**Interview Justification:** "Cluster coordinates all nodes. Singleton ensures single source of truth."

---

### Pattern 5: Template Method Pattern
**Where:** `CacheValue` implementations  
**Why:** Define common structure with type-specific behavior  
**Interview Justification:** "All value types share common operations (serialize, size). Template method defines structure while allowing type-specific implementations."

---

## 💡 Step 8: Interview Discussion Points

### 1. Eviction Algorithm: LRU vs LFU

**Interviewer:** "When would you use LRU vs LFU?"

**Answer:**
"**Trade-offs:**

**LRU (Least Recently Used):**
- **Best for:** Temporal locality (recently accessed data likely accessed again)
- **Example:** User sessions, news feeds, recent orders
- **Pros:** Simple, works for most cases
- **Cons:** One-time burst access pollutes cache

**LFU (Least Frequently Used):**
- **Best for:** Data with consistent access patterns
- **Example:** Popular products, hot config, trending videos
- **Pros:** Keeps genuinely popular items
- **Cons:** Slow to adapt (old popular item stays even if not accessed recently)

**Real-world example (YouTube):**
```
Video views over time:
- Video A: 1000 views in hour 1, then 0 views
- Video B: 10 views/hour consistently

LRU: After some time, Video A evicted (not accessed recently) ✅
LFU: Video A stays (high total frequency), Video B evicted ❌

Result: LRU better for trending content with time decay
```

**Hybrid approach (ARC - Adaptive Replacement Cache):**
```java
// Maintain two LRU lists:
// L1: Accessed once (recent)
// L2: Accessed multiple times (frequent)

// Dynamically balance between L1 and L2 based on hit rates
if (hitRateL1 < hitRateL2) {
    increaseL2Size();  // Favor frequency
} else {
    increaseL1Size();  // Favor recency
}
```

**My recommendation:** Use LRU by default, switch to LFU for predictable workloads."

---

### 2. Consistent Hashing: Why Virtual Nodes?

**Interviewer:** "Why use virtual nodes instead of hashing nodes directly?"

**Answer:**
"**Problem without virtual nodes:**

Imagine 3 physical nodes (A, B, C) on hash ring:
```
Hash ring: [0 ... A ... B ... C ... 2^64]
```

If node A fails, ALL of A's keys move to B (next clockwise node).
Result: **Hot spot** on B, uneven load distribution.

**Solution: Virtual Nodes (VNodes)**

Each physical node maps to multiple points on ring:
```
Physical node A → A1, A2, A3, ..., A128 (128 virtual nodes)
Physical node B → B1, B2, B3, ..., B128
Physical node C → C1, C2, C3, ..., C128
```

Hash ring:
```
[A1, C3, B2, A4, B7, C1, ..., A128]  // Interspersed
```

**Benefits:**
1. **Load balancing:** Each node gets ~equal keys (uniform distribution)
2. **Smooth failover:** When A fails, its keys distributed across B and C
3. **Scalability:** Adding node D takes keys evenly from A, B, C

**Math:**
- Without vnodes: 1 node failure → 1 node takes all load (100% increase)
- With vnodes: 1 node failure → N-1 nodes share load (~1/(N-1) increase each)

**Implementation:**
```java
// 128 virtual nodes per physical node
for (int i = 0; i < 128; i++) {
    String vnodeKey = nodeId + \":\" + i;
    long hash = md5(vnodeKey);
    ring.put(hash, physicalNode);
}
```

**Trade-off:** More vnodes = better distribution, but slower lookups (larger TreeMap)."

---

### 3. Replication: Synchronous vs Asynchronous

**Interviewer:** "Should we replicate synchronously or asynchronously?"

**Answer:**
"**Trade-offs:**

**Synchronous Replication (Write to master + all slaves before ACK):**
```java
public void set(String key, CacheValue value) {
    // Write to master
    masterNode.set(key, value);
    
    // Write to all slaves (block until complete)
    for (CacheNode slave : slaves) {
        slave.set(key, value);  // BLOCKING
    }
    
    // Return success only after all writes complete
}
```

**Pros:**
- ✅ Strong consistency (all replicas have same data)
- ✅ No data loss on master failure

**Cons:**
- ❌ High latency (wait for slowest slave)
- ❌ Reduced availability (write fails if any slave down)

**Asynchronous Replication (Write to master, replicate in background):**
```java
public void set(String key, CacheValue value) {
    // Write to master
    masterNode.set(key, value);
    
    // Replicate to slaves async
    CompletableFuture.runAsync(() -> {
        for (CacheNode slave : slaves) {
            slave.set(key, value);  // NON-BLOCKING
        }
    });
    
    // Return success immediately
}
```

**Pros:**
- ✅ Low latency (only master write counts)
- ✅ High availability (write succeeds even if slaves down)

**Cons:**
- ❌ Eventual consistency (replicas lag behind master)
- ❌ Data loss possible (master fails before replication)

**Redis approach: Async by default**
```
Replication lag: typically < 100ms
Data loss window: last 0-100ms of writes
Acceptable for cache use case (cache is not source of truth)
```

**My recommendation for cache:**
- Use **async replication** (favor latency over consistency)
- Cache is secondary storage (database is source of truth)
- If master fails, acceptable to lose last 100ms of writes
- Use **write-behind** cache invalidation pattern

**If strong consistency needed:**
- Don't use cache as primary storage
- Use database with synchronous replication
- Cache only for reads (invalidate on writes)"

---

### 4. Memory Management: Eviction Threshold

**Interviewer:** "When should eviction start? At 100% memory or earlier?"

**Answer:**
"**Start eviction BEFORE 100% memory:**

**Why?**
1. **Prevent OOM:** At 100%, next write fails (no space for eviction metadata)
2. **Maintain performance:** Eviction is expensive, spread it over time
3. **Leave buffer:** Need space for temporary objects during eviction

**Recommended thresholds:**
```java
double softLimit = 0.95;  // Start eviction at 95%
double hardLimit = 1.00;  // Reject writes at 100%

if (memoryUsage >= softLimit) {
    // Gradually evict
    int evictCount = (int) ((memoryUsage - softLimit) * totalKeys);
    evict(evictCount);
}

if (memoryUsage >= hardLimit) {
    throw new OutOfMemoryException(\"Cache full\");
}
```

**Eviction batch size:**
- Too small (evict 1 at a time): High overhead, frequent scans
- Too large (evict 50% at once): Pauses write path
- **Optimal: Evict 5-10% of keys**

**Redis approach:**
```
maxmemory-policy: allkeys-lru
maxmemory: 10GB

When memory reaches 10GB:
1. Sample 5 random keys
2. Evict key with oldest access time
3. Repeat until memory < 10GB

Sampling avoids scanning entire keyspace (O(N) → O(1))
```

**Advanced: Proactive eviction**
```java
// Background thread: evict before reaching limit
scheduledExecutor.scheduleAtFixedRate(() -> {
    if (memoryUsage > 0.90) {
        evict(100);  // Evict 100 keys proactively
    }
}, 1, 1, TimeUnit.SECONDS);
```

This prevents sudden spikes from causing OOM."

---

### 5. Scalability: Handling 1 Million QPS

**Interviewer:** "How do you scale to 1 million queries per second?"

**Answer:**
"**Vertical scaling (single node):**

**1. CPU optimization:**
```java
// Lock-free data structures (ConcurrentHashMap)
cache.get(key);  // No lock contention

// Thread-local caches
ThreadLocal<Map<String, CacheEntry>> localCache;

// Zero-copy operations
ByteBuffer.allocateDirect(size);  // Avoid heap allocations
```

**2. Memory optimization:**
```java
// Compressed values
byte[] compressed = Compressor.compress(value.serialize());

// Object pooling
ObjectPool<CacheEntry> pool = new ObjectPool<>();
CacheEntry entry = pool.acquire();  // Reuse objects
```

**3. Network optimization:**
```
// Binary protocol (vs text): 3× faster parsing
// Pipelining: batch 100 commands → 1 RTT
// Zero-copy TCP (sendfile syscall)
```

**Single node capacity:** ~100K QPS

---

**Horizontal scaling (cluster):**

**4. Partition data (10 nodes):**
```
1M QPS ÷ 10 nodes = 100K QPS per node ✅
```

**5. Replication (3 replicas per master):**
```
Read: 1M QPS → 900K reads + 100K writes
Distribute reads: 900K ÷ 3 replicas = 300K reads per node

Write: 100K writes → master only
Master capacity: 100K writes/node ✅
```

**6. Client-side caching:**
```java
// L1 cache (client memory): 1ms latency
// L2 cache (Redis): 1ms network + 0.1ms Redis = 1.1ms

if (l1Cache.contains(key)) {
    return l1Cache.get(key);  // 99% of reads hit L1
}

return l2Cache.get(key);  // 1% miss → fetch from Redis
```

**Result:**
- L1 cache: 990K QPS (99% hits) → 0 network calls
- L2 cache: 10K QPS (1% miss) → 10K network calls

**7. Architecture:**
```
[Clients with L1 cache] (990K QPS handled locally)
         ↓ (10K QPS)
[Load Balancer]
         ↓
[10 Master Nodes] (100K QPS each, sharded by key)
  ├─ [Replica 1]  (handle read traffic)
  ├─ [Replica 2]
  └─ [Replica 3]

Total capacity: 1M writes/sec, 3M reads/sec
```

**Cost:**
- 10 masters × 64GB RAM = 640GB cache
- 30 replicas × 64GB RAM = 1920GB cache
- Total: ~40 servers for 1M QPS

**This is how Memcached/Redis scales in production.**"

---

## 🗄️ Step 9: Database Schema (Optional Persistence)

```sql
-- Snapshot Metadata Table
CREATE TABLE snapshots (
    snapshot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_id VARCHAR(50) NOT NULL,
    timestamp BIGINT NOT NULL,
    key_count INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    INDEX idx_node_timestamp (node_id, timestamp DESC)
);

-- AOF (Append-Only File) Log Table (alternative to file-based AOF)
CREATE TABLE aof_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_id VARCHAR(50) NOT NULL,
    command_type VARCHAR(20) NOT NULL,
    key_name VARCHAR(255) NOT NULL,
    value_data BLOB,
    ttl_millis BIGINT,
    timestamp BIGINT NOT NULL,
    INDEX idx_node_timestamp (node_id, timestamp),
    INDEX idx_key (key_name)
);

-- Replication Offset Table (tracks slave sync position)
CREATE TABLE replication_offset (
    slave_id VARCHAR(50) PRIMARY KEY,
    master_id VARCHAR(50) NOT NULL,
    offset BIGINT NOT NULL,
    last_sync BIGINT NOT NULL,
    INDEX idx_master (master_id)
);

-- Cluster Topology Table (tracks node membership)
CREATE TABLE cluster_nodes (
    node_id VARCHAR(50) PRIMARY KEY,
    node_role ENUM('MASTER', 'SLAVE', 'STANDALONE') NOT NULL,
    master_id VARCHAR(50),  -- NULL if master/standalone
    ip_address VARCHAR(45) NOT NULL,
    port INT NOT NULL,
    status ENUM('ONLINE', 'OFFLINE', 'SYNCING') NOT NULL,
    max_memory BIGINT NOT NULL,
    last_heartbeat BIGINT NOT NULL,
    INDEX idx_status (status),
    FOREIGN KEY (master_id) REFERENCES cluster_nodes(node_id)
);
```

---

## 🛠️ Step 10: Concurrency Handling

### 1. Race Condition: Concurrent PUT

**Problem:** Two threads update same key simultaneously

**Solution:**
```java
// Use ConcurrentHashMap.compute (atomic)
cache.compute(key, (k, oldEntry) -> {
    if (oldEntry != null) {
        currentMemory.addAndGet(-oldEntry.getSizeInBytes());
    }
    
    CacheEntry newEntry = new CacheEntry(k, value, ttl);
    currentMemory.addAndGet(newEntry.getSizeInBytes());
    
    return newEntry;
});
```

---

### 2. Memory Accounting Race

**Problem:** Concurrent writes corrupt memory counter

**Solution:**
```java
// Use AtomicLong for memory tracking
private final AtomicLong currentMemory = new AtomicLong(0);

currentMemory.addAndGet(entry.getSizeInBytes());  // Thread-safe
```

---

### 3. Eviction During Read

**Problem:** Key evicted while client reading it

**Solution:**
```java
// Copy-on-read (client gets immutable copy)
public Optional<CacheValue> get(String key) {
    CacheEntry entry = cache.get(key);
    if (entry != null) {
        return Optional.of(entry.getValue().clone());  // Defensive copy
    }
    return Optional.empty();
}
```

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `CacheNode`: Cache operations only
- `EvictionPolicy`: Eviction logic only
- `HashRing`: Key distribution only
- `ReplicationManager`: Replication only

### Open/Closed ✅
```java
// Add new data type without modifying CacheNode
public class GeoValue implements CacheValue {
    private double latitude;
    private double longitude;
    // ...
}
```

### Liskov Substitution ✅
```java
// All EvictionPolicy implementations interchangeable
EvictionPolicy policy = new LRUPolicy();
policy = new LFUPolicy();  // Works seamlessly
```

### Interface Segregation ✅
```java
interface Evictable {
    List<String> evict(int count);
}

interface Trackable {
    void recordAccess(CacheEntry entry);
}

// Policy can implement only what it needs
class RandomPolicy implements Evictable { }  // No tracking needed
```

### Dependency Inversion ✅
```java
public class CacheNode {
    private EvictionPolicy policy;  // Depends on abstraction
    
    public CacheNode(EvictionPolicy policy) {
        this.policy = policy;  // Inject dependency
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **In-memory storage** with O(1) operations
- ✅ **Pluggable eviction** (LRU, LFU, FIFO)
- ✅ **TTL-based expiration** (background cleanup)
- ✅ **Distributed via consistent hashing** (minimal key migration)
- ✅ **Master-slave replication** (async for performance)
- ✅ **Pub/Sub messaging** (real-time events)
- ✅ **Optional persistence** (RDB snapshots + AOF log)

### Scalability
- ✅ **100K+ req/sec per node**
- ✅ **Horizontal scaling** with consistent hashing
- ✅ **Replication** for high availability
- ✅ **Client-side caching** (L1 + L2)

### Performance
- ✅ GET/PUT: < 1ms (P99)
- ✅ Lock-free data structures (ConcurrentHashMap)
- ✅ Proactive eviction (prevent OOM)
- ✅ Binary protocol (efficient serialization)

### Data Structures
- ✅ **String, List, Set, Sorted Set, Hash** (Redis-compatible)
- ✅ ConcurrentHashMap for cache storage
- ✅ TreeMap for consistent hashing
- ✅ CopyOnWriteArrayList for pub/sub subscribers

---

**Total: 136 DSA + 10 LLD Problems**

All changes ready for review!
