# Low-Level Design: Stack Overflow System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-75 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle a complex system in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a Q&A platform like Stack Overflow where users can post questions, provide answers, vote on content, and earn reputation."*

### Clarifying Questions to Ask:

1. **Q:** What are the core features we need to support?  
   **A:** Questions, answers, comments, voting, tagging, search, reputation system.

2. **Q:** Are there different types of users with different permissions?  
   **A:** Yes - guests (read only), members (post/vote), moderators (edit/delete any), admins (full control).

3. **Q:** How does the voting system work?  
   **A:** Users can upvote or downvote questions and answers. Votes affect reputation.

4. **Q:** How is reputation calculated?  
   **A:** +10 for answer upvote, +5 for question upvote, +15 for accepted answer, -2 for downvote.

5. **Q:** Can users edit or delete their own content?  
   **A:** Yes, users can edit/delete their own content. Moderators can edit/delete any content.

6. **Q:** Should we support real-time notifications?  
   **A:** Yes, notify users when their question gets answered, answer gets accepted, etc.

7. **Q:** How should search work?  
   **A:** Search by keywords, tags, author. Filter by date, votes, answer status.

8. **Q:** What about tags?  
   **A:** Questions can have multiple tags (max 5). Users can search by tags.

9. **Q:** Scale expectations?  
   **A:** Should support millions of users, tens of millions of questions, handle 10,000+ concurrent users.

10. **Q:** Do we need badges/gamification?  
    **A:** Yes, award badges for milestones (first answer, 100 upvotes, etc.).

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

Listed point-wise as interviewer presents them:

1. Users should be able to register and create profiles with username and email
2. Users should be able to post questions with title, description, and tags
3. Users should be able to post answers to any open question
4. Users should be able to add comments on questions and answers
5. Users should be able to upvote or downvote questions and answers
6. Each user can vote only once per question/answer (can change vote)
7. Question owners should be able to accept one answer as the correct solution
8. Users should be able to edit their own questions, answers, and comments
9. Users should be able to delete their own content
10. Moderators should be able to edit or delete any content
11. Moderators should be able to close questions (duplicate, off-topic, etc.)
12. Users should earn reputation points based on votes and accepted answers
13. Users should earn badges for achievements (milestones)
14. Users should be able to search questions by keywords, tags, or author
15. Users should receive notifications for answers, comments, votes on their content
16. Questions should support multiple tags (1-5 tags per question)
17. System should prevent duplicate votes from the same user
18. Higher reputation users should get additional privileges (close votes, create tags, etc.)

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS (Systematic Analysis)**

#### Step-by-Step NFR Deduction:

#### 1. **Scalability Analysis**

**Think:** "What volumes are we dealing with?"
- Millions of users
- Tens of millions of questions
- Hundreds of millions of votes
- 10,000+ concurrent users

**Deduced NFRs:**
- ✅ System must support horizontal scaling (add more servers)
- ✅ Database must be shardable (by user ID or question ID)
- ✅ Need distributed caching (Redis) for hot content
- ✅ Search index separate from primary database (Elasticsearch)

**Why these matter:** Monolithic database won't handle millions of records efficiently.

---

#### 2. **Consistency Analysis**

**Think:** "What data must always be accurate?"
- Vote counts (users trust this for quality)
- Reputation scores (determines privileges)
- Accepted answer status (only one per question)
- User uniqueness (no duplicate usernames/emails)

