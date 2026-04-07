# Low-Level Design: Pub/Sub Messaging System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a publish-subscribe messaging system like Apache Kafka, RabbitMQ, or Google Pub/Sub that allows producers to publish messages to topics and consumers to subscribe and receive those messages."*

### Clarifying Questions to Ask:

1. **Q:** What are the core operations?  
   **A:** Publish messages, subscribe to topics, consume messages, acknowledge/NACK messages.

2. **Q:** What delivery guarantees do we need?  
   **A:** Support all three: at-most-once, at-least-once, exactly-once.

3. **Q:** Should messages be ordered?  
   **A:** Yes, maintain order within a partition. Global ordering optional.

4. **Q:** What about message persistence?  
   **A:** Yes, persist messages to disk for durability. Support retention policies (time-based, size-based).

5. **Q:** How many messages per second?  
   **A:** Start with 100K messages/sec, scale to millions.

6. **Q:** Should we support consumer groups?  
   **A:** Yes, multiple consumers in a group share message load (parallel processing).

7. **Q:** What about failed message delivery?  
   **A:** Support retries with exponential backoff, dead letter queues (DLQ).

8. **Q:** Push or pull model?  
   **A:** Support both. Push for real-time, pull for batch processing.

9. **Q:** Should we support message filtering?  
   **A:** Yes, consumers can filter by attributes/tags.

10. **Q:** What about topic partitioning?  
    **A:** Yes, partition topics for horizontal scaling.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Core Messaging (FR1-FR8)
1. Publishers should publish messages to topics
2. Subscribers should subscribe to topics
3. Messages should be delivered to all active subscribers
4. System should support multiple message formats (JSON, Avro, Protobuf)
5. Messages should have:
   - Unique ID
   - Timestamp
   - Payload (data)
   - Attributes/metadata (key-value pairs)
6. System should support message acknowledgment (ACK/NACK)
7. Consumers should be able to commit offsets (track progress)
8. System should support message TTL (time-to-live)

#### Topics & Partitions (FR9-FR14)
9. System should support creating/deleting topics
10. Topics should be partitioned for scalability
11. Partition count should be configurable (default 3)
12. Messages with same key should go to same partition (ordering)
13. System should auto-balance partitions across brokers
14. Topics should support replication (configurable factor)

#### Consumer Groups (FR15-FR19)
15. Multiple consumers can form a group
16. Each partition assigned to only one consumer in group
17. System should rebalance partitions when consumer joins/leaves
18. Consumers in different groups receive all messages independently
19. System should track consumer offsets per partition

#### Delivery Guarantees (FR20-FR23)
20. **At-most-once:** Message delivered 0 or 1 time (may lose)
21. **At-least-once:** Message delivered 1+ times (may duplicate)
22. **Exactly-once:** Message delivered exactly 1 time (deduplication)
23. Consumers should choose delivery guarantee per subscription

#### Advanced Features (FR24-FR30)
24. Support dead letter queues (DLQ) for failed messages
25. Support message filtering by attributes
26. Support message replay (rewind to older offset)
27. Support batch publishing (publish 100 messages at once)
28. Support batch consumption (fetch 100 messages at once)
29. Support push (server pushes to consumer) and pull (consumer polls)
30. System should provide monitoring metrics:
    - Message publish rate
    - Consumer lag (messages behind)
    - Partition size

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many messages? How many topics?"
- 100,000 messages/sec initially
- Scale to 1,000,000 messages/sec
- Support 10,000+ topics
- Support 100,000+ consumers
- Average message size: 1KB

**Deduced NFRs:**
- ✅ Horizontal scaling (add brokers dynamically)
- ✅ Topic partitioning (distribute load)
- ✅ Consumer groups (parallel processing)
- ✅ Partition rebalancing (automatic load balancing)
- ✅ Message batching (reduce network overhead)

---

#### 2. **Consistency Analysis**

**Think:** "What consistency guarantees?"
- Message order critical within partition
- Cross-partition order not guaranteed
- Consumer offsets must be consistent
- Message acknowledgment must be reliable

**Deduced NFRs:**
- ✅ **Sequential consistency** within partition (FIFO order)
- ✅ **Eventual consistency** across partitions
- ✅ **Strong consistency** for consumer offsets (checkpoint to database)
- ✅ **Idempotent publishing** (duplicate detection)
- ✅ **Transactional publishing** (all-or-nothing for batch)

---

#### 3. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Critical infrastructure (messaging backbone)
- High availability needed (99.95%+)
- No message loss acceptable (persist to disk)

**Deduced NFRs:**
- ✅ **99.95% availability** (4.3 hours downtime/year)
- ✅ **Replication** - each partition replicated 3× (leader + 2 replicas)
- ✅ **Automatic failover** - promote replica to leader on failure
- ✅ **Persistent storage** - write-ahead log (WAL) to disk
- ✅ **No single point of failure** - distributed brokers
- ✅ **Graceful degradation:**
  - If broker fails → reroute to replicas
  - If consumer fails → rebalance partitions

---

#### 4. **Maintainability Analysis**

**Think:** "How to operate and debug?"
- Need to monitor message flow
- Need to debug slow consumers
- Need to add topics/partitions without downtime

**Deduced NFRs:**
- ✅ **Comprehensive metrics:**
  - Publish rate (msgs/sec)
  - Consumer lag (messages behind)
  - Partition size (bytes)
  - Replication lag (follower behind leader)
  - Error rate (failed deliveries)
- ✅ **Distributed tracing** (trace message from publish to consume)
- ✅ **Admin API** (create topics, add partitions, view offsets)
- ✅ **Rolling upgrades** (upgrade brokers without downtime)
- ✅ **Configuration hot-reload**

---

#### 5. **Performance Analysis**

**Think:** "Latency requirements?"
- Publish latency: < 10ms (P99)
- End-to-end latency: < 100ms (P99) (publish → consume)
- Consumer throughput: 10K msgs/sec per consumer
- Broker throughput: 100K msgs/sec per broker

