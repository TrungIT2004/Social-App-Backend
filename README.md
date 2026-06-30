# Social Media Backend API

A robust RESTful API for a social media platform built with Spring Boot, featuring real-time communication, friend recommendations, media handling, and advanced database optimizations.

---

## 🚀 Features

- **User Authentication & Authorization** – JWT-based login, device tracking, and session management.
- **Post Management** – Create, read, delete posts with text and media (images/videos).
- **Comment System** – Nested replies (threaded comments) with support for media.
- **Reactions** – Like/react to posts, comments, and shares.
- **Real-time Updates** – WebSocket (STOMP) for instant notifications, new comments, and reaction updates.
- **Friend System** – Send/accept/reject friend requests, list friends, and view mutual friends.
- **Friend Recommendations** – Adamic/Adar algorithm and Jaccard similarity for personalized suggestions.
- **Media Handling** – Automatic image compression and conversion to WebP (using Thumbnailator) to reduce storage size by 25–35%.
- **Email Verification** – JavaMailSender with custom HTML templates for account activation.
- **Structured Logging** – MDC with unique `traceId` for distributed request tracing.
- **Client Metadata Extraction** – Parse IP, geolocation, browser, device OS from `User-Agent` (Yauaa).
- **Global Exception Handling** – Consistent error responses with specific `DataIntegrityViolationException` handling.

---

## 🛠️ Tech Stack

| Layer                | Technologies |
|----------------------|--------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security, JWT |
| **ORM** | Spring Data JPA, Hibernate |
| **Database** | PostgreSQL |
| **Real-time** | WebSocket (STOMP), SockJS |
| **Email** | JavaMailSender |
| **File Processing** | Thumbnailator (WebP compression) |
| **Logging** | SLF4J, Logback, MDC |
| **Build Tool** | Maven |
| **Other** | Lombok, User-Agent parsing (Yauaa) |

---

## 📦 Setup & Configuration

### Prerequisites

- Java 21
- PostgreSQL 14+
- Maven 3.8+

### Database Setup

1. Create a database named `social_app`:
   ```sql
   CREATE DATABASE social_app;
   ```

2. Update `application.properties`:
   ```properties
   # Database
   spring.datasource.url=jdbc:postgresql://localhost:5432/social_app
   spring.datasource.username=your_username
   spring.datasource.password=your_password

   # JPA
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

   # JWT Secret
   jwt.secret.access=your_jwt_secret_key

   # Email (Gmail example)
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.default-encoding=UTF-8

   # File Upload
   file.upload-dir=./uploads
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB

   # Logging
   logging.level.com.minhtrung.social_app=DEBUG
   logging.level.org.springframework.security=INFO
   ```

### Run the Application

```bash
mvn clean install
mvn spring-boot:run
```
The server will start at `http://localhost:8080`.

---

## 📂 Project Structure

```text
social-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/minhtrung/social_app/
│       │       ├── config/          # Security, WebSocket, JWT configs
│       │       ├── controllers/     # REST API endpoints
│       │       ├── dtos/            # Data Transfer Objects
│       │       ├── enums/           # Enums (Visibility, Reaction, FriendshipStatus)
│       │       ├── exceptions/      # Custom exceptions & GlobalExceptionHandler
│       │       ├── filters/         # JWT filter
│       │       ├── models/          # JPA Entities
│       │       ├── repositories/    # Spring Data JPA repositories
│       │       ├── seeder/          # Data seeder (Faker)
│       │       └── services/        # Business logic
│       └── resources/
│           ├── application.properties
│           └── static/              # Static assets
├── uploads/                         # Uploaded files (ignored in Git)
├── logs/                            # Log files (ignored in Git)
├── pom.xml
└── README.md
```

---

## 📌 API Endpoints Overview

> **Note:** All endpoints except `/auth/**` require a JWT token in the `Authorization: Bearer <token>` header.

### 1. Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user (sends verification email) |
| POST | `/auth/verify` | Verify email with code |
| POST | `/auth/login` | Login → returns JWT + user info |
| POST | `/auth/logout` | Logout (invalidate session) |
| POST | `/auth/refresh-token` | Refresh JWT (if implemented) |

