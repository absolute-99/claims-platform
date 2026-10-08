## Technology Stack

- Java 21
- Spring Boot
- Spring Web / REST
- Spring Data JPA
- PostgreSQL
- Flyway
- Apache Kafka
- JUnit 5
- Mockito
- Maven


## Architecture

The application is implemented as a modular monolith.

```text
                         Client / Postman
                                |
                                | HTTP / REST
                                v
                    +------------------------+
                    |    ClaimController     |
                    +------------------------+
                                |
                                v
                    +------------------------+
                    |      ClaimService      |
                    |                        |
                    |  Business Rules        |
                    |  State Transitions     |
                    |  Claim History         |
                    +------------------------+
                         |              |
                         v              v
                +----------------+  +----------------------+
                | ClaimRepository|  | ClaimHistoryRepository|
                +----------------+  +----------------------+
                         |              |
                         +------+-------+
                                |
                                v
                       +----------------+
                       |   PostgreSQL   |
                       +----------------+

                         Status Transition
                                |
                                v
                    +------------------------+
                    | ClaimEventPublisher    |
                    +------------------------+
                                |
                                | Kafka
                                v
                    +------------------------+
                    | Topic: claim-events    |
                    +------------------------+

                         Dashboard Request
                                |
                                v
                    +------------------------+
                    |  DashboardController   |
                    +------------------------+
                                |
                                v
                    +------------------------+
                    |   DashboardService     |
                    +------------------------+
                                |
                                v
                       +----------------+
                       | ClaimRepository|
                       +----------------+
                                |
                                v
                       +----------------+
                       |   PostgreSQL   |
                       +----------------+
```

---

## Why a Modular Monolith?

A modular monolith was selected instead of multiple microservices because the assessment focuses on backend design, domain boundaries, persistence, communication, and business workflow.

The claim workflow is cohesive and does not require independently deployable services for this assessment.

The code is separated into clear modules:

```text
claim
dashboard
exception
```

This keeps the system simple while allowing future extraction of modules into services if required.

---

## Main Modules

### Claim Module

Responsible for:

- Claim submission
- Claim retrieval
- Claim assignment
- Claim review
- Information requests
- Claim approval
- Claim rejection
- Claim settlement
- Claim history
- Claim status transitions
- Kafka event publishing

### Dashboard Module

Responsible for:

- Total claim count
- Claim counts by status
- Outstanding liability calculation

### Exception Module

Responsible for:

- Request validation errors
- Invalid state transitions
- Resource-not-found responses
- Consistent API error responses

---

## Core Domain Model

### Claim

The `Claim` entity represents an insurance claim.

Important fields include:

- `id`
- `claimNumber`
- `claimantId`
- `market`
- `claimType`
- `incidentDate`
- `description`
- `status`
- `estimatedLiability`
- `assignedTo`
- `createdAt`
- `updatedAt`
- `version`

The claim number is unique and is used as the external claim reference.

---

## Claim History

Every claim status transition is recorded in `claim_history`.

The history contains:

- Claim ID
- Previous status
- New status
- User/officer who performed the change
- Timestamp
- Optional reason

This provides an audit trail for the claim lifecycle.

Example:

```text
SUBMITTED
    |
    v
ASSIGNED
    |
    v
UNDER_REVIEW
    |
    v
INFORMATION_REQUIRED
    |
    v
UNDER_REVIEW
    |
    v
APPROVED
    |
    v
SETTLED
```

A claim can also move from:

```text
UNDER_REVIEW
       |
       v
    REJECTED
```

---

## Claim Lifecycle

The implemented claim states are:

```text
SUBMITTED
ASSIGNED
UNDER_REVIEW
INFORMATION_REQUIRED
APPROVED
REJECTED
SETTLED
```

The supported transitions are:

```text
SUBMITTED
    |
    v
ASSIGNED
    |
    v
UNDER_REVIEW
   / \
  /   \
 v     v
INFO   APPROVED
REQ      |
  |       v
  |     SETTLED
  |
  v
UNDER_REVIEW

UNDER_REVIEW
    |
    v
 REJECTED
```

### Valid Transitions

| Current Status | Allowed Transition |
|---|---|
| SUBMITTED | ASSIGNED |
| ASSIGNED | UNDER_REVIEW |
| UNDER_REVIEW | INFORMATION_REQUIRED |
| INFORMATION_REQUIRED | UNDER_REVIEW |
| UNDER_REVIEW | APPROVED |
| UNDER_REVIEW | REJECTED |
| APPROVED | SETTLED |

Invalid transitions are rejected by the service layer with HTTP `409 Conflict`.

---

## Business Rule Enforcement

The claim service validates every status transition.

For example:

```text
SUBMITTED -> APPROVED
```

is not allowed.

Similarly:

```text
ASSIGNED -> SETTLED
```

is not allowed.

The service validates the current state before changing it.

If an invalid transition is attempted, the API returns a conflict response.

This prevents claims from bypassing required workflow stages.

---

## REST APIs

Base URL:

```text
/api/v1
```

### Create Claim

```http
POST /api/v1/claims
```

Example request:

```json
{
  "claimantId": "11111111-1111-1111-1111-111111111111",
  "market": "SG",
  "claimType": "MOTOR",
  "incidentDate": "2026-10-01",
  "description": "Vehicle accident",
  "estimatedLiability": 25000
}
```

New claims are created with:

```text
SUBMITTED
```

### Get Claims

```http
GET /api/v1/claims
```

By status:

```http
GET /api/v1/claims?status=SUBMITTED
```

By assigned officer:

```http
GET /api/v1/claims?assignedTo={officerId}
```

### Get Claim

```http
GET /api/v1/claims/{claimNumber}
```

Example:

```text
GET /api/v1/claims/CLM-123
```

### Get Claim History

```http
GET /api/v1/claims/{claimNumber}/history
```

Returns the status transition history for the claim.

### Assign Claim

```http
POST /api/v1/claims/{claimNumber}/assign?officerId={officerId}
```

Example:

```text
POST /api/v1/claims/CLM-123/assign?officerId=11111111-1111-1111-1111-111111111111
```

Transition:

```text
SUBMITTED -> ASSIGNED
```

### Start Review

```http
POST /api/v1/claims/{claimNumber}/review
```

Transition:

```text
ASSIGNED -> UNDER_REVIEW
```

### Request Additional Information

```http
POST /api/v1/claims/{claimNumber}/information-request
```

Transition:

```text
UNDER_REVIEW -> INFORMATION_REQUIRED
```

### Resume Review

```http
POST /api/v1/claims/{claimNumber}/resume-review
```

Transition:

```text
INFORMATION_REQUIRED -> UNDER_REVIEW
```

### Approve Claim

```http
POST /api/v1/claims/{claimNumber}/approve
```

Transition:

```text
UNDER_REVIEW -> APPROVED
```

### Reject Claim

```http
POST /api/v1/claims/{claimNumber}/reject
```

Transition:

```text
UNDER_REVIEW -> REJECTED
```

### Settle Claim

```http
POST /api/v1/claims/{claimNumber}/settle
```

Transition:

```text
APPROVED -> SETTLED
```

---

## Dashboard API

Claims staff can retrieve an operational summary using:

```http
GET /api/v1/dashboard/summary
```

Example:

```json
{
  "totalClaims": 4,
  "submitted": 1,
  "assigned": 1,
  "underReview": 0,
  "informationRequired": 0,
  "approved": 0,
  "rejected": 0,
  "settled": 2,
  "outstandingLiability": 50000.00
}
```

### Outstanding Liability

Outstanding liability includes claims that are not:

- REJECTED
- SETTLED

This provides a simple operational view of current financial exposure.

---

## Kafka Events

Claim lifecycle changes publish events to:

