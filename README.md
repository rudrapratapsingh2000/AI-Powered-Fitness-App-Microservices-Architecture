# 🏋️‍♂️ AI-Powered Fitness App — Microservices Architecture

![Java](https://img.shields.io/badge/Java-17-blue)  
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-green)  
![Kafka](https://img.shields.io/badge/Kafka-EventStreaming-orange)  
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue)  
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-green)  
![Keycloak](https://img.shields.io/badge/Auth-Keycloak-red)  
![Gemini](https://img.shields.io/badge/AI-GoogleGemini-purple)  

---

## 📌 Table of Contents
- Overview  
- Architecture Summary  
- Tech Stack  
- AI Integration  
- Authentication & Security  
- Services & Responsibilities  
- Local Development Setup   
- How to Run (Recommended order)  
- Deployment Notes  
- Testing & Observability  
- Roadmap  
- Diagram  
- Contributing  

---

## 🚀 Overview
A production-ready **microservices fitness app** that collects user activity, processes events asynchronously with **Kafka**, and provides personalized fitness insights using **Google Gemini AI**.  
Built with **Spring Boot & Spring Cloud**, secured by **Keycloak**, and backed by **PostgreSQL** & **MongoDB**.

---

## 🔧 Architecture Summary
- **Gateway** → Routes requests via Spring Cloud Gateway; secures endpoints with Keycloak.  
- **User Service** → Manages user profiles & registration. Stores data in **PostgreSQL**.  
- **Activity Service** → Captures workout data, stores in **MongoDB**, and publishes events to **Kafka**.  
- **AI Service** → Consumes Kafka events, enriches data, and calls **Google Gemini API**. Persists AI insights in **MongoDB**.  
- **Infra Services**:  
  - **Kafka**: Messaging backbone  
  - **Config Server**: Centralized configuration  
  - **Eureka**: Service discovery  
  - **Keycloak**: Authentication & IAM  

---

## 🧩 Tech Stack
- **Backend** → Java, Spring Boot, Spring Cloud  
- **Databases** → PostgreSQL (User), MongoDB (Activity & AI)  
- **Eventing** → Apache Kafka  
- **Security** → Keycloak (OIDC/OAuth2)  
- **AI** → Google Gemini API  

---

## 🧠 AI Integration
- The **AI Service** consumes user activities from Kafka.  
- Sends structured activity data to **Google Gemini API**.  
- Stores AI-driven insights in MongoDB for personalized recommendations.  

---

## 🔐 Authentication & Security
- All endpoints secured by **Bearer <TOKEN>** via **Keycloak**.  
- Keycloak manages user roles, permissions, and OIDC flows.  

---

## 🗂 Services & Responsibilities

| Service          | Responsibility                           | Persistence |
|------------------|------------------------------------------|-------------|
| Gateway          | API routing & auth enforcement           | —           |
| User Service     | Registration, login, profile             | PostgreSQL  |
| Activity Service | Store activities, publish Kafka events   | MongoDB     |
| AI Service       | Consume Kafka, call Gemini, store insights | MongoDB   |
| Kafka            | Event streaming backbone                 | —           |
| Keycloak         | Authentication & Authorization           | —           |
| Config Server    | Centralized configuration                | —           |
| Eureka           | Service discovery & registration         | —           |

---

## 🛠 Local Development Setup
```bash
# clone repository
git clone https://github.com/your-org/fitness-app.git
cd fitness-app

# build services
mvn clean install

```

## ⚙️ How to run (Recommended order)

1. Start infrastructure components:

   * Start **Config Server**
   * Start **Eureka Server**
   * Start **Keycloak** (import realm and client configs)
2. Start messaging & databases:

   * Start **Zookeeper** and **Kafka**
   * Start **PostgreSQL** and **MongoDB**
3. Start microservices (use environment variables or config server):

   * User Service
   * Activity Service
   * AI Service
   * Gateway Service
4. Ensure services register with Eureka and pick up config from Config Server.
5. Use Postman or your frontend to call the Gateway endpoints with a Keycloak-issued token.

## ⚙️ Deployment notes

* Secure access to Keycloak and Config Server; use TLS in all environments.
* For Gemini integration, store API keys/secrets in a secure vault and inject via Config Server or environment variables.

## 🧪 Testing & Observability

* Unit & integration tests per-service (Spring Boot Test).
* Use Prometheus & Grafana for metrics; centralized logs with ELK / Loki.
* Configure Kafka consumer groups and monitor lag for AI Service.
  
## 📁 Diagram
```mermaid
flowchart LR
  subgraph External[ ]
    direction TB
    FE["POSTMAN / FRONTEND"]
  end

  subgraph System["(Microservices Boundary)"]
    direction TB
    GW["Gateway\n(Spring Cloud Gateway)"]
    subgraph ServicesRow
      direction TB
      User["User Service\n(PostgreSQL)"]
      Activity["Activity Service\n(MongoDB)"]
    end

    Kafka["Kafka (Topics)"]
    AI["AI Service\n(MongoDB)\nConsumes Kafka -> Gemini"]

    subgraph InfraBottom
      direction LR
      Keycloak["Keycloak\n(Auth & IAM)"]
      Config["Config Server\n(Spring Cloud Config)"]
      Eureka["Eureka Server\n(Service Discovery)"]
    end

    DBUser[(PostgreSQL)]
    DBActivity[(MongoDB)]
    DBAI[(MongoDB)]
  end

  Gemini["Google's Gemini API"]

  %% external -> gateway
  FE -->|Bearer <TOKEN>| GW

  %% gateway to services
  GW --> User
  GW --> Activity
  GW --> Keycloak

  %% service dbs
  User --> DBUser
  Activity --> DBActivity

  %% async flow
  Activity -->|produce events| Kafka
  Kafka -->|consume| AI
  AI --> DBAI

  %% AI -> Gemini
  AI -->|AI requests| Gemini

  %% infra connections
  GW --> Config
  User --> Config
  Activity --> Config
  AI --> Config
  User --> Eureka
  Activity --> Eureka
  AI --> Eureka

  Keycloak -->|secures| GW

  classDef box stroke:#ffffff,stroke-width:2px,fill:#6f42c1,color:#fff
  class User,Activity,AI box
  class GW stroke:#ffffff,stroke-width:2px,fill:#b93b3b,color:#fff
  class Kafka stroke:#ffffff,stroke-width:2px,fill:#1f3a57,color:#fff
  class Gemini stroke:#ffffff,stroke-width:2px,fill:#7b6b1f,color:#fff
  class Keycloak,Config,Eureka stroke:#ffffff,stroke-width:2px,fill:#203b54,color:#fff
  class FE stroke:#ffffff,stroke-width:2px,fill:#0f2b3b,color:#fff
```
---

## ✅ Current Status

* Backend microservices implemented and working locally.
* AI integration with Gemini wired via the AI Service.
* Frontend (React) is in progress.

## 🙏 Contributing

Contributions are welcome — please fork, open issues for bugs or feature requests, and submit PRs.

---

---
