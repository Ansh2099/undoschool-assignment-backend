# 🧠 Undoschool Assignment – Backend

A Spring Boot backend that indexes and searches course data in Elasticsearch.  
Built for speed, clarity, and real-world search use cases.

---

## 🚀 Features

- ✅ Bulk indexing of 50 sample courses into Elasticsearch at startup
- 🔍 Full-text & fuzzy search using Elasticsearch DSL
- 🎯 Filters: age, category, type, price, start date
- 📈 Sorting (by date or price), with pagination
- ✨ Autocomplete title suggestions using completion suggester
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

**Verify it's running:**

```bash
curl http://localhost:9200
```

You should see a JSON response with cluster info like:
```json
{
  "name" : "elasticsearch",
  "cluster_name" : "docker-cluster",
  "version" : { ... },
  "tagline" : "You Know, for Search"
}
```

### 3. Build & Run the App

```bash
./mvnw clean package
java -jar target/undoschool-assignment-backend-0.0.1-SNAPSHOT.jar
```

✅ **On startup, the app will:**
- Load `sample-courses.json` (50 courses)
- Create the "courses" index with proper mappings
- Bulk index all courses with suggest fields for autocomplete
- Log success messages for data loading

---

## 📡 REST API Endpoints

### 🔍 Search Courses

```
GET /api/search
```

**Query Parameters:**

| Param | Type | Description | Example |
|-------|------|-------------|---------|
| `q` | String | Keyword search (title/description) | `"math"` |
| `minAge` | Integer | Minimum age filter | `6` |
| `maxAge` | Integer | Maximum age filter | `10` |
| `category` | String | Course category | `"Math"` |
| `type` | String | ONE_TIME, COURSE, CLUB | `"COURSE"` |
| `minPrice` | Double | Minimum price | `10.0` |
| `maxPrice` | Double | Maximum price | `60.0` |
| `startDate` | ISO 8601 | Courses on/after this date | `"2025-06-01T00:00:00Z"` |
| `sort` | String | upcoming (default), priceAsc, priceDesc | `"priceAsc"` |
| `page` | Integer | Page number (default: 0) | `0` |
| `size` | Integer | Page size (default: 10) | `10` |

**Example Requests:**

```bash
# Basic search
curl "http://localhost:8080/api/search?q=math"

# Filtered search
curl "http://localhost:8080/api/search?q=math&minAge=6&maxAge=10&category=Math&type=COURSE&minPrice=10&maxPrice=60&startDate=2025-06-01T00:00:00Z&sort=priceAsc&page=0&size=5"

# Price-based sorting
curl "http://localhost:8080/api/search?sort=priceDesc&size=3"

# Date-based filtering
curl "http://localhost:8080/api/search?startDate=2025-07-01T00:00:00Z&sort=upcoming"
```

**Response Format:**
```json
{
  "content": [
    {
      "id": "1",
      "title": "Math Explorers",
      "description": "A fun introduction to numbers and problem solving.",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": "1st–3rd",
      "minAge": 6,
      "maxAge": 8,
      "price": 49.99,
      "nextSessionDate": "2025-06-10T15:00:00Z"
    }
  ],
  "totalElements": 15,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
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

**Fuzzy Search Examples:**

```bash
# Typo tolerance - "dinors" matches "Dinosaurs 101"
curl "http://localhost:8080/api/search?q=dinors"