**Deduced NFRs:**
- ✅ **Sequential disk I/O** (append-only log, very fast)
- ✅ **Zero-copy transfers** (sendfile syscall, avoid user-space copy)
- ✅ **Message batching** (amortize network/disk overhead)
- ✅ **Async replication** (don't block on followers)
- ✅ **Page cache** (OS cache for recent messages)
- ✅ **Compression** (reduce network/disk bandwidth)

---

#### 6. **Security Analysis**

**Think:** "Security risks?"
- Unauthorized publishing (message injection)
- Unauthorized consumption (data theft)
- Message tampering in transit
- Consumer impersonation

**Deduced NFRs:**
- ✅ **Authentication** (SASL/OAuth for clients)
- ✅ **Authorization** (ACLs per topic: who can publish/subscribe)
- ✅ **TLS encryption** (in-transit encryption)
- ✅ **Message signing** (optional, detect tampering)
- ✅ **Rate limiting** (prevent abuse)
- ✅ **Audit logging** (track all operations)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Publish to topics" | Publisher, Topic, Message |
| "Subscribe to topics" | Subscriber, Subscription |
| "Consumer groups" | ConsumerGroup, Consumer |
| "Partitions" | Partition, PartitionLeader |
| "Message acknowledgment" | Acknowledgment, Offset |
| "Delivery guarantees" | DeliveryGuarantee |
| "Dead letter queue" | DeadLetterQueue |
| "Brokers" | Broker, Cluster |
| "Replication" | Replica, ReplicationLog |
| "Filtering" | Filter, MessageAttribute |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Publisher | ✅ YES | Client that produces messages |
| Topic | ✅ YES | Logical message channel |
| Message | ✅ YES | Data unit with metadata |
| Subscriber | ✅ YES | Client that consumes messages |
| Subscription | ✅ YES | Binding between subscriber and topic |
| ConsumerGroup | ✅ YES | Collection of consumers |
| Consumer | ✅ YES | Individual message processor |
| Partition | ✅ YES | Ordered message log |
| PartitionLeader | ❌ NO | Role, not separate entity |
| Acknowledgment | ✅ YES | Delivery confirmation |
| Offset | ✅ YES | Position in partition |
| DeliveryGuarantee | ✅ YES | Enum (at-most-once, at-least-once, exactly-once) |
| DeadLetterQueue | ✅ YES | Topic for failed messages |
| Broker | ✅ YES | Server node |
| Cluster | ✅ YES | Collection of brokers |
| Replica | ✅ YES | Partition copy |
| ReplicationLog | ✅ YES | Log of replicated messages |
| Filter | ✅ YES | Message filtering rule |
| MessageAttribute | ❌ NO | Part of Message (Map<String, String>) |

### Final Entity List

**Core Messaging Entities:**
1. **Message** - Data unit with ID, timestamp, payload, attributes
2. **Topic** - Logical channel for messages
3. **Partition** - Ordered append-only log (part of topic)
4. **Offset** - Position in partition (long value)

**Publisher Entities:**
5. **Publisher** - Client that produces messages
6. **PublishResult** - Result of publish operation (success/failure)

**Subscriber Entities:**
7. **Subscriber** - Client that consumes messages
8. **Subscription** - Binding between subscriber and topic
9. **ConsumerGroup** - Collection of consumers sharing load
10. **DeliveryGuarantee** - Enum (AT_MOST_ONCE, AT_LEAST_ONCE, EXACTLY_ONCE)

**Acknowledgment Entities:**
11. **Acknowledgment** - Message delivery confirmation
12. **AckStatus** - Enum (ACK, NACK, RETRY)

**Broker & Cluster:**
13. **Broker** - Server node managing partitions
14. **Cluster** - Collection of brokers
15. **BrokerMetadata** - Broker health, capacity, partitions

**Replication:**
16. **Replica** - Partition copy (leader or follower)
17. **ReplicaRole** - Enum (LEADER, FOLLOWER)
18. **ReplicationLog** - Log of operations to replicate

**Advanced Features:**
19. **DeadLetterQueue** - Topic for failed messages
20. **MessageFilter** - Filtering rule (attribute-based)
21. **RetryPolicy** - Retry configuration (max attempts, backoff)
22. **OffsetCommit** - Consumer offset checkpoint
23. **PartitionAssignment** - Mapping consumer → partition

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Topic & Partition Relationships

#### Topic ↔ Partition
**Conclusion:** **Composition** (topic owns partitions)
```
Topic ◆────→ Partition [1..*]
```

#### Partition ↔ Message
**Conclusion:** **Composition** (partition stores messages)
```
Partition ◆────→ Message [0..*]
```

#### Partition ↔ Replica
**Conclusion:** **Composition** (partition has replicas)
```
Partition ◆────→ Replica [1..*]  // 1 leader + N followers
```

---

### Pass 2: Publisher Relationships

#### Publisher ↔ Topic
**Conclusion:** **Association** (publisher writes to topics)
```
Publisher ─────→ Topic [0..*]
```

#### Publisher ↔ Message
**Conclusion:** **Association** (publisher creates messages)
```
Publisher ─────→ Message [0..*]
```

---

### Pass 3: Subscriber Relationships

#### Subscriber ↔ Subscription
**Conclusion:** **Composition** (subscriber owns subscriptions)
```
Subscriber ◆────→ Subscription [1..*]
```

#### Subscription ↔ Topic
**Conclusion:** **Association** (subscription binds to topic)
```
Subscription ─────→ Topic [1]
```

#### ConsumerGroup ↔ Subscriber
**Conclusion:** **Aggregation** (group contains subscribers)
```
ConsumerGroup ◇────→ Subscriber [1..*]
```

#### Subscriber ↔ Partition
**Conclusion:** **Association** (via PartitionAssignment)
```
Subscriber ─────→ PartitionAssignment ←───── Partition
```

---

### Pass 4: Broker & Cluster

#### Cluster ↔ Broker
**Conclusion:** **Aggregation** (cluster manages brokers)
```
Cluster ◇────→ Broker [1..*]
```

#### Broker ↔ Partition
**Conclusion:** **Aggregation** (broker hosts partitions)
```
Broker ◇────→ Partition [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Topic → Partition | 1:N | Composition |
| Partition → Message | 1:N | Composition |
| Partition → Replica | 1:N | Composition |
| Publisher → Topic | M:N | Association |
| Subscriber → Subscription | 1:N | Composition |
| Subscription → Topic | N:1 | Association |
| ConsumerGroup → Subscriber | 1:N | Aggregation |
| Subscriber ↔ Partition | M:N | Via PartitionAssignment |
| Cluster → Broker | 1:N | Aggregation |
| Broker → Partition | 1:N | Aggregation |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────────┐  ┌──────────────────────┐  ┌─────────────────┐
│  <<enumeration>>    │  │  <<enumeration>>     │  │ <<enumeration>> │
│  DeliveryGuarantee  │  │     AckStatus        │  │  ReplicaRole    │
├─────────────────────┤  ├──────────────────────┤  ├─────────────────┤
│ AT_MOST_ONCE        │  │ ACK                  │  │ LEADER          │
│ AT_LEAST_ONCE       │  │ NACK                 │  │ FOLLOWER        │
│ EXACTLY_ONCE        │  │ RETRY                │  └─────────────────┘
└─────────────────────┘  └──────────────────────┘

┌─────────────────────┐  ┌──────────────────────┐
│  <<enumeration>>    │  │  <<enumeration>>     │
│   PushPullMode      │  │  MessageStatus       │
├─────────────────────┤  ├──────────────────────┤
│ PUSH                │  │ PENDING              │
│ PULL                │  │ DELIVERED            │
└─────────────────────┘  │ ACKED                │
                         │ NACKED               │
                         │ DEAD_LETTERED        │
                         └──────────────────────┘
```

---

### Class Diagram 2: Message & Topic

```
┌───────────────────────────────────────────────────────────┐
│                       Message                             │
├───────────────────────────────────────────────────────────┤
│ - messageId: String (UUID)                                │
│ - topic: String                                           │
│ - partition: int                                          │
│ - offset: long                                            │
│ - key: String (for partition routing)                     │
│ - payload: byte[]                                         │
│ - attributes: Map<String, String>  (metadata)             │
│ - timestamp: long (publish time)                          │
│ - expiresAt: long (TTL)                                   │
│ - publisherId: String                                     │
│ - retryCount: int                                         │
├───────────────────────────────────────────────────────────┤
│ + Message(topic, key, payload, attributes)                │
│ + isExpired(): boolean                                    │
│ + getSize(): int                                          │
│ + serialize(): byte[]                                     │
│ + matches(filter: MessageFilter): boolean                 │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                        Topic                              │
├───────────────────────────────────────────────────────────┤
│ - topicName: String                                       │
│ - partitions: List<Partition>       ◆────────────────┐    │
│ - partitionCount: int                                │    │
│ - replicationFactor: int (default: 3)                │    │
│ - retentionMs: long (message TTL)                    │    │
│ - createdAt: long                                    │    │
│ - config: Map<String, String>                        │    │
├───────────────────────────────────────────────────────────┤
│ + Topic(name, partitionCount, replicationFactor)          │
│ + addPartition(): Partition                               │
│ + getPartition(key: String): Partition                    │
│ + getAllPartitions(): List<Partition>                     │
│ + getTotalMessages(): long                                │
└───────────────────────────────────────────────────────────┘
                                            │
                                            │
                                            ▼
┌───────────────────────────────────────────────────────────┐
│                      Partition                            │
├───────────────────────────────────────────────────────────┤
│ - partitionId: int                                        │
│ - topic: Topic                                            │
│ - leader: Replica                       ◆─────────────┐   │
│ - replicas: List<Replica>               ◆─────────────┼─┐ │
│ - messages: List<Message>  (append-only log) ◆────────┼─┼─┐
│ - currentOffset: AtomicLong                           │ │ │
│ - startOffset: long (oldest message offset)           │ │ │
│ - endOffset: long (newest message offset)             │ │ │
│ - sizeBytes: AtomicLong                               │ │ │
├───────────────────────────────────────────────────────────┤
│ + Partition(topicName, partitionId)                       │
│ + append(message: Message): Offset                        │
│ + read(offset: long, count: int): List<Message>           │
│ + getLeader(): Replica                                    │
│ + electLeader(): void  // On leader failure               │
│ + replicate(follower: Replica): void                      │
│ + trim(): void  // Remove expired messages                │
└───────────────────────────────────────────────────────────┘
         │                    │                   │
         │                    │                   │
         ▼                    ▼                   ▼
┌──────────────┐   ┌──────────────────┐   ┌──────────────┐
│   Replica    │   │   Replica        │   │   Message    │
├──────────────┤   ├──────────────────┤   ├──────────────┤
│ - replicaId: │   │ (FOLLOWER)       │   │ (stored in   │
│   String     │   │ - currentOffset  │   │  partition)  │
│ - role:      │   │ - lag: long      │   └──────────────┘
│   ReplicaRole│   │ - lastFetchTime  │
│ - broker:    │   └──────────────────┘
│   Broker     │
│ - isInSync:  │
│   boolean    │
├──────────────┤
│ + sync():    │
│   void       │
└──────────────┘
```

---

### Class Diagram 3: Publisher

```
┌───────────────────────────────────────────────────────────┐
│                      Publisher                            │
├───────────────────────────────────────────────────────────┤
│ - publisherId: String                                     │
│ - cluster: Cluster                                        │
│ - compressionEnabled: boolean                             │
│ - batchSize: int (default: 100)                           │
│ - batchTimeoutMs: long (default: 10ms)                    │
│ - pendingBatch: List<Message>                             │
│ - metrics: PublisherMetrics                               │
├───────────────────────────────────────────────────────────┤
│ + Publisher(publisherId, cluster)                         │
│ + publish(topic: String, message: Message):               │
│     CompletableFuture<PublishResult>                      │
│ + publishBatch(topic: String, messages: List<Message>):   │
│     List<PublishResult>                                   │
│ + publishAsync(topic: String, message: Message):          │
│     void  // Fire and forget                              │
│ + flush(): void  // Send pending batch                    │
│ + close(): void                                           │
└───────────────────────────────────────────────────────────┘
                    │
                    │ returns
                    ▼
┌───────────────────────────────────────────────────────────┐
│                   PublishResult                           │
├───────────────────────────────────────────────────────────┤
│ - messageId: String                                       │
│ - topic: String                                           │
│ - partition: int                                          │
│ - offset: long                                            │
│ - success: boolean                                        │
│ - error: Exception                                        │
│ - latencyMs: long                                         │
├───────────────────────────────────────────────────────────┤
│ + PublishResult(messageId, partition, offset)             │
│ + isSuccess(): boolean                                    │
│ + getError(): Optional<Exception>                         │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Subscriber & Consumer Group

```
┌───────────────────────────────────────────────────────────┐
│                     Subscriber                            │
├───────────────────────────────────────────────────────────┤
│ - subscriberId: String                                    │
│ - group: ConsumerGroup                                    │
│ - subscriptions: List<Subscription>  ◆────────────────┐   │
│ - assignedPartitions: List<PartitionAssignment> ──────┼─┐ │
│ - mode: PushPullMode (PUSH or PULL)                   │ │ │
│ - autoCommit: boolean (default: false)                │ │ │
│ - deliveryGuarantee: DeliveryGuarantee                │ │ │
│ - messageHandler: Consumer<Message>  (callback)       │ │ │
│ - offsetManager: OffsetManager                        │ │ │
├───────────────────────────────────────────────────────────┤
│ + Subscriber(subscriberId, group)                         │
│ + subscribe(topic: String, filter: MessageFilter):        │
│     Subscription                                          │
│ + unsubscribe(topic: String): void                        │
│ + poll(timeoutMs: long): List<Message>  // Pull mode      │
│ + acknowledge(message: Message): void                     │
│ + nack(message: Message): void                            │
│ + commitOffset(partition: int, offset: long): void        │
│ + seekToOffset(partition: int, offset: long): void        │
│ + close(): void                                           │
└───────────────────────────────────────────────────────────┘
         │                              │
         │                              │
         ▼                              ▼
┌────────────────────┐      ┌──────────────────────────────┐
│   Subscription     │      │   PartitionAssignment        │
├────────────────────┤      ├──────────────────────────────┤
│ - subscriptionId:  │      │ - partition: Partition       │
│   String           │      │ - subscriber: Subscriber     │
│ - topic: Topic     │      │ - currentOffset: long        │
│ - subscriber:      │      │ - lagMessages: long          │
│   Subscriber       │      ├──────────────────────────────┤
│ - filter:          │      │ + updateOffset(offset): void │
│   MessageFilter    │      │ + getLag(): long             │
│ - createdAt: long  │      └──────────────────────────────┘
├────────────────────┤
│ + matches(msg:     │
│   Message): bool   │
└────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                   ConsumerGroup                           │
├───────────────────────────────────────────────────────────┤
│ - groupId: String                                         │
│ - subscribers: List<Subscriber>                           │
│ - assignments: Map<Partition, Subscriber>                 │
│ - coordinator: Broker  (manages rebalancing)              │
│ - rebalancer: RebalanceStrategy                           │
├───────────────────────────────────────────────────────────┤
│ + ConsumerGroup(groupId, rebalancer)                      │
│ + addSubscriber(subscriber: Subscriber): void             │
│ + removeSubscriber(subscriberId: String): void            │
│ + rebalance(): void  // Reassign partitions               │
│ + getAssignment(subscriberId): List<Partition>            │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                  MessageFilter                            │
├───────────────────────────────────────────────────────────┤
│ - attributeFilters: Map<String, String>                   │
│ - filterExpression: String  (optional SQL-like)           │
├───────────────────────────────────────────────────────────┤
│ + MessageFilter(attributes)                               │
│ + matches(message: Message): boolean                      │
│ + evaluate(message: Message): boolean                     │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Broker & Cluster

```
┌───────────────────────────────────────────────────────────┐
│                       Broker                              │
├───────────────────────────────────────────────────────────┤
│ - brokerId: String                                        │
│ - host: String                                            │
│ - port: int                                               │
│ - partitions: List<Partition>  (partitions hosted)        │
│ - leaderPartitions: List<Partition>  (where leader)       │
│ - followerPartitions: List<Partition>  (where follower)   │
│ - storageManager: StorageManager                          │
│ - replicationManager: ReplicationManager                  │
│ - diskSpaceBytes: long                                    │
│ - networkBandwidthMbps: int                               │
│ - isAlive: boolean                                        │
│ - lastHeartbeat: long                                     │
├───────────────────────────────────────────────────────────┤
│ + Broker(brokerId, host, port)                            │
│ + publish(message: Message): PublishResult                │
│ + fetchMessages(partition: int, offset: long, count: int):│
│     List<Message>                                         │
│ + assignPartition(partition: Partition, role: Role): void │
│ + removePartition(partitionId: int): void                 │
│ + electLeader(partition: Partition): void                 │
│ + replicateToFollowers(partition: Partition): void        │
│ + getMetrics(): BrokerMetrics                             │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                      Cluster                              │
├───────────────────────────────────────────────────────────┤
│ - clusterId: String                                       │
│ - brokers: List<Broker>                                   │
│ - topics: Map<String, Topic>                              │
│ - zookeeper: ZooKeeperClient  (coordination service)      │
│ - partitionAssigner: PartitionAssigner                    │
├───────────────────────────────────────────────────────────┤
│ + Cluster(clusterId)                                      │
│ + addBroker(broker: Broker): void                         │
│ + removeBroker(brokerId: String): void                    │
│ + createTopic(name: String, partitions: int,              │
│               replicationFactor: int): Topic              │
│ + deleteTopic(name: String): void                         │
│ + getTopicPartition(topic: String, key: String):          │
│     Partition                                             │
│ + rebalancePartitions(): void                             │
│ + handleBrokerFailure(brokerId: String): void             │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                  StorageManager                           │
├───────────────────────────────────────────────────────────┤
│ - dataDir: String                                         │
│ - segmentSize: long (default: 1GB)                        │
│ - segments: List<Segment>  (log segments)                 │
│ - indexManager: IndexManager                              │
├───────────────────────────────────────────────────────────┤
│ + append(message: Message): Offset                        │
│ + read(offset: long, count: int): List<Message>           │
│ + createSegment(): Segment                                │
│ + trimExpiredSegments(): void                             │
│ + compact(): void  // Remove duplicates (exactly-once)    │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                     Segment                               │
├───────────────────────────────────────────────────────────┤
│ - startOffset: long                                       │
│ - endOffset: long                                         │
│ - filePath: String                                        │
│ - sizeBytes: long                                         │
│ - createdAt: long                                         │
│ - indexFile: File  (offset → file position)               │
│ - dataFile: File   (actual messages)                      │
├───────────────────────────────────────────────────────────┤
│ + append(message: Message): void                          │
│ + read(offset: long): Message                             │
│ + isFull(): boolean                                       │
│ + flush(): void  // Force write to disk                   │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Dead Letter Queue & Retry

```
┌───────────────────────────────────────────────────────────┐
│                 DeadLetterQueue                           │
├───────────────────────────────────────────────────────────┤
│ - topic: Topic  (special DLQ topic)                       │
│ - maxRetries: int (default: 3)                            │
│ - failedMessages: Map<String, List<Message>>              │
├───────────────────────────────────────────────────────────┤
│ + DeadLetterQueue(sourceTopic: String)                    │
│ + add(message: Message, reason: String): void             │
│ + retry(messageId: String): void                          │
│ + getFailedMessages(): List<Message>                      │
│ + purge(): void                                           │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                   RetryPolicy                             │
├───────────────────────────────────────────────────────────┤
│ - maxAttempts: int (default: 3)                           │
│ - initialDelayMs: long (default: 1000)                    │
│ - maxDelayMs: long (default: 60000)                       │
│ - backoffMultiplier: double (default: 2.0)                │
├───────────────────────────────────────────────────────────┤
│ + RetryPolicy(maxAttempts, initialDelay, multiplier)      │
│ + getDelay(attemptNumber: int): long                      │
│ + shouldRetry(attemptNumber: int): boolean                │
└───────────────────────────────────────────────────────────┘

// Exponential backoff: delay = min(initialDelay * multiplier^attempt, maxDelay)
// Attempt 1: 1s, Attempt 2: 2s, Attempt 3: 4s, Attempt 4: 8s, ...
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Message & Topic

```java
// Message.java
import java.util.*;

public class Message {
    private final String messageId;
    private final String topic;
    private int partition;
    private long offset;
    private final String key;  // For partition routing
    private final byte[] payload;
    private final Map<String, String> attributes;
    private final long timestamp;
    private long expiresAt;
    private final String publisherId;
    private int retryCount;
    
    public Message(String topic, String key, byte[] payload, 
                   Map<String, String> attributes) {
        this.messageId = UUID.randomUUID().toString();
        this.topic = topic;
        this.key = key;
        this.payload = payload;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.expiresAt = 0;  // No expiration by default
        this.publisherId = null;
        this.retryCount = 0;
    }
    
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
    
    public int getSize() {
        return payload.length + messageId.length() * 2 + 
               attributes.toString().length() * 2;
    }
    
    public byte[] serialize() {
        // Simplified serialization (use Avro/Protobuf in production)
        return payload;
    }
    
    public boolean matches(MessageFilter filter) {
        if (filter == null) return true;
        return filter.matches(this);
    }
    
    // Getters and setters
    public String getMessageId() { return messageId; }
    public String getTopic() { return topic; }
    public int getPartition() { return partition; }
    public void setPartition(int partition) { this.partition = partition; }
    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }
    public String getKey() { return key; }
    public byte[] getPayload() { return payload; }
    public Map<String, String> getAttributes() { return attributes; }
    public long getTimestamp() { return timestamp; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
}
```

```java
// Topic.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic {
    private final String topicName;
    private final List<Partition> partitions;
    private final int replicationFactor;
    private final long retentionMs;
    private final long createdAt;
    
    public Topic(String topicName, int partitionCount, int replicationFactor, 
                 long retentionMs) {
        this.topicName = topicName;
        this.partitions = new CopyOnWriteArrayList<>();
        this.replicationFactor = replicationFactor;
        this.retentionMs = retentionMs;
        this.createdAt = System.currentTimeMillis();
        
        // Create partitions
        for (int i = 0; i < partitionCount; i++) {
            partitions.add(new Partition(topicName, i));
        }
    }
    
    public Partition getPartition(String key) {
        if (key == null || key.isEmpty()) {
            // Round-robin if no key
            int partitionId = (int) (System.nanoTime() % partitions.size());
            return partitions.get(partitionId);
        }
        
        // Hash-based partitioning
        int hash = Math.abs(key.hashCode());
        int partitionId = hash % partitions.size();
        return partitions.get(partitionId);
    }
    
    public Partition getPartitionById(int partitionId) {
        if (partitionId < 0 || partitionId >= partitions.size()) {
            throw new IllegalArgumentException("Invalid partition ID");
        }
        return partitions.get(partitionId);
    }
    
    public List<Partition> getAllPartitions() {
        return new ArrayList<>(partitions);
    }
    
    public long getTotalMessages() {
        return partitions.stream()
            .mapToLong(Partition::getMessageCount)
            .sum();
    }
    
    public String getTopicName() { return topicName; }
    public int getPartitionCount() { return partitions.size(); }
    public int getReplicationFactor() { return replicationFactor; }
    public long getRetentionMs() { return retentionMs; }
}
```

```java
// Partition.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Partition {
    private final String topic;
    private final int partitionId;
    private final List<Message> messages;  // Append-only log
    private final AtomicLong currentOffset;
    private long startOffset;
    private final List<Replica> replicas;
    private Replica leader;
    private final AtomicLong sizeBytes;
    
    public Partition(String topic, int partitionId) {
        this.topic = topic;
        this.partitionId = partitionId;
        this.messages = new CopyOnWriteArrayList<>();
        this.currentOffset = new AtomicLong(0);
        this.startOffset = 0;
        this.replicas = new ArrayList<>();
        this.sizeBytes = new AtomicLong(0);
    }
    
    /**
     * Append message to partition (leader only)
     */
    public synchronized long append(Message message) {
        long offset = currentOffset.getAndIncrement();
        
        message.setPartition(partitionId);
        message.setOffset(offset);
        
        messages.add(message);
        sizeBytes.addAndGet(message.getSize());
        
        // Replicate to followers (async)
        replicateToFollowers(message);
        
        return offset;
    }
    
    /**
     * Read messages starting from offset
     */
    public List<Message> read(long startOffset, int maxCount) {
        if (startOffset < this.startOffset) {
            throw new IllegalArgumentException(
                "Offset too old (already trimmed): " + startOffset
            );
        }
        
        int startIndex = (int) (startOffset - this.startOffset);
        int endIndex = Math.min(startIndex + maxCount, messages.size());
        
        if (startIndex >= messages.size()) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(messages.subList(startIndex, endIndex));
    }
    
    /**
     * Replicate message to follower replicas
     */
    private void replicateToFollowers(Message message) {
        for (Replica replica : replicas) {
            if (replica.getRole() == ReplicaRole.FOLLOWER) {
                // Async replication
                CompletableFuture.runAsync(() -> {
                    replica.append(message);
                });
            }
        }
    }
    
    /**
     * Elect new leader on failure
     */
    public synchronized void electLeader() {
        // Find in-sync replica with highest offset
        Replica newLeader = replicas.stream()
            .filter(r -> r.isInSync() && r.getRole() == ReplicaRole.FOLLOWER)
            .max(Comparator.comparingLong(Replica::getCurrentOffset))
            .orElse(null);
        
        if (newLeader != null) {
            if (leader != null) {
                leader.setRole(ReplicaRole.FOLLOWER);
            }
            newLeader.setRole(ReplicaRole.LEADER);
            this.leader = newLeader;
        }
    }
    
    /**
     * Trim expired messages (retention policy)
     */
    public synchronized void trim(long retentionMs) {
        long cutoffTime = System.currentTimeMillis() - retentionMs;
        
        int removeCount = 0;
        for (Message message : messages) {
            if (message.getTimestamp() < cutoffTime) {
                sizeBytes.addAndGet(-message.getSize());
                removeCount++;
            } else {
                break;  // Messages are ordered by time
            }
        }
        
        if (removeCount > 0) {
            messages.subList(0, removeCount).clear();
            startOffset += removeCount;
        }
    }
    
    public int getPartitionId() { return partitionId; }
    public long getCurrentOffset() { return currentOffset.get(); }
    public long getStartOffset() { return startOffset; }
    public long getEndOffset() { return currentOffset.get() - 1; }
    public long getMessageCount() { return messages.size(); }
    public long getSizeBytes() { return sizeBytes.get(); }
    public Replica getLeader() { return leader; }
    public void setLeader(Replica leader) { this.leader = leader; }
    public void addReplica(Replica replica) { replicas.add(replica); }
}
```

---

### Publisher

```java
// Publisher.java
import java.util.*;
import java.util.concurrent.*;

public class Publisher {
    private final String publisherId;
    private final Cluster cluster;
    private final boolean compressionEnabled;
    private final int batchSize;
    private final long batchTimeoutMs;
    private final List<Message> pendingBatch;
    private final ScheduledExecutorService batchScheduler;
    
    public Publisher(String publisherId, Cluster cluster) {
        this(publisherId, cluster, 100, 10);
    }
    
    public Publisher(String publisherId, Cluster cluster, 
                    int batchSize, long batchTimeoutMs) {
        this.publisherId = publisherId;
        this.cluster = cluster;
        this.compressionEnabled = false;
        this.batchSize = batchSize;
        this.batchTimeoutMs = batchTimeoutMs;
        this.pendingBatch = new ArrayList<>();
        this.batchScheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Auto-flush batch on timeout
        batchScheduler.scheduleAtFixedRate(
            this::flush,
            batchTimeoutMs, batchTimeoutMs,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Publish single message (async)
     */
    public CompletableFuture<PublishResult> publish(String topic, Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get partition for message
                Topic topicObj = cluster.getTopic(topic);
                Partition partition = topicObj.getPartition(message.getKey());
                
                // Append to partition (leader)
                long offset = partition.append(message);
                
                return new PublishResult(
                    message.getMessageId(),
                    topic,
                    partition.getPartitionId(),
                    offset,
                    true,
                    null
                );
                
            } catch (Exception e) {
                return new PublishResult(
                    message.getMessageId(),
                    topic,
                    -1, -1,
                    false,
                    e
                );
            }
        });
    }
    
    /**
     * Publish batch of messages
     */
    public List<PublishResult> publishBatch(String topic, List<Message> messages) {
        List<PublishResult> results = new ArrayList<>();
        
        for (Message message : messages) {
            try {
                PublishResult result = publish(topic, message).get();
                results.add(result);
            } catch (Exception e) {
                results.add(new PublishResult(
                    message.getMessageId(), topic, -1, -1, false, e
                ));
            }
        }
        
        return results;
    }
    
    /**
     * Add message to pending batch (for batching optimization)
     */
    public synchronized void publishAsync(String topic, Message message) {
        pendingBatch.add(message);
        
        if (pendingBatch.size() >= batchSize) {
            flush();
        }
    }
    
    /**
     * Flush pending batch
     */
    public synchronized void flush() {
        if (pendingBatch.isEmpty()) return;
        
        List<Message> batch = new ArrayList<>(pendingBatch);
        pendingBatch.clear();
        
        // Publish batch in background
        CompletableFuture.runAsync(() -> {
            for (Message message : batch) {
                publish(message.getTopic(), message);
            }
        });
    }
    
    public void close() {
        flush();
        batchScheduler.shutdown();
    }
}
```

```java
// PublishResult.java
public class PublishResult {
    private final String messageId;
    private final String topic;
    private final int partition;
    private final long offset;
    private final boolean success;
    private final Exception error;
    
    public PublishResult(String messageId, String topic, int partition, 
                        long offset, boolean success, Exception error) {
        this.messageId = messageId;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.success = success;
        this.error = error;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessageId() { return messageId; }
    public int getPartition() { return partition; }
    public long getOffset() { return offset; }
    public Optional<Exception> getError() { return Optional.ofNullable(error); }
}
```

---

### Subscriber & Consumer Group

```java
// Subscriber.java
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Subscriber {
    private final String subscriberId;
    private final ConsumerGroup group;
    private final List<Subscription> subscriptions;
    private final Map<Integer, Long> offsets;  // partitionId -> offset
    private final PushPullMode mode;
    private final boolean autoCommit;
    private final DeliveryGuarantee deliveryGuarantee;
    private final Consumer<Message> messageHandler;
    private final OffsetManager offsetManager;
    private final ScheduledExecutorService pollScheduler;
    private volatile boolean running;
    
    public Subscriber(String subscriberId, ConsumerGroup group, 
                     PushPullMode mode, DeliveryGuarantee guarantee) {
        this.subscriberId = subscriberId;
        this.group = group;
        this.subscriptions = new CopyOnWriteArrayList<>();
        this.offsets = new ConcurrentHashMap<>();
        this.mode = mode;
        this.autoCommit = false;
        this.deliveryGuarantee = guarantee;
        this.messageHandler = null;
        this.offsetManager = new OffsetManager(subscriberId);
        this.pollScheduler = Executors.newSingleThreadScheduledExecutor();
        this.running = false;
    }
    
    /**
     * Subscribe to topic with optional filter
     */
    public Subscription subscribe(String topicName, MessageFilter filter) {
        Topic topic = group.getCluster().getTopic(topicName);
        
        Subscription subscription = new Subscription(
            UUID.randomUUID().toString(),
            topic,
            this,
            filter
        );
        
        subscriptions.add(subscription);
        
        // Trigger rebalance (assign partitions to this subscriber)
        group.rebalance();
        
        return subscription;
    }
    
    /**
     * Unsubscribe from topic
     */
    public void unsubscribe(String topicName) {
        subscriptions.removeIf(sub -> sub.getTopic().getTopicName().equals(topicName));
        group.rebalance();
    }
    
    /**
     * Poll messages (PULL mode)
     */
    public List<Message> poll(long timeoutMs) {
        List<Message> messages = new ArrayList<>();
        long deadline = System.currentTimeMillis() + timeoutMs;
        
        // Get assigned partitions for this subscriber
        List<PartitionAssignment> assignments = group.getAssignments(subscriberId);
        
        for (PartitionAssignment assignment : assignments) {
            if (System.currentTimeMillis() >= deadline) break;
            
            Partition partition = assignment.getPartition();
            long currentOffset = offsets.getOrDefault(partition.getPartitionId(), 0L);
            
            // Fetch messages from partition
            List<Message> batch = partition.read(currentOffset, 100);
            
            for (Message message : batch) {
                // Apply filter
                if (matches(message)) {
                    messages.add(message);
                    
                    // Update offset (but don't commit yet)
                    offsets.put(partition.getPartitionId(), message.getOffset() + 1);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Start consuming in PUSH mode
     */
    public void startConsuming(Consumer<Message> handler) {
        if (mode != PushPullMode.PUSH) {
            throw new IllegalStateException("Not in PUSH mode");
        }
        
        this.running = true;
        
        // Poll periodically and push to handler
        pollScheduler.scheduleAtFixedRate(() -> {
            if (!running) return;
            
            List<Message> messages = poll(100);
            for (Message message : messages) {
                try {
                    handler.accept(message);
                    
                    // Auto-ack if delivery guarantee allows
                    if (deliveryGuarantee == DeliveryGuarantee.AT_MOST_ONCE) {
                        acknowledge(message);
                    }
                } catch (Exception e) {
                    // NACK and retry
                    nack(message);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Acknowledge message delivery
     */
    public void acknowledge(Message message) {
        // Commit offset for this message
        commitOffset(message.getPartition(), message.getOffset() + 1);
    }
    
    /**
     * Negative acknowledgment (retry)
     */
    public void nack(Message message) {
        // Don't commit offset, will be re-delivered
        System.out.println("NACK: " + message.getMessageId());
    }
    
    /**
     * Commit offset to persistent storage
     */
    public void commitOffset(int partitionId, long offset) {
        offsets.put(partitionId, offset);
        offsetManager.commit(partitionId, offset);
    }
    
    /**
     * Seek to specific offset (replay)
     */
    public void seekToOffset(int partitionId, long offset) {
        offsets.put(partitionId, offset);
    }
    
    private boolean matches(Message message) {
        return subscriptions.stream()
            .anyMatch(sub -> sub.matches(message));
    }
    
    public void close() {
        running = false;
        pollScheduler.shutdown();
        group.removeSubscriber(subscriberId);
    }
    
    public String getSubscriberId() { return subscriberId; }
    public List<Subscription> getSubscriptions() { return subscriptions; }
}
```

```java
// ConsumerGroup.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsumerGroup {
    private final String groupId;
    private final List<Subscriber> subscribers;
    private final Map<Partition, Subscriber> assignments;
    private final Cluster cluster;
    
    public ConsumerGroup(String groupId, Cluster cluster) {
        this.groupId = groupId;
        this.subscribers = new CopyOnWriteArrayList<>();
        this.assignments = new HashMap<>();
        this.cluster = cluster;
    }
    
    public void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
        rebalance();
    }
    
    public void removeSubscriber(String subscriberId) {
        subscribers.removeIf(s -> s.getSubscriberId().equals(subscriberId));
        rebalance();
    }
    
    /**
     * Rebalance partitions across subscribers
     * Strategy: Round-robin assignment
     */
    public synchronized void rebalance() {
        assignments.clear();
        
        if (subscribers.isEmpty()) return;
        
        // Get all subscribed topics
        Set<String> topics = new HashSet<>();
        for (Subscriber subscriber : subscribers) {
            subscriber.getSubscriptions().forEach(sub -> 
                topics.add(sub.getTopic().getTopicName())
            );
        }
        
        // Get all partitions for these topics
        List<Partition> allPartitions = new ArrayList<>();
        for (String topicName : topics) {
            Topic topic = cluster.getTopic(topicName);
            allPartitions.addAll(topic.getAllPartitions());
        }
        
        // Round-robin assignment
        int subscriberIndex = 0;
        for (Partition partition : allPartitions) {
            Subscriber subscriber = subscribers.get(subscriberIndex);
            assignments.put(partition, subscriber);
            
            subscriberIndex = (subscriberIndex + 1) % subscribers.size();
        }
        
        System.out.println("Rebalanced: " + assignments.size() + 
                         " partitions across " + subscribers.size() + " subscribers");
    }
    
    public List<PartitionAssignment> getAssignments(String subscriberId) {
        List<PartitionAssignment> result = new ArrayList<>();
        
        for (Map.Entry<Partition, Subscriber> entry : assignments.entrySet()) {
            if (entry.getValue().getSubscriberId().equals(subscriberId)) {
                result.add(new PartitionAssignment(entry.getKey(), entry.getValue()));
            }
        }
        
        return result;
    }
    
    public String getGroupId() { return groupId; }
    public Cluster getCluster() { return cluster; }
}
```

```java
// PartitionAssignment.java
public class PartitionAssignment {
    private final Partition partition;
    private final Subscriber subscriber;
    private long currentOffset;
    
    public PartitionAssignment(Partition partition, Subscriber subscriber) {
        this.partition = partition;
        this.subscriber = subscriber;
        this.currentOffset = 0;
    }
    
    public void updateOffset(long offset) {
        this.currentOffset = offset;
    }
    
    public long getLag() {
        return partition.getCurrentOffset() - currentOffset;
    }
    
    public Partition getPartition() { return partition; }
    public Subscriber getSubscriber() { return subscriber; }
    public long getCurrentOffset() { return currentOffset; }
}
```

---

### Cluster & Demo

```java
// Cluster.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cluster {
    private final String clusterId;
    private final List<Broker> brokers;
    private final Map<String, Topic> topics;
    
    public Cluster(String clusterId) {
        this.clusterId = clusterId;
        this.brokers = new ArrayList<>();
        this.topics = new ConcurrentHashMap<>();
    }
    
    public void addBroker(Broker broker) {
        brokers.add(broker);
    }
    
    public Topic createTopic(String name, int partitionCount, 
                            int replicationFactor, long retentionMs) {
        Topic topic = new Topic(name, partitionCount, replicationFactor, retentionMs);
        topics.put(name, topic);
        
        // Assign partitions to brokers (round-robin)
        assignPartitionsToBrokers(topic);
        
        return topic;
    }
    
    private void assignPartitionsToBrokers(Topic topic) {
        if (brokers.isEmpty()) {
            throw new IllegalStateException("No brokers available");
        }
        
        List<Partition> partitions = topic.getAllPartitions();
        int brokerIndex = 0;
        
        for (Partition partition : partitions) {
            Broker broker = brokers.get(brokerIndex);
            broker.assignPartition(partition, ReplicaRole.LEADER);
            
            brokerIndex = (brokerIndex + 1) % brokers.size();
        }
    }
    
    public Topic getTopic(String name) {
        Topic topic = topics.get(name);
        if (topic == null) {
            throw new IllegalArgumentException("Topic not found: " + name);
        }
        return topic;
    }
    
    public void deleteTopic(String name) {
        topics.remove(name);
    }
    
    public String getClusterId() { return clusterId; }
    public List<Broker> getBrokers() { return brokers; }
}
```

```java
// PubSubDemo.java
public class PubSubDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Pub/Sub Messaging System Demo ===\n");
        
        // Create cluster with 3 brokers
        Cluster cluster = new Cluster("cluster-1");
        
        Broker broker1 = new Broker("broker-1", "localhost", 9001);
        Broker broker2 = new Broker("broker-2", "localhost", 9002);
        Broker broker3 = new Broker("broker-3", "localhost", 9003);
        
        cluster.addBroker(broker1);
        cluster.addBroker(broker2);
        cluster.addBroker(broker3);
        
        System.out.println("Cluster initialized with 3 brokers\n");
        
        // Create topic with 3 partitions, replication factor 2
        Topic topic = cluster.createTopic(
            "user-events",
            3,  // 3 partitions
            2,  // replication factor
            7 * 24 * 60 * 60 * 1000L  // 7 days retention
        );
        
        System.out.println("Created topic: " + topic.getTopicName());
        System.out.println("Partitions: " + topic.getPartitionCount());
        System.out.println("Replication factor: " + topic.getReplicationFactor() + "\n");
        
        // Demo 1: Publish messages
        System.out.println("--- Demo 1: Publishing Messages ---");
        Publisher publisher = new Publisher("publisher-1", cluster);
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("source", "web");
        
        for (int i = 1; i <= 5; i++) {
            String key = "user-" + (i % 3);  // Keys 0, 1, 2 (go to different partitions)
            String payload = "Event " + i + " for " + key;
            
            Message message = new Message(
                "user-events",
                key,
                payload.getBytes(),
                attributes
            );
            
            PublishResult result = publisher.publish("user-events", message).get();
            System.out.println("Published: " + payload + 
                             " → Partition " + result.getPartition() + 
                             ", Offset " + result.getOffset());
        }
        
        // Demo 2: Consumer Group with multiple subscribers
        System.out.println("\n--- Demo 2: Consumer Group (At-Least-Once) ---");
        
        ConsumerGroup group = new ConsumerGroup("group-1", cluster);
        
        Subscriber subscriber1 = new Subscriber(
            "subscriber-1",
            group,
            PushPullMode.PULL,
            DeliveryGuarantee.AT_LEAST_ONCE
        );
        
        Subscriber subscriber2 = new Subscriber(
            "subscriber-2",
            group,
            PushPullMode.PULL,
            DeliveryGuarantee.AT_LEAST_ONCE
        );
        
        group.addSubscriber(subscriber1);
        group.addSubscriber(subscriber2);
        
        subscriber1.subscribe("user-events", null);
        subscriber2.subscribe("user-events", null);
        
        // Subscribers poll messages
        System.out.println("\nSubscriber 1 polling:");
        List<Message> messages1 = subscriber1.poll(1000);
        for (Message msg : messages1) {
            System.out.println("  Received: " + new String(msg.getPayload()) + 
                             " (Partition " + msg.getPartition() + ")");
            subscriber1.acknowledge(msg);
        }
        
        System.out.println("\nSubscriber 2 polling:");
        List<Message> messages2 = subscriber2.poll(1000);
        for (Message msg : messages2) {
            System.out.println("  Received: " + new String(msg.getPayload()) + 
                             " (Partition " + msg.getPartition() + ")");
            subscriber2.acknowledge(msg);
        }
        
        // Demo 3: Message ordering within partition
        System.out.println("\n--- Demo 3: Message Ordering ---");
        
        String orderKey = "user-123";  // Same key → same partition
        for (int i = 1; i <= 3; i++) {
            Message message = new Message(
                "user-events",
                orderKey,
                ("Order-" + i).getBytes(),
                null
            );
            
            PublishResult result = publisher.publish("user-events", message).get();
            System.out.println("Published Order-" + i + 
                             " → Partition " + result.getPartition());
        }
        
        System.out.println("\nConsuming (should be in order):");
        Subscriber orderSubscriber = new Subscriber(
            "order-subscriber",
            new ConsumerGroup("order-group", cluster),
            PushPullMode.PULL,
            DeliveryGuarantee.AT_LEAST_ONCE
        );
        orderSubscriber.subscribe("user-events", null);
        
        List<Message> orderedMessages = orderSubscriber.poll(1000);
        for (Message msg : orderedMessages) {
            if (new String(msg.getPayload()).startsWith("Order-")) {
                System.out.println("  Received: " + new String(msg.getPayload()));
            }
        }
        
        // Demo 4: Topic metrics
        System.out.println("\n--- Demo 4: Topic Metrics ---");
        System.out.println("Total messages in topic: " + topic.getTotalMessages());
        
        for (Partition partition : topic.getAllPartitions()) {
            System.out.println("Partition " + partition.getPartitionId() + 
                             ": " + partition.getMessageCount() + " messages, " +
                             "Offset range: [" + partition.getStartOffset() + 
                             ", " + partition.getEndOffset() + "]");
        }
        
        System.out.println("\n=== Demo Complete ===");
        
        // Cleanup
        publisher.close();
        subscriber1.close();
        subscriber2.close();
        orderSubscriber.close();
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Publish-Subscribe Pattern
**Where:** Core architecture  
**Why:** Decouple publishers from subscribers  
**Interview Justification:** "Publishers don't know subscribers. Topics act as intermediary. Enables dynamic subscription/unsubscription."

---

### Pattern 2: Partition Strategy
**Where:** Topic partitioning for scalability  
**Why:** Horizontal scaling + ordering guarantee  
**Interview Justification:** "Partition by key maintains ordering within partition. Multiple partitions enable parallel processing by consumer group."

---

### Pattern 3: Leader-Follower Replication
**Where:** Partition replicas  
**Why:** High availability + fault tolerance  
**Interview Justification:** "Leader handles all writes. Followers replicate asynchronously. On leader failure, promote follower to leader."

---

### Pattern 4: Offset Tracking
**Where:** Consumer progress tracking  
**Why:** Enable replay, exactly-once semantics  
**Interview Justification:** "Store offset per (consumer, partition). Allows resuming from last position, reprocessing old data."

---

### Pattern 5: Dead Letter Queue
**Where:** Failed message handling  
**Why:** Prevent message loss, enable debugging  
**Interview Justification:** "After N retries, move to DLQ topic. Ops team can investigate and manually reprocess."

---

## 💡 Step 8: Interview Discussion Points

### 1. Delivery Guarantees: At-Most-Once vs At-Least-Once vs Exactly-Once

**Interviewer:** "How do you implement exactly-once delivery?"

**Answer:**
"**Three delivery semantics:**

**1. At-Most-Once (may lose messages):**
```java
// Auto-commit offset BEFORE processing
subscriber.commitOffset(partition, offset);
message = poll();
process(message);  // If crash here, message lost
```

**Use case:** Metrics, logs (some loss acceptable)  
**Guarantee:** Message delivered 0 or 1 time

---

**2. At-Least-Once (may duplicate):**
```java
// Commit offset AFTER processing
message = poll();
process(message);
subscriber.commitOffset(partition, offset);  // If crash before this, reprocessed
```

**Use case:** Most common (98% of use cases)  
**Guarantee:** Message delivered 1+ times  
**Handling:** Make processing idempotent

---

**3. Exactly-Once (no loss, no duplicate):**
```java
// Two-phase approach: Idempotency + Transactions

// Phase 1: Idempotent production
messageId = generateIdempotentId(key, sequence);
if (alreadyPublished(messageId)) {
    return;  // Skip duplicate publish
}
publish(message, messageId);

// Phase 2: Transactional consumption
transaction.begin();
  message = poll();
  process(message);
  commitOffset(partition, offset);  // In same transaction
transaction.commit();  // Atomic: either both succeed or both fail
```

**Implementation details:**

**Idempotent publishing:**
- Generate deterministic message ID: `hash(publisherId + sequence)`
- Broker deduplicates: check if ID already exists
- Store (messageId → offset) mapping

**Transactional consumption:**
- Process message + commit offset atomically
- Use external transaction coordinator (database, etc.)
- If process fails → rollback offset commit → message redelivered

**Kafka's approach:**
```
Producer: Send with idempotence (producer ID + sequence number)
Broker: Deduplicate based on (producerId, sequence)
Consumer: Write output + offset to same transactional store
```

**Example:**
```java
// Bank transfer (must be exactly-once)
transaction.begin();
  Message msg = poll();  // \"Transfer $100 from A to B\"
  
  // Process in transactional database
  db.execute(\"UPDATE accounts SET balance = balance - 100 WHERE id = 'A'\");
  db.execute(\"UPDATE accounts SET balance = balance + 100 WHERE id = 'B'\");
  
  // Commit offset in same transaction
  db.execute(\"UPDATE consumer_offsets SET offset = ? WHERE partition = ?\");
  
transaction.commit();  // All or nothing
```

**Trade-off:** Exactly-once has 2-3× latency vs at-least-once."

---

### 2. Consumer Lag: Why It Happens & How to Fix

**Interviewer:** "Consumer is lagging behind. How do you diagnose and fix?"

**Answer:**
"**Consumer lag = Latest offset - Consumer offset**

**Diagnosis:**

**1. Check lag per partition:**
```java
for (Partition partition : topic.getPartitions()) {
    long latestOffset = partition.getCurrentOffset();
    long consumerOffset = subscriber.getOffset(partition.getId());
    long lag = latestOffset - consumerOffset;
    
    System.out.println(\"Partition \" + partition.getId() + \": lag = \" + lag);
}
```

**2. Identify root cause:**

**Slow consumer processing:**
```
Producer: 1000 msgs/sec
Consumer: 100 msgs/sec → Lag grows at 900 msgs/sec

Symptom: Lag increases linearly over time
```

**Insufficient consumer instances:**
```
Topic: 10 partitions
Consumers: 3 (each gets ~3 partitions)

If each consumer saturated → add more consumers
```

**Network issues:**
```
Consumer fetching slowly (high network latency)
Check: fetch batch size, network bandwidth
```

**3. Solutions:**

**a) Add more consumers (horizontal scaling):**
```java
// Before: 1 consumer, 10 partitions
ConsumerGroup group = new ConsumerGroup(\"group-1\", cluster);
Subscriber consumer1 = new Subscriber(\"consumer-1\", group);

// After: 10 consumers, 1 partition each (max parallelism)
for (int i = 1; i <= 10; i++) {
    Subscriber consumer = new Subscriber(\"consumer-\" + i, group);
    group.addSubscriber(consumer);
}
// Rebalance: each consumer gets 1 partition
```

**Max consumers = # partitions** (more consumers idle)

**b) Increase batch size (fetch more per poll):**
```java
// Before: Fetch 10 messages/poll → 100 polls/sec
List<Message> messages = subscriber.poll(100, TimeUnit.MILLISECONDS);

// After: Fetch 1000 messages/poll → 1 poll/sec (same throughput, less overhead)
List<Message> messages = subscriber.poll(1000, 10000, TimeUnit.MILLISECONDS);
```

**c) Optimize consumer processing:**
```java
// Parallel processing within consumer
messages.parallelStream().forEach(message -> {
    process(message);
});

// Async I/O (don't block on network calls)
CompletableFuture.allOf(
    messages.stream()
        .map(msg -> CompletableFuture.runAsync(() -> process(msg)))
        .toArray(CompletableFuture[]::new)
).join();
```

**d) Scale producer (if bottleneck is single partition):**
```
// Before: All messages to 1 partition (slow)
publisher.publish(topic, new Message(null, payload));  // No key → round-robin

// After: Partition by key
publisher.publish(topic, new Message(userId, payload));  // userId → specific partition
```

**Monitoring:**
```java
// Alert if lag > threshold
if (lag > 100000) {
    alert(\"Consumer lag critical: \" + lag);
}

// Estimate time to catch up
double consumeRate = 1000;  // msgs/sec
double catchUpTime = lag / consumeRate / 3600;  // hours
System.out.println(\"Time to catch up: \" + catchUpTime + \" hours\");
```"

---

### 3. Message Ordering: Global vs Partition-Level

**Interviewer:** "Can you guarantee global ordering across partitions?"

**Answer:**
"**Short answer: No (with multiple partitions)**

**Why?**

**Single partition (global order possible):**
```
Partition 0: [M1, M2, M3, M4, M5]  → FIFO guaranteed
```

**Multiple partitions (no global order):**
```
Partition 0: [M1, M3, M5]  → Local FIFO
Partition 1: [M2, M4]      → Local FIFO

Consumer reads:
- From P0: M1
- From P1: M2 (published after M1, but read before M3)
- From P0: M3

Result: M2 appears between M1 and M3 (not global order)
```

**Trade-off:**
- **1 partition:** Global order ✅, No parallelism ❌ (single consumer)
- **N partitions:** No global order ❌, High parallelism ✅ (N consumers)

**Solution: Partition by key**

If you need ordering for related messages, use same key:

```java
// All events for user-123 go to same partition
publisher.publish(topic, new Message(\"user-123\", \"event1\"));
publisher.publish(topic, new Message(\"user-123\", \"event2\"));
publisher.publish(topic, new Message(\"user-123\", \"event3\"));

// Guaranteed order: event1 → event2 → event3 (same partition)
```

**Example: E-commerce orders**

```java
// Order for user-123
publish(new Message(\"user-123\", \"OrderCreated\"));
publish(new Message(\"user-123\", \"PaymentReceived\"));
publish(new Message(\"user-123\", \"OrderShipped\"));

// Order for user-456 (different partition, can process in parallel)
publish(new Message(\"user-456\", \"OrderCreated\"));
```

**Result:**
- User-123 events processed in order (same partition)
- User-456 events processed in parallel (different partition)
- Orders for different users don't block each other

**When global order required:**
- Use 1 partition (sacrifice parallelism)
- Or use external sequencing (add timestamp, sort later)
- Or use distributed coordination (ZooKeeper, etcd)"

---

### 4. Replication: Sync vs Async

**Interviewer:** "Should replication be synchronous or asynchronous?"

**Answer:**
"**Kafka/RabbitMQ use async replication. Here's why:**

**Synchronous Replication:**
```java
public long append(Message message) {
    long offset = partition.append(message);
    
    // Wait for all replicas to acknowledge
    for (Replica replica : replicas) {
        replica.append(message);  // BLOCKING
    }
    
    return offset;  // Return only after all replicas ack
}
```

**Pros:** No data loss (all replicas have copy)  
**Cons:** High latency (wait for slowest replica), Reduced availability (write fails if any replica down)

---

**Asynchronous Replication (Kafka's approach):**
```java
public long append(Message message) {
    long offset = partition.append(message);
    
    // Replicate in background
    CompletableFuture.runAsync(() -> {
        for (Replica replica : replicas) {
            replica.append(message);  // NON-BLOCKING
        }
    });
    
    return offset;  // Return immediately
}
```

**Pros:** Low latency (don't wait), High availability (write succeeds even if replicas slow)  
**Cons:** Potential data loss (leader crashes before replication)

---

**Kafka's optimization: In-Sync Replicas (ISR)**

```
Replicas: [Leader, Follower1, Follower2, Follower3]

In-Sync Replicas (ISR): Replicas that are \"caught up\" (< N messages behind)

ISR = [Leader, Follower1, Follower2]  // Follower3 lagging
```

**Write behavior:**
```java
public long append(Message message) {
    long offset = leader.append(message);
    
    // Wait for ISR (not all replicas)
    CountDownLatch latch = new CountDownLatch(ISR.size() - 1);
    
    for (Replica replica : ISR) {
        if (replica != leader) {
            CompletableFuture.runAsync(() -> {
                replica.append(message);
                latch.countDown();
            });
        }
    }
    
    latch.await(100, TimeUnit.MILLISECONDS);  // Wait up to 100ms
    
    return offset;
}
```

**Result:**
- Wait for in-sync replicas (typically 2-3 replicas)
- Don't wait for slow replicas (excluded from ISR)
- Balance durability (most replicas have copy) + latency (don't wait forever)

**Durability guarantee:**
```
min.insync.replicas = 2

Write succeeds only if >= 2 replicas ack
If ISR.size < 2 → reject write (prevent data loss)
```

**My recommendation:** Async with ISR (Kafka model)"

---

### 5. Scaling to 1 Million Messages/Second

**Interviewer:** "How do you scale to 1 million messages per second?"

**Answer:**
"**Vertical Scaling (single broker):**

**1. Sequential disk I/O (KEY OPTIMIZATION):**
```
Random disk writes: 100 writes/sec
Sequential writes: 100,000 writes/sec (1000× faster!)

Pub/Sub uses append-only log → all sequential ✅
```

**2. Zero-copy transfers:**
```java
// Traditional (2 copies):
disk → kernel buffer → user space → kernel socket buffer → NIC
(4 context switches)

// Zero-copy (sendfile syscall):
disk → kernel buffer → NIC
(0 copies to user space, 2 context switches)

// 3× faster throughput
```

**3. Batching:**
```java
// Unbatched: 1 message/write → 10,000 writes/sec
for (Message msg : messages) {
    disk.write(msg);  // 1 ms per write
}

// Batched: 100 messages/write → 100,000 messages at 1000 writes/sec
List<Message> batch = new ArrayList<>();
for (Message msg : messages) {
    batch.add(msg);
    if (batch.size() >= 100) {
        disk.write(batch);  // 1 ms per batch (100 messages)
        batch.clear();
    }
}
```

**Single broker capacity: ~100K msgs/sec**

---

**Horizontal Scaling (cluster):**

**4. Partition topic (10 partitions):**
```
Topic: user-events (1M msgs/sec)

Partition 0: 100K msgs/sec
Partition 1: 100K msgs/sec
...
Partition 9: 100K msgs/sec

Each partition handled by 1 broker → 10 brokers
```

**5. Consumer groups (parallel processing):**
```
Consumer Group: 10 consumers (1 per partition)

Each consumer: 100K msgs/sec → Total: 1M msgs/sec processed
```

**6. Producer partitioning:**
```java
// Client-side partitioning (avoid broker hotspot)
int partition = hash(message.getKey()) % partitionCount;
publisher.publish(topic, partition, message);

// Broker only appends (no routing overhead)
```

**7. Compression:**
```
Average message: 1KB
Compressed (gzip): 300 bytes

Network bandwidth saved: 70%
Disk space saved: 70%
→ Effective throughput: 3.3M msgs/sec (on 1M network capacity)
```

**Architecture:**
```
[Producers × 100] (each 10K msgs/sec)
         ↓
[Load Balancer] (client-side partitioning)
         ↓
[10 Brokers] (each 100K msgs/sec, 1 partition per broker)
  ├─ Partition 0 (replicated 3×)
  ├─ Partition 1 (replicated 3×)
  ...
  └─ Partition 9 (replicated 3×)
         ↓
[Consumer Groups]
  └─ [10 Consumers] (each 100K msgs/sec)

Total: 1M msgs/sec publish, 1M msgs/sec consume
```

**Cost estimate:**
- 10 brokers × 16 cores, 64GB RAM, 1TB SSD = ~$10,000/month
- Storage: 1M msgs/sec × 1KB × 7 days = 604TB → Kafka compressed: ~200TB

**This is how Kafka scales at LinkedIn/Uber.**"

---

## 🗄️ Step 9: Database Schema (Offset Management)

```sql
-- Consumer Offsets Table (persistent offset tracking)
CREATE TABLE consumer_offsets (
    group_id VARCHAR(100) NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL,
    offset_value BIGINT NOT NULL,
    last_updated BIGINT NOT NULL,
    PRIMARY KEY (group_id, topic_name, partition_id),
    INDEX idx_group (group_id),
    INDEX idx_topic (topic_name)
);

-- Topic Metadata Table
CREATE TABLE topics (
    topic_name VARCHAR(100) PRIMARY KEY,
    partition_count INT NOT NULL,
    replication_factor INT NOT NULL,
    retention_ms BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    config JSON
);

-- Partition Assignment Table (tracks which broker hosts which partition)
CREATE TABLE partition_assignments (
    topic_name VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL,
    broker_id VARCHAR(50) NOT NULL,
    replica_role ENUM('LEADER', 'FOLLOWER') NOT NULL,
    is_in_sync BOOLEAN NOT NULL DEFAULT TRUE,
    last_updated BIGINT NOT NULL,
    PRIMARY KEY (topic_name, partition_id, broker_id),
    INDEX idx_broker (broker_id)
);

-- Consumer Group Metadata
CREATE TABLE consumer_groups (
    group_id VARCHAR(100) PRIMARY KEY,
    coordinator_broker VARCHAR(50) NOT NULL,
    state ENUM('EMPTY', 'PREPARING_REBALANCE', 'STABLE') NOT NULL,
    protocol_type VARCHAR(50) NOT NULL DEFAULT 'consumer',
    created_at BIGINT NOT NULL,
    last_rebalance BIGINT NOT NULL
);

-- Broker Metadata Table
CREATE TABLE brokers (
    broker_id VARCHAR(50) PRIMARY KEY,
    host VARCHAR(100) NOT NULL,
    port INT NOT NULL,
    status ENUM('ONLINE', 'OFFLINE', 'DRAINING') NOT NULL,
    last_heartbeat BIGINT NOT NULL,
    disk_space_bytes BIGINT NOT NULL,
    network_bandwidth_mbps INT NOT NULL
);
```

---

## 🛠️ Step 10: Concurrency Handling

### 1. Partition Append Race

**Problem:** Multiple threads append to same partition

**Solution:**
```java
// Synchronize append operation
public synchronized long append(Message message) {
    long offset = currentOffset.getAndIncrement();
    message.setOffset(offset);
    messages.add(message);
    return offset;
}
```

---

### 2. Consumer Rebalance Race

**Problem:** Two consumers join simultaneously

**Solution:**
```java
// Use distributed lock (ZooKeeper/etcd)
public synchronized void rebalance() {
    Lock lock = zookeeper.acquireLock("/consumer-group/" + groupId);
    
    try {
        // Perform rebalance under lock
        reassignPartitions();
    } finally {
        lock.release();
    }
}
```

---

### 3. Offset Commit Race

**Problem:** Consumer commits offset out of order

**Solution:**
```java
// Use ConcurrentHashMap with atomic updates
public void commitOffset(int partition, long offset) {
    offsets.compute(partition, (k, current) -> {
        return current == null || offset > current ? offset : current;
    });
}
```

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Publisher`: Publishes messages only
- `Subscriber`: Consumes messages only
- `Partition`: Stores ordered log only
- `ConsumerGroup`: Manages rebalancing only

### Open/Closed ✅
```java
// Add new message format without modifying core
public interface MessageSerializer {
    byte[] serialize(Message message);
}

public class AvroSerializer implements MessageSerializer { }
public class ProtobufSerializer implements MessageSerializer { }
```

### Liskov Substitution ✅
```java
// Any PushPullMode works
PushPullMode mode = PushPullMode.PULL;
mode = PushPullMode.PUSH;  // Seamless swap
```

### Interface Segregation ✅
```java
interface Publishable {
    PublishResult publish(Message message);
}

interface Subscribable {
    List<Message> poll(long timeout);
}

// Subscriber only implements Subscribable (not forced to implement Publishable)
```

### Dependency Inversion ✅
```java
public class Publisher {
    private Cluster cluster;  // Depends on abstraction
    
    public Publisher(Cluster cluster) {
        this.cluster = cluster;  // Inject dependency
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Publish-Subscribe** pattern with topics & partitions
- ✅ **Partition-based** ordering + parallelism
- ✅ **Consumer groups** for load sharing
- ✅ **Leader-follower** replication (async)
- ✅ **Offset tracking** for replay & exactly-once

### Delivery Guarantees
- ✅ **At-most-once** (commit before process)
- ✅ **At-least-once** (commit after process)
- ✅ **Exactly-once** (idempotency + transactions)

### Scalability
- ✅ **1M msgs/sec** with 10 partitions, 10 brokers
- ✅ Sequential disk I/O (100K writes/sec per broker)
- ✅ Zero-copy transfers (3× throughput)
- ✅ Message batching (10× reduction in overhead)

### Advanced Features
- ✅ Dead letter queue (DLQ)
- ✅ Message filtering
- ✅ Consumer lag monitoring
- ✅ Partition rebalancing
- ✅ Message replay (seek to offset)

---

**Total: 136 DSA + 11 LLD Problems**

All changes ready for review!
