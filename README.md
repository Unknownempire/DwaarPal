# DwaarPal — Society Vehicle Management API

DwaarPal is a Spring Boot REST API for managing residential society memberships, vehicle approvals, gates, and vehicle entry/exit records. Residents, society administrators, and security guards use authenticated, role-based endpoints to perform the actions allowed for their role.

This repository contains the backend API. It does not currently contain a mobile or web frontend.

## Current features

- Resident account registration and phone/password login.
- Stateless JWT bearer-token authentication with role-based endpoint access.
- Public society, building, and flat catalogs used when applying for membership.
- Resident membership applications, application history, admin decisions, and resident membership leave.
- Resident vehicle registration, personal vehicle listing, and vehicle lookup for authorized users.
- Admin review of pending membership applications and vehicle registrations.
- Admin gate creation and guard gate listing.
- Guard-recorded vehicle entry and exit, with checks for approval, society membership, gate ownership, and current vehicle location.
- Society movement history for admins and guards, and vehicle movement history for its resident owner or active society staff.
- Bean validation and centralized application error handling.

## Roles and workflows

### Resident

1. Register an account. Public registration creates a resident account.
2. Log in with phone and password to receive a JWT.
3. Browse societies, buildings, and flats, then submit a membership application.
4. Check membership application status. An active membership is required to register vehicles.
5. Register a vehicle and check its approval status and movement history.
6. Leave an active membership when needed; the membership record is retained as inactive.

### Society administrator

An administrator with an active membership in the relevant society can review pending resident membership applications, approve or reject vehicle registrations, view society movement history, and create gates for that society.

### Security guard

A guard with an active membership in the vehicle's society can list that society's gates and record an approved vehicle's entry or exit at a gate belonging to that society. Duplicate movements that conflict with the recorded inside/outside status are rejected.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring MVC, Spring Security, OAuth2 Resource Server JWT support
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway database migrations
- Maven Wrapper

## Run locally

### Prerequisites

- Java 21
- PostgreSQL

Create a database, for example:

```sql
CREATE DATABASE society_vehicle_manager;
```

Configure these environment variables before starting the API:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/society_vehicle_manager` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | Your local PostgreSQL password |
| `DWAARPAL_JWT_SECRET` | Base64-encoded secret key for HS256 signing |

Keep secrets out of source control. Generate a suitably long random key and provide its Base64 encoding as `DWAARPAL_JWT_SECRET`.

Start the application from the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

By default, the API listens at `http://localhost:8080`. Flyway migrations are applied at startup, and Hibernate validates the resulting schema (`ddl-auto=validate`).

## Authentication

Registration and login are public. All other endpoints require a bearer token unless noted in the endpoint table.

Register a resident:

```http
POST /api/users
Content-Type: application/json

{
  "name": "Rohan Mehta",
  "phone": "9876543210",
  "email": "rohan@example.com",
  "password": "change-me-123"
}
```

Log in with the registered phone number and password:

```http
POST /api/auth/login
Content-Type: application/json

{
  "phone": "9876543210",
  "password": "change-me-123"
}
```

The response contains `accessToken`, `tokenType` (`Bearer`), and `expiresIn` (seconds). Send the token on protected requests:

```http
Authorization: Bearer <accessToken>
```

Passwords are encoded with BCrypt. Registration always creates a `RESIDENT`; administrator and guard accounts must be provisioned through an appropriate trusted process. The API uses stateless JWT authentication and derives the acting user from the token rather than caller-supplied user IDs.

## API endpoints

