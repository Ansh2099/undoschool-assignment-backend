# 🧠 Undoschool Assignment – Backend

A Spring Boot backend that indexes and searches course data in Elasticsearch.  
Built for speed, clarity, and real-world search use cases.

---

## 🚀 Features

- ✅ Bulk indexing of 50 sample courses into Elasticsearch at startup
- 🔍 Full-text & fuzzy search using Elasticsearch DSL
- 🎯 Filters: age, category, type, price, start date
- 📈 Sorting (by date or price), with pagination
- ✨ Autocomplete title suggestions using `@CompletionField`
- 🧪 Integration tests for search + suggestions using `MockMvc`

---

## ⚙️ Requirements

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

---

## 🛠️ Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/undoschool-assignment-backend.git
cd undoschool-assignment-backend/undoschool-assignment-backend
```

### 2. Start Elasticsearch with Docker

```bash
docker-compose up -d
```

Verify it's up:

```bash
curl http://localhost:9200
```

You should see a JSON response with cluster info.

### 3. Build & Run the App

```bash
mvn clean package
java -jar target/undoschool-assignment-backend-0.0.1-SNAPSHOT.jar
```

✅ On startup, the app will:
- Load sample-courses.json
- Bulk index the data into Elasticsearch
- Populate autocomplete suggest fields for title

---

## 📡 REST API Endpoints

### 🔍 Search Courses

```
GET /api/search
```

**Query Parameters:**

| Param | Description |
|-------|-------------|
| `q` | Keyword (title/description) |
| `minAge` | Minimum age filter |
| `maxAge` | Maximum age filter |
| `category` | Course category (e.g., "Math") |
| `type` | ONE_TIME, COURSE, CLUB |
| `minPrice` | Minimum price |
| `maxPrice` | Maximum price |
| `startDate` | ISO 8601 date (e.g. 2025-06-01T00:00:00Z) |
| `sort` | upcoming (default), priceAsc, priceDesc |
| `page` | Page number (default: 0) |
| `size` | Page size (default: 10) |

**Example:**

```bash
curl "http://localhost:8080/api/search?q=math&minAge=6&maxAge=10&category=Math&type=COURSE&minPrice=10&maxPrice=60&startDate=2025-06-01T00:00:00Z&sort=priceAsc&page=0&size=5"
```

### ✨ Autocomplete Suggestions

```
GET /api/search/suggest?q={partialTitle}
```

**Example:**

```bash
curl "http://localhost:8080/api/search/suggest?q=math"
```

**Response:**

```json
["Math Explorers", "Math Games", "Math Puzzles"]
```

---

## 🧪 Running Tests

There are two test classes:

### ✅ 1. CourseSearchIntegrationTest.java
**Path:**
```
src/test/java/com/example/undoschool_assignment_backend/CourseSearchIntegrationTest.java
```

**Covers:**
- `/api/search` returns matching results
- `/api/search/suggest` returns autocomplete titles

### ✅ 2. UndoschoolAssignmentBackendApplicationTests.java
**Path:**
```
src/test/java/com/example/undoschool_assignment_backend/UndoschoolAssignmentBackendApplicationTests.java
```

**Note:**
This is the default Spring Boot context-load test, and does not contain any business logic assertions. It simply verifies the application context boots without errors. Left intact for completeness.

### ▶️ Run All Tests

```bash
mvn test
```

**Expected output:**
```
Tests run: 3, Failures: 0, Errors: 0
```

---

## 🧠 Design Choices

### No Repository Layer:
This project uses the official `elasticsearch-java` client instead of Spring Data Elasticsearch repositories.
This gives more control over query DSLs, scoring, fuzziness, and bulk operations.
✅ More flexible, ✅ More production-like.

### Autocompletion with @CompletionField:
Suggestions are powered by a dedicated field populated from course titles — not fuzzy hacks.
This aligns with how real apps like LinkedIn or Udemy implement autosuggest.

### ZonedDateTime Handling:
Course sessions use ZonedDateTime for proper timezone-aware filtering (startDate param).
Mapped using ISO 8601 (uuuu-MM-dd'T'HH:mm:ssXXX) pattern.

### Modular Package Structure:
- `config/` → Elasticsearch and data loading
- `controller/` → Search endpoints
- `document/` → Elasticsearch model
- `service/` → Search logic and indexing

---

## 📁 Project Structure

```
undoschool-assignment-backend/
├── docker-compose.yml
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/undoschool_assignment_backend/
│   │   │   ├── config/
│   │   │   │   ├── DataLoader.java
│   │   │   │   └── ElasticsearchConfig.java
│   │   │   ├── controller/
│   │   │   │   └── CourseController.java
│   │   │   ├── document/
│   │   │   │   └── CourseDocument.java
│   │   │   ├── service/
│   │   │   │   └── CourseService.java
│   │   │   └── UndoschoolAssignmentBackendApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── sample-courses.json
│   └── test/
│       └── java/com/example/undoschool_assignment_backend/
│           ├── CourseSearchIntegrationTest.java
│           └── UndoschoolAssignmentBackendApplicationTests.java
```

---

## ⏱️ Time to Run from Scratch

With Java, Docker, and Maven installed:

1. Clone the repo
2. Start Elasticsearch
3. Run the app
4. Test the endpoints

✅ **Expected time: under 30 minutes**

---

## 🙋 Contact

Maintained by **Anshpreet Singh**  
For doubts or improvements, feel free to raise a PR or connect.