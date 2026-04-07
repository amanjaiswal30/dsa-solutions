# Low-Level Design: LinkedIn System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a professional networking platform like LinkedIn that allows users to create profiles, connect with other professionals, share posts, search for jobs, and engage with content."*

### Clarifying Questions to Ask:

1. **Q:** What are the core features we need to support?  
   **A:** User profiles, connections, posts/feed, comments/likes, job postings, messaging, notifications, search.

2. **Q:** What types of users exist?  
   **A:** Regular users, recruiters, company pages, premium users.

3. **Q:** How do connections work?  
   **A:** Bidirectional (mutual acceptance required), up to 30,000 connections per user.

4. **Q:** What content can users post?  
   **A:** Text posts, images, videos, articles, job posts, polls.

5. **Q:** How does the news feed work?  
   **A:** Show posts from connections, followed pages, and recommended content. Ranked by relevance/engagement.

6. **Q:** What about job functionality?  
   **A:** Companies post jobs, users apply, save jobs, get recommendations.

7. **Q:** Should we support messaging?  
   **A:** Yes, 1-on-1 and group messaging.

8. **Q:** What about search?  
   **A:** Search users, companies, jobs, posts by keywords, filters.

9. **Q:** Should we support endorsements/recommendations?  
   **A:** Yes, skills endorsements and written recommendations.

10. **Q:** What about privacy settings?  
    **A:** Users can control profile visibility, connection requests, messaging permissions.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### User Management (FR1-FR5)
1. Users should register with email/phone
2. Users should create detailed profiles:
   - Basic info (name, headline, location)
   - Work experience (company, title, dates, description)
   - Education (school, degree, dates)
   - Skills (with endorsement counts)
   - Certifications, projects, languages
   - Profile photo, background photo
3. Users should search and discover other users
4. Users should send/accept/reject connection requests
5. Users can have up to 30,000 connections