```text
claim-events
```

Example event:

```json
{
  "claimId": "f26e0664-c99d-46f2-afe6-0f1e5726997f",
  "claimNumber": "CLM-b15faf2e-de9d-4596-80cc-3385263fec2e",
  "fromStatus": "SUBMITTED",
  "toStatus": "ASSIGNED",
  "changedBy": "11111111-1111-1111-1111-111111111111",
  "estimatedLiability": 25000.00,
  "occurredAt": "2026-10-08T13:23:21.954999100"
}
```

Kafka is used for asynchronous communication so downstream consumers can independently react to claim lifecycle events.

Potential consumers include:

- Notifications
- Analytics
- Reporting
- Fraud/risk processing
- Operational monitoring

---

## Synchronous vs Asynchronous Communication

### Synchronous

REST is used for operations where the caller needs an immediate response:

- Create claim
- Assign claim
- Start review
- Request information
- Resume review
- Approve claim
- Reject claim
- Settle claim
- Get claim
- Get claim history
- Get dashboard summary

### Asynchronous

Kafka is used for claim lifecycle events.

```text
Claim status change
       |
       v
Kafka event
       |
       +--> Notifications
       +--> Analytics
       +--> Reporting
       +--> Future downstream services
```

This keeps the core claim transaction independent from downstream consumers.

---

## Read and Write Profile

### Write Operations

Writes are transactional and include:

- Claim creation
- Assignment
- Status transitions
- History creation

The claim and corresponding history are persisted in PostgreSQL.

### Read Operations

Reads include:

- Claim retrieval
- Claim history retrieval
- Status-based work queues
- Officer-based claim retrieval
- Dashboard summary

Indexes are added to support common read patterns.

For a larger production system, read-heavy dashboard workloads could later be moved to a dedicated read model or analytics store.

---

## Database Design

### Claims

The `claims` table stores the current state of each claim.

Important fields include:

```text
id
claim_number
claimant_id
market
claim_type
incident_date
description
status
estimated_liability
assigned_to
created_at
updated_at
version
```

Indexes support common work-queue queries:

```text
assigned_to + status + created_at
status
market
```

The `version` column is used for optimistic locking through JPA `@Version`.

### Claim History

The `claim_history` table stores lifecycle transitions:

```text
id
claim_id
from_status
to_status
changed_by
changed_at
reason
```

A foreign key links history records to the claim.

---

## Database Indexes

Indexes are provided for common query patterns.

### Work Queue Index

```text
(assigned_to, status, created_at)
```

This supports retrieving an officer's workload efficiently.

### Status Index

```text
(status)
```

This supports filtering claims by workflow state.

### Market Index

```text
(market)
```

This supports market-based filtering.

### Claim History Index

```text
(claim_id, changed_at)
```

This supports retrieving claim history in chronological order.

---

## Database Migrations

Flyway manages database schema changes.

Migrations are stored under:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__create_claims_table.sql
V2__create_claim_history_table.sql
```

Hibernate schema generation is disabled for application schema management:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This ensures the application validates the schema instead of silently modifying it.

---

## Optimistic Locking

The `claims` table contains a `version` column.

The JPA entity uses:

```java
@Version
private Long version;
```

This provides optimistic locking.

The purpose is to reduce the risk of concurrent updates overwriting each other.

---

## Request Validation

Claim creation requests are validated using Jakarta Bean Validation.

Required fields include:

```text
claimantId
market
claimType
incidentDate
```

Additional validation is applied to:

```text
market length
claimType length
description length
estimatedLiability
```

Invalid requests return:

```text
400 Bad Request
```

Example:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "errors": {
    "claimantId": "claimantId is required",
    "market": "market is required",
    "claimType": "claimType is required",
    "incidentDate": "incidentDate is required"
  }
}
```

---

## Error Handling

A global exception handler provides consistent API errors.