**Deduced NFRs:**
- ✅ Strong consistency for votes (can't lose votes)
- ✅ ACID transactions for reputation updates
- ✅ Unique constraints on usernames and emails
- ✅ Atomic operations for vote changes (upvote → downvote)

**Trade-off Discussion:**
"We can use eventual consistency for less critical data like view counts or notification delivery, but votes must be strongly consistent."

---

#### 3. **Availability Analysis**

**Think:** "What happens if system goes down?"
- Users can't access critical information
- Loss of productivity for developers
- Impact on company reputation

**Deduced NFRs:**
- ✅ 99.95% availability target (< 4.5 hours downtime/year)
- ✅ Read replicas for high read throughput
- ✅ Graceful degradation (serve cached data if DB down)
- ✅ No single point of failure

**Why 99.95% not 99.99%?** Cost-benefit analysis. Higher availability exponentially more expensive.

---

#### 4. **Performance Analysis**

**Think:** "What operations are time-critical?"
- **Search:** Users expect results < 500ms
- **Page load:** Question page should load < 200ms
- **Vote:** Instant feedback expected < 100ms
- **Posting:** Can tolerate 1-2 seconds

**Deduced NFRs:**
- ✅ Search response time < 500ms (use Elasticsearch)
- ✅ Question page load < 200ms (cache hot questions)
- ✅ Vote operation < 100ms (async reputation update)
- ✅ Database queries < 50ms (proper indexes)

**Bottleneck Identification:**
- Search is most expensive → separate search service
- Hot questions accessed frequently → aggressive caching

---

#### 5. **Concurrency Analysis**

**Think:** "What can happen simultaneously?"
- Multiple users voting on same question
- User voting while someone else accepts answer
- Multiple users answering same question
- Concurrent edits to same content

**Deduced NFRs:**
- ✅ Thread-safe vote processing (prevent double voting)
- ✅ Optimistic locking for content edits (version field)
- ✅ Atomic reputation updates
- ✅ Race condition prevention for accepted answer

**Critical Sections:**
```java
// Prevent double voting
synchronized (voteLock) {
    if (hasUserVoted(userId, questionId)) {
        return false;
    }
    recordVote(vote);
}
```

---

#### 6. **Security Analysis**

**Think:** "What can malicious users do?"
- Spam questions/answers
- Vote manipulation (fake accounts)
- XSS attacks in content
- SQL injection in search
- DDOS attacks

**Deduced NFRs:**
- ✅ Rate limiting (max 10 questions/day for new users)
- ✅ Input sanitization (prevent XSS)
- ✅ CAPTCHA for registration
- ✅ Reputation-based throttling (low rep = strict limits)
- ✅ Content moderation queue

---

**NFR Summary Table:**

| Dimension | Requirement | Deduction Method | Impact |
|-----------|-------------|------------------|--------|
| Scalability | Support 10M+ users | Load estimate | Sharding, caching |
| Consistency | Accurate vote counts | Business criticality | ACID transactions |
| Availability | 99.95% uptime | User impact | Read replicas, failover |
| Performance | Search < 500ms | User expectation | Elasticsearch, indexes |
| Concurrency | 10K concurrent users | Simultaneous operations | Locks, atomic ops |
| Security | Prevent spam/abuse | Threat modeling | Rate limiting, validation |

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Systematic Entity Identification Method

**Three-Step Process:**

#### Step 1: Noun Extraction from Requirements

Go through each requirement and extract nouns:

| Requirement | Nouns Identified |
|-------------|------------------|
| "Users should register and create profiles" | User, Profile |
| "Users post questions with title, description, tags" | Question, Tag |
| "Users post answers to questions" | Answer |
| "Users add comments on questions and answers" | Comment |
| "Users upvote or downvote" | Vote, Upvote, Downvote |
| "Question owners accept one answer" | Acceptance |
| "Users earn reputation points" | Reputation |
| "Users earn badges for achievements" | Badge, Achievement |
| "Search questions by keywords" | Search |
| "Receive notifications" | Notification |

#### Step 2: Entity Validation (Does it qualify?)

**Criteria for an entity:**
1. Has multiple attributes
2. Has behavior (methods)
3. Has lifecycle (CRUD operations)
4. Appears in multiple requirements
5. Has relationships with other entities

**Analysis:**

| Noun | Attributes? | Behavior? | Lifecycle? | Relationships? | Entity? |
|------|-------------|-----------|------------|----------------|---------|
| User | ✅ name, email, rep | ✅ post, vote | ✅ CRUD | ✅ owns questions | ✅ YES |
| Profile | ⚠️ same as User | ⚠️ no unique behavior | ⚠️ part of User | ⚠️ none | ❌ NO (merge with User) |
| Question | ✅ title, desc, votes | ✅ accept, close | ✅ CRUD | ✅ has answers, tags | ✅ YES |
| Answer | ✅ content, votes | ✅ accept, edit | ✅ CRUD | ✅ belongs to question | ✅ YES |
| Comment | ✅ content, author | ✅ edit, delete | ✅ CRUD | ✅ belongs to Q/A | ✅ YES |
| Vote | ✅ user, type, target | ✅ cast, change | ✅ CRUD | ✅ links user + content | ✅ YES |
| Upvote/Downvote | ⚠️ just a type | ❌ no unique behavior | ❌ no lifecycle | ❌ same as Vote | ❌ NO (enum in Vote) |
| Tag | ✅ name, desc, count | ✅ associate | ✅ CRUD | ✅ many questions | ✅ YES |
| Reputation | ⚠️ just a number | ⚠️ add/subtract | ⚠️ part of User | ⚠️ belongs to User | ❌ NO (attribute of User) |
| Badge | ✅ name, level, icon | ✅ award | ✅ CRUD | ✅ many users | ✅ YES |
| Notification | ✅ content, type, read | ✅ send, mark read | ✅ CRUD | ✅ belongs to user | ✅ YES |
| Search | ⚠️ just a query | ⚠️ service logic | ❌ no state | ❌ no relationships | ❌ NO (Service class) |

#### Step 3: Group and Refine

**Group 1: Core Content Entities**
- Question, Answer, Comment → hierarchical relationship

**Group 2: User Management**
- User (with role variants: Guest, Member, Moderator, Admin)

**Group 3: Interaction Entities**
- Vote, Tag, Badge

**Group 4: System Entities**
- Notification

**Group 5: Services (Not entities)**
- Search, Authentication, ReputationCalculator

### Final Entity List (8 Core Entities)

1. **User** (Abstract base) + 4 role types
2. **Question** (Core content)
3. **Answer** (Core content)
4. **Comment** (Supporting content)
5. **Vote** (Interaction)
6. **Tag** (Categorization)
7. **Badge** (Gamification)
8. **Notification** (Communication)

**Supporting Concepts (Not separate entities):**
- Reputation → attribute of User
- Search → Service class
- Authentication → Service class

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Systematic Relationship Mapping

**Three-Pass Methodology:**

---

### Pass 1: Inheritance Hierarchies (IS-A)

**Rule:** "X is a type of Y" → Inheritance

#### Analysis:

**User Types:**
```
Guest IS-A User? → Yes (has username, can read)
Member IS-A User? → Yes (inherits from User, adds posting capability)
Moderator IS-A User? → Yes (inherits from Member, adds moderation)
Admin IS-A User? → Yes (inherits, adds system management)
```

**Decision:** Create User hierarchy
```
User (Abstract)
  ├─ Guest (read-only)
  ├─ Member (post, vote, comment)
  ├─ Moderator (+ moderate, close, delete)
  └─ Admin (+ manage system, create badges)
```

**Why not interfaces?**
- Shared state (username, email, reputation)
- Shared behavior (getters, reputation management)
- Clear hierarchical privileges

**No other IS-A relationships because:**
- Answer is NOT a type of Question (different purpose)
- Comment is NOT a type of Answer (different structure)
- Vote is NOT a type of Comment (completely different)

---

### Pass 2: Ownership Relationships (HAS-A)

**Method:** For each entity pair, ask three questions:

**Question Set:**
1. "Does X contain/own Y?"
2. "Can Y exist without X?"
3. "If X is deleted, should Y be deleted?"

Let's apply systematically:

#### Question ↔ Answer

**Q1:** Does Question contain Answers? → **Yes**  
**Q2:** Can Answer exist without Question? → **No** (answers are responses)  
**Q3:** Delete Question → Delete Answers? → **Yes** (answers meaningless without question)

**Conclusion:** **Composition** (strong ownership)
```
Question ◆────→ Answer [1:N]
```

#### Question ↔ Comment

**Q1:** Does Question contain Comments? → **Yes**  
**Q2:** Can Comment exist without Question? → **No**  
**Q3:** Delete Question → Delete Comments? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
Question ◆────→ Comment [1:N]
```

#### Answer ↔ Comment

**Q1:** Does Answer contain Comments? → **Yes**  
**Q2:** Can Comment exist without Answer? → **No**  
**Q3:** Delete Answer → Delete Comments? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
Answer ◆────→ Comment [1:N]
```

#### User ↔ Question

**Q1:** Does User contain Questions? → **Kind of** (authored by)  
**Q2:** Can Question exist without User? → **Yes** (show as [deleted user])  
**Q3:** Delete User → Delete Questions? → **No** (preserve content)

**Conclusion:** **Aggregation** (weak reference)
```
User ◇────→ Question [1:N]
```

#### User ↔ Answer

**Same analysis as User-Question**

**Conclusion:** **Aggregation**
```
User ◇────→ Answer [1:N]
```

#### Vote ↔ Question/Answer

**Q1:** Does Vote own Question/Answer? → **No** (just references)  
**Q2:** Can Vote exist without target? → **No** (must have target)  
**Q3:** Delete Vote → Delete target? → **No** (vote just disappears)

**Conclusion:** **Association** (reference only)
```
Vote ─────→ Question/Answer [N:1]
```

---

### Pass 3: Cardinality Mapping

For each relationship, determine "how many?"

| Relationship | From | To | Cardinality | Reasoning |
|--------------|------|----|-------------|-----------|
| User → Question | 1 | 0..N | 1:N | User can post multiple questions |
| User → Answer | 1 | 0..N | 1:N | User can post multiple answers |
| User → Comment | 1 | 0..N | 1:N | User can post multiple comments |
| User → Vote | 1 | 0..N | 1:N | User can vote on multiple items |
| Question → Answer | 1 | 0..N | 1:N | Question can have multiple answers |
| Question → Comment | 1 | 0..N | 1:N | Question can have multiple comments |
| Answer → Comment | 1 | 0..N | 1:N | Answer can have multiple comments |
| Question → Tag | 1 | 1..5 | N:M | Question has 1-5 tags, tag on many questions |
| User → Badge | 1 | 0..N | N:M | User earns many badges, badge given to many users |
| Question → Vote | 1 | 0..N | 1:N | Question receives many votes |
| Answer → Vote | 1 | 0..N | 1:N | Answer receives many votes |
| Question → AcceptedAnswer | 1 | 0..1 | 1:0..1 | Question has at most one accepted answer |
| User → Notification | 1 | 0..N | 1:N | User receives many notifications |

---

### Relationship Design Decisions

#### Decision 1: Comment Polymorphism

**Problem:** Comments can be on Questions OR Answers

**Option A:** Separate tables (QuestionComment, AnswerComment)
- ❌ Code duplication
- ❌ Can't query all comments easily

**Option B:** Single Comment table with parent reference
- ✅ Single implementation
- ✅ Use interface `Commentable`
- ✅ Flexible for future (comments on comments)

**Choice:** **Option B** - Use polymorphic association

```java
public interface Commentable {
    long getId();
    void addComment(Comment comment);
}

public class Question implements Commentable { ... }
public class Answer implements Commentable { ... }
```

#### Decision 2: Vote Polymorphism

**Problem:** Votes can be on Questions OR Answers

**Same analysis as Comment**

**Choice:** Use `Votable` interface

```java
public interface Votable {
    long getId();
    void addVote(Vote vote);
    int getVoteCount();
}
```

#### Decision 3: Many-to-Many Junction Tables

**Question ↔ Tag:** Need junction table `QuestionTag`
**User ↔ Badge:** Need junction table `UserBadge`

**Why not just List?**
- Need to track additional info (date earned for badges)
- Need efficient queries both ways
- Database requires junction table anyway

---

### Complete Relationship Diagram

```
┌─────────┐
│  User   │
└────┬────┘
     │
     ├─── [1:N Aggregation] ──→ Question
     │                              │
     │                              ├─── [1:N Composition] ──→ Answer
     │                              │                              │
     │                              ├─── [1:N Composition] ──→ Comment
     │                              │                              │
     │                              └─── [N:M Association] ──→ Tag
     │                                                           
     ├─── [1:N Aggregation] ──→ Answer
     │                              │
     │                              └─── [1:N Composition] ──→ Comment
     │
     ├─── [1:N Association] ──→ Vote ──→ Question/Answer (polymorphic)
     │
     ├─── [N:M Association] ──→ Badge (via UserBadge junction)
     │
     └─── [1:N Association] ──→ Notification


Special Relationships:
Question ──→ [0..1] AcceptedAnswer (specific Answer reference)
Vote ──→ [1] Votable (polymorphic to Question or Answer)
Comment ──→ [1] Commentable (polymorphic to Question or Answer)
```

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Design Strategy:
1. Core entities first (User, Question, Answer)
2. Supporting entities (Comment, Vote, Tag)
3. Interfaces for polymorphism
4. Service classes for business logic

---

### Class Diagram 1: User Hierarchy

```
┌────────────────────────────────────────────────┐
│            <<abstract>>                        │
│               User                             │
├────────────────────────────────────────────────┤
│ - id: long                                     │
│ - username: String                             │
│ - email: String                                │
│ - passwordHash: String                         │
│ - reputation: int                              │
│ - createdAt: LocalDateTime                     │
│ - role: UserRole                               │
│ - questions: List<Question>           ◇────────┼──→ Question [0..*]
│ - answers: List<Answer>               ◇────────┼──→ Answer [0..*]
│ - badges: List<UserBadge>                      │
├────────────────────────────────────────────────┤
│ + User(id, username, email, password, role)    │
│ + canModerate(): boolean [abstract]            │
│ + canCloseQuestion(): boolean [abstract]       │
│ + canDeleteAny(): boolean [abstract]           │
│ + canVote(): boolean                           │
│ + canComment(): boolean                        │
│ + addReputation(points: int): void             │
│ + deductReputation(points: int): void          │
└────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼───────────┬───────────┐
        │           │           │           │
┌───────┴────┐ ┌───┴──────┐ ┌─┴─────────┐ ┌┴──────┐
│   Guest    │ │  Member  │ │ Moderator │ │ Admin │
├────────────┤ ├──────────┤ ├───────────┤ ├───────┤
│            │ │          │ │           │ │       │
├────────────┤ ├──────────┤ ├───────────┤ ├───────┤
│+canModerate│ │+canMod.. │ │+canMod..  │ │+can...│
│  : false   │ │  : false │ │  : true   │ │: true │
│+canVote    │ │+canVote  │ │           │ │+ban...│
│  : false   │ │  : true  │ │           │ │+crea..│
└────────────┘ └──────────┘ └───────────┘ └───────┘
```

**Design Rationale:**
- Abstract parent: Common state and behavior
- Concrete children: Role-specific permissions
- Reputation in parent: All users (except Guest) have reputation
- Template methods: canVote(), canComment() check reputation

---

### Class Diagram 2: Interfaces (Polymorphism)

```
┌─────────────────────┐      ┌──────────────────────┐
│   <<interface>>     │      │   <<interface>>      │
│      Votable        │      │    Commentable       │
├─────────────────────┤      ├──────────────────────┤
│ + getId(): long     │      │ + getId(): long      │
│ + addVote(v: Vote)  │      │ + addComment(c)      │
│ + removeVote(v)     │      │ + getComments()      │
│ + getVoteCount()    │      │                      │
└─────────────────────┘      └──────────────────────┘
         △                            △
         │                            │
    ┌────┴────┐                  ┌────┴────┐
    │         │                  │         │
┌───┴───┐ ┌──┴────┐         ┌───┴───┐ ┌──┴────┐
│Question│ │Answer │         │Question│ │Answer │
└────────┘ └───────┘         └────────┘ └───────┘
```

**Why interfaces?**
- Questions and Answers both can be voted on → share `Votable`
- Questions and Answers both can have comments → share `Commentable`
- Avoids code duplication
- Vote/Comment classes work with interface, not concrete types

---

### Class Diagram 3: Question Entity (Central)

```
┌──────────────────────────────────────────────────────┐
│            Question                                  │
│         implements Votable, Commentable              │
├──────────────────────────────────────────────────────┤
│ - id: long                                           │
│ - title: String                                      │
│ - description: String                                │
│ - author: User                           ◇───────────┼──→ User
│ - createdAt: LocalDateTime                           │
│ - modifiedAt: LocalDateTime                          │
│ - viewCount: int                                     │
│ - voteCount: int                                     │
│ - status: QuestionStatus                             │
│ - acceptedAnswer: Answer                 ◇───────────┼──→ Answer [0..1]
│ - tags: List<Tag>                        ◇───────────┼──→ Tag [1..5]
│ - answers: List<Answer>                  ◆───────────┼──→ Answer [0..*]
│ - comments: List<Comment>                ◆───────────┼──→ Comment [0..*]
│ - votes: List<Vote>                      ◆───────────┼──→ Vote [0..*]
├──────────────────────────────────────────────────────┤
│ + Question(id, title, desc, author)                  │
│ + addAnswer(answer: Answer): void                    │
│ + acceptAnswer(answer: Answer, acceptor: User): bool │
│ + addComment(comment: Comment): void                 │
│ + addTag(tag: Tag): void                             │
│ + close(closer: User, reason: String): void          │
│ + incrementViewCount(): void                         │
│ + addVote(vote: Vote): void [from Votable]           │
│ + getVoteCount(): int [from Votable]                 │
│ + getComments(): List<Comment> [from Commentable]    │
└──────────────────────────────────────────────────────┘
```

**Relationship Explanations:**

| Related Entity | Type | Symbol | Why? |
|----------------|------|--------|------|
| User (author) | Aggregation | ◇ | Question survives user deletion |
| Answer | Composition | ◆ | Answers deleted with question |
| Comment | Composition | ◆ | Comments deleted with question |
| Vote | Composition | ◆ | Votes deleted with question |
| Tag | Aggregation | ◇ | Tags exist independently |
| AcceptedAnswer | Aggregation | ◇ | Just a reference to one answer |

---

### Class Diagram 4: Answer Entity

```
┌──────────────────────────────────────────────────────┐
│              Answer                                  │
│         implements Votable, Commentable              │
├──────────────────────────────────────────────────────┤
│ - id: long                                           │
│ - content: String                                    │
│ - author: User                           ◇───────────┼──→ User
│ - question: Question                     ◇───────────┼──→ Question
│ - createdAt: LocalDateTime                           │
│ - modifiedAt: LocalDateTime                          │
│ - voteCount: int                                     │
│ - isAccepted: boolean                                │
│ - comments: List<Comment>                ◆───────────┼──→ Comment [0..*]
│ - votes: List<Vote>                      ◆───────────┼──→ Vote [0..*]
├──────────────────────────────────────────────────────┤
│ + Answer(id, content, author, question)              │
│ + addComment(comment: Comment): void                 │
│ + edit(newContent: String, editor: User): void       │
│ + setAccepted(accepted: boolean): void               │
│ + addVote(vote: Vote): void [from Votable]           │
│ + getVoteCount(): int [from Votable]                 │
│ + getComments(): List<Comment> [from Commentable]    │
└──────────────────────────────────────────────────────┘
```

**Key Design Point:**
- Answer references back to Question (bidirectional)
- Needed for: "Get question of this answer" queries
- Trade-off: More memory, but better query performance

---

### Class Diagram 5: Comment Entity

```
┌──────────────────────────────────────────────────────┐
│               Comment                                │
├──────────────────────────────────────────────────────┤
│ - id: long                                           │
│ - content: String                                    │
│ - author: User                           ◇───────────┼──→ User
│ - parent: Commentable                    ◇───────────┼──→ Commentable
│ - createdAt: LocalDateTime                           │
│ - modifiedAt: LocalDateTime                          │
├──────────────────────────────────────────────────────┤
│ + Comment(id, content, author, parent)               │
│ + edit(newContent: String, editor: User): boolean    │
│ + delete(deleter: User): boolean                     │
└──────────────────────────────────────────────────────┘
```

**Parent Polymorphism:**
```
parent: Commentable ─┬─→ Question
                     └─→ Answer
```

**Interview Explanation:** "I'm using the `Commentable` interface so Comment class doesn't need to know if parent is Question or Answer. This makes code flexible - we could later add comments on comments if needed."

---

### Class Diagram 6: Vote Entity

```
┌──────────────────────────────────────────────────────┐
│                Vote                                  │
├──────────────────────────────────────────────────────┤
│ - id: long                                           │
│ - user: User                             ◇───────────┼──→ User
│ - target: Votable                        ◇───────────┼──→ Votable
│ - type: VoteType (UPVOTE/DOWNVOTE)                   │
│ - timestamp: LocalDateTime                           │
├──────────────────────────────────────────────────────┤
│ + Vote(id, user, target, type)                       │
│ + changeVote(newType: VoteType): void                │
│ + getValue(): int                                    │
└──────────────────────────────────────────────────────┘
```

**Target Polymorphism:**
```
target: Votable ─┬─→ Question
                 └─→ Answer
```

**Design Decision:** Store both upvotes and downvotes as Vote objects with type enum rather than separate classes.

---

### Class Diagram 7: Tag Entity

```
┌──────────────────────────────────────────────────────┐
│                 Tag                                  │
├──────────────────────────────────────────────────────┤
│ - id: long                                           │
│ - name: String                                       │
│ - description: String                                │
│ - usageCount: int                                    │
│ - createdBy: User                        ◇───────────┼──→ User
│ - questions: List<Question>              ◇───────────┼──→ Question [0..*]
├──────────────────────────────────────────────────────┤
│ + Tag(id, name, description, createdBy)              │
│ + incrementUsage(): void                             │
│ + associateWithQuestion(q: Question): void           │
└──────────────────────────────────────────────────────┘
```

**Many-to-Many Implementation:**
```
Question ◇────→ [QuestionTag] ←────◇ Tag
```

---

### Class Diagram 8: Badge & Notification

```
┌──────────────────────────┐      ┌────────────────────────────┐
│        Badge             │      │      Notification          │
├──────────────────────────┤      ├────────────────────────────┤
│ - id: long               │      │ - id: long                 │
│ - name: String           │      │ - recipient: User      ◇───┼──→ User
│ - description: String    │      │ - content: String          │
│ - level: BadgeLevel      │      │ - type: NotificationType   │
│ - iconUrl: String        │      │ - isRead: boolean          │
├──────────────────────────┤      │ - createdAt: LocalDateTime │
│ + awardTo(user): UserBadge│     │ - link: String             │
└──────────────────────────┘      ├────────────────────────────┤
                                  │ + markAsRead(): void       │
                                  └────────────────────────────┘

┌──────────────────────────┐
│      UserBadge           │
│   (Junction Entity)      │
├──────────────────────────┤
│ - user: User         ◇───┼──→ User
│ - badge: Badge       ◇───┼──→ Badge
│ - earnedAt: LocalDateTime│
└──────────────────────────┘
```

---

### Complete System Architecture

```
                    ┌──────────┐
                    │   User   │
                    └─────┬────┘
                          │ (author)
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
   ┌─────────┐       ┌────────┐       ┌─────────┐
   │Question │       │ Answer │       │ Comment │
   └────┬────┘       └───┬────┘       └─────────┘
        │                │
        │                │
        └────────┬───────┘
                 │
            ┌────┴─────┐
            │          │
            ▼          ▼
        ┌──────┐   ┌─────┐
        │ Vote │   │ Tag │
        └──────┘   └─────┘
                      │
                      ▼
               ┌────────────┐
               │   Badge    │
               └────────────┘
                      │
                      ▼
               ┌─────────────┐
               │Notification │
               └─────────────┘
```

---

## 💻 Step 6: Core Implementation (20-25 minutes)

### Implementation Strategy:
1. **Bottom-Up:** Start with entities that have no dependencies
2. **Interfaces First:** Define contracts
3. **Core Entities:** User, Question, Answer
4. **Services:** Business logic layer

---

### Enums

```java
// UserRole.java
public enum UserRole {
    GUEST(0),
    MEMBER(15),      // Min reputation to vote
    MODERATOR(10000),
    ADMIN(25000);
    
    private final int minReputation;
    
    UserRole(int minReputation) {
        this.minReputation = minReputation;
    }
    
    public int getMinReputation() {
        return minReputation;
    }
}

// QuestionStatus.java
public enum QuestionStatus {
    OPEN,
    CLOSED,
    ON_HOLD,
    DUPLICATE,
    DELETED
}

// VoteType.java
public enum VoteType {
    UPVOTE(1),
    DOWNVOTE(-1);
    
    private final int value;
    
    VoteType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}

// BadgeLevel.java
public enum BadgeLevel {
    BRONZE,
    SILVER,
    GOLD
}

// NotificationType.java
public enum NotificationType {
    NEW_ANSWER,
    NEW_COMMENT,
    VOTE_RECEIVED,
    ANSWER_ACCEPTED,
    BADGE_EARNED,
    QUESTION_CLOSED,
    MENTION
}
```

---

### Interfaces

```java
// Votable.java
public interface Votable {
    long getId();
    void addVote(Vote vote);
    void removeVote(Vote vote);
    int getVoteCount();
}

// Commentable.java
public interface Commentable {
    long getId();
    void addComment(Comment comment);
    List<Comment> getComments();
}
```

---

### User Hierarchy

```java
// User.java
import java.time.LocalDateTime;
import java.util.*;

public abstract class User {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private int reputation;
    private LocalDateTime createdAt;
    private UserRole role;
    private List<Question> questions;
    private List<Answer> answers;
    private List<UserBadge> badges;
    
    public User(long id, String username, String email, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = hashPassword(password);
        this.reputation = 0;
        this.createdAt = LocalDateTime.now();
        this.role = role;
        this.questions = new ArrayList<>();
        this.answers = new ArrayList<>();
        this.badges = new ArrayList<>();
    }
    
    // Abstract methods - each role defines its permissions
    public abstract boolean canModerate();
    public abstract boolean canCloseQuestion();
    public abstract boolean canDeleteAny();
    
    // Template methods - use reputation thresholds
    public boolean canVote() {
        return reputation >= 15;
    }
    
    public boolean canComment() {
        return reputation >= 50;
    }
    
    public boolean canCreateTags() {
        return reputation >= 1500;
    }
    
    public boolean canEditAny() {
        return reputation >= 2000;
    }
    
    // Reputation management
    public synchronized void addReputation(int points) {
        this.reputation += points;
        checkBadgeEligibility(); // Auto-award badges
    }
    
    public synchronized void deductReputation(int points) {
        this.reputation = Math.max(0, this.reputation - points);
    }
    
    private void checkBadgeEligibility() {
        // Check and award badges based on reputation/activity
        // E.g., "Nice Answer" badge for answer with 10+ upvotes
    }
    
    private String hashPassword(String password) {
        // In production: use BCrypt
        return Integer.toString(password.hashCode());
    }
    
    // Getters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getReputation() { return reputation; }
    public UserRole getRole() { return role; }
    public List<Question> getQuestions() { return questions; }
    public List<Answer> getAnswers() { return answers; }
    public List<UserBadge> getBadges() { return badges; }
}

// Guest.java
public class Guest extends User {
    public Guest(long id, String username, String email, String password) {
        super(id, username, email, password, UserRole.GUEST);
    }
    
    @Override
    public boolean canModerate() {
        return false;
    }
    
    @Override
    public boolean canCloseQuestion() {
        return false;
    }
    
    @Override
    public boolean canDeleteAny() {
        return false;
    }
    
    @Override
    public boolean canVote() {
        return false; // Guests can't vote even with reputation
    }
    
    @Override
    public boolean canComment() {
        return false;
    }
}

// Member.java
public class Member extends User {
    public Member(long id, String username, String email, String password) {
        super(id, username, email, password, UserRole.MEMBER);
    }
    
    @Override
    public boolean canModerate() {
        return false;
    }
    
    @Override
    public boolean canCloseQuestion() {
        return getReputation() >= 3000; // Trusted members can vote to close
    }
    
    @Override
    public boolean canDeleteAny() {
        return false;
    }
}

// Moderator.java
public class Moderator extends User {
    public Moderator(long id, String username, String email, String password) {
        super(id, username, email, password, UserRole.MODERATOR);
    }
    
    @Override
    public boolean canModerate() {
        return true;
    }
    
    @Override
    public boolean canCloseQuestion() {
        return true;
    }
    
    @Override
    public boolean canDeleteAny() {
        return true;
    }
}

// Admin.java
public class Admin extends User {
    public Admin(long id, String username, String email, String password) {
        super(id, username, email, password, UserRole.ADMIN);
    }
    
    @Override
    public boolean canModerate() {
        return true;
    }
    
    @Override
    public boolean canCloseQuestion() {
        return true;
    }
    
    @Override
    public boolean canDeleteAny() {
        return true;
    }
    
    // Admin-specific methods
    public void banUser(User user, String reason) {
        System.out.println("Admin banning user: " + user.getUsername() + 
                         ". Reason: " + reason);
    }
    
    public void createBadge(Badge badge) {
        System.out.println("Admin created badge: " + badge.getName());
    }
}
```

---

### Question Class

```java
// Question.java
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Question implements Votable, Commentable {
    private long id;
    private String title;
    private String description;
    private User author;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int viewCount;
    private int voteCount;
    private QuestionStatus status;
    private User closedBy;
    private String closeReason;
    private Answer acceptedAnswer;
    
    private List<Tag> tags;
    private List<Answer> answers;
    private List<Comment> comments;
    private List<Vote> votes;
    
    public Question(long id, String title, String description, User author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.voteCount = 0;
        this.status = QuestionStatus.OPEN;
        
        // Use thread-safe collections for concurrent access
        this.tags = new CopyOnWriteArrayList<>();
        this.answers = new CopyOnWriteArrayList<>();
        this.comments = new CopyOnWriteArrayList<>();
        this.votes = new CopyOnWriteArrayList<>();
    }
    
    // Votable interface implementation
    @Override
    public void addVote(Vote vote) {
        votes.add(vote);
        voteCount += vote.getType().getValue();
        
        // Update author's reputation
        if (vote.getType() == VoteType.UPVOTE) {
            author.addReputation(5); // +5 for question upvote
        } else {
            author.deductReputation(2); // -2 for downvote
        }
    }
    
    @Override
    public void removeVote(Vote vote) {
        votes.remove(vote);
        voteCount -= vote.getType().getValue();
    }
    
    @Override
    public int getVoteCount() {
        return voteCount;
    }
    
    // Commentable interface implementation
    @Override
    public void addComment(Comment comment) {
        comments.add(comment);
    }
    
    @Override
    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }
    
    // Question-specific methods
    public void addAnswer(Answer answer) {
        if (status == QuestionStatus.OPEN) {
            answers.add(answer);
        } else {
            throw new IllegalStateException("Cannot add answer to closed question");
        }
    }
    
    public boolean acceptAnswer(Answer answer, User acceptor) {
        // Validation 1: Only author can accept
        if (!acceptor.equals(author)) {
            System.out.println("❌ Only question author can accept answers");
            return false;
        }
        
        // Validation 2: Answer must belong to this question
        if (!answers.contains(answer)) {
            System.out.println("❌ Answer doesn't belong to this question");
            return false;
        }
        
        // Unaccept previous answer if exists
        if (acceptedAnswer != null) {
            acceptedAnswer.setAccepted(false);
            acceptedAnswer.getAuthor().deductReputation(15); // Remove previous bonus
        }
        
        // Accept new answer
        this.acceptedAnswer = answer;
        answer.setAccepted(true);
        
        // Award reputation
        answer.getAuthor().addReputation(15); // +15 for accepted answer
        author.addReputation(2); // +2 for accepting answer
        
        System.out.println("✅ Answer accepted!");
        return true;
    }
    
    public void addTag(Tag tag) {
        if (tags.size() >= 5) {
            throw new IllegalStateException("Maximum 5 tags allowed");
        }
        tags.add(tag);
        tag.incrementUsage();
        tag.associateWithQuestion(this);
    }
    
    public void close(User closer, String reason) {
        if (!closer.canCloseQuestion()) {
            System.out.println("❌ User doesn't have permission to close questions");
            return;
        }
        
        this.status = QuestionStatus.CLOSED;
        this.closedBy = closer;
        this.closeReason = reason;
        System.out.println("❌ Question closed. Reason: " + reason);
    }
    
    public void reopen(User reopener) {
        if (!reopener.canModerate()) {
            System.out.println("❌ Only moderators can reopen questions");
            return;
        }
        
        this.status = QuestionStatus.OPEN;
        this.closedBy = null;
        this.closeReason = null;
        System.out.println("✅ Question reopened");
    }
    
    public synchronized void incrementViewCount() {
        this.viewCount++;
    }
    
    public void edit(String newTitle, String newDescription, User editor) {
        if (!editor.equals(author) && !editor.canModerate()) {
            System.out.println("❌ No permission to edit");
            return;
        }
        
        this.title = newTitle;
        this.description = newDescription;
        this.modifiedAt = LocalDateTime.now();
    }
    
    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getViewCount() { return viewCount; }
    public QuestionStatus getStatus() { return status; }
    public Answer getAcceptedAnswer() { return acceptedAnswer; }
    public List<Tag> getTags() { return new ArrayList<>(tags); }
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }
}
```

---

### Answer Class

```java
// Answer.java
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Answer implements Votable, Commentable {
    private long id;
    private String content;
    private User author;
    private Question question;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int voteCount;
    private boolean isAccepted;
    
    private List<Comment> comments;
    private List<Vote> votes;
    
    public Answer(long id, String content, User author, Question question) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.question = question;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.voteCount = 0;
        this.isAccepted = false;
        
        this.comments = new CopyOnWriteArrayList<>();
        this.votes = new CopyOnWriteArrayList<>();
    }
    
    // Votable interface implementation
    @Override
    public void addVote(Vote vote) {
        votes.add(vote);
        voteCount += vote.getType().getValue();
        
        // Update author's reputation
        if (vote.getType() == VoteType.UPVOTE) {
            author.addReputation(10); // +10 for answer upvote
        } else {
            author.deductReputation(2); // -2 for downvote
        }
    }
    
    @Override
    public void removeVote(Vote vote) {
        votes.remove(vote);
        voteCount -= vote.getType().getValue();
        
        // Revert reputation change
        if (vote.getType() == VoteType.UPVOTE) {
            author.deductReputation(10);
        } else {
            author.addReputation(2);
        }
    }
    
    @Override
    public int getVoteCount() {
        return voteCount;
    }
    
    // Commentable interface implementation
    @Override
    public void addComment(Comment comment) {
        comments.add(comment);
    }
    
    @Override
    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }
    
    // Answer-specific methods
    public void edit(String newContent, User editor) {
        if (!editor.equals(author) && !editor.canModerate()) {
            System.out.println("❌ No permission to edit");
            return;
        }
        
        this.content = newContent;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public void setAccepted(boolean accepted) {
        this.isAccepted = accepted;
    }
    
    public boolean delete(User deleter) {
        if (!deleter.equals(author) && !deleter.canDeleteAny()) {
            System.out.println("❌ No permission to delete");
            return false;
        }
        return true;
    }
    
    // Getters
    public long getId() { return id; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public Question getQuestion() { return question; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isAccepted() { return isAccepted; }
}
```

---

### Comment, Vote, Tag Classes

```java
// Comment.java
import java.time.LocalDateTime;

public class Comment {
    private long id;
    private String content;
    private User author;
    private Commentable parent; // Question or Answer
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    
    public Comment(long id, String content, User author, Commentable parent) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }
    
    public void edit(String newContent, User editor) {
        if (!editor.equals(author)) {
            System.out.println("❌ Only author can edit comment");
            return;
        }
        this.content = newContent;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public boolean delete(User deleter) {
        return deleter.equals(author) || deleter.canModerate();
    }
    
    // Getters
    public long getId() { return id; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

// Vote.java
import java.time.LocalDateTime;

public class Vote {
    private long id;
    private User user;
    private Votable target; // Question or Answer
    private VoteType type;
    private LocalDateTime timestamp;
    
    public Vote(long id, User user, Votable target, VoteType type) {
        this.id = id;
        this.user = user;
        this.target = target;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
    
    public void changeVote(VoteType newType) {
        if (newType != this.type) {
            // Remove old vote effect
            target.removeVote(this);
            
            // Change type
            this.type = newType;
            this.timestamp = LocalDateTime.now();
            
            // Add new vote effect
            target.addVote(this);
        }
    }
    
    // Getters
    public long getId() { return id; }
    public User getUser() { return user; }
    public Votable getTarget() { return target; }
    public VoteType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// Tag.java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tag {
    private long id;
    private String name;
    private String description;
    private int usageCount;
    private User createdBy;
    private List<Question> questions;
    
    public Tag(long id, String name, String description, User createdBy) {
        this.id = id;
        this.name = name.toLowerCase(); // Normalize for consistency
        this.description = description;
        this.usageCount = 0;
        this.createdBy = createdBy;
        this.questions = new CopyOnWriteArrayList<>();
    }
    
    public synchronized void incrementUsage() {
        this.usageCount++;
    }
    
    public void associateWithQuestion(Question question) {
        if (!questions.contains(question)) {
            questions.add(question);
        }
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getUsageCount() { return usageCount; }
    public List<Question> getQuestions() { return new ArrayList<>(questions); }
}

// Badge.java
public class Badge {
    private long id;
    private String name;
    private String description;
    private BadgeLevel level;
    private String iconUrl;
    
    public Badge(long id, String name, String description, BadgeLevel level) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.level = level;
        this.iconUrl = "/badges/" + name.toLowerCase().replace(" ", "-") + ".png";
    }
    
    public UserBadge awardTo(User user) {
        UserBadge userBadge = new UserBadge(user, this, LocalDateTime.now());
        user.getBadges().add(userBadge);
        return userBadge;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public BadgeLevel getLevel() { return level; }
}

// UserBadge.java (Junction entity)
import java.time.LocalDateTime;

public class UserBadge {
    private User user;
    private Badge badge;
    private LocalDateTime earnedAt;
    
    public UserBadge(User user, Badge badge, LocalDateTime earnedAt) {
        this.user = user;
        this.badge = badge;
        this.earnedAt = earnedAt;
    }
    
    public User getUser() { return user; }
    public Badge getBadge() { return badge; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
}

// Notification.java
import java.time.LocalDateTime;

public class Notification {
    private long id;
    private User recipient;
    private String content;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String link;
    
    public Notification(long id, User recipient, String content, 
                       NotificationType type, String link) {
        this.id = id;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.link = link;
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public long getId() { return id; }
    public String getContent() { return content; }
    public boolean isRead() { return isRead; }
}
```

---

### Service Layer (Business Logic)

```java
// QuestionService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class QuestionService {
    private static QuestionService instance;
    private Map<Long, Question> questions;
    private AtomicLong questionIdCounter;
    
    private QuestionService() {
        this.questions = new ConcurrentHashMap<>();
        this.questionIdCounter = new AtomicLong(1);
    }
    
    public static synchronized QuestionService getInstance() {
        if (instance == null) {
            instance = new QuestionService();
        }
        return instance;
    }
    
    public Question createQuestion(String title, String description, 
                                   User author, List<Tag> tags) {
        // Validation
        if (title.length() < 10) {
            throw new IllegalArgumentException("Title must be at least 10 characters");
        }
        
        if (tags.isEmpty() || tags.size() > 5) {
            throw new IllegalArgumentException("Must have 1-5 tags");
        }
        
        // Create question
        long id = questionIdCounter.getAndIncrement();
        Question question = new Question(id, title, description, author);
        
        // Add tags
        for (Tag tag : tags) {
            question.addTag(tag);
        }
        
        // Store
        questions.put(id, question);
        author.getQuestions().add(question);
        
        return question;
    }
    
    public Question getQuestion(long id) {
        Question question = questions.get(id);
        if (question != null) {
            question.incrementViewCount(); // Track views
        }
        return question;
    }
    
    public List<Question> searchByTag(Tag tag) {
        return tag.getQuestions();
    }
    
    public List<Question> searchByKeyword(String keyword) {
        List<Question> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (Question q : questions.values()) {
            String searchText = (q.getTitle() + " " + q.getDescription()).toLowerCase();
            if (searchText.contains(lowerKeyword)) {
                results.add(q);
            }
        }
        
        // Sort by relevance (votes, then views)
        results.sort((q1, q2) -> {
            int voteDiff = Integer.compare(q2.getVoteCount(), q1.getVoteCount());
            if (voteDiff != 0) return voteDiff;
            return Integer.compare(q2.getViewCount(), q1.getViewCount());
        });
        
        return results;
    }
}

// AnswerService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AnswerService {
    private static AnswerService instance;
    private Map<Long, Answer> answers;
    private AtomicLong answerIdCounter;
    
    private AnswerService() {
        this.answers = new ConcurrentHashMap<>();
        this.answerIdCounter = new AtomicLong(1);
    }
    
    public static synchronized AnswerService getInstance() {
        if (instance == null) {
            instance = new AnswerService();
        }
        return instance;
    }
    
    public Answer createAnswer(String content, User author, Question question) {
        // Validation
        if (content.length() < 30) {
            throw new IllegalArgumentException("Answer must be at least 30 characters");
        }
        
        if (question.getStatus() != QuestionStatus.OPEN) {
            throw new IllegalStateException("Cannot answer closed question");
        }
        
        // Create answer
        long id = answerIdCounter.getAndIncrement();
        Answer answer = new Answer(id, content, author, question);
        
        // Store
        answers.put(id, answer);
        question.addAnswer(answer);
        author.getAnswers().add(answer);
        
        // Notify question author
        NotificationService.getInstance().createNotification(
            question.getAuthor(),
            author.getUsername() + " answered your question: " + question.getTitle(),
            NotificationType.NEW_ANSWER,
            "/questions/" + question.getId()
        );
        
        return answer;
    }
    
    public Answer getAnswer(long id) {
        return answers.get(id);
    }
}

// VoteService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class VoteService {
    private static VoteService instance;
    private Map<Long, Vote> votes;
    private Map<String, Vote> userVoteMap; // "userId-targetId-targetType" -> Vote
    private AtomicLong voteIdCounter;
    
    private VoteService() {
        this.votes = new ConcurrentHashMap<>();
        this.userVoteMap = new ConcurrentHashMap<>();
        this.voteIdCounter = new AtomicLong(1);
    }
    
    public static synchronized VoteService getInstance() {
        if (instance == null) {
            instance = new VoteService();
        }
        return instance;
    }
    
    public Vote castVote(User user, Votable target, VoteType type) {
        // Permission check
        if (!user.canVote()) {
            System.out.println("❌ Need " + 15 + " reputation to vote. " +
                             "Current: " + user.getReputation());
            return null;
        }
        
        // Check for existing vote
        String voteKey = generateVoteKey(user.getId(), target.getId());
        Vote existingVote = userVoteMap.get(voteKey);
        
        if (existingVote != null) {
            if (existingVote.getType() == type) {
                // Same vote clicked → remove vote (toggle off)
                target.removeVote(existingVote);
                votes.remove(existingVote.getId());
                userVoteMap.remove(voteKey);
                System.out.println("🔄 Vote removed");
                return null;
            } else {
                // Different vote → change vote
                existingVote.changeVote(type);
                System.out.println("🔄 Vote changed to " + type);
                return existingVote;
            }
        } else {
            // New vote
            long id = voteIdCounter.getAndIncrement();
            Vote vote = new Vote(id, user, target, type);
            
            votes.put(id, vote);
            userVoteMap.put(voteKey, vote);
            target.addVote(vote);
            
            System.out.println("✅ Vote cast: " + type);
            return vote;
        }
    }
    
    private String generateVoteKey(long userId, long targetId) {
        return userId + "-" + targetId;
    }
    
    public boolean hasUserVoted(User user, Votable target) {
        String voteKey = generateVoteKey(user.getId(), target.getId());
        return userVoteMap.containsKey(voteKey);
    }
}

// NotificationService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationService {
    private static NotificationService instance;
    private Map<Long, Notification> notifications;
    private Map<Long, List<Notification>> userNotifications; // userId -> List
    private AtomicLong notificationIdCounter;
    
    private NotificationService() {
        this.notifications = new ConcurrentHashMap<>();
        this.userNotifications = new ConcurrentHashMap<>();
        this.notificationIdCounter = new AtomicLong(1);
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    public Notification createNotification(User recipient, String content,
                                          NotificationType type, String link) {
        long id = notificationIdCounter.getAndIncrement();
        Notification notification = new Notification(id, recipient, content, type, link);
        
        notifications.put(id, notification);
        userNotifications.computeIfAbsent(recipient.getId(), 
                         k -> new ArrayList<>()).add(notification);
        
        return notification;
    }
    
    public List<Notification> getUnreadNotifications(User user) {
        List<Notification> unread = new ArrayList<>();
        List<Notification> userNotifs = userNotifications.get(user.getId());
        
        if (userNotifs != null) {
            for (Notification n : userNotifs) {
                if (!n.isRead()) {
                    unread.add(n);
                }
            }
        }
        
        return unread;
    }
}
```

---

### Demo Application

```java
// StackOverflowDemo.java
import java.util.Arrays;
import java.util.List;

public class StackOverflowDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   STACK OVERFLOW SYSTEM DEMONSTRATION    ");
        System.out.println("═══════════════════════════════════════════\n");
        
        // Get service instances
        QuestionService questionService = QuestionService.getInstance();
        AnswerService answerService = AnswerService.getInstance();
        VoteService voteService = VoteService.getInstance();
        NotificationService notificationService = NotificationService.getInstance();
        
        // Create users
        Member alice = new Member(1, "alice_dev", "alice@example.com", "pass123");
        alice.addReputation(150); // Give reputation to enable voting
        
        Member bob = new Member(2, "bob_coder", "bob@example.com", "pass456");
        bob.addReputation(100);
        
        Moderator charlie = new Moderator(3, "charlie_mod", "charlie@example.com", "pass789");
        
        System.out.println("✅ Users Created:");
        System.out.println("   - " + alice.getUsername() + " (Reputation: " + 
                         alice.getReputation() + ")");
        System.out.println("   - " + bob.getUsername() + " (Reputation: " + 
                         bob.getReputation() + ")");
        System.out.println("   - " + charlie.getUsername() + " (Moderator)\n");
        
        // Create tags
        Tag javaTag = new Tag(1, "java", "Java programming language", alice);
        Tag algoTag = new Tag(2, "algorithm", "Algorithm design and analysis", alice);
        Tag dataStructTag = new Tag(3, "data-structures", "Data structures", bob);
        
        System.out.println("✅ Tags Created: java, algorithm, data-structures\n");
        
        System.out.println("═══ QUESTION POSTING ═══\n");
        
        // Alice posts question
        Question question = questionService.createQuestion(
            "How to reverse a linked list in Java?",
            "I'm trying to reverse a singly linked list in Java. " +
            "What's the most efficient approach? Should I use iteration or recursion?",
            alice,
            Arrays.asList(javaTag, algoTag, dataStructTag)
        );
        
        System.out.println("✅ Question Posted by " + alice.getUsername());
        System.out.println("   Title: " + question.getTitle());
        System.out.println("   ID: " + question.getId());
        System.out.println("   Tags: " + question.getTags().size());
        System.out.println("   Status: " + question.getStatus() + "\n");
        
        System.out.println("═══ ANSWERING ═══\n");
        
        // Bob posts answer
        Answer answer1 = answerService.createAnswer(
            "Use three pointers approach:\n" +
            "1. Initialize prev = null, current = head\n" +
            "2. While current != null:\n" +
            "   - Save next node\n" +
            "   - Reverse the link: current.next = prev\n" +
            "   - Move pointers forward\n" +
            "Time: O(n), Space: O(1)",
            bob,
            question
        );
        
        System.out.println("✅ Answer Posted by " + bob.getUsername());
        System.out.println("   Length: " + answer1.getContent().length() + " chars\n");
        
        // Charlie posts answer
        Answer answer2 = answerService.createAnswer(
            "Recursive approach is cleaner but uses O(n) stack space:\n" +
            "public Node reverse(Node node, Node prev) {\n" +
            "    if (node == null) return prev;\n" +
            "    Node next = node.next;\n" +
            "    node.next = prev;\n" +
            "    return reverse(next, node);\n" +
            "}\n" +
            "However, iterative is preferred for production due to stack safety.",
            charlie,
            question
        );
        
        System.out.println("✅ Answer Posted by " + charlie.getUsername() + "\n");
        
        System.out.println("═══ VOTING ═══\n");
        
        // Bob upvotes Charlie's answer
        voteService.castVote(bob, answer2, VoteType.UPVOTE);
        System.out.println("   " + charlie.getUsername() + "'s reputation: " + 
                         charlie.getReputation());
        
        // Alice upvotes both answers
        voteService.castVote(alice, answer1, VoteType.UPVOTE);
        System.out.println("   " + bob.getUsername() + "'s reputation: " + 
                         bob.getReputation());
        
        voteService.castVote(alice, answer2, VoteType.UPVOTE);
        System.out.println("   " + charlie.getUsername() + "'s reputation: " + 
                         charlie.getReputation());
        
        // Alice upvotes her own question
        voteService.castVote(bob, question, VoteType.UPVOTE);
        System.out.println("   Question votes: " + question.getVoteCount());
        System.out.println("   " + alice.getUsername() + "'s reputation: " + 
                         alice.getReputation() + "\n");
        
        System.out.println("═══ ACCEPTING ANSWER ═══\n");
        
        // Alice accepts Bob's answer
        boolean accepted = question.acceptAnswer(answer1, alice);
        System.out.println("   Accepted: " + accepted);
        System.out.println("   " + bob.getUsername() + "'s reputation: " + 
                         bob.getReputation());
        System.out.println("   " + alice.getUsername() + "'s reputation: " + 
                         alice.getReputation() + "\n");
        
        System.out.println("═══ COMMENTING ═══\n");
        
        // Alice adds comment
        Comment comment1 = new Comment(1, "Thanks for the detailed explanation!", 
                                      alice, answer1);
        answer1.addComment(comment1);
        System.out.println("✅ Comment added by " + alice.getUsername() + "\n");
        
        System.out.println("═══ QUESTION SUMMARY ═══\n");
        printQuestionSummary(question);
        
        System.out.println("\n═══ NOTIFICATIONS ═══\n");
        List<Notification> aliceNotifs = notificationService.getUnreadNotifications(alice);
        System.out.println(alice.getUsername() + " has " + aliceNotifs.size() + 
                         " unread notifications:");
        for (Notification n : aliceNotifs) {
            System.out.println("   📬 " + n.getContent());
        }
        
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("           DEMO COMPLETED                  ");
        System.out.println("═══════════════════════════════════════════");
    }
    
    private static void printQuestionSummary(Question question) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║            QUESTION SUMMARY                  ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Title: " + truncate(question.getTitle(), 38) + "║");
        System.out.println("║ Author: " + question.getAuthor().getUsername() + 
                         " (Rep: " + question.getAuthor().getReputation() + ")");
        System.out.println("║ Views: " + question.getViewCount());
        System.out.println("║ Votes: " + question.getVoteCount());
        System.out.println("║ Answers: " + question.getAnswers().size());
        System.out.println("║ Comments: " + question.getComments().size());
        System.out.println("║ Accepted: " + (question.getAcceptedAnswer() != null ? "Yes" : "No"));
        System.out.println("║ Status: " + question.getStatus());
        System.out.println("╚══════════════════════════════════════════════╝");
    }
    
    private static String truncate(String str, int maxLen) {
        return str.length() > maxLen ? str.substring(0, maxLen-3) + "..." : str;
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern Application with Justification

#### 1. **Singleton Pattern**
**Where:** All service classes  
**Why:** Single point of coordination  
**Interview Justification:**
"QuestionService, AnswerService, VoteService use Singleton because we need centralized management of all questions/answers/votes. Multiple instances would lead to inconsistent data."

#### 2. **Strategy Pattern**
**Where:** Search algorithms  
**Why:** Multiple search strategies
```java
public interface SearchStrategy {
    List<Question> search(String query, Map<String, Object> filters);
}

public class KeywordSearch implements SearchStrategy { ... }
public class TagSearch implements SearchStrategy { ... }
public class AdvancedSearch implements SearchStrategy { ... }
```

**Interview Justification:**
"Different search needs (keyword vs tag vs advanced). Strategy pattern lets us swap algorithms at runtime and add new search types easily."

#### 3. **Observer Pattern**
**Where:** Notification system  
**Why:** Decouple event generation from notification
```java
public interface EventObserver {
    void onEvent(Event event);
}

public class NotificationObserver implements EventObserver {
    public void onEvent(Event event) {
        if (event instanceof AnswerPostedEvent) {
            notifyQuestionAuthor((AnswerPostedEvent) event);
        }
    }
}
```

**Interview Justification:**
"When answer is posted, multiple things happen (notify author, update metrics, check for badge). Observer pattern decouples these actions from core posting logic."

#### 4. **Template Method Pattern**
**Where:** User permission checks
```java
public abstract class User {
    // Template method
    public final boolean performAction(Action action) {
        if (!hasPermission(action)) return false;
        if (!checkRateLimit()) return false;
        return executeAction(action);
    }
    
    protected abstract boolean hasPermission(Action action);
}
```

#### 5. **Factory Pattern**
**Where:** User creation
```java
public class UserFactory {
    public static User createUser(UserRole role, long id, 
                                  String username, String email, String password) {
        switch (role) {
            case GUEST: return new Guest(id, username, email, password);
            case MEMBER: return new Member(id, username, email, password);
            case MODERATOR: return new Moderator(id, username, email, password);
            case ADMIN: return new Admin(id, username, email, password);
            default: throw new IllegalArgumentException("Invalid role");
        }
    }
}
```

---

## 🔒 Step 8: Handle Concurrency (8-10 minutes)

### Critical Section Analysis

#### 1. **Double Voting Prevention**

**Problem:** User votes twice on same content

**Solution:**
```java
private Map<String, Vote> userVoteMap; // Track existing votes

public synchronized Vote castVote(User user, Votable target, VoteType type) {
    String voteKey = user.getId() + "-" + target.getId();
    
    // Atomic check-and-set
    if (userVoteMap.containsKey(voteKey)) {
        return handleExistingVote(voteKey, type);
    }
    
    Vote vote = new Vote(...);
    userVoteMap.put(voteKey, vote); // Atomic put
    return vote;
}
```

**Interview Explanation:**
"The synchronized method ensures atomic check-and-put. Using ConcurrentHashMap alone isn't enough because check + put is two operations. The synchronized block makes it atomic."

#### 2. **Vote Count Updates**

**Problem:** Lost updates when multiple users vote simultaneously

**Solution:**
```java
public synchronized void incrementVoteCount(int value) {
    this.voteCount += value;
}
```

**Alternative (Database):**
```sql
-- Atomic increment in database
UPDATE questions SET vote_count = vote_count + 1 WHERE id = ?;
```

**Interview Explanation:**
"Vote count updates must be atomic. In-memory: use synchronized. In database: use atomic UPDATE operations or optimistic locking with version field."

#### 3. **Accepted Answer Race Condition**

**Problem:** Two users trying to accept different answers simultaneously

**Solution:**
```java
public synchronized boolean acceptAnswer(Answer answer, User acceptor) {
    // Synchronized at Question level
    // Only one thread can accept at a time
}
```

#### 4. **Reputation Updates**

**Problem:** Multiple vote operations updating reputation concurrently

**Solution:**
```java
public synchronized void addReputation(int points) {
    this.reputation += points;
}
```

**Advanced Solution (Async):**
```java
public class ReputationUpdateQueue {
    private BlockingQueue<ReputationEvent> queue;
    
    public void enqueue(ReputationEvent event) {
        queue.offer(event);
    }
    
    // Background worker processes queue
    public void processQueue() {
        while (true) {
            ReputationEvent event = queue.take();
            event.getUser().addReputation(event.getPoints());
        }
    }
}
```

**Interview Explanation:**
"For high-scale systems, we can make reputation updates eventually consistent. Use a queue (Kafka) and process asynchronously. Users won't notice 1-2 second delay in reputation update."

---

## 📊 Step 9: Database Schema (If Time Permits)

```sql
-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    reputation INT DEFAULT 0,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_reputation_desc (reputation DESC)
);