Protected endpoints require `Authorization: Bearer <accessToken>`. Role access is enforced both in Spring Security and by service-level society/membership checks.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/users` | Public | Register a resident account |
| `POST` | `/api/auth/login` | Public | Authenticate with phone and password; return a JWT |
| `GET` | `/api/societies` | Public | List societies |
| `GET` | `/api/societies/{societyId}/buildings` | Public | List buildings in a society |
| `GET` | `/api/buildings/{buildingId}/flats` | Public | List flats in a building |
| `POST` | `/api/memberships` | Resident | Apply to join a society and flat |
| `GET` | `/api/memberships/mine` | Resident | List own membership applications and memberships |
| `GET` | `/api/memberships/pending?societyId={id}` | Admin | List pending applications for an authorized society |
| `PATCH` | `/api/memberships/{membershipId}/decision?decision={ACTIVE\|REJECTED}` | Admin | Approve or reject a pending application |
| `PATCH` | `/api/memberships/{membershipId}/leave` | Resident | End own active membership |
| `POST` | `/api/vehicles?membershipId={id}` | Resident | Register a vehicle for own active membership |
| `GET` | `/api/vehicles/my-vehicles` | Resident | List own vehicles |
| `GET` | `/api/vehicles/{registrationNumber}` | Resident, guard, admin | Look up a vehicle if authorized for its society |
| `GET` | `/api/vehicles/pending?societyId={id}` | Admin | List pending vehicles for an authorized society |
| `PATCH` | `/api/vehicles/{registrationNumber}/status?societyId={id}&updateStatus={APPROVED\|REJECTED}` | Admin | Approve or reject a pending vehicle |
| `POST` | `/api/vehicles/{registrationNumber}/entry-exit?entryExitType={ENTRY\|EXIT}&gateId={id}` | Guard | Record vehicle movement at an authorized society gate |
| `GET` | `/api/gates?societyId={id}` | Guard | List gates for an authorized society |
| `POST` | `/api/societies/{societyId}/gates` | Admin | Create a gate in an authorized society |
| `GET` | `/api/entry-exit/history` | Admin, guard | List movement history for societies where the user has active membership |
| `GET` | `/api/entry-exit/vehicles/{registrationNumber}/history` | Resident, guard, admin | View a vehicle's history when authorized for its society |

Vehicle types are `CAR`, `BIKE`, `SCOOTER`, and `OTHER`. Vehicle approval statuses are `PENDING`, `APPROVED`, `REJECTED`, and `INACTIVE`; membership statuses are `PENDING`, `ACTIVE`, `REJECTED`, and `INACTIVE`. Movement types are `ENTRY` and `EXIT`.

## Example: apply for a membership

After registering and logging in, use the JWT to submit a society ID and flat ID from the public location catalogs:

```http
POST /api/memberships
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "societyId": 2,
  "flatId": 14
}
```

The request starts as `PENDING`. The selected flat must belong to the selected society, and a resident cannot have another pending or active membership in that society.

## Example: register a vehicle

An active resident membership is required. Registration starts with `PENDING` approval and an unknown location status.

```http
POST /api/vehicles?membershipId=1
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "registrationNumber": "DL01AB1234",
  "type": "CAR",
  "company": "Toyota",
  "model": "Fortuner"
}
```

Registration numbers are trimmed and normalized to uppercase. A vehicle registration number can only be registered once.

## Data model

- **Society**, **Building**, and **Flat** provide the location hierarchy.
- **User** stores an account, role, and account status.
- **Membership** links a user to a society and flat, including application, review, and end history. Multiple residents can share a flat; a user can have at most one active membership per society.
- **Vehicle** belongs to a membership and stores vehicle details, approval status, and current location status.
- **Gate** belongs to a society.
- **EntryExitRecord** records the vehicle, gate, guard, movement type, and timestamp.

## Current scope and limitations

- The repository provides backend APIs only; a mobile app/frontend has not been implemented here.
- Public registration creates resident accounts. The current API does not expose public admin or guard provisioning.
- Movement history endpoints do not currently expose pagination or date/filter query parameters.
- No automated integration test coverage for all role and movement rules is documented yet.
- Configure database credentials and the JWT signing secret securely for each environment before deployment.

---

**DwaarPal** — API foundations for safer, more organized vehicle access in residential societies.