#### Content & Feed (FR6-FR12)
6. Users should create posts (text, images, videos, articles, polls)
7. Users should see personalized news feed
8. Users should like, comment on, share posts
9. Users should follow companies/influencers without connecting
10. Posts should support mentions (@user) and hashtags (#topic)
11. Users should report inappropriate content
12. Posts can be edited within 15 minutes

#### Job Features (FR13-FR18)
13. Companies should post job listings
14. Job posts should include:
    - Title, description, location, salary range
    - Required skills, experience level
    - Application method (easy apply, external)
15. Users should apply for jobs
16. Users should save jobs for later
17. System should recommend relevant jobs
18. Users should set "Open to Work" status

#### Messaging (FR19-FR22)
19. Users should send direct messages (1-on-1)
20. Users should create group conversations
21. Messages should support text, attachments, emojis
22. Users should see read receipts and typing indicators

#### Additional Features (FR23-FR30)
23. Users should endorse connections' skills
24. Users should write recommendations for connections
25. Users should receive notifications (connection requests, comments, job matches)
26. System should suggest "People You May Know"
27. System should track profile views ("Who viewed your profile")
28. Users should share articles/links with preview
29. Premium users get InMail (message non-connections)
30. Analytics for company pages (post reach, engagement)

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many users? How much data?"
- 900 million registered users
- 310 million monthly active users (MAU)
- ~2 million posts/day
- ~5 billion feed views/day
- Peak load: 3× average (during business hours)

**Deduced NFRs:**
- ✅ Horizontal scaling for web servers (load balancers)
- ✅ Database sharding by user ID
- ✅ Feed generation: pre-compute and cache (fan-out on write)
- ✅ CDN for images/videos
- ✅ Elasticsearch for search (user, job, post indexing)
- ✅ Message queue for async tasks (notifications, feed updates)

---

#### 2. **Consistency Analysis**

**Think:** "What must be accurate?"
- Connection relationships (bidirectional integrity)
- Job application records (no duplicate applications)
- Message delivery (reliable)
- Endorsement counts (eventually consistent OK)
- Feed content (eventual consistency OK)

**Deduced NFRs:**
- ✅ **Strong consistency** for: Connections, job applications, messages
- ✅ **Eventual consistency** for: Feed updates, like counts, endorsements
- ✅ Idempotent APIs (prevent duplicate likes/applications)
- ✅ Transaction support for critical operations

---

#### 3. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Critical business platform
- Global availability needed
- Partial failures acceptable (degraded mode)

**Deduced NFRs:**
- ✅ 99.99% availability (52 minutes downtime/year)
- ✅ Multi-region deployment (disaster recovery)
- ✅ Graceful degradation:
  - If feed service fails → show cached feed
  - If search fails → show basic results
  - If recommendations fail → skip personalization
- ✅ Circuit breakers for external dependencies
- ✅ Database replication (master-slave)

---

#### 4. **Maintainability Analysis**

**Think:** "How to evolve the system?"
- Frequent feature additions
- A/B testing needed
- Bug fixes without downtime

**Deduced NFRs:**
- ✅ Microservices architecture (loose coupling)
- ✅ Service mesh for inter-service communication
- ✅ Feature flags for gradual rollouts
- ✅ Comprehensive logging and monitoring
- ✅ Blue-green deployments

---

#### 5. **Performance Analysis**

**Think:** "Response time expectations?"
- Feed load < 500ms (P95)
- Search results < 200ms
- Post creation < 300ms
- Profile load < 400ms
- Message delivery < 100ms (real-time)

**Deduced NFRs:**
- ✅ Redis for caching:
  - User sessions (30 min TTL)
  - Feed cache (10 min TTL)
  - Connection lists (60 min TTL)
- ✅ Database query optimization (indexes on user_id, created_at)
- ✅ CDN for static assets (images, videos)
- ✅ WebSocket for real-time messaging
- ✅ Pagination for feeds/search (load 20 items at a time)
- ✅ Lazy loading for images

---

#### 6. **Security Analysis**

**Think:** "What security risks exist?"
- Unauthorized access to profiles/messages
- Spam and fake accounts
- Data privacy (GDPR compliance)
- Cross-site scripting (XSS)

**Deduced NFRs:**
- ✅ OAuth 2.0 authentication
- ✅ JWT tokens for session management (rotate every 15 min)
- ✅ HTTPS for all endpoints
- ✅ Rate limiting (100 requests/min per user)
- ✅ CAPTCHA for registration/suspicious activity
- ✅ Content moderation (AI + human review)
- ✅ Privacy controls (who can see profile, send messages)
- ✅ Data encryption at rest (AES-256)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Users register" | User |
| "Create profiles" | Profile |
| "Work experience" | WorkExperience |
| "Education" | Education |
| "Skills with endorsements" | Skill, Endorsement |
| "Connection requests" | Connection, ConnectionRequest |
| "Create posts" | Post |
| "News feed" | Feed |
| "Like, comment, share" | Like, Comment, Share |
| "Follow companies" | Company, Follow |
| "Mentions and hashtags" | Mention, Hashtag |
| "Job listings" | Job, JobApplication |
| "Direct messages" | Message, Conversation |
| "Notifications" | Notification |
| "Recommendations" | Recommendation |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| User | ✅ YES | Core entity with identity, behavior, lifecycle |
| Profile | ❌ NO | Part of User (embedded attributes) |
| WorkExperience | ✅ YES | Complex structure with multiple attributes |
| Education | ✅ YES | Similar to WorkExperience |
| Skill | ✅ YES | Reusable across users, has endorsements |
| Endorsement | ✅ YES | Relationship between users and skills |
| Connection | ✅ YES | Bidirectional relationship entity |
| ConnectionRequest | ✅ YES | Lifecycle separate from Connection |
| Post | ✅ YES | Rich content entity with lifecycle |
| Feed | ❌ NO | Computed view, not stored entity |
| Like | ✅ YES | User-Post relationship |
| Comment | ✅ YES | Rich entity with replies |
| Share | ✅ YES | User-Post relationship with optional note |
| Company | ✅ YES | Distinct entity type with pages |
| Follow | ✅ YES | User-Company/User relationship |
| Mention | ❌ NO | Metadata within Post |
| Hashtag | ✅ YES | Reusable tag entity |
| Job | ✅ YES | Complex entity with requirements |
| JobApplication | ✅ YES | User-Job relationship with status |
| Message | ✅ YES | Content entity in conversations |
| Conversation | ✅ YES | Container for messages |
| Notification | ✅ YES | Event entity with type, read status |
| Recommendation | ✅ YES | Written reference between users |

### Final Entity List

**Core User Entities:**
1. **User** - Primary actor
2. **WorkExperience** - Job history entry
3. **Education** - Academic history entry
4. **Skill** - Professional skill (reusable)
5. **UserSkill** - User-Skill association with endorsements
6. **Endorsement** - Skill endorsement record

**Connection Entities:**
7. **Connection** - Bidirectional relationship
8. **ConnectionRequest** - Pending connection invitation
9. **Follow** - One-way follow relationship

**Content Entities:**
10. **Post** - User-generated content
11. **PostType** - Enum (TEXT, IMAGE, VIDEO, ARTICLE, POLL, JOB)
12. **Comment** - Post comment (recursive for replies)
13. **Like** - User-Post/Comment like
14. **Share** - Post share with optional note

**Job Entities:**
15. **Company** - Organization profile
16. **Job** - Job posting
17. **JobApplication** - User application record
18. **JobStatus** - Enum (SAVED, APPLIED, INTERVIEWING, REJECTED, ACCEPTED)

**Messaging Entities:**
19. **Conversation** - Chat container
20. **ConversationMember** - User-Conversation association
21. **Message** - Individual message
22. **MessageStatus** - Enum (SENT, DELIVERED, READ)

**Other Entities:**
23. **Notification** - System notification
24. **NotificationType** - Enum (CONNECTION, COMMENT, JOB_MATCH, MESSAGE)
25. **Hashtag** - Content tag
26. **Recommendation** - Written reference
27. **Privacy** - Enum (PUBLIC, CONNECTIONS_ONLY, PRIVATE)

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: User Relationships

#### User ↔ WorkExperience
**Conclusion:** **Composition** (experience part of user profile)
```
User ◆────→ WorkExperience [0..*]
```

#### User ↔ Education
**Conclusion:** **Composition** (education part of user profile)
```
User ◆────→ Education [0..*]
```

#### User ↔ Skill (via UserSkill)
**Conclusion:** **Many-to-Many** (users have many skills, skills shared by many users)
```
User ─────→ UserSkill ←───── Skill
```

#### User ↔ User (Connection)
**Conclusion:** **Many-to-Many** (bidirectional friendship)
```
User ←───→ Connection ←───→ User
```

---

### Pass 2: Content Relationships

#### User ↔ Post
**Conclusion:** **Composition** (user owns posts)
```
User ◆────→ Post [0..*]
```

#### Post ↔ Comment
**Conclusion:** **Composition** (comments belong to post)
```
Post ◆────→ Comment [0..*]
```

#### Comment ↔ Comment (Replies)
**Conclusion:** **Self-referencing** (recursive replies)
```
Comment ◆────→ Comment [0..*] (replies)
```

#### User ↔ Post (Like)
**Conclusion:** **Many-to-Many** (users like many posts)
```
User ─────→ Like ←───── Post/Comment
```

---

### Pass 3: Job Relationships

#### Company ↔ Job
**Conclusion:** **Composition** (company owns job postings)
```
Company ◆────→ Job [0..*]
```

#### User ↔ Job (Application)
**Conclusion:** **Many-to-Many** (users apply to many jobs)
```
User ─────→ JobApplication ←───── Job
```

---

### Pass 4: Messaging Relationships

#### User ↔ Conversation (via ConversationMember)
**Conclusion:** **Many-to-Many** (users in multiple conversations)
```
User ─────→ ConversationMember ←───── Conversation
```

#### Conversation ↔ Message
**Conclusion:** **Composition** (messages belong to conversation)
```
Conversation ◆────→ Message [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| User → WorkExperience | 1:N | Composition |
| User → Education | 1:N | Composition |
| User ↔ Skill | M:N | Via UserSkill |
| User ↔ User (Connection) | M:N | Via Connection |
| User → Post | 1:N | Composition |
| Post → Comment | 1:N | Composition |
| Comment → Comment | 1:N | Self-reference |
| User → Like | 1:N | Association |
| Company → Job | 1:N | Composition |
| User ↔ Job | M:N | Via JobApplication |
| User ↔ Conversation | M:N | Via ConversationMember |
| Conversation → Message | 1:N | Composition |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│    PostType     │  │   JobStatus      │  │ MessageStatus   │
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ TEXT            │  │ SAVED            │  │ SENT            │
│ IMAGE           │  │ APPLIED          │  │ DELIVERED       │
│ VIDEO           │  │ INTERVIEWING     │  │ READ            │
│ ARTICLE         │  │ REJECTED         │  └─────────────────┘
│ POLL            │  │ ACCEPTED         │
│ JOB             │  └──────────────────┘
└─────────────────┘

┌─────────────────┐  ┌──────────────────────┐
│ <<enumeration>> │  │ <<enumeration>>      │
│    Privacy      │  │  NotificationType    │
├─────────────────┤  ├──────────────────────┤
│ PUBLIC          │  │ CONNECTION_REQUEST   │
│ CONNECTIONS     │  │ CONNECTION_ACCEPTED  │
│ PRIVATE         │  │ POST_COMMENT         │
└─────────────────┘  │ POST_LIKE            │
                     │ JOB_MATCH            │
                     │ MESSAGE_RECEIVED     │
                     │ PROFILE_VIEW         │
                     │ ENDORSEMENT          │
                     └──────────────────────┘
```

---

### Class Diagram 2: User & Profile

```
┌───────────────────────────────────────────────────────────┐
│                         User                              │
├───────────────────────────────────────────────────────────┤
│ - userId: String (UUID)                                   │
│ - email: String (unique, indexed)                         │
│ - passwordHash: String                                    │
│ - firstName: String                                       │
│ - lastName: String                                        │
│ - headline: String                                        │
│ - location: String                                        │
│ - profilePhotoUrl: String                                 │
│ - backgroundPhotoUrl: String                              │
│ - about: String (2600 chars max)                          │
│ - isPremium: boolean                                      │
│ - isOpenToWork: boolean                                   │
│ - connectionCount: int                                    │
│ - followerCount: int                                      │
│ - privacySettings: Map<String, Privacy>                   │
│ - createdAt: LocalDateTime                                │
│ - lastActive: LocalDateTime                               │
│ - experiences: List<WorkExperience>  ◆──────────────┐     │
│ - educations: List<Education>        ◆──────────────┼──┐  │
│ - skills: List<UserSkill>            ◆──────────────┼──┼──┐
├───────────────────────────────────────────────────────────┤
│ + User(email, password, firstName, lastName)              │
│ + addExperience(exp: WorkExperience): void                │
│ + addEducation(edu: Education): void                      │
│ + addSkill(skill: Skill): void                            │
│ + updateProfilePhoto(url: String): void                   │
│ + setOpenToWork(status: boolean): void                    │
│ + canSendMessage(other: User): boolean                    │
│ + canViewProfile(other: User): boolean                    │
│ + getProfileCompleteness(): int                           │
└───────────────────────────────────────────────────────────┘
         │                    │                │
         │                    │                │
         ▼                    ▼                ▼
┌──────────────────┐  ┌──────────────┐  ┌─────────────────┐
│ WorkExperience   │  │  Education   │  │   UserSkill     │
├──────────────────┤  ├──────────────┤  ├─────────────────┤
│ - id: String     │  │ - id: String │  │ - skill: Skill  │
│ - company: String│  │ - school:    │  │ - user: User    │
│ - title: String  │  │   String     │  │ - endorsements: │
│ - location:      │  │ - degree:    │  │   List<Endorse> │
│   String         │  │   String     │  │ - proficiency:  │
│ - description:   │  │ - field:     │  │   int (1-5)     │
│   String         │  │   String     │  │ - yearsOfExp:   │
│ - startDate:     │  │ - startDate: │  │   int           │
│   LocalDate      │  │   LocalDate  │  ├─────────────────┤
│ - endDate:       │  │ - endDate:   │  │ + addEndorse(): │
│   LocalDate      │  │   LocalDate  │  │   void          │
│ - isCurrent:     │  │ - grade: str │  └─────────────────┘
│   boolean        │  └──────────────┘
├──────────────────┤
│ + getDuration(): │
│   String         │
└──────────────────┘

┌────────────────────────────────┐
│           Skill                │
├────────────────────────────────┤
│ - skillId: String              │
│ - name: String (indexed)       │
│ - category: String             │
│ - totalEndorsements: long      │
├────────────────────────────────┤
│ + Skill(name)                  │
└────────────────────────────────┘

┌────────────────────────────────┐
│        Endorsement             │
├────────────────────────────────┤
│ - id: String                   │
│ - endorser: User               │
│ - endorsee: User               │
│ - skill: Skill                 │
│ - timestamp: LocalDateTime     │
├────────────────────────────────┤
│ + Endorsement(from, to, skill) │
└────────────────────────────────┘
```

---

### Class Diagram 3: Connections

```
┌──────────────────────────────────────────────────────────┐
│                   Connection                             │
├──────────────────────────────────────────────────────────┤
│ - connectionId: String                                   │
│ - user1: User                                            │
│ - user2: User                                            │
│ - connectedAt: LocalDateTime                             │
├──────────────────────────────────────────────────────────┤
│ + Connection(user1, user2)                               │
│ + getOtherUser(currentUser: User): User                  │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│              ConnectionRequest                           │
├──────────────────────────────────────────────────────────┤
│ - requestId: String                                      │
│ - sender: User                                           │
│ - receiver: User                                         │
│ - message: String (optional)                             │
│ - status: RequestStatus (PENDING, ACCEPTED, REJECTED)    │
│ - sentAt: LocalDateTime                                  │
│ - respondedAt: LocalDateTime                             │
├──────────────────────────────────────────────────────────┤
│ + ConnectionRequest(sender, receiver, message)           │
│ + accept(): Connection                                   │
│ + reject(): void                                         │
│ + withdraw(): void                                       │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                    Follow                                │
├──────────────────────────────────────────────────────────┤
│ - followId: String                                       │
│ - follower: User                                         │
│ - followee: User/Company                                 │
│ - followedAt: LocalDateTime                              │
├──────────────────────────────────────────────────────────┤
│ + Follow(follower, followee)                             │
│ + unfollow(): void                                       │
└──────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Posts & Engagement

```
┌───────────────────────────────────────────────────────────┐
│                        Post                               │
├───────────────────────────────────────────────────────────┤
│ - postId: String (UUID)                                   │
│ - author: User                                            │
│ - content: String (3000 chars max)                        │
│ - type: PostType                                          │
│ - mediaUrls: List<String>                                 │
│ - hashtags: List<Hashtag>                                 │
│ - mentionedUsers: List<User>                              │
│ - visibility: Privacy                                     │
│ - likeCount: int                                          │
│ - commentCount: int                                       │
│ - shareCount: int                                         │
│ - viewCount: int                                          │
│ - createdAt: LocalDateTime                                │
│ - updatedAt: LocalDateTime                                │
│ - isEdited: boolean                                       │
│ - comments: List<Comment>        ◆────────────────┐       │
│ - likes: List<Like>              ◆────────────────┼──┐    │
├───────────────────────────────────────────────────────────┤
│ + Post(author, content, type)                             │
│ + addComment(comment: Comment): void                      │
│ + like(user: User): void                                  │
│ + unlike(user: User): void                                │
│ + share(user: User, note: String): void                   │
│ + edit(newContent: String): boolean                       │
│ + canEdit(): boolean  // within 15 minutes                │
│ + extractHashtags(): List<String>                         │
│ + extractMentions(): List<String>                         │
└───────────────────────────────────────────────────────────┘
         │                                   │
         │                                   │
         ▼                                   ▼
┌─────────────────────────┐      ┌──────────────────────────┐
│       Comment           │      │         Like             │
├─────────────────────────┤      ├──────────────────────────┤
│ - commentId: String     │      │ - likeId: String         │
│ - author: User          │      │ - user: User             │
│ - post: Post            │      │ - target: Post/Comment   │
│ - parentComment: Comment│      │ - createdAt: DateTime    │
│ - content: String       │      ├──────────────────────────┤
│ - likeCount: int        │      │ + Like(user, target)     │
│ - replies: List<Comment>│      └──────────────────────────┘
│ - createdAt: DateTime   │
│ - updatedAt: DateTime   │
├─────────────────────────┤
│ + Comment(author, post, │
│          content)       │
│ + addReply(reply:       │
│           Comment): void│
│ + like(user: User): void│
└─────────────────────────┘

┌─────────────────────────┐
│        Share            │
├─────────────────────────┤
│ - shareId: String       │
│ - sharer: User          │
│ - originalPost: Post    │
│ - note: String (opt)    │
│ - sharedAt: DateTime    │
├─────────────────────────┤
│ + Share(user, post,     │
│        note)            │
└─────────────────────────┘

┌─────────────────────────┐
│       Hashtag           │
├─────────────────────────┤
│ - hashtagId: String     │
│ - tag: String (indexed) │
│ - postCount: long       │
│ - followersCount: long  │
├─────────────────────────┤
│ + Hashtag(tag)          │
└─────────────────────────┘
```

---

### Class Diagram 5: Jobs

```
┌────────────────────────────────────────────────────────────┐
│                        Company                             │
├────────────────────────────────────────────────────────────┤
│ - companyId: String                                        │
│ - name: String                                             │
│ - industry: String                                         │
│ - size: String (1-10, 11-50, 51-200, etc.)                │
│ - headquarters: String                                     │
│ - website: String                                          │
│ - description: String                                      │
│ - logoUrl: String                                          │
│ - followerCount: int                                       │
│ - jobs: List<Job>              ◆───────────────────┐       │
├────────────────────────────────────────────────────────────┤
│ + Company(name, industry)                                  │
│ + postJob(job: Job): void                                  │
│ + getActiveJobs(): List<Job>                               │
└────────────────────────────────────────────────────────────┘
                                                  │
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────┐
│                         Job                                │
├────────────────────────────────────────────────────────────┤
│ - jobId: String                                            │
│ - company: Company                                         │
│ - title: String                                            │
│ - description: String                                      │
│ - location: String                                         │
│ - locationType: String (ON_SITE, REMOTE, HYBRID)           │
│ - employmentType: String (FULL_TIME, PART_TIME, CONTRACT)  │
│ - experienceLevel: String (ENTRY, MID, SENIOR, EXECUTIVE)  │
│ - requiredSkills: List<Skill>                              │
│ - salaryMin: int                                           │
│ - salaryMax: int                                           │
│ - applicationUrl: String (external link)                   │
│ - isEasyApply: boolean                                     │
│ - postedAt: LocalDateTime                                  │
│ - expiresAt: LocalDateTime                                 │
│ - viewCount: int                                           │
│ - applicationCount: int                                    │
│ - isActive: boolean                                        │
│ - applications: List<JobApplication>  ◆────────────────┐   │
├────────────────────────────────────────────────────────────┤
│ + Job(company, title, description)                         │
│ + apply(user: User, resume: String): JobApplication        │
│ + isExpired(): boolean                                     │
│ + matchesUser(user: User): double  // similarity score     │
└────────────────────────────────────────────────────────────┘
                                                  │
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────┐
│                   JobApplication                           │
├────────────────────────────────────────────────────────────┤
│ - applicationId: String                                    │
│ - job: Job                                                 │
│ - applicant: User                                          │
│ - resumeUrl: String                                        │
│ - coverLetter: String                                      │
│ - status: JobStatus                                        │
│ - appliedAt: LocalDateTime                                 │
│ - updatedAt: LocalDateTime                                 │
├────────────────────────────────────────────────────────────┤
│ + JobApplication(user, job, resume)                        │
│ + updateStatus(status: JobStatus): void                    │
│ + withdraw(): void                                         │
└────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Messaging

```
┌──────────────────────────────────────────────────────────┐
│                   Conversation                           │
├──────────────────────────────────────────────────────────┤
│ - conversationId: String                                 │
│ - name: String (for group chats)                         │
│ - isGroup: boolean                                       │
│ - createdAt: LocalDateTime                               │
│ - lastMessageAt: LocalDateTime                           │
│ - members: List<ConversationMember> ◆────────────────┐   │
│ - messages: List<Message>           ◆────────────────┼─┐ │
├──────────────────────────────────────────────────────────┤
│ + Conversation(members: List<User>)                      │
│ + addMember(user: User): void                            │
│ + removeMember(user: User): void                         │
│ + sendMessage(sender: User, content: String): Message    │
│ + markAsRead(user: User): void                           │
└──────────────────────────────────────────────────────────┘
         │                                      │
         │                                      │
         ▼                                      ▼
┌──────────────────────┐          ┌──────────────────────────┐
│ ConversationMember   │          │       Message            │
├──────────────────────┤          ├──────────────────────────┤
│ - user: User         │          │ - messageId: String      │
│ - conversation:      │          │ - conversation:          │
│   Conversation       │          │   Conversation           │
│ - joinedAt: DateTime │          │ - sender: User           │
│ - lastReadAt:        │          │ - content: String        │
│   DateTime           │          │ - mediaUrl: String       │
│ - unreadCount: int   │          │ - status: MessageStatus  │
├──────────────────────┤          │ - sentAt: LocalDateTime  │
│ + markAsRead(): void │          │ - deliveredAt: DateTime  │
└──────────────────────┘          │ - readAt: LocalDateTime  │
                                  ├──────────────────────────┤
                                  │ + Message(sender, conv,  │
                                  │          content)        │
                                  │ + markAsDelivered(): void│
                                  │ + markAsRead(): void     │
                                  └──────────────────────────┘
```

---

### Class Diagram 7: Notifications

```
┌──────────────────────────────────────────────────────────┐
│                   Notification                           │
├──────────────────────────────────────────────────────────┤
│ - notificationId: String                                 │
│ - recipient: User                                        │
│ - type: NotificationType                                 │
│ - actor: User  // who triggered this notification        │
│ - targetId: String  // ID of post/job/message            │
│ - targetType: String (POST, JOB, MESSAGE, USER)          │
│ - message: String                                        │
│ - isRead: boolean                                        │
│ - createdAt: LocalDateTime                               │
│ - expiresAt: LocalDateTime                               │
├──────────────────────────────────────────────────────────┤
│ + Notification(recipient, type, actor, targetId)         │
│ + markAsRead(): void                                     │
│ + isExpired(): boolean                                   │
│ + getRedirectUrl(): String                               │
└──────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### User Service

```java
// UserService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private static UserService instance;
    private final Map<String, User> users;  // userId -> User
    private final Map<String, User> emailIndex;  // email -> User
    
    private UserService() {
        this.users = new ConcurrentHashMap<>();
        this.emailIndex = new ConcurrentHashMap<>();
    }
    
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User registerUser(String email, String password, 
                            String firstName, String lastName) {
        if (emailIndex.containsKey(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        User user = new User(email, password, firstName, lastName);
        users.put(user.getUserId(), user);
        emailIndex.put(email, user);
        
        return user;
    }
    
    public User authenticate(String email, String password) {
        User user = emailIndex.get(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        if (!user.verifyPassword(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        user.updateLastActive();
        return user;
    }
    
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    public List<User> searchUsers(String query, int limit) {
        return users.values().stream()
            .filter(u -> matchesQuery(u, query))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private boolean matchesQuery(User user, String query) {
        String lowerQuery = query.toLowerCase();
        return user.getFirstName().toLowerCase().contains(lowerQuery) ||
               user.getLastName().toLowerCase().contains(lowerQuery) ||
               user.getHeadline().toLowerCase().contains(lowerQuery);
    }
    
    public List<User> suggestConnections(User user, int limit) {
        // Simple recommendation: mutual connections' connections
        Set<User> suggestions = new HashSet<>();
        List<User> connections = ConnectionService.getInstance()
            .getConnections(user);
        
        for (User connection : connections) {
            List<User> secondDegree = ConnectionService.getInstance()
                .getConnections(connection);
            
            for (User candidate : secondDegree) {
                if (!candidate.equals(user) && 
                    !ConnectionService.getInstance()
                        .areConnected(user, candidate)) {
                    suggestions.add(candidate);
                }
            }
        }
        
        return suggestions.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

---

### Connection Service

```java
// ConnectionService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionService {
    private static ConnectionService instance;
    
    // userId -> Set<userId> (bidirectional)
    private final Map<String, Set<String>> connectionGraph;
    
    // requestId -> ConnectionRequest
    private final Map<String, ConnectionRequest> pendingRequests;
    
    // userId -> List<requestId> (received requests)
    private final Map<String, List<String>> userRequests;
    
    private ConnectionService() {
        this.connectionGraph = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.userRequests = new ConcurrentHashMap<>();
    }
    
    public static synchronized ConnectionService getInstance() {
        if (instance == null) {
            instance = new ConnectionService();
        }
        return instance;
    }
    
    public ConnectionRequest sendRequest(User sender, User receiver, 
                                        String message) {
        // Check if already connected
        if (areConnected(sender, receiver)) {
            throw new IllegalStateException("Already connected");
        }
        
        // Check if request already exists
        if (hasPendingRequest(sender, receiver)) {
            throw new IllegalStateException("Request already sent");
        }
        
        // Check connection limit
        if (getConnectionCount(sender) >= 30000) {
            throw new IllegalStateException("Connection limit reached");
        }
        
        ConnectionRequest request = new ConnectionRequest(sender, receiver, message);
        pendingRequests.put(request.getRequestId(), request);
        
        userRequests.computeIfAbsent(receiver.getUserId(), k -> new ArrayList<>())
            .add(request.getRequestId());
        
        // Send notification
        NotificationService.getInstance().notify(
            receiver, 
            NotificationType.CONNECTION_REQUEST, 
            sender, 
            request.getRequestId()
        );
        
        return request;
    }
    
    public Connection acceptRequest(String requestId, User acceptor) {
        ConnectionRequest request = pendingRequests.get(requestId);
        
        if (request == null) {
            throw new IllegalArgumentException("Request not found");
        }
        
        if (!request.getReceiver().equals(acceptor)) {
            throw new IllegalArgumentException("Not authorized");
        }
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request already processed");
        }
        
        // Create connection (bidirectional)
        User user1 = request.getSender();
        User user2 = request.getReceiver();
        
        connectionGraph.computeIfAbsent(user1.getUserId(), k -> new HashSet<>())
            .add(user2.getUserId());
        
        connectionGraph.computeIfAbsent(user2.getUserId(), k -> new HashSet<>())
            .add(user1.getUserId());
        
        // Update counts
        user1.incrementConnectionCount();
        user2.incrementConnectionCount();
        
        // Update request status
        request.accept();
        pendingRequests.remove(requestId);
        
        // Send notification
        NotificationService.getInstance().notify(
            user1, 
            NotificationType.CONNECTION_ACCEPTED, 
            user2, 
            null
        );
        
        return new Connection(user1, user2);
    }
    
    public void rejectRequest(String requestId, User rejector) {
        ConnectionRequest request = pendingRequests.get(requestId);
        
        if (request == null || !request.getReceiver().equals(rejector)) {
            throw new IllegalArgumentException("Invalid request");
        }
        
        request.reject();
        pendingRequests.remove(requestId);
    }
    
    public boolean areConnected(User user1, User user2) {
        Set<String> connections = connectionGraph.get(user1.getUserId());
        return connections != null && connections.contains(user2.getUserId());
    }
    
    public List<User> getConnections(User user) {
        Set<String> connectionIds = connectionGraph.getOrDefault(
            user.getUserId(), 
            Collections.emptySet()
        );
        
        return connectionIds.stream()
            .map(id -> UserService.getInstance().getUserById(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public int getConnectionCount(User user) {
        return connectionGraph.getOrDefault(user.getUserId(), Collections.emptySet())
            .size();
    }
    
    public List<ConnectionRequest> getPendingRequests(User user) {
        return userRequests.getOrDefault(user.getUserId(), Collections.emptyList())
            .stream()
            .map(pendingRequests::get)
            .filter(Objects::nonNull)
            .filter(r -> r.getStatus() == RequestStatus.PENDING)
            .collect(Collectors.toList());
    }
    
    private boolean hasPendingRequest(User sender, User receiver) {
        return pendingRequests.values().stream()
            .anyMatch(r -> r.getSender().equals(sender) && 
                          r.getReceiver().equals(receiver) &&
                          r.getStatus() == RequestStatus.PENDING);
    }
}
```

---

### Post Service

```java
// PostService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PostService {
    private static PostService instance;
    
    private final Map<String, Post> posts;  // postId -> Post
    private final Map<String, List<String>> userPosts;  // userId -> postIds
    
    private PostService() {
        this.posts = new ConcurrentHashMap<>();
        this.userPosts = new ConcurrentHashMap<>();
    }
    
    public static synchronized PostService getInstance() {
        if (instance == null) {
            instance = new PostService();
        }
        return instance;
    }
    
    public Post createPost(User author, String content, PostType type, 
                          List<String> mediaUrls, Privacy visibility) {
        Post post = new Post(author, content, type, mediaUrls, visibility);
        
        posts.put(post.getPostId(), post);
        userPosts.computeIfAbsent(author.getUserId(), k -> new ArrayList<>())
            .add(post.getPostId());
        
        // Extract and save hashtags
        List<String> hashtags = post.extractHashtags();
        for (String tag : hashtags) {
            HashtagService.getInstance().incrementCount(tag);
        }
        
        // Notify mentioned users
        List<User> mentions = post.getMentionedUsers();
        for (User mentioned : mentions) {
            NotificationService.getInstance().notify(
                mentioned,
                NotificationType.POST_MENTION,
                author,
                post.getPostId()
            );
        }
        
        // Trigger feed fanout (async)
        FeedService.getInstance().fanOutPost(post);
        
        return post;
    }
    
    public Comment addComment(Post post, User author, String content, 
                             Comment parentComment) {
        Comment comment = new Comment(author, post, content, parentComment);
        post.addComment(comment);
        
        // Notify post author
        if (!post.getAuthor().equals(author)) {
            NotificationService.getInstance().notify(
                post.getAuthor(),
                NotificationType.POST_COMMENT,
                author,
                post.getPostId()
            );
        }
        
        // Notify parent comment author (for replies)
        if (parentComment != null && 
            !parentComment.getAuthor().equals(author)) {
            NotificationService.getInstance().notify(
                parentComment.getAuthor(),
                NotificationType.COMMENT_REPLY,
                author,
                comment.getCommentId()
            );
        }
        
        return comment;
    }
    
    public synchronized void likePost(Post post, User user) {
        // Idempotent: check if already liked
        if (post.isLikedBy(user)) {
            return;  // Already liked
        }
        
        Like like = new Like(user, post);
        post.addLike(like);
        
        // Notify post author
        if (!post.getAuthor().equals(user)) {
            NotificationService.getInstance().notify(
                post.getAuthor(),
                NotificationType.POST_LIKE,
                user,
                post.getPostId()
            );
        }
    }
    
    public synchronized void unlikePost(Post post, User user) {
        post.removeLike(user);
    }
    
    public Share sharePost(Post originalPost, User sharer, String note) {
        Share share = new Share(sharer, originalPost, note);
        originalPost.incrementShareCount();
        
        // Create a new post that references the original
        Post sharePost = new Post(sharer, note, PostType.SHARE, 
                                 Collections.emptyList(), Privacy.PUBLIC);
        sharePost.setSharedPost(originalPost);
        
        posts.put(sharePost.getPostId(), sharePost);
        
        return share;
    }
    
    public Post getPost(String postId) {
        Post post = posts.get(postId);
        if (post != null) {
            post.incrementViewCount();
        }
        return post;
    }
    
    public List<Post> getUserPosts(User user, int limit) {
        return userPosts.getOrDefault(user.getUserId(), Collections.emptyList())
            .stream()
            .map(posts::get)
            .filter(Objects::nonNull)
            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

---

### Feed Service

```java
// FeedService.java
import java.util.*;
import java.util.concurrent.*;

public class FeedService {
    private static FeedService instance;
    
    // userId -> List<postId> (pre-computed feed)
    private final Map<String, LinkedList<String>> userFeeds;
    
    // Cache TTL: 10 minutes
    private final Map<String, Long> feedTimestamps;
    private static final long CACHE_TTL_MS = 10 * 60 * 1000;
    
    // Async executor for fan-out
    private final ExecutorService executor;
    
    private FeedService() {
        this.userFeeds = new ConcurrentHashMap<>();
        this.feedTimestamps = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    public static synchronized FeedService getInstance() {
        if (instance == null) {
            instance = new FeedService();
        }
        return instance;
    }
    
    /**
     * Fan-out on write: When user creates post, push to followers' feeds
     */
    public void fanOutPost(Post post) {
        executor.submit(() -> {
            User author = post.getAuthor();
            
            // Get author's connections
            List<User> connections = ConnectionService.getInstance()
                .getConnections(author);
            
            // Add post to each connection's feed
            for (User connection : connections) {
                addToFeed(connection.getUserId(), post.getPostId());
            }
            
            // Also add to author's own feed
            addToFeed(author.getUserId(), post.getPostId());
        });
    }
    
    private synchronized void addToFeed(String userId, String postId) {
        LinkedList<String> feed = userFeeds.computeIfAbsent(
            userId, 
            k -> new LinkedList<>()
        );
        
        feed.addFirst(postId);
        
        // Keep only latest 1000 posts
        if (feed.size() > 1000) {
            feed.removeLast();
        }
        
        // Invalidate cache
        feedTimestamps.put(userId, System.currentTimeMillis());
    }
    
    /**
     * Generate feed on demand (fallback if cache expired)
     */
    public List<Post> getFeed(User user, int page, int pageSize) {
        // Check cache
        if (isCacheValid(user.getUserId())) {
            return getCachedFeed(user, page, pageSize);
        }
        
        // Regenerate feed
        return generateFeed(user, page, pageSize);
    }
    
    private boolean isCacheValid(String userId) {
        Long timestamp = feedTimestamps.get(userId);
        if (timestamp == null) return false;
        
        return (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS;
    }
    
    private List<Post> getCachedFeed(User user, int page, int pageSize) {
        LinkedList<String> postIds = userFeeds.get(user.getUserId());
        
        if (postIds == null) {
            return Collections.emptyList();
        }
        
        int start = page * pageSize;
        int end = Math.min(start + pageSize, postIds.size());
        
        if (start >= postIds.size()) {
            return Collections.emptyList();
        }
        
        return postIds.subList(start, end).stream()
            .map(id -> PostService.getInstance().getPost(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private List<Post> generateFeed(User user, int page, int pageSize) {
        // Get all posts from connections
        List<User> connections = ConnectionService.getInstance()
            .getConnections(user);
        
        List<Post> allPosts = new ArrayList<>();
        
        for (User connection : connections) {
            allPosts.addAll(PostService.getInstance()
                .getUserPosts(connection, 50));
        }
        
        // Add user's own posts
        allPosts.addAll(PostService.getInstance().getUserPosts(user, 50));
        
        // Sort by engagement score (likes + comments + recency)
        allPosts.sort((p1, p2) -> {
            double score1 = calculateEngagementScore(p1);
            double score2 = calculateEngagementScore(p2);
            return Double.compare(score2, score1);
        });
        
        // Pagination
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allPosts.size());
        
        if (start >= allPosts.size()) {
            return Collections.emptyList();
        }
        
        List<Post> feed = allPosts.subList(start, end);
        
        // Cache the result
        cacheF eed(user.getUserId(), allPosts);
        
        return feed;
    }
    
    private double calculateEngagementScore(Post post) {
        long ageHours = ChronoUnit.HOURS.between(
            post.getCreatedAt(), 
            LocalDateTime.now()
        );
        
        double recencyFactor = Math.max(0, 1.0 - (ageHours / 168.0)); // 1 week decay
        
        double engagement = post.getLikeCount() * 1.0 + 
                           post.getCommentCount() * 2.0 + 
                           post.getShareCount() * 3.0;
        
        return engagement * recencyFactor;
    }
    
    private void cacheFeed(String userId, List<Post> posts) {
        LinkedList<String> postIds = new LinkedList<>();
        posts.forEach(p -> postIds.add(p.getPostId()));
        
        userFeeds.put(userId, postIds);
        feedTimestamps.put(userId, System.currentTimeMillis());
    }
}
```

---

### Job Service

```java
// JobService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JobService {
    private static JobService instance;
    
    private final Map<String, Job> jobs;
    private final Map<String, JobApplication> applications;
    private final Map<String, Set<String>> userApplications;  // userId -> jobIds
    
    private JobService() {
        this.jobs = new ConcurrentHashMap<>();
        this.applications = new ConcurrentHashMap<>();
        this.userApplications = new ConcurrentHashMap<>();
    }
    
    public static synchronized JobService getInstance() {
        if (instance == null) {
            instance = new JobService();
        }
        return instance;
    }
    
    public Job postJob(Company company, String title, String description,
                       String location, List<Skill> requiredSkills, 
                       int salaryMin, int salaryMax) {
        Job job = new Job(company, title, description, location, 
                         requiredSkills, salaryMin, salaryMax);
        
        jobs.put(job.getJobId(), job);
        
        // Find matching candidates and notify
        List<User> matches = findMatchingCandidates(job, 50);
        for (User candidate : matches) {
            NotificationService.getInstance().notify(
                candidate,
                NotificationType.JOB_MATCH,
                null,
                job.getJobId()
            );
        }
        
        return job;
    }
    
    public JobApplication applyForJob(User user, Job job, 
                                      String resumeUrl, String coverLetter) {
        // Idempotent: check if already applied
        if (hasApplied(user, job)) {
            throw new IllegalStateException("Already applied to this job");
        }
        
        JobApplication application = new JobApplication(
            user, job, resumeUrl, coverLetter
        );
        
        applications.put(application.getApplicationId(), application);
        
        userApplications.computeIfAbsent(user.getUserId(), k -> new HashSet<>())
            .add(job.getJobId());
        
        job.incrementApplicationCount();
        
        return application;
    }
    
    public boolean hasApplied(User user, Job job) {
        Set<String> jobIds = userApplications.get(user.getUserId());
        return jobIds != null && jobIds.contains(job.getJobId());
    }
    
    public List<Job> searchJobs(String query, String location, 
                                List<String> skills, int limit) {
        return jobs.values().stream()
            .filter(Job::isActive)
            .filter(j -> matchesSearch(j, query, location, skills))
            .sorted((j1, j2) -> j2.getPostedAt().compareTo(j1.getPostedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private boolean matchesSearch(Job job, String query, 
                                  String location, List<String> skills) {
        if (query != null && !query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            if (!job.getTitle().toLowerCase().contains(lowerQuery) &&
                !job.getDescription().toLowerCase().contains(lowerQuery)) {
                return false;
            }
        }
        
        if (location != null && !job.getLocation().contains(location)) {
            return false;
        }
        
        if (skills != null && !skills.isEmpty()) {
            Set<String> jobSkills = job.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());
            
            boolean hasMatch = skills.stream()
                .anyMatch(jobSkills::contains);
            
            if (!hasMatch) return false;
        }
        
        return true;
    }
    
    public List<Job> recommendJobs(User user, int limit) {
        // Score jobs based on skill match and location
        List<JobScore> scores = new ArrayList<>();
        
        for (Job job : jobs.values()) {
            if (!job.isActive()) continue;
            
            double score = calculateJobScore(user, job);
            if (score > 0) {
                scores.add(new JobScore(job, score));
            }
        }
        
        return scores.stream()
            .sorted((s1, s2) -> Double.compare(s2.score, s1.score))
            .map(s -> s.job)
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private double calculateJobScore(User user, Job job) {
        double score = 0.0;
        
        // Skill match (60% weight)
        Set<String> userSkills = user.getSkills().stream()
            .map(us -> us.getSkill().getName())
            .collect(Collectors.toSet());
        
        Set<String> jobSkills = job.getRequiredSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.toSet());
        
        long matchingSkills = userSkills.stream()
            .filter(jobSkills::contains)
            .count();
        
        if (!jobSkills.isEmpty()) {
            score += (matchingSkills * 1.0 / jobSkills.size()) * 0.6;
        }
        
        // Location match (20% weight)
        if (user.getLocation().equals(job.getLocation())) {
            score += 0.2;
        }
        
        // Experience level match (20% weight)
        // Simplified: assume we have experience years on user
        // and experience level on job (ENTRY, MID, SENIOR)
        score += 0.2;  // Placeholder
        
        return score;
    }
    
    private List<User> findMatchingCandidates(Job job, int limit) {
        // Reverse of recommendJobs: find users matching this job
        // Implementation similar to recommendJobs but iterating users
        return new ArrayList<>();  // Simplified
    }
    
    private static class JobScore {
        Job job;
        double score;
        
        JobScore(Job job, double score) {
            this.job = job;
            this.score = score;
        }
    }
}
```

---

### Messaging Service

```java
// MessagingService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingService {
    private static MessagingService instance;
    
    private final Map<String, Conversation> conversations;
    private final Map<String, List<String>> userConversations;  // userId -> conversationIds
    
    private MessagingService() {
        this.conversations = new ConcurrentHashMap<>();
        this.userConversations = new ConcurrentHashMap<>();
    }
    
    public static synchronized MessagingService getInstance() {
        if (instance == null) {
            instance = new  MessagingService();
        }
        return instance;
    }
    
    public Conversation createConversation(List<User> members, boolean isGroup, 
                                          String name) {
        // Check if 1-on-1 conversation already exists
        if (!isGroup && members.size() == 2) {
            Conversation existing = findExistingConversation(members.get(0), members.get(1));
            if (existing != null) {
                return existing;
            }
        }
        
        Conversation conversation = new Conversation(members, isGroup, name);
        conversations.put(conversation.getConversationId(), conversation);
        
        for (User member : members) {
            userConversations.computeIfAbsent(member.getUserId(), k -> new ArrayList<>())
                .add(conversation.getConversationId());
        }
        
        return conversation;
    }
    
    public Message sendMessage(Conversation conversation, User sender, 
                              String content, String mediaUrl) {
        // Verify sender is member
        if (!conversation.hasMember(sender)) {
            throw new IllegalArgumentException("Not a conversation member");
        }
        
        Message message = conversation.sendMessage(sender, content, mediaUrl);
        
        // Notify other members (real-time via WebSocket)
        for (User member : conversation.getMembers()) {
            if (!member.equals(sender)) {
                NotificationService.getInstance().notify(
                    member,
                    NotificationType.MESSAGE_RECEIVED,
                    sender,
                    conversation.getConversationId()
                );
            }
        }
        
        return message;
    }
    
    public void markAsRead(Conversation conversation, User user) {
        conversation.markAsRead(user);
    }
    
    public List<Conversation> getUserConversations(User user) {
        return userConversations.getOrDefault(user.getUserId(), Collections.emptyList())
            .stream()
            .map(conversations::get)
            .filter(Objects::nonNull)
            .sorted((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()))
            .collect(Collectors.toList());
    }
    
    public int getUnreadCount(User user) {
        return getUserConversations(user).stream()
            .mapToInt(c -> c.getUnreadCount(user))
            .sum();
    }
    
    private Conversation findExistingConversation(User user1, User user2) {
        List<String> user1Convos = userConversations.get(user1.getUserId());
        if (user1Convos == null) return null;
        
        for (String convoId : user1Convos) {
            Conversation convo = conversations.get(convoId);
            if (convo != null && !convo.isGroup() && 
                convo.hasMember(user2)) {
                return convo;
            }
        }
        
        return null;
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Singleton Pattern
**Where:** All service classes (UserService, PostService, ConnectionService, etc.)  
**Why:** Single instance manages global state  
**Interview Justification:** "Services coordinate cross-cutting concerns (users, posts, connections). Singleton ensures single source of truth and simplifies access."

---

### Pattern 2: Fan-Out on Write
**Where:** Feed generation (`FeedService.fanOutPost()`)  
**Why:** Pre-compute feeds for fast read performance  
**Interview Justification:** "When user posts, we push to all followers' pre-computed feeds. Read-heavy system (1000 reads : 1 write), so optimize reads at write cost."

---

### Pattern 3: Observer Pattern
**Where:** Notification system  
**Why:** Multiple subscribers react to events  
**Interview Justification:** "Post comments, likes, connection requests trigger notifications. Observer pattern decouples event producers from consumers."

---

### Pattern 4: Strategy Pattern
**Where:** Feed ranking algorithms  
**Why:** Multiple feed ranking strategies (chronological, engagement-based, ML-based)  
**Interview Justification:** "Different users might get different ranking algorithms (A/B testing). Strategy pattern enables swapping algorithms without changing feed service."

---

### Pattern 5: Repository Pattern
**Where:** Data access layer (implicit in services)  
**Why:** Abstract database operations  
**Interview Justification:** "Services act as repositories. Separates business logic from data persistence. Easy to swap in-memory maps with actual databases."

---

## 💡 Step 8: Interview Discussion Points

### 1. Feed Generation: Fan-Out vs Pull

**Interviewer:** "Why fan-out on write? What about users with millions of followers?"

**Answer:**
"**Two approaches:**

**1. Fan-Out on Write (Push Model) - What I used:**
- When user posts → write to all followers' feeds immediately
- **Pros:** Fast reads (feed pre-computed)
- **Cons:** Slow writes for high-follower users (celebrities)
- **Use case:** Regular users (< 10K followers)

**2. Pull on Read (Pull Model):**
- When user requests feed → query posts from all followees
- **Pros:** Fast writes
- **Cons:** Slow reads (aggregate from many sources)
- **Use case:** High-follower accounts

**Hybrid Approach (LinkedIn's actual solution):**
```
if (author.followerCount < 10000) {
    fanOutOnWrite();  // Pre-compute for followers
} else {
    // Celebrity: don't fan out
    // Followers pull this user's posts on-demand
    pullOnRead();
}
```

**Feed generation:**
```
feed = preComputedFeed  // Fan-out posts
       + pullFromCelebrities()  // Query celebrity posts on-demand
       + rankByEngagement()
```

**Trade-off:** Most users get fast reads, celebrities take slightly longer to fetch."

---

### 2. Database Sharding Strategy

**Interviewer:** "How would you shard the database?"

**Answer:**
"**Primary sharding key: User ID**

**Why User ID?**
- Most queries are user-centric (\"get my feed\", \"my connections\", \"my posts\")
- Avoids cross-shard joins for common operations

**Sharding scheme:**
```
shardId = hash(userId) % NUM_SHARDS
```

**Example with 16 shards:**
- User 'abc123' → shard 7
- All data for 'abc123' lives on shard 7:
  - Profile
  - Posts
  - Connections (stored bidirectionally)
  - Applications

**Cross-shard queries:**
- **Search:** Use Elasticsearch (replicated index across all shards)
- **Feed from multiple users:** Fan-out query to multiple shards, aggregate results

**Hot shard problem:**
- If celebrity on shard 3 → shard 3 overloaded
- **Solution:** Consistent hashing with virtual nodes (1000 virtual nodes per shard)
- Celebrities spread across multiple physical shards

**Connection storage:**
```
// Bidirectional storage (denormalized)
Shard[userId1]: Connection(userId1 ↔ userId2)
Shard[userId2]: Connection(userId2 ↔ userId1)
```

This avoids cross-shard lookup for \"get my connections\"."

---

### 3. Eventual Consistency Trade-offs

**Interviewer:** "Where can we tolerate eventual consistency?"

**Answer:**
"**Strong Consistency Required:**
1. ✅ **Connections:** Can't have asymmetric connections (A connected to B, but not vice versa)
2. ✅ **Job Applications:** No duplicate applications allowed
3. ✅ **Messages:** Delivery guarantees needed

**Eventual Consistency OK:**
1. ✅ **Like counts:** If count shows 99 instead of 100 for 1 second, acceptable
2. ✅ **Feed updates:** If new post appears 5 seconds late, acceptable
3. ✅ **Endorsement counts:** Stale counts OK
4. ✅ **Profile views:** \"Who viewed your profile\" can be delayed

**Implementation:**
```java
// Strong consistency (use database transaction)
@Transactional
public Connection acceptRequest(ConnectionRequest request) {
    // Write to both user's connection lists atomically
    connection1.add(user2);
    connection2.add(user1);
    commit();
}

// Eventual consistency (use message queue)
public void likePost(Post post, User user) {
    post.incrementLikeCount();  // Async write to DB
    queue.publish(new LikeEvent(post, user));  // Process later
}
```

**Monitoring:**
- Track replication lag
- Alert if lag > 5 seconds
- Graceful degradation: show cached data if replica unavailable"

---

### 4. Scalability: Handling 310M MAU

**Interviewer:** "How do you scale to 310 million monthly active users?"

**Answer:**
"**Read path (95% of traffic):**

**1. CDN (Cloudflare):**
- Static assets (images, videos): 60% of bandwidth
- Geographically distributed
- Cache hit ratio: 90%

**2. Application-level caching (Redis):**
```
Cache Key Structure:
- user:{userId}:profile  → 1 hour TTL
- user:{userId}:feed     → 10 min TTL
- post:{postId}          → 1 hour TTL
- user:{userId}:connections → 1 hour TTL
```

Cache hit ratio: 80%

**3. Database read replicas:**
- 1 master + 5 read replicas per shard
- Route reads to replicas via load balancer

**Write path (5% of traffic):**

**1. Message queue (Kafka):**
```
Post Created → Kafka Topic → Fan-Out Workers (50 instances)
```

Async processing prevents write spikes from overwhelming DB

**2. Database writes:**
- Batch inserts where possible
- Connection pooling (100 connections per shard)

**Capacity planning:**
- 310M MAU, 50% DAU = 155M daily active
- Avg 10 requests/user/day = 1.55B requests/day
- Peak (3× avg) = 4.65B requests/day = ~54K req/sec
- Each web server handles 1K req/sec → 54 servers
- With 2× redundancy → **108 web servers**

**16 database shards:**
- 54K req/sec ÷ 16 shards = ~3.4K req/sec per shard
- Each shard: 1 master + 5 replicas
- Each replica handles ~680 req/sec (achievable with caching)"

---

### 5. Search Implementation

**Interviewer:** "How do you implement search across users, jobs, posts?"

**Answer:**
"**Elasticsearch for full-text search**

**Index structure:**
```json
// User Index
{
  "index": "users",
  "fields": ["firstName", "lastName", "headline", "skills"],
  "userId": "abc123",
  "firstName": "John",
  "lastName": "Doe",
  "headline": "Software Engineer at Google",
  "skills": ["Java", "Python", "AWS"],
  "location": "San Francisco",
  "connectionCount": 450
}

// Job Index
{
  "index": "jobs",
  "jobId": "job456",
  "title": "Senior Backend Engineer",
  "company": "Meta",
  "location": "Remote",
  "skills": ["Java", "Microservices", "Kafka"],
  "salaryMax": 250000,
  "postedAt": "2026-01-15"
}
```

**Query:**
```
GET /users/_search
{
  "query": {
    "multi_match": {
      "query": "software engineer AWS",
      "fields": ["firstName^2", "headline^3", "skills^5"]
    }
  },
  "filter": {
    "geo_distance": {
      "location": "San Francisco",
      "distance": "50km"
    }
  },
  "sort": [
    {"_score": "desc"},
    {"connectionCount": "desc"}
  ]
}
```

**Indexing strategy:**
- Real-time indexing via Kafka
- When user updates profile → publish to Kafka → Elasticsearch consumer updates index
- Lag: < 5 seconds

**Autocomplete:**
- Edge N-Gram tokenizer
- \"soft\" → [\"s\", \"so\", \"sof\", \"soft\"]
- Fast prefix matching

**Ranking:**
- **TF-IDF** score (relevance)
- Boosted by:
  - Connection count (social proof)
  - Mutual connections (\"People You May Know\")
  - Recent activity"

---

## 🛠️ Step 9: Concurrency Handling

### 1. Connection Race Condition

**Problem:** Two users send connection requests to each other simultaneously.

**Solution:**
```java
public synchronized ConnectionRequest sendRequest(User sender, User receiver) {
    // Acquire locks in deterministic order (prevent deadlock)
    User first = sender.getUserId().compareTo(receiver.getUserId()) < 0 
                 ? sender : receiver;
    User second = (first == sender) ? receiver : sender;
    
    synchronized (first) {
        synchronized (second) {
            // Check if request exists in either direction
            if (hasPendingRequest(sender, receiver) || 
                hasPendingRequest(receiver, sender)) {
                // Auto-accept if bidirectional
                return autoAcceptBoth(sender, receiver);
            }
            
            // Create request
            return new ConnectionRequest(sender, receiver);
        }
    }
}
```

---

### 2. Like Idempotency

**Problem:** User double-clicks like button → duplicate likes

**Solution:**
```java
// Database: unique constraint on (userId, postId)
CREATE UNIQUE INDEX idx_likes ON likes(user_id, post_id);

public void likePost(Post post, User user) {
    try {
        Like like = new Like(user, post);
        likeRepository.save(like);  // Will fail if duplicate
        
        post.incrementLikeCount();
    } catch (DuplicateKeyException e) {
        // Already liked, ignore
        return;
    }
}
```

---

### 3. Feed Generation Concurrency

**Problem:** Multiple workers fan-out same post to overlapping followers

**Solution:**
```java
// Distributed lock (Redis)
public void fanOutPost(Post post) {
    String lockKey = "lock:fanout:" + post.getPostId();
    
    if (redisLock.tryLock(lockKey, 60_SECONDS)) {
        try {
            List<User> followers = getFollowers(post.getAuthor());
            
            // Partition followers across workers
            int numWorkers = 10;
            for (int i = 0; i < numWorkers; i++) {
                List<User> partition = partition(followers, i, numWorkers);
                
                executor.submit(() -> {
                    for (User follower : partition) {
                        addToFeed(follower, post);
                    }
                });
            }
        } finally {
            redisLock.unlock(lockKey);
        }
    }
}
```

---

## 🗄️ Step 10: Database Schema

```sql
-- Users Table
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    headline VARCHAR(200),
    location VARCHAR(100),
    profile_photo_url VARCHAR(500),
    about TEXT,
    is_premium BOOLEAN DEFAULT FALSE,
    is_open_to_work BOOLEAN DEFAULT FALSE,
    connection_count INT DEFAULT 0,
    follower_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_location (location),
    INDEX idx_last_active (last_active)
);

-- Work Experience Table
CREATE TABLE work_experience (
    experience_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    company VARCHAR(200) NOT NULL,
    title VARCHAR(200) NOT NULL,
    location VARCHAR(100),
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_company (company)
);

-- Skills Table (Global skill catalog)
CREATE TABLE skills (
    skill_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50),
    total_endorsements BIGINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category (category)
);

-- User Skills (Junction table)
CREATE TABLE user_skills (
    user_id VARCHAR(36) NOT NULL,
    skill_id VARCHAR(36) NOT NULL,
    endorsement_count INT DEFAULT 0,
    proficiency INT CHECK (proficiency BETWEEN 1 AND 5),
    years_of_experience INT,
    PRIMARY KEY (user_id, skill_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_skill (skill_id)
);

-- Connections Table
CREATE TABLE connections (
    connection_id VARCHAR(36) PRIMARY KEY,
    user_id_1 VARCHAR(36) NOT NULL,
    user_id_2 VARCHAR(36) NOT NULL,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id_1) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_2) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE INDEX idx_connection (user_id_1, user_id_2),
    INDEX idx_user1 (user_id_1),
    INDEX idx_user2 (user_id_2)
);

-- Connection Requests Table
CREATE TABLE connection_requests (
    request_id VARCHAR(36) PRIMARY KEY,
    sender_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_receiver_status (receiver_id, status),
    INDEX idx_sender (sender_id)
);

-- Posts Table
CREATE TABLE posts (
    post_id VARCHAR(36) PRIMARY KEY,
    author_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('TEXT', 'IMAGE', 'VIDEO', 'ARTICLE', 'POLL', 'JOB', 'SHARE') NOT NULL,
    visibility ENUM('PUBLIC', 'CONNECTIONS', 'PRIVATE') DEFAULT 'PUBLIC',
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_edited BOOLEAN DEFAULT FALSE,
    shared_post_id VARCHAR(36),  -- If this is a share
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (shared_post_id) REFERENCES posts(post_id) ON DELETE SET NULL,
    INDEX idx_author_created (author_id, created_at DESC),
    INDEX idx_created (created_at DESC)
);

-- Comments Table
CREATE TABLE comments (
    comment_id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    parent_comment_id VARCHAR(36),  -- For nested replies
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post_created (post_id, created_at DESC),
    INDEX idx_parent (parent_comment_id)
);

-- Likes Table
CREATE TABLE likes (
    like_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    target_type ENUM('POST', 'COMMENT') NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE INDEX idx_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
);

-- Companies Table
CREATE TABLE companies (
    company_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) UNIQUE NOT NULL,
    industry VARCHAR(100),
    size VARCHAR(50),
    headquarters VARCHAR(100),
    website VARCHAR(500),
    description TEXT,
    logo_url VARCHAR(500),
    follower_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_industry (industry)
);

-- Jobs Table
CREATE TABLE jobs (
    job_id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(100),
    location_type ENUM('ON_SITE', 'REMOTE', 'HYBRID') DEFAULT 'ON_SITE',
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP') NOT NULL,
    experience_level ENUM('ENTRY', 'MID', 'SENIOR', 'EXECUTIVE') NOT NULL,
    salary_min INT,
    salary_max INT,
    application_url VARCHAR(500),
    is_easy_apply BOOLEAN DEFAULT FALSE,
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    view_count INT DEFAULT 0,
    application_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    INDEX idx_company_active (company_id, is_active),
    INDEX idx_location_active (location, is_active),
    INDEX idx_posted (posted_at DESC)
);

-- Job Applications Table
CREATE TABLE job_applications (
    application_id VARCHAR(36) PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL,
    applicant_id VARCHAR(36) NOT NULL,
    resume_url VARCHAR(500),
    cover_letter TEXT,
    status ENUM('SAVED', 'APPLIED', 'INTERVIEWING', 'REJECTED', 'ACCEPTED') DEFAULT 'APPLIED',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE INDEX idx_applicant_job (applicant_id, job_id),
    INDEX idx_job_status (job_id, status),
    INDEX idx_applicant_applied (applicant_id, applied_at DESC)
);

-- Conversations Table
CREATE TABLE conversations (
    conversation_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100),  -- For group chats
    is_group BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    INDEX idx_last_message (last_message_at DESC)
);

-- Conversation Members Table
CREATE TABLE conversation_members (
    conversation_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP,
    unread_count INT DEFAULT 0,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_conversation (conversation_id)
);

-- Messages Table
CREATE TABLE messages (
    message_id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    media_url VARCHAR(500),
    status ENUM('SENT', 'DELIVERED', 'READ') DEFAULT 'SENT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_conversation_sent (conversation_id, sent_at DESC)
);

-- Notifications Table
CREATE TABLE notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    type ENUM('CONNECTION_REQUEST', 'CONNECTION_ACCEPTED', 'POST_COMMENT', 
              'POST_LIKE', 'JOB_MATCH', 'MESSAGE_RECEIVED', 'PROFILE_VIEW', 
              'ENDORSEMENT') NOT NULL,
    actor_id VARCHAR(36),  -- User who triggered notification
    target_type VARCHAR(50),
    target_id VARCHAR(36),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_recipient_created (recipient_id, created_at DESC),
    INDEX idx_recipient_unread (recipient_id, is_read)
);
```

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `UserService`: User management only
- `ConnectionService`: Connection logic only
- `PostService`: Post CRUD only
- `FeedService`: Feed generation only

### Open/Closed ✅
```java
// Add new post type without modifying PostService
public enum PostType {
    TEXT, IMAGE, VIDEO, ARTICLE, POLL, JOB, 
    LIVE_VIDEO  // New type added
}
```

### Liskov Substitution ✅
```java
// All IUserRepository implementations interchangeable
public interface IUserRepository {
    User findById(String id);
}

public class InMemoryUserRepo implements IUserRepository { }
public class MySQLUserRepo implements IUserRepository { }
```

### Interface Segregation ✅
```java
interface Likeable {
    void like(User user);
    int getLikeCount();
}

interface Commentable {
    void addComment(Comment comment);
    List<Comment> getComments();
}

class Post implements Likeable, Commentable { }
class Comment implements Likeable { }  // Not Commentable
```

### Dependency Inversion ✅
```java
public class FeedService {
    private IPostRepository postRepo;  // Depends on abstraction
    private IConnectionRepository connRepo;
    
    public FeedService(IPostRepository postRepo, IConnectionRepository connRepo) {
        this.postRepo = postRepo;
        this.connRepo = connRepo;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Microservices** - User, Post, Connection, Job, Messaging services
- ✅ **Fan-out on Write** - Pre-compute feeds for fast reads
- ✅ **Hybrid Feed** - Fan-out for regular users, pull for celebrities
- ✅ **Database Sharding** - Shard by user ID (16 shards)
- ✅ **Caching Strategy** - Redis for sessions, feeds, connections (80% hit rate)
- ✅ **Async Processing** - Kafka for notifications, feed updates

### Scalability
- ✅ **310M MAU** supported with 108 web servers + 16 DB shards
- ✅ **Elasticsearch** for search (users, jobs, posts)
- ✅ **CDN** for static assets (90% hit rate)
- ✅ **WebSocket** for real-time messaging

### Data Consistency
- ✅ **Strong consistency** for connections, applications, messages
- ✅ **Eventual consistency** for likes, feed, endorsements
- ✅ **Idempotent APIs** (duplicate like/application protection)

### Core Features
- ✅ User profiles with experience, education, skills
- ✅ Connection requests (bidirectional, 30K limit)
- ✅ News feed (ranked by engagement)
- ✅ Posts (text, image, video, polls, shares)
- ✅ Job postings & applications
- ✅ Messaging (1-on-1 & group)
- ✅ Notifications (8 types)
- ✅ Search (users, jobs, companies)

---

**Total: 136 DSA + 9 LLD Problems**

All changes ready for review!