-- Questions table
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    vote_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'OPEN',
    accepted_answer_id BIGINT,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    FULLTEXT INDEX idx_fulltext (title, description),
    INDEX idx_author_created (author_id, created_at DESC),
    INDEX idx_vote_count (vote_count DESC),
    INDEX idx_status (status)
);

-- Answers table
CREATE TABLE answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    vote_count INT DEFAULT 0,
    is_accepted BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_question_votes (question_id, vote_count DESC),
    INDEX idx_author (author_id)
);

-- Votes table (with duplicate prevention)
CREATE TABLE votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    votable_id BIGINT NOT NULL,
    votable_type VARCHAR(20) NOT NULL, -- 'QUESTION' or 'ANSWER'
    vote_type VARCHAR(10) NOT NULL, -- 'UPVOTE' or 'DOWNVOTE'
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_user_vote (user_id, votable_id, votable_type),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_votable (votable_id, votable_type)
);

-- Tags table
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    usage_count INT DEFAULT 0,
    created_by BIGINT,
    
    INDEX idx_name (name),
    INDEX idx_usage_desc (usage_count DESC)
);

-- Question_Tags junction table
CREATE TABLE question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    
    PRIMARY KEY (question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    INDEX idx_tag_questions (tag_id, question_id)
);

-- Comments table
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(500) NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    parent_type VARCHAR(20) NOT NULL, -- 'QUESTION' or 'ANSWER'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_parent (parent_id, parent_type)
);
```

---

## 💡 Step 10: Interview Discussion Points

### Question 1: "How do you prevent vote manipulation?"

**Answer Structure:**
1. Identify the threat
2. Propose prevention mechanisms
3. Discuss trade-offs

**Response:**
"Vote manipulation threats include:
- Fake accounts voting for own content
- Vote rings (groups voting for each other)
- Bot accounts mass voting

**Prevention Mechanisms:**

1. **Reputation Threshold:**
```java
public boolean canVote() {
    return reputation >= 15; // Must earn 15 rep before voting
}
```

2. **Rate Limiting:**
```java
public class RateLimiter {
    private Map<Long, Integer> userVoteCounts; // userId -> votes in last hour
    
