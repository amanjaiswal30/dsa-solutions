# LRU Cache

**Difficulty:** Medium ⚡  
**Topics:** `Linked Lists`, `Hash Table`, `Design`

---

## 📜 Problem Statement

Design a data structure that follows the constraints of a **Least Recently Used (LRU) cache**.

Implement the `LRUCache` class:

- `LRUCache(int capacity)` Initialize the LRU cache with **positive** size `capacity`.
- `int get(int key)` Return the value of the `key` if the key exists, otherwise return `-1`.
- `void put(int key, int value)` Update the value of the `key` if the `key` exists. Otherwise, add the `key-value` pair to the cache. If the number of keys exceeds the `capacity` from this operation, **evict** the least recently used key.

The functions `get` and `put` must each run in **O(1)** average time complexity.

---

## 💡 Examples

### Example 1:

**Input:**
```
["LRUCache", "put", "put", "get", "put", "get", "put", "get", "get", "get"]
[[2], [1, 1], [2, 2], [1], [3, 3], [2], [4, 4], [1], [3], [4]]
```

**Output:**
```
[null, null, null, 1, null, -1, null, -1, 3, 4]
```

**Explanation:**
```java
LRUCache lRUCache = new LRUCache(2);
lRUCache.put(1, 1); // cache is {1=1}
lRUCache.put(2, 2); // cache is {1=1, 2=2}
lRUCache.get(1);    // return 1
lRUCache.put(3, 3); // LRU key was 2, evicts key 2, cache is {1=1, 3=3}
lRUCache.get(2);    // returns -1 (not found)
lRUCache.put(4, 4); // LRU key was 1, evicts key 1, cache is {4=4, 3=3}
lRUCache.get(1);    // return -1 (not found)
lRUCache.get(3);    // return 3
lRUCache.get(4);    // return 4
```

### Example 2:

**Input:**
```
["LRUCache", "put", "put", "put", "put", "get", "get"]
[[2], [2, 1], [1, 1], [2, 3], [4, 1], [1], [2]]
```

**Output:**
```
[null, null, null, null, null, -1, 3]
```

**Explanation:**
```java
LRUCache cache = new LRUCache(2);
cache.put(2, 1); // cache is {2=1}
cache.put(1, 1); // cache is {2=1, 1=1}
cache.put(2, 3); // update key 2, cache is {1=1, 2=3}
cache.put(4, 1); // evict key 1, cache is {2=3, 4=1}
cache.get(1);    // return -1 (not found)
cache.get(2);    // return 3
```

---

## 🎯 Constraints

- `1 <= capacity <= 3000`
- `0 <= key <= 10^4`
- `0 <= value <= 10^5`
- At most `2 * 10^5` calls will be made to `get` and `put`

---

## 🔍 Intuition & Logic

### Key Insights:

1. **LRU Policy:** When cache is full, evict the **least recently used** key
2. **Recent Usage:** Both `get` and `put` count as "using" a key
3. **O(1) Requirement:** Need instant access and instant eviction

### Why HashMap + Doubly Linked List?

**HashMap:**
- **O(1) lookup** by key
- Maps `key → Node` in linked list

**Doubly Linked List:**
- **O(1) removal** from middle (need prev pointer)
- **O(1) addition** to head/tail
- Maintains **LRU order** (head = most recent, tail = least recent)

### Structure:

```
HashMap: key → Node

Doubly Linked List (Most Recent → Least Recent):

  head                                      tail
   ↓                                         ↓
[Dummy] ↔ [Node(k1,v1)] ↔ [Node(k2,v2)] ↔ [Dummy]
           (most recent)   (least recent)

- head.next = Most Recently Used (MRU)
- tail.prev = Least Recently Used (LRU)
```

### Operations:

**GET(key):**
1. If key not in HashMap → return -1
2. If key exists:
   - Get node from HashMap
   - **Move node to head** (mark as most recently used)
   - Return node.value

**PUT(key, value):**
1. If key exists:
   - Update node.value
   - **Move node to head**
2. If key doesn't exist:
   - Create new node
   - Add to HashMap
   - **Add node to head**
   - If size > capacity:
     - **Remove tail.prev** (LRU)
     - Remove from HashMap

---

## 🧩 Approaches

### Approach 1: HashMap + Doubly Linked List (Optimal)

**Implementation:**

```java
class LRUCache {
    // Node for doubly linked list
    class Node {
        int key;
        int value;
        Node prev;
        Node next;
        
        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final int capacity;
    private final Map<Integer, Node> cache;  // key → Node
    private final Node head;  // Dummy head (most recent)
    private final Node tail;  // Dummy tail (least recent)
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        
        // Initialize dummy nodes
        head = new Node(-1, -1);
        tail = new Node(-1, -1);
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        
        Node node = cache.get(key);
        
        // Move to head (most recently used)
        moveToHead(node);
        
        return node.value;
    }
    
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            // Update existing key
            Node node = cache.get(key);
            node.value = value;
            moveToHead(node);
        } else {
            // Add new key
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
            
            // Check capacity
            if (cache.size() > capacity) {
                // Remove LRU (tail.prev)
                Node lru = tail.prev;
                removeNode(lru);
                cache.remove(lru.key);
            }
        }
    }
    
    // Helper: Add node right after head
    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
    
    // Helper: Remove node from list
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    // Helper: Move existing node to head
    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }
}

/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
```

**Detailed Walkthrough:**

**Initial State (capacity = 2):**
```
HashMap: {}
List: [head] ↔ [tail]
```

**put(1, 1):**
```
HashMap: {1 → Node(1,1)}
List: [head] ↔ [Node(1,1)] ↔ [tail]
```