# Partial word matching
curl "http://localhost:8080/api/search?q=prog"
```

---

## 🧪 Running Tests

### ✅ Integration Tests

```bash
./mvnw test
```

**Expected output:**
```
Tests run: 2, Failures: 0, Errors: 0
```

**Test Coverage:**
- `CourseSearchIntegrationTest.java` - Tests search and autocomplete endpoints
- `UndoschoolAssignmentBackendApplicationTests.java` - Context loading test

---

## 🧠 Design Choices

### **Elasticsearch Client:**
- Uses official `elasticsearch-java` client (v8.11.0)
- Direct control over query DSLs and bulk operations
- Better performance and flexibility than Spring Data Elasticsearch

### **Autocomplete Implementation:**
- Primary: Elasticsearch completion suggester with `@CompletionField`
- Fallback: Prefix matching for reliability
- Automatic duplicate removal and size limiting

### **Data Loading:**
- Startup listener loads data on application ready
- Automatic suggest field population
- Proper error handling and logging

### **Search Features:**
- **Full-text**: Multi-match on title (boosted) and description
- **Fuzzy**: Automatic fuzziness for typo tolerance
- **Filters**: Range queries for age, price, date; term queries for category/type
- **Sorting**: Price ascending/descending, default date sorting
- **Pagination**: Standard page/size implementation

---

## 📁 Project Structure

```
undoschool-assignment-backend/
├── docker-compose.yml                    # Elasticsearch setup
├── pom.xml                              # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/com/example/undoschool_assignment_backend/
│   │   │   ├── config/
│   │   │   │   ├── DataLoader.java           # Startup data loading
│   │   │   │   └── ElasticsearchConfig.java  # ES client configuration
│   │   │   ├── controller/
│   │   │   │   └── CourseController.java     # REST endpoints
│   │   │   ├── document/
│   │   │   │   └── CourseDocument.java       # ES document model
│   │   │   ├── service/
│   │   │   │   └── CourseService.java        # Search & indexing logic
│   │   │   └── UndoschoolAssignmentBackendApplication.java
│   │   └── resources/
│   │       ├── application.properties        # App configuration
│   │       └── sample-courses.json          # 50 sample courses
│   └── test/
│       └── java/com/example/undoschool_assignment_backend/
│           ├── CourseSearchIntegrationTest.java
│           └── UndoschoolAssignmentBackendApplicationTests.java
```

---

## 🔧 Implementation Details

### **Index Configuration:**
- Index name: "courses" (as required)
- Proper field mappings: text, keyword, integer, double, date, completion
- Suggest field for autocomplete functionality

### **Search Query Features:**
- **Multi-match query**: Searches title (boosted) and description
- **Fuzzy matching**: Automatic fuzziness for typo tolerance
- **Range filters**: Age, price, and date range filtering
- **Term filters**: Exact category and type matching
- **Sorting**: Support for price ascending/descending and default date sorting
- **Pagination**: Standard page/size pagination

### **Autocomplete Features:**
- **Completion suggester**: Uses proper Elasticsearch completion suggester
- **Fallback mechanism**: Falls back to prefix matching if completion fails
- **Duplicate removal**: Automatically removes duplicate suggestions
- **Size limit**: Limited to 10 suggestions

### **Data Loading:**
- **Startup loading**: `DataLoader` component loads data on application startup
- **JSON parsing**: Uses Jackson ObjectMapper for JSON deserialization
- **Suggest field population**: Automatically populates suggest fields for autocomplete

---

Tests cover:
- Search with query and filters
- Age range filtering
- Category filtering
- Empty results handling
- Pagination
- Autocomplete suggestions
- No-match scenarios

---

## ⏱️ Time to Run from Scratch

With Java, Docker, and Maven installed:

1. **Clone & setup**: 2 minutes
2. **Start Elasticsearch**: 1 minute
3. **Build & run app**: 2 minutes
4. **Test endpoints**: 5 minutes

✅ **Total time: under 10 minutes**

---

## 🚨 Verification Checklist

### **Setup Verification:**
- [x] Docker Compose starts Elasticsearch successfully
- [x] Application connects to Elasticsearch on localhost:9200
- [x] 50 courses indexed at startup
- [x] All endpoints respond correctly

### **Search Functionality:**
- [x] Full-text search on title and description
- [x] Fuzzy matching for typos
- [x] Age range filtering (minAge, maxAge)
- [x] Category and type exact filtering
- [x] Price range filtering
- [x] Date filtering (startDate)
- [x] Sorting (upcoming, priceAsc, priceDesc)
- [x] Pagination (page, size)

### **Autocomplete Functionality:**
- [x] Completion suggester implementation
- [x] Prefix matching fallback
- [x] Proper suggest field mapping
- [x] Duplicate removal
- [x] Size limiting

### **Testing:**
- [x] Integration tests pass
- [x] Application context loads successfully
- [x] Search and suggest endpoints tested

---

## 📊 Sample Data Overview

The `sample-courses.json` contains 50 courses with:
- **Categories**: Math, Science, Art, Technology, Language, Sports, etc.
- **Types**: ONE_TIME, COURSE, CLUB
- **Age ranges**: 6-12 years
- **Price range**: $14.99 - $64.99
- **Session dates**: June 2025 - September 2025
- **Grade ranges**: 1st-6th grade

All courses include proper suggest fields for autocomplete functionality.

---

## 🎯 Assignment Requirements Status

### **Assignment A (Required) - ✅ COMPLETE**
- [x] Elasticsearch setup with Docker Compose
- [x] 50+ sample courses with all required fields
- [x] Spring Boot application with proper dependencies
- [x] Elasticsearch configuration for localhost:9200
- [x] CourseDocument entity with proper mappings
- [x] Bulk indexing at startup
- [x] Search service with filters, sorting, pagination
- [x] REST endpoint with all required parameters
- [x] Integration tests
- [x] Comprehensive README

### **Assignment B (Bonus) - ✅ COMPLETE**
- [x] Autocomplete suggestions using completion suggester
- [x] Fuzzy matching in search queries
- [x] `/api/search/suggest` endpoint
- [x] Proper suggest field mapping
- [x] Documentation with examples

**All requirements satisfied!** 🎉