    public boolean canVote(User user) {
        int recentVotes = userVoteCounts.getOrDefault(user.getId(), 0);
        return recentVotes < 40; // Max 40 votes per hour
    }
}
```

3. **Anomaly Detection:**
```java
public class VoteAnomalyDetector {
    public boolean detectSuspicious(User user, Votable target) {
        // Check if user always votes for same author
        // Check if votes happen in burst patterns
        // Check if user and author have suspicious connection
        return false;
    }
}
```

---

### Question 2: "How would you implement search at scale?"

**Answer:**

"In-memory search doesn't scale to millions of questions. Strategy:

**Phase 1: Elasticsearch Integration**
```java
public class SearchService {
    private ElasticsearchClient esClient;
    
    public List<Question> search(String query, SearchFilters filters) {
        // 1. Build Elasticsearch query
        SearchQuery esQuery = new SearchQuery()
            .match("title", query).boost(2.0)  // Title more important
            .match("description", query).boost(1.0)
            .filter("tags", filters.getTags())
            .filter("status", "OPEN")
            .sort("_score", "DESC")  // Relevance first
            .sort("vote_count", "DESC");  // Then by votes
        
        // 2. Execute search
        SearchResult result = esClient.search(esQuery);
        
        // 3. Fetch full objects from database
        return loadQuestions(result.getIds());
    }
}
```

**Indexing Strategy:**
```java
// When question is created
public Question createQuestion(...) {
    Question q = new Question(...);
    questions.put(q.getId(), q);
    
    // Async index to Elasticsearch
    searchIndexer.indexAsync(q);
    
    return q;
}
```

**Trade-offs:**
- ✅ Fast search (< 100ms)
- ✅ Relevance ranking
- ✅ Scalable to billions of documents
- ❌ Eventual consistency (newly posted questions appear after 1-2 seconds)
- ❌ Additional infrastructure cost"

---

### Question 3: "How to handle hot questions (millions of views)?"

**Answer:**

"Multi-layer caching strategy:

**Layer 1: Application Cache (Redis)**
```java
public class QuestionCache {
    private RedisClient redis;
    private static final int TTL = 300; // 5 minutes
    
