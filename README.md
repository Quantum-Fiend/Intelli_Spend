# 💰 IntelliSpend — Next-Gen AI Financial Intelligence

![IntelliSpend Hero Banner](https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&q=80&w=2070&ixlib=rb-4.0.3) 


IntelliSpend is a high-performance, production-hardened financial tracking ecosystem. It leverages **LLM-powered reasoning** to categorize expenses and provide proactive financial advice, all within a strictly validated, secure Spring Boot environment.

---

## 🏗 System Architecture

IntelliSpend is designed for scalability and resilience. The core Java engine orchestrates data persistence, security, and AI analysis.

```mermaid
graph TD
    subgraph "Frontend Layer"
        Client[Mobile/Web Client]
    end

    subgraph "Application Layer (Spring Boot)"
        API[REST Controllers]
        Security[Spring Security / JWT]
        Services[Core Services Layer]
        AI_Manager[AI Integration Engine]
        Scheduler[Task Scheduler]
    end

    subgraph "Data Layer"
        DB[(PostgreSQL)]
        Cache[Redis / Persistence]
    end

    subgraph "External Ecosystem"
        OpenAI[OpenAI LLM API]
        n8n[n8n Automation Hub]
    end

    Client --> API
    API --> Security
    Security --> Services
    Services --> DB
    Services --> AI_Manager
    AI_Manager --> OpenAI
    Scheduler --> Services
    Services -->|Webhook| n8n
```

---

## 🤖 Advanced Automation with n8n

IntelliSpend features a powerful integration pattern with **n8n**, allowing for real-time notifications and multi-channel report delivery.

### n8n Workflow Visualization
```mermaid
graph LR
    WS[IntelliSpend Webhook] --> NT{Event Type}
    
    NT -->|Budget Alert| SL[Slack/Discord Alert]
    NT -->|Monthly Report| EM[Email PDF Report]
    NT -->|Transaction Sync| GS[Google Sheets Backup]
    
    SL --> User((User))
    EM --> User
    GS --> Storage[(Cloud Archive)]
    
    style n8n fill:#f96,stroke:#333,stroke-width:2px
    style WS fill:#6c5ce7,color:#fff
```

---

## 📂 Project Structure

A deep dive into the IntelliSpend directory architecture:

```text
IntelliSpend/
├── src/main/java/com/intellispend/
│   ├── config/              # Security, JWT, OpenAPI & Web Config
│   ├── controller/          # RESTful Endpoint definitions
│   ├── dto/                 # Data Transfer Objects & API requests/responses
│   ├── entity/              # JPA Domain Models (User, Expense, Budget, Insight)
│   ├── exception/           # Global Exception Handling & Custom Errors
│   ├── repository/          # Spring Data JPA Repositories & Specifications
│   ├── service/             # Business Logic & AI Orchestration
│   └── util/                # JWT Utilities, Constants & Demo Seeders
├── src/main/resources/
│   ├── db/migration/        # Flyway DB Versioning
│   ├── application.yml      # Base Configurations
│   └── application-prod.yml # Production Overrides
├── Dockerfile               # Multi-stage optimized build
├── docker-compose.yml       # Full stack orchestration (App + DB)
└── pom.xml                  # Maven Dependency Management
```

---

## 💎 Premium Features

### 🧠 LLM-Driven Categorization
Uses OpenAI's GPT-3.5 to intelligently classify transactions based on natural language descriptions, fallback to lightning-fast keyword matching when API limits are reached.

### 🛡 Production Hardening
- **Soft Delete**: Data integrity via Hibernate `@SQLDelete` and `@Where`.
- **Global Validation**: Strict JSR-303 constraints on all entry points.
- **Fail-Safe AI**: Implemented `Spring Retry` with exponential backoff for high-availability AI services.

### 📊 Deep Analytics
- **Behavioral Patterns**: Daily and weekly spending trend analysis.
- **Proactive Budgeting**: Real-time threshold monitoring with multi-level alerts (Warning @ 90%, Critical @ 100%+).

---

## 🚀 Deployment Guide

### Rapid Launch (Docker)
```bash
# 1. Provide your OpenAI Key in .env
# 2. Fire up the ecosystem
docker-compose up --build -d
```

### Manual Build
```bash
mvn clean package
java -jar target/intelli-spend-0.0.1-SNAPSHOT.jar
```

---

## 📜 API Documentation
Interactive documentation is available out-of-the-box via Swagger:
🔗 `http://localhost:8080/swagger-ui.html`

---

## 🤝 Project Roadmap
- [x] Phase 1-7: Core Features & Hardening
- [x] Phase 8: Technical Debt & Polish
- [x] Phase 9: n8n Integration & Branding
- [ ] Phase 10: Multi-tenant Support (Upcoming)

---
© 2026 IntelliSpend Engineering. *Built for Financial Clarity. By Tushar*