#### Example: Register Request
```json
POST /auth/register
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "gender": "Male",
  "birthDate": "2000-01-01"
}
```

#### Example: Login Request
```json
POST /auth/login
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

#### Example: Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "john_doe",
  "email": "john@example.com",
  "profilePicUrl": "https://..."
}
```

### 2. Posts

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/posts` | Paginated feed of posts (with user info) |
| POST | `/api/v1/posts` | Create a new post (multipart/form-data) |
| DELETE | `/api/v1/posts/{postId}` | Delete a post (only by owner) |

#### Example: Create Post (`multipart/form-data`)
```text
POST /api/v1/posts
Content-Type: multipart/form-data

post: {"text": "Hello world!", "visibility": "PUBLIC"}
files: image1.jpg, image2.png
```

#### Example: Create Post Response
```json
{
  "postId": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "123e4567-e89b-12d3-a456-426614174001",
  "text": "Hello world!",
  "mediaUrls": ["uploads/posts/uuid.webp"],
  "createdAt": "2026-06-30T10:00:00",
  "visibility": "PUBLIC",
  "reactionCount": 0,
  "commentCount": 0,
  "shareCount": 0
}
```

#### Example: Get Posts (paginated)
```text
GET /api/v1/posts?page=0&size=10
```

#### Example: Get Posts Response
```json
[
  {
    "postId": "...",
    "userId": "...",
    "fullname": "John Doe",
    "profilePicUrl": "https://...",
    "text": "Hello world!",
    "mediaUrls": ["uploads/posts/uuid.webp"],
    "createdAt": "2026-06-30T10:00:00",
    "visibility": "PUBLIC",
    "reactionCount": 5,
    "commentCount": 3,
    "shareCount": 1
  }
]
```

### 3. Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/comments` | Get root comments for a post or share (query params: `postId` or `shareId`) |
| POST | `/api/v1/comments` | Create a comment or reply (send `parentCommentId` for replies) |
| DELETE | `/api/v1/comments/{commentId}` | Delete a comment (owner only) |

#### Example: Create Comment (root)
```json
POST /api/v1/comments
{
  "postId": "123e4567-e89b-12d3-a456-426614174000",
  "textContent": "Nice post!",
  "mediaFile": null
}
```

#### Example: Create Reply
```json
POST /api/v1/comments
{
  "postId": "123e4567-e89b-12d3-a456-426614174000",
  "parentCommentId": "123e4567-e89b-12d3-a456-426614174001",
  "textContent": "I agree!",
  "mediaFile": null
}
```

#### Example: Get Comments
```text
GET /api/v1/comments?postId=123e4567-e89b-12d3-a456-426614174000&page=0&size=10
```

### 4. Reactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/posts/{postId}/react` | React to a post |
| DELETE | `/api/v1/posts/{postId}/react` | Remove reaction |
| POST | `/api/v1/comments/{commentId}/react` | React to a comment |
| DELETE | `/api/v1/comments/{commentId}/react` | Remove comment reaction |

#### Example: React to Post
```json
POST /api/v1/posts/123e4567-e89b-12d3-a456-426614174000/react
{
  "reaction": "LIKE"
}
```
*Reaction types:* `LIKE`, `LOVE`, `HAHA`, `WOW`, `SAD`, `ANGRY`

### 5. Friendships

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/friends/requests` | Send a friend request |
| PUT | `/api/v1/friends/requests/{requestId}/accept` | Accept a pending request |
| PUT | `/api/v1/friends/requests/{requestId}/decline` | Decline |
| GET | `/api/v1/friends/requests/incoming/{userId}` | Get incoming requests |
| GET | `/api/v1/friends/requests/outgoing/{userId}` | Get outgoing requests |
| GET | `/api/v1/friends/{userId}` | List all friends (with mutual count) |
| DELETE | `/api/v1/friends/{friendId}` | Unfriend |
| POST | `/api/v1/friends/block` | Block a user |

#### Example: Send Friend Request
```json
POST /api/v1/friends/requests
{
  "addresseeId": "123e4567-e89b-12d3-a456-426614174002"
}
```

#### Example: List Friends Response
```json
[
  {
    "friendshipId": "...",
    "userId": "...",
    "fullname": "Jane Smith",
    "profilePicUrl": "https://...",
    "mutualFriendsCount": 5
  }
]
```

### 6. Friend Recommendations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/friends/recommendations/adamic-adar/{userId}?page=0&size=10` | Top suggestions using Adamic/Adar |
| GET | `/api/v1/friends/recommendations/jaccard/{userId}?page=0&size=10` | Top suggestions using Jaccard |