    public Question getQuestion(long id) {
        // Try cache first
        String cached = redis.get("question:" + id);
        if (cached != null) {
            return deserialize(cached);
        }
        
        // Cache miss - load from DB
        Question q = questionService.loadFromDB(id);
        redis.setex("question:" + id, TTL, serialize(q));
        return q;
    }
}
```

**Layer 2: CDN for Static Content**
- Rendered HTML pages cached at edge locations
- 95% of views served from CDN, never hit backend

**Layer 3: Database Read Replicas**
- Write to master
- Read from replicas (10+ replicas)
- Load balancer distributes reads

**View Count Optimization:**
```java
// Don't update DB on every view (too expensive)
public void incrementViewCount(long questionId) {
    // Increment in Redis
    redis.incr("views:" + questionId);
    
    // Batch update to DB every 5 minutes
    if (shouldFlushViews()) {
        batchUpdateViewCounts();
    }
}
```

---

### Question 4: "How to implement reputation-based privileges?"

**Answer:**

"Reputation determines what users can do. Systematic approach:

**Step 1: Define Privilege Thresholds**
```java
public enum Privilege {
    VOTE(15),
    COMMENT(50),
    CREATE_TAG(1500),
    EDIT_ANY(2000),
    CLOSE_VOTE(3000),
    MODERATE(10000);
    
