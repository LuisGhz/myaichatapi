# MyAIChat 🤖💬

An intelligent, multi‑modal AI chat API for real conversations, content understanding and productivity.

---

## Description

MyAIChat is a modern, production‑ready REST API built with **Spring Boot 3** and **Spring AI** that unifies several cutting‑edge AI capabilities (chat, image understanding, audio transcription, web search enrichment and reusable prompt templates) behind a clean, secure interface. 

It targets real use cases: from prototyping ideas and summarising documents to creating content workflows or transcribing spoken audio. The platform abstracts provider differences (OpenAI + Google Vertex AI Gemini) through a strategy layer, adds governance (token limits, usage visibility), and offers extensibility for new models or tools.

Non‑technical view: Think of it as an AI “backend brain” you can plug any UI into. Technical view: A layered Spring application applying common design patterns (Strategy, DTO, Repository, Service) with strong validation, test coverage and cloud‑ready integration (S3, OAuth2, JWT, PostgreSQL).

---

## Features

### Core AI & Conversation
* Multi‑provider chat (OpenAI + Vertex AI Gemini) with fallback strategy
* Streaming assistant responses (reactive `Flux` endpoints)
* Persistent chat threads with auto‑generated titles & favourites
* Per‑conversation max output token limits & web‑search toggle
* Tool / function calling to integrate Google Custom Search results

### Multimodality
* Image understanding (JPEG / PNG / GIF) with contextual replies
* Audio transcription (MP3 / WAV / M4A) via dedicated endpoint
* Attach optional file when sending the first user message

### Prompt Engineering Workspace
* Custom prompt templates (CRUD)
* Structured prompt message lists (system / user / assistant roles)
* Reusable across conversations

### Web Search Augmentation
* Google Programmable Search integration for real‑time context
* Fact enrichment to reduce hallucinations

### Security & Identity
* OAuth2 (GitHub) login flow + JWT based session tokens
* User‑scoped resources & validation annotations (`@UserJwtData`)

### Cloud & Storage
* AWS S3 media storage + optional CDN domain
* File upload abstraction with UUID naming

### Data & Persistence
* PostgreSQL (JPA/Hibernate) with pagination for message history
* Transactional write patterns & entity separation

### Architecture & Quality
* Clean layering: Controller → Service → Repository / External client
* Strategy pattern for model selection & future extensibility
* Centralised error handling (global advice) & structured logging (Log4j2)
* Test suite (JUnit 5 + Mockito + Spring Boot Test) covering services, controllers, utilities

### Observability & Governance
* Token usage boundaries per conversation
* Actuator endpoints (health/metrics) via `spring-boot-starter-actuator`

### Developer Experience
* Hot reload (DevTools)
* `.env` support (`spring-dotenv`) for local development
* Dockerfile for containerised deployment

---

## Installation Instructions

### 1. Prerequisites
* Java 17 (e.g. Temurin 17)
* Maven (wrapper included: `./mvnw` / `mvnw.cmd`)
* PostgreSQL 14+ running locally (or a connection string to a hosted instance)
* An AWS S3 bucket (or mock/minio if adapting)
* OpenAI API key (if using OpenAI models)
* Google Cloud project with Vertex AI Gemini enabled + location (e.g. `us-central1`)
* Google Programmable Search (API key + Custom Search Engine ID) for web augmentation
* GitHub OAuth App (Client ID & Secret) for login (optional but recommended)

### 2. Clone the Repository
```bash
git clone https://github.com/LuisGhz/myaichatapi.git
cd myaichatapi
```

### 3. Create a `.env` File (Local Dev)
The project uses `spring-dotenv` to load environment variables automatically.
```bash
OPENAI_API_KEY=your_openai_key
GEMINI_PROJECT_ID=your_gcp_project
GEMINI_LOCATION=us-central1
GOOGLE_WEB_SEARCH_API_KEY=your_google_search_api_key
GOOGLE_CUSTOM_SEARCH_ENGINE_ID=your_search_engine_id

DB_URL=jdbc:postgresql://localhost:5432/myaichat
DB_USERNAME=postgres
DB_PASSWORD=postgres

CDN_DOMAIN=https://your-cdn-domain.com
S3_ACCESS_KEY=your_aws_access_key
S3_SECRET_KEY=your_aws_secret_key
S3_BUCKET_NAME=your-bucket-name

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
APP_JWT_SECRET=replace_with_strong_base64_secret
JWT_EXPIRATION=86400000
ALLOWED_ORIGINS=http://localhost:5173
APP_BASE_URL=http://localhost:8080
```
Never commit real secrets. Use a secrets manager in production.

### 4. Start PostgreSQL
Create the database if it does not exist:
```sql
CREATE DATABASE myaichat;
```
Hibernate will handle schema creation (can be replaced later with migrations tool like Flyway or Liquibase).

### 5. Run the Application
```bash
./mvnw spring-boot:run
```
Application defaults to `http://localhost:8080`.

### 6. (Optional) Run Tests
```bash
./mvnw test
```

### 7. (Optional) Docker Build & Run
```bash
docker build -t myaichat:local .
docker run -p 8080:8080 --env-file .env myaichat:local
```

---

## Requirements

### Runtime / Platform
* Java: 17
* Spring Boot: 3.4.7
* Spring AI: 1.0.0
* Database: PostgreSQL (runtime) / H2 (tests)
* Build Tool: Maven (wrapper supplied)

### Key Dependencies
| Purpose | Dependency |
|---------|------------|
| Web / REST | `spring-boot-starter-web` |
| Validation | `spring-boot-starter-validation` |
| Data / ORM | `spring-boot-starter-data-jpa` + PostgreSQL driver |
| Security | `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`, `java-jwt` |
| AI Providers | `spring-ai-starter-model-openai`, `spring-ai-starter-model-vertex-ai-gemini` |
| Object Mapping | Lombok |
| Storage | `software.amazon.awssdk:s3` |
| Utilities | `com.fasterxml.uuid:java-uuid-generator` |
| Env Support | `spring-dotenv` |
| Testing | `spring-boot-starter-test`, JUnit 5, Mockito |
| Observability | `spring-boot-starter-actuator` |

### External Services & Accounts
* OpenAI (model access; billed per usage)
* Google Cloud Vertex AI Gemini (enable Generative AI APIs)
* Google Programmable Search (API key + CSE ID)
* AWS (S3 bucket + IAM user or role)
* GitHub OAuth (Client ID/Secret) for login flow

### Non‑Functional Qualities
* Token governance per chat thread
* Reactive streaming for assistant messages
* Pagination for historical messages (`Pageable` on history endpoint)
* Centralised exception advice for consistent HTTP responses

---

## References

### Spring & AI
* OpenAI Integration: https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html
* Vertex AI Gemini: https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html

### AI Model Docs
* OpenAI Models: https://platform.openai.com/docs/models
* Gemini API: https://ai.google.dev/gemini-api/docs/models
* Gemini Pricing: https://ai.google.dev/gemini-api/docs/pricing

### Google Cloud / Search
* Vertex AI Locations: https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations
* Gemini Models (example): https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash
* Custom Search API: https://developers.google.com/custom-search/v1/overview
* Programmable Search Engine: https://programmablesearchengine.google.com/controlpanel/all