#### Example: Get Recommendations (Adamic/Adar)
```text
GET /api/v1/friends/recommendations/adamic-adar/123e4567-e89b-12d3-a456-426614174000?page=0&size=10
```

#### Example: Response
```json
[
  {
    "userId": "...",
    "fullname": "Alice Johnson",
    "profilePicUrl": "https://...",
    "mutualFriendsCount": 8,
    "algorithmScore": 2.45
  }
]
```

### 7. Shares

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/shares` | Share a post (with optional text) |
| DELETE | `/api/v1/shares/{shareId}` | Unshare a post |
| GET | `/api/v1/shares/{shareId}` | Get share details with owner info |

#### Example: Share a Post
```json
POST /api/v1/shares
{
  "postId": "123e4567-e89b-12d3-a456-426614174000",
  "textContent": "Check this out!"
}
```

#### Example: Response
```json
{
  "shareId": "...",
  "userId": "...",
  "shareOwnerFullname": "John Doe",
  "shareOwnerProfilePic": "https://...",
  "postOwnerFullname": "Jane Smith",
  "postOwnerProfilePic": "https://...",
  "postText": "Hello world!",
  "postMediaUrls": ["uploads/posts/uuid.webp"],
  "postCreatedAt": "2026-06-30T10:00:00",
  "shareText": "Check this out!",
  "shareCreatedAt": "2026-06-30T11:00:00"
}
```

### 8. Hashtags

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/posts/hashtags/{tag}?page=0&size=10` | Get posts by hashtag |

### 9. Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/{userId}` | Get user profile (with followers/following counts) |
| PUT | `/api/v1/users/{userId}` | Update user profile |
| POST | `/api/v1/users/{userId}/avatar` | Upload/update profile picture |

---

## 🔌 WebSocket Real‑time

### Connection
Connect to `ws://localhost:8080/ws` using STOMP over SockJS.

### JavaScript Example
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    // Subscribe to user's feed
    stompClient.subscribe('/topic/user/123e4567-e89b-12d3-a456-426614174000/feed', function(message) {
        const notification = JSON.parse(message.body);
        console.log('Received:', notification);
        
        // Handle notification based on type
        if (notification.type === 'create-comment') {
            // Add comment to UI
        } else if (notification.type === 'reaction-update') {
            // Update reaction count
        }
    });
});
```

### Message Format
```json
{
  "type": "create-comment",
  "data": {
    // depends on type – usually the full entity DTO
  }
}
```

---

## 🧠 Friend Recommendation Algorithms

Two algorithms are implemented and exposed via the `/recommendations` endpoints:

### Adamic/Adar
- **Formula:** $AA(u, v) = \sum_{w \in N(u) \cap N(v)} rac{1}{\log(	ext{deg}(w))}$
- **Explanation:** Weighs common friends by their popularity (less popular friends have higher weight).
- **Best for:** Sparse graphs where mutual connections are meaningful.

### Jaccard Similarity
- **Formula:** $J(u, v) = rac{|N(u) \cap N(v)|}{|N(u) \cup N(v)|}$
- **Explanation:** Ratio of common friends to total friends of both users.
- **Best for:** When you want to normalize by total friend count.

---

## 📂 File Upload & Image Compression

### Supported Formats
- **Images:** JPEG, PNG, GIF, BMP → automatically converted to WebP
- **Videos:** MP4, MOV, AVI, WEBM (kept as-is)
- **Other:** PDF, DOC, etc. (kept as-is)

### Compression Settings

| Folder | Max Size (pixels) | Quality | Output Format |
|--------|-------------------|---------|---------------|
| `avatars` | 500×500 | 80% | WebP |
| `posts` | 1200×1200 | 80% | WebP |
| `comments` | 800×800 | 80% | WebP |

- **Storage Path:** `./uploads/{subfolder}/{uuid}.webp`
- **Access via Browser:** `http://localhost:8080/uploads/avatars/uuid.webp`