    private final int requiredReputation;
    
    Privilege(int rep) {
        this.requiredReputation = rep;
    }
    
    public boolean hasPrivilege(User user) {
        return user.getReputation() >= requiredReputation;
    }
}
```

**Step 2: Check Before Action**
```java
public class PrivilegeChecker {
    public boolean canPerformAction(User user, Action action) {
        Privilege required = action.getRequiredPrivilege();
        return required.hasPrivilege(user);
    }
}
```

**Step 3: Reputation Calculation Rules**

| Action | Reputation Change | Recipient |
|--------|------------------|-----------|
| Question upvoted | +5 | Question author |
| Question downvoted | -2 | Question author |
| Answer upvoted | +10 | Answer author |
| Answer downvoted | -2 | Answer author |
| Answer accepted | +15 | Answer author |
| Accept an answer | +2 | Question author |
| Downvote someone | -1 | Voter (cost to downvote) |
| Edit approved | +2 | Editor |

**Implementation:**
```java
public class ReputationCalculator {
    public int calculateReputationChange(Event event) {
        if (event instanceof UpvoteEvent) {
            return event.getTarget() instanceof Answer ? 10 : 5;
        } else if (event instanceof DownvoteEvent) {
            return -2;
        } else if (event instanceof AcceptAnswerEvent) {
            return 15;
        }
        return 0;
    }
}
```

---

### Question 5: "How to implement trending/hot questions algorithm?"

**Answer:**

"Trending algorithm needs to consider recency + engagement:

```java
public class TrendingCalculator {
    public double calculateScore(Question question) {
        long ageInHours = Duration.between(
            question.getCreatedAt(), 
            LocalDateTime.now()
        ).toHours();
        
        // Decay factor: older questions score lower
        double decay = Math.pow(0.8, ageInHours / 24.0);
        
        // Engagement score
        int engagement = question.getVoteCount() * 10 +
                        question.getAnswers().size() * 5 +
                        question.getViewCount() / 10;
        
        return engagement * decay;
    }
    
