# GitHub Repo Reader API

![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-H2-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

A professional, backend-only Spring Boot application that automatically synchronizes and serves content from the [JavaInREADME](https://github.com/abusaeed2433/JavaInREADME) GitHub repository. It provides structured REST API endpoints to fetch repository contributors, learning topics indices, and Markdown blog content.

---

## 🚀 Features

- **Automated Synchronization**: Reflects repository changes within a day using a scheduled background processor.
- **Persistent Storage**: Utilizes a persistent H2 database (`./data/github_repo_db`) to store synced data and avoid excessive GitHub API calls.
- **Smart Change Detection**: Leverages GitHub API's Git SHAs to optimize caching—content is only downloaded when it has actually changed.
- **Rate Limit Handling**: Robust retry mechanism with exponential backoff to handle GitHub's unauthenticated API rate limits seamlessly.
- **Standardized API Responses**: Consistent JSON response wrappers for success and error handling.

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Framework** | Spring Boot 3.5.3 |
| **Language** | Java 25 |
| **Database** | H2 Database (File-based, Persistent) |
| **ORM** | Spring Data JPA / Hibernate |
| **Build Tool** | Gradle |
| **Testing Core**| JUnit 5 |

## 📐 Architecture Overview

The system runs a **Daemon Thread on Startup** and a **12-Hour Cron Job** to fetch data. The data is parsed, optimized by checking file SHAs to reduce API bloat, converted to entities, and persisted using robust standard JPA repositories.
The `ReaderController` serves this pre-fetched, organized data rapidly without waiting on external networks.

## 📡 API Endpoints

All endpoints are completely decoupled from UI and expose exactly the specified structure. Responses wrap data as:
```json
{ 
  "data": { ... }, 
  "message": "Read successful", 
  "success": true 
}
```

### 1. Read Contributions
Returns a list of tracked repositories along with their contributors.

**Endpoint:** `GET /api/v1/read_contributions`

### 2. Read Indices
Returns the main topics ordered logically, along with their respective sub-topic lists.

**Endpoint:** `GET /api/v1/read_indices`

### 3. Read Blog
Returns the actual Markdown content from the synced README for a specific topic/sub-topic.

**Endpoint:** `GET /api/v1/read_blog`

**Query Parameters:**
- `topic_name` (e.g., "Data type")
- `sub_topic_name` (e.g., "Data type")

---

## ⚙️ Getting Started

### Prerequisites
- **Java 25** installed and configured in your environment.
- **Gradle** (or use the provided Gradle Wrapper).

### 1. Clone & Build
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd "Project_25 on Github Repo reader"
./gradlew clean build -x test
```

### 2. Run the Application
```bash
./gradlew bootRun
```
The application will start on `http://localhost:8080`. Note that upon the very first run, it begins an initial GitHub data sync in the background. Content will be available via the API once the sync processes those topics.

### 3. Postman Collection
For easy testing, a `postman_collection.json` file is included in the project root. Simply import this file into Postman to instantly access all configured endpoints.

### 4. Database Access (H2 Console)
The embedded H2 database console is enabled for easy debugging:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:file:./data/github_repo_db`
- **User Name**: `sa`
- **Password**: *(leave blank)*

## ⚠️ Notes on GitHub API Limits
This application uses the public, unauthenticated GitHub REST API which has a rate limit of **60 requests per hour**. By combining Git SHA checks and local persistent data, the footprint is kept minimal. If the application hits the limit during initialization, you will see it automatically pause and patiently await the rate reset.