**put(2, 2):**
```
HashMap: {1 → Node(1,1), 2 → Node(2,2)}
List: [head] ↔ [Node(2,2)] ↔ [Node(1,1)] ↔ [tail]
                (MRU)         (LRU)
```

**get(1):** (Move Node(1,1) to head)
```
HashMap: {1 → Node(1,1), 2 → Node(2,2)}
List: [head] ↔ [Node(1,1)] ↔ [Node(2,2)] ↔ [tail]
                (MRU)         (LRU)
Return: 1
```

**put(3, 3):** (Evict LRU = Node(2,2))
```
HashMap: {1 → Node(1,1), 3 → Node(3,3)}
List: [head] ↔ [Node(3,3)] ↔ [Node(1,1)] ↔ [tail]
                (MRU)         (LRU)
```

**get(2):**
```
Not in HashMap → Return -1
```

**Complexity Analysis:**

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| `get(key)` | O(1) | O(1) |
| `put(key, value)` | O(1) | O(1) |
| **Overall Space** | - | **O(capacity)** |

**Space Breakdown:**
- HashMap: O(capacity) for storing key-node mappings
- Doubly Linked List: O(capacity) for storing nodes
- Total: O(capacity)

---

### Alternative Approach: Using LinkedHashMap (Java Built-in)

**Implementation:**

```java
import java.util.LinkedHashMap;
import java.util.Map;

class LRUCache extends LinkedHashMap<Integer, Integer> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        // accessOrder = true → LRU order (most recent at tail)
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }
    
    public int get(int key) {
        return super.getOrDefault(key, -1);
    }
    
    public void put(int key, int value) {
        super.put(key, value);
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        // Remove eldest (LRU) when size exceeds capacity
        return size() > capacity;
    }
}
```

**Pros:**
- ✅ Very concise (5 lines of code)
- ✅ Built-in LRU ordering
- ✅ O(1) operations

**Cons:**
- ❌ Uses Java-specific API (not portable)
- ❌ Less control over internals
- ❌ Interviewers may not accept (want to see manual implementation)

**Complexity:** Same as Approach 1 (O(1) time, O(capacity) space)

---

## 🧪 Edge Cases

1. **Capacity = 1:**
   ```java
   LRUCache cache = new LRUCache(1);
   cache.put(1, 1);
   cache.put(2, 2); // Evict key 1
   cache.get(1);    // -1
   cache.get(2);    // 2
   ```

2. **Update existing key:**
   ```java
   cache.put(1, 1);
   cache.put(1, 10); // Update, don't evict
   cache.get(1);     // 10
   ```

3. **Access pattern (MRU protection):**
   ```java
   LRUCache cache = new LRUCache(2);
   cache.put(1, 1);
   cache.put(2, 2);
   cache.get(1);    // Key 1 becomes MRU
   cache.put(3, 3); // Evict key 2 (not key 1)
   cache.get(2);    // -1
   cache.get(1);    // 1
   ```

4. **All gets (no eviction):**
   ```java
   cache.put(1, 1);
   cache.get(1);    // 1
   cache.get(1);    // 1 (no eviction, just reorder)
   ```

---

## 🎓 Key Learnings

1. **Doubly Linked List:**
   - Need `prev` pointer for O(1) removal from middle
   - Singly linked list would be O(n) to find previous node

2. **Dummy Nodes:**
   - Simplify edge cases (empty list, single node)
   - No null checks needed

3. **Why Not Other Data Structures?**
   - **Array:** O(n) to shift elements
   - **Binary Heap:** O(log n) operations
   - **BST:** O(log n) operations
   - **Only HashMap + DLL gives O(1)** for both get and put

4. **Interview Tips:**
   - Start with brute force (explain why it's slow)
   - Mention LinkedHashMap (shows knowledge)
   - Implement manual HashMap + DLL (shows skill)

---

## 🔗 Related Problems

| Problem | Difficulty | Similarity |
|---------|-----------|------------|
| [LFU Cache](https://leetcode.com/problems/lfu-cache/) | Hard 🔥 | Similar design problem (evict by frequency) |
| [Design Browser History](https://leetcode.com/problems/design-browser-history/) | Medium ⚡ | Doubly linked list for navigation |
| [All O(1) Data Structure](https://leetcode.com/problems/all-oone-data-structure/) | Hard 🔥 | HashMap + DLL for O(1) operations |

---

## ❓ Follow-up Questions

1. **Q:** How would you implement an LFU (Least Frequently Used) cache?  
   **A:** Track access frequency for each key. Use HashMap + DLL per frequency level.

2. **Q:** How would you make this thread-safe?  
   **A:** Use `synchronized` methods or `ConcurrentHashMap` + lock-free linked list.

3. **Q:** How would you add TTL (time-to-live) to keys?  
   **A:** Store `expiresAt` timestamp in node. Background thread removes expired keys.

4. **Q:** Can you do this with only HashMap?  
   **A:** Yes, but not O(1) for eviction. Need to iterate all keys to find LRU.

5. **Q:** How to implement distributed LRU cache across multiple servers?  
   **A:** See consistent hashing, replication, and eviction in distributed caches; for full LLD practice see the [Low-Level Design](../low_level_design/) collection (e.g. [Stack Overflow](../low_level_design/18_stack_overflow_lld.md) for large-scale read-heavy designs). ✨

---

## 🏆 Similar Interview Questions

- **Google:** Design a cache with expiration
- **Facebook:** Implement LRU cache with O(1) operations
- **Amazon:** Design a cache eviction policy
- **Microsoft:** Implement a cache with size limits

---

**Recommended Practice Order:**
1. Implement basic LRU cache (this problem)
2. Add TTL support
3. Make it thread-safe
4. Implement LFU cache (harder variant)