    public List<Question> getTrending(int count) {
        return questions.stream()
            .sorted((q1, q2) -> Double.compare(
                calculateScore(q2), 
                calculateScore(q1)
            ))
            .limit(count)
            .collect(Collectors.toList());
    }
}
```

**Optimization:**
- Calculate trending scores every 15 minutes (background job)
- Cache results in Redis
- Serve from cache (updated periodically)"

---

## ✅ Step 11: SOLID Principles Verification

### Systematic SOLID Check

#### S - Single Responsibility Principle
**Check:** Does each class have only one reason to change?

| Class | Responsibility | One Reason? |
|-------|---------------|-------------|
| Question | Manage question data and business rules | ✅ Changes only if question logic changes |
| Answer | Manage answer data and acceptance | ✅ Changes only if answer logic changes |
| VoteService | Handle voting operations | ✅ Changes only if voting logic changes |
| QuestionService | Manage question lifecycle | ✅ Changes only if question CRUD changes |

#### O - Open/Closed Principle
**Check:** Can we extend without modifying existing code?

- ✅ New user roles: Add new subclass of User without modifying User
- ✅ New vote types: Extend VoteType enum, no logic changes needed
- ✅ New notification types: Add to NotificationType, observer handles it

#### L - Liskov Substitution Principle
**Check:** Can subtypes replace parent without breaking functionality?

```java
User user = new Member(...);  // Can substitute any User subtype
user.canVote();               // Works correctly for all subtypes

