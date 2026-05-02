# Array & string problems — pattern decision tree

Three views: **mindmap** (families at a glance), **flowchart** (questions → pattern), **text tree** (no Mermaid). Many problems **combine** patterns (e.g. **window + hash map**). This is a **heuristic**, not a proof.

---

## 1 · Mindmap (overview)

```mermaid
mindmap
  root((Array and String))
    Contiguous segment
      Fixed k window
        sum max in k
      Variable window
        counts distinct
        HashMap int26
      Sliding min max
        monotonic deque
    Range and sums
      Prefix sum
      Subarray sum K
        plus HashMap
    Order and search
      Binary search on array
      Sort two pointers
      Binary search on answer
    Lookup structure
      HashMap HashSet
      Monotonic stack
      Next greater smaller
    Scheduling
      Sort greedy sweep
    Optimal substructure
      DP two strings
      DP substring
    String tooling
      Rolling hash
      Trie
```

---

## 2 · Flowchart (decisions → pattern)

Start at **Array or string** → **Contiguous?** → **yes** = left box (window family); **no** = right box (follow the chain top to bottom).

```mermaid
flowchart TB
  classDef root fill:#dbeafe,stroke:#1d4ed8,stroke-width:2px,color:#1e3a8a
  classDef q fill:#fff7ed,stroke:#ea580c,stroke-width:1.5px,color:#7c2d12
  classDef p fill:#dcfce7,stroke:#16a34a,stroke-width:1.5px,color:#14532d

  R([Array or string])
  class R root

  R --> Q0{Contiguous<br/>subarray or substring?}
  class Q0 q

  subgraph WIN [Contiguous]
    direction TB
    A1{Fixed length k?}
    A1 -->|yes| P1[Fixed sliding window]
    A1 -->|no| A2{Longest or shortest<br/>under count constraint?}
    A2 -->|yes| P2[Variable window plus map or int26]
    A2 -->|no| P3[Monotonic deque window min or max]
  end

  subgraph REST [Not contiguous]
    direction TB
    B1{Range sums many queries<br/>or subarray sum equals K?}
    B1 -->|yes| P4[Prefix sum optional HashMap]
    B1 -->|no| B2{Sorted or sort unlocks pairs?}
    B2 -->|yes| B2a{Single value or boundary?}
    B2a -->|yes| P5[Binary search on sorted array]
    B2a -->|no| P6[Sort plus two pointers]
    B2 -->|no| B3{O1 by value freq or index?}
    B3 -->|yes| P7[HashMap or HashSet]
    B3 -->|no| B4{Next greater smaller or bars?}
    B4 -->|yes| P8[Monotonic stack]
    B4 -->|no| B5{Minimize max feasible X monotone?}
    B5 -->|yes| P9[Binary search on answer]
    B5 -->|no| B6{Intervals deadlines overlap?}
    B6 -->|yes| P10[Sort plus greedy sweep]
    B6 -->|no| B7{Two strings optimal overlap?}
    B7 -->|yes| P11[DP]
    B7 -->|no| B8{String pattern or dictionary?}
    B8 -->|yes| B8a{Many equal length substring compares?}
    B8a -->|yes| P12[Rolling hash]
    B8a -->|no| B8b{Prefix queries many words?}
    B8b -->|yes| P13[Trie]
    B8b -->|no| P14[Re-read constraints]
    B8 -->|no| P14
  end

  Q0 -->|yes| A1
  Q0 -->|no| B1

  class A1,A2,B1,B2,B2a,B3,B4,B5,B6,B7,B8,B8a,B8b q
  class P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14 p
```

---

## 3 · Text tree (same logic as flowchart)

```
Array / string
├─ contiguous subarray or substring?
│  ├─ YES
│  │  ├─ fixed length k        → fixed sliding window
│  │  ├─ variable length + constraint
│  │  │   └─ counts / distinct  → variable window + HashMap or int[26]
│  │  └─ min/max *inside* window as you slide → monotonic deque
│  └─ NO
│     ├─ many range sums OR “subarray sum = K” / count by sum
│     │  └─ prefix sum (+ HashMap for “sum → index/count”)
│     ├─ sorted (or sort unlocks pairs / intervals)
│     │  ├─ single target / boundary     → binary search on array
│     │  └─ pairs, triplets, intervals   → sort + two pointers (or greedy)
│     ├─ O(1) by value / frequency / “complement”
│     │  └─ HashMap / HashSet
│     ├─ “next greater / smaller” or histogram bars
│     │  └─ monotonic stack
│     ├─ “minimize maximum” chunk / capacity / split (monotone feasible(mid))
│     │  └─ binary search on answer
│     ├─ intervals, deadlines, max non-overlapping
│     │  └─ sort + greedy (often sweep)
│     ├─ optimal over two strings / substring structure
│     │  └─ DP (1D/2D by problem)
│     └─ else
│        ├─ string-heavy: pattern / many substring compares → rolling hash
│        ├─ string-heavy: many prefixes / dictionary → trie
│        └─ otherwise → re-read constraints; hash / greedy / ad hoc
```

---

## Common combos

| Combo | When |
|--------|------|
| **Sliding window + HashMap** | Longest substring with at most K distinct, min window substring |
| **Prefix + HashMap** | Subarray sum equals K; count subarrays with sum |
| **Sort + greedy + (heap)** | Meeting rooms II, task scheduler style |
| **Two pointers + hash** | Dedupe or map while moving ends |

---

## Pitfalls

- Nested “every subarray” brute → **one** scan with **window** or **prefix + map**.
- Re-sorting every step → **sort once** or heap.
- Only `a`–`z` → prefer **`int[26]`** over `HashMap` when counts fit.

Re-check **`n`**, **value bounds**, **sorted?**, **mod input allowed?** before finalizing.