---

## 🧪 Testing

### Using Postman
1. Import the Postman collection (if available).
2. Set the environment variable `{{json_web_token}}` after login.
3. Test endpoints with the examples above.

### Using cURL
```bash
# Register
curl -X POST http://localhost:8080/auth/register   -H "Content-Type: application/json"   -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"SecurePass123"}'

# Login
curl -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"email":"john@example.com","password":"SecurePass123"}'

# Create Post (with file)
curl -X POST http://localhost:8080/api/v1/posts   -H "Authorization: Bearer <token>"   -F "post={"text":"Hello"}"   -F "files=@image.jpg"
```

---

## 🗑️ Data Seeding

The application includes a data seeder (`FriendshipDataSeeder`) that generates:
- 500 users (preserving your existing 2 users)
- ~8000 friendships with:
  - ~5000 `ACCEPTED` (including cross-clique edges)
  - ~1500 `PENDING`
  - ~1500 `REJECTED/UNFRIENDED`

### How to Re-seed
Clear existing data (optional):
```sql
DELETE FROM friendships;
DELETE FROM users WHERE user_id NOT IN ('user1_id', 'user2_id');
```
Restart the application – the seeder will run automatically.

---

## 🔐 Security

### JWT Authentication
- **Access Token:** Short-lived (15–30 min), used for API access.
- **Refresh Token:** Longer-lived (7 days), used to get new access tokens.
- **Device Tracking:** Each login generates a unique device ID.

### Password Encryption
Passwords are stored using `BCryptPasswordEncoder` (Spring Security).

### CORS
Configured to allow requests from `http://localhost:3000` (React dev server).

---

## 🐛 Error Handling

### Global Exception Handler
- `DataIntegrityViolationException`: Handles database constraint violations (foreign key, unique).
- `MethodArgumentNotValidException`: Handles validation errors (e.g., missing fields).
- `Exception`: Fallback for unexpected errors (returns 500).

### Error Response Format
```json
{
  "errorCode": "FRIENDSHIP_ALREADY_EXISTS",
  "msg": "Friendship already exists"
}
```

---

## 📝 Logging

MDC (Mapped Diagnostic Context) adds a unique `traceId` to every log entry.
- **TRACE:** Hibernate SQL with bind values
- **DEBUG:** Application flow
- **INFO:** Important events (post creation, login, etc.)
- **WARN:** Client errors (404, 409)
- **ERROR:** Server exceptions

**Log File:** `logs/application.log`

---

## 🚀 Deployment

### Building the JAR
```bash
mvn clean package -DskipTests
```

### Running on Server
```bash
java -jar target/social-app-0.0.1-SNAPSHOT.jar   --spring.profiles.active=prod   --file.upload-dir=/var/data/uploads   --spring.datasource.url=jdbc:postgresql://production-db:5432/social_app
```

### Environment Variables (Recommended)
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/social_app
export SPRING_DATASOURCE_USERNAME=db_user
export SPRING_DATASOURCE_PASSWORD=db_password
export JWT_SECRET_ACCESS=your_secret_key
export FILE_UPLOAD_DIR=/var/data/uploads
```

---

## 📄 License

This project is for educational purposes only. All rights reserved.

---

## 👨‍💻 Author

Hoàng Lê Minh Trung  
GitHub: @your-username  
Email: htmtrung2004@gmail.com  
LinkedIn: your-linkedin  

### 🙏 Acknowledgments
- Spring Boot community
- PostgreSQL
- Thumbnailator (image compression)
- Java Faker (data seeding)
- Yauaa (User-Agent parsing)

Built with ❤️ and ☕ by Minh Trung.