Votable target = question;    // Can use Question or Answer
voteService.castVote(user, target, UPVOTE);  // Works for both
```

✅ All substitutions work correctly

#### I - Interface Segregation Principle
**Check:** Are interfaces cohesive and minimal?

- ✅ `Votable`: Only vote-related methods
- ✅ `Commentable`: Only comment-related methods
- ✅ No fat interfaces with unused methods

#### D - Dependency Inversion Principle
**Check:** Do high-level modules depend on abstractions?

```java
public class Vote {
    private Votable target;  // ✅ Depends on interface
    // NOT: private Question target;  // ❌ Would depend on concrete
}

public class Comment {
    private Commentable parent;  // ✅ Depends on interface
}
```

---

## 🎯 Interview Tips & Talking Points

### Opening Statement (30 seconds):
"Let me start by clarifying requirements and understanding the scale. Then I'll identify core entities, establish relationships, and design a scalable architecture. I'll use design patterns where appropriate and handle concurrency. Is that approach okay?"

### During Entity Identification (2 minutes):
"I'm extracting nouns from requirements - User, Question, Answer, Comment, Vote, Tag. For each, I'm checking if it has state and behavior. For example, Vote has state (type, timestamp) and behavior (changeVote), so it qualifies as an entity..."

### During Relationship Design (3 minutes):
"Question HAS-A Answer with composition because answers can't exist without questions - if we delete a question, answers should cascade delete. But User HAS-A Question with aggregation because questions should persist even if user is deleted - we just show [deleted user]..."

### During Concurrency Discussion (2 minutes):
"The critical section is vote casting - we need to prevent duplicate votes. I'm using a synchronized method with a userVoteMap to track existing votes. The synchronized keyword ensures atomic check-and-set..."

### During Scale Discussion (3 minutes):
"For millions of questions, in-memory won't work. I'd use Elasticsearch for search, Redis for caching hot questions, and database sharding by question ID. Read replicas handle 95% of queries. Write to master, read from replicas..."

### Closing Statement (1 minute):
"The design follows SOLID principles, uses appropriate design patterns, handles concurrency, and scales horizontally. Key trade-offs: strong consistency for votes vs eventual consistency for reputation updates, normalized schema vs denormalized for performance."

---

## 📈 Complexity Analysis

| Operation | Time Complexity | Explanation |
|-----------|----------------|-------------|
| Post Question | O(1) | HashMap insertion |
| Post Answer | O(1) | HashMap insertion + notification |
| Cast Vote | O(1) | HashMap lookup + update |
| Search (in-memory) | O(n) | Linear scan through questions |
| Search (Elasticsearch) | O(log n) | Indexed search |
| Get Question by ID | O(1) | HashMap lookup |
| Accept Answer | O(1) | Direct reference update |
| Get User Questions | O(1) | Direct list access |

**Space Complexity:** O(U + Q + A + V) where:
- U = users
- Q = questions
- A = answers
- V = votes

---

## 🎓 Key Takeaways

### Interview Success Formula:

1. **Clarify First** (5 min) - Ask questions, understand scale
2. **Requirements** (5 min) - List functional, deduce non-functional systematically
3. **Entities** (10 min) - Noun extraction, validation, grouping
4. **Relationships** (12 min) - Three-pass: IS-A, HAS-A, Cardinality
5. **Class Diagrams** (12 min) - Core classes with key attributes/methods
6. **Implementation** (20 min) - Code core flow, show patterns
7. **Concurrency** (8 min) - Identify critical sections, add synchronization
8. **Scale** (10 min) - Discuss caching, sharding, async processing
9. **Q&A** (Remaining) - Handle follow-ups with confidence

### What Interviewers Look For:

✅ **Structured Thinking** - Not jumping to code immediately  
✅ **Justification** - Explaining WHY you made each design decision  
✅ **Trade-offs** - Discussing alternatives and choosing based on requirements  
✅ **Scalability** - Thinking beyond single-machine solutions  
✅ **Real-world Awareness** - Mentioning actual technologies (Redis, Elasticsearch)

### Red Flags to Avoid:

❌ Starting to code without clarifying requirements  
❌ Creating entities without explaining why  
❌ Missing critical relationships  
❌ Ignoring concurrency completely  
❌ Not discussing scale at all  
❌ Using design patterns without justification

---

**This systematic approach works for ANY LLD interview problem!**