| Scenario | HTTP Status |
|---|---:|
| Invalid request | 400 |
| Invalid state transition | 409 |
| Claim not found | 404 |

---

## Work Queue

Claims staff need to retrieve claims that require action.

The repository supports queries such as:

```text
Claims by status
Claims assigned to an officer
Claims assigned to an officer and status
```

Ordering by creation time provides a simple oldest-first work queue.

For a production system, additional prioritization could be introduced based on:

- Claim severity
- Liability exposure
- SLA
- Market
- Claim type
- Age of claim

---

## Liability Exposure

Each claim may contain an estimated liability.

The dashboard calculates outstanding liability by excluding:

```text
REJECTED
SETTLED
```

claims.

Therefore:

```text
Outstanding Liability
=
SUM(estimatedLiability)
for active claims
```

This provides a simple view of the organization's current open exposure.

In a larger implementation, liability could become a more detailed financial model including:

- Reserve
- Approved amount
- Settlement amount
- Currency
- Currency conversion
- Payments
- Adjustments

---

## Testing

The project contains unit tests for the core claim lifecycle.

Current tests include:

- Assign submitted claim
- Start claim review
- Request additional information
- Resume review
- Approve claim
- Settle claim
- Reject invalid transition

The Spring application context is also tested.

Latest successful result:

```text
Tests run: 8
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Run tests using:

```powershell
.\mvnw.cmd clean test
```

---

## Running Locally

### Prerequisites

- Java 21
- Docker Desktop
- PostgreSQL
- Git

Kafka is run locally using Docker.

### Verify Kafka

```powershell
docker ps
```

The Kafka container should be running:

```text
claims-kafka
```

Verify the topic:

```powershell
docker exec claims-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Expected:

```text
claim-events
```

### Run the Application

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Application:

```text
http://localhost:8080
```

### Run Tests

```powershell
.\mvnw.cmd clean test
```

---

## Example End-to-End Workflow

```text
1. Create claim
        |
        v
   SUBMITTED
        |
        v
2. Assign officer
        |
        v
    ASSIGNED
        |
        v
3. Start review
        |
        v
   UNDER_REVIEW
        |
        +----------------------+
        |                      |
        v                      v
4. Request info           Approve / Reject
        |                      |
        v                      v
INFORMATION_REQUIRED      APPROVED / REJECTED
        |
        v
5. Resume review
        |
        v
   UNDER_REVIEW
        |
        v
6. Approve
        |
        v
    APPROVED
        |
        v
7. Settle
        |
        v
    SETTLED
```

Each status transition:

```text
updates current claim state
        +
creates claim history
        +
publishes Kafka event
```

---

## Design Decisions

### Modular Monolith

Chosen to keep the implementation simple and maintainable within the assessment time limit while maintaining clear domain boundaries.

### PostgreSQL

Used because claim state and lifecycle history require transactional persistence and relational consistency.

### Flyway

Used for explicit and version-controlled database migrations.

### Kafka

Used for asynchronous claim lifecycle events and future downstream integrations.

### Claim History

Stored separately rather than relying only on current claim state because auditability is important for insurance workflows.

### Optimistic Locking

JPA `@Version` is used to protect against concurrent updates to the same claim.

### REST

Used for transactional commands and query APIs because callers require immediate responses.

---

## Trade-offs

The implementation intentionally avoids over-engineering.

The following capabilities were not implemented because they were outside the MVP scope:

- Authentication and authorization
- Full claimant management
- Payment processing
- Document storage
- Complex fraud detection
- Advanced SLA management
- Multi-region deployment
- Kubernetes deployment
- Distributed microservices
- Dedicated analytics infrastructure
- Advanced notification service

These can be added later if the platform grows.

---

## Future Improvements

Possible future improvements include:

- Transactional Outbox Pattern for reliable database/Kafka publishing
- Authentication and role-based authorization
- Officer/workload management
- Pagination for large claim queues
- Advanced search/filtering
- Information request entity and document attachments
- Claim assessment entity
- Notifications
- Fraud/risk integration
- Metrics and distributed tracing
- API versioning
- Integration/contract tests
- Kafka consumers
- Production deployment configuration

---

## Security Considerations

A production implementation should include:

- OAuth2 / OpenID Connect
- Role-based authorization
- Input validation
- Secure secret management
- Encryption in transit
- Database credential management
- Audit logging
- Kafka authentication and encryption

The take-home implementation keeps authentication outside the core MVP to focus on the claim workflow.

---

## Project Structure

```text
claims-platform
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── chubb
│   │   │           └── claims_platform
│   │   │               │
│   │   │               ├── ClaimsPlatformApplication.java
│   │   │               │
│   │   │               ├── claim
│   │   │               │   ├── controller
│   │   │               │   │   └── ClaimController.java
│   │   │               │   │
│   │   │               │   ├── dto
│   │   │               │   │   ├── ClaimHistoryResponse.java
│   │   │               │   │   ├── ClaimResponse.java
│   │   │               │   │   └── CreateClaimRequest.java
│   │   │               │   │
│   │   │               │   ├── entity
│   │   │               │   │   ├── Claim.java
│   │   │               │   │   ├── ClaimHistory.java
│   │   │               │   │   └── ClaimStatus.java
│   │   │               │   │
│   │   │               │   ├── event
│   │   │               │   │   ├── ClaimEvent.java
│   │   │               │   │   └── ClaimEventPublisher.java
│   │   │               │   │
│   │   │               │   ├── repository
│   │   │               │   │   ├── ClaimHistoryRepository.java
│   │   │               │   │   └── ClaimRepository.java
│   │   │               │   │
│   │   │               │   └── service
│   │   │               │       └── ClaimService.java
│   │   │               │
│   │   │               ├── dashboard
│   │   │               │   ├── controller
│   │   │               │   │   └── DashboardController.java
│   │   │               │   ├── dto
│   │   │               │   │   └── DashboardResponse.java
│   │   │               │   └── service
│   │   │               │       └── DashboardService.java
│   │   │               │
│   │   │               └── exception
│   │   │                   └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       └── db
│   │           └── migration
│   │               ├── V1__create_claims_table.sql
│   │               └── V2__create_claim_history_table.sql
│   │
│   └── test
│       └── java
│           └── com
│               └── chubb
│                   └── claims_platform
│                       ├── ClaimsPlatformApplicationTests.java
│                       └── claim
│                           └── service
│                               └── ClaimServiceTest.java
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## Current Status

The current MVP supports:

- Claim submission
- Claim retrieval
- Claim assignment
- Claim review
- Information request workflow
- Resume review workflow
- Claim approval
- Claim rejection
- Claim settlement
- Claim history
- Work queue queries
- Dashboard summary
- Outstanding liability calculation
- Kafka lifecycle events
- Request validation
- Global error handling
- Optimistic locking
- Automated lifecycle tests

The implementation focuses on the core claim workflow and keeps the design intentionally simple enough to demonstrate the main backend architecture within the assessment time limit.

---

## Verification

Run the automated test suite:

```powershell
.\mvnw.cmd clean test
```

Expected result:

```text
Tests run: 8
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Then verify:

```text
GET http://localhost:8080/api/v1/dashboard/summary
```

The application should return the current claim dashboard summary.

---

## Conclusion

The Chubb APAC Claims Platform provides a focused backend implementation for the insurance claim lifecycle.

The solution demonstrates:

- REST API design
- Domain modeling
- Business workflow enforcement
- Relational persistence
- Database migrations
- Audit history
- Kafka event-driven communication
- Dashboard reporting
- Validation
- Error handling
- Optimistic locking
- Automated testing

The architecture is intentionally designed to provide a strong MVP while leaving clear extension points for authentication, notifications, advanced work queues, analytics, and distributed service extraction in the future.
'''


