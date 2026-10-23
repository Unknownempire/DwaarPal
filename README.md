# DwaarPal — Society Vehicle Management System

DwaarPal is a backend application designed to simplify vehicle registration, approval, and gate entry/exit tracking in residential societies. It helps society administrators manage registered vehicles and enables security guards to record vehicle movements at society gates.

## Features

- **Vehicle Registration:** Residents can register vehicles with details such as registration number, type, company, and model.
- **Vehicle Approval:** Administrators can approve or reject pending vehicle registrations.
- **Vehicle Lookup:** Retrieve vehicle details using the registration number.
- **Resident Vehicle Listing:** Retrieve vehicles registered by a resident.
- **Gate Management:** Guards can retrieve the gates configured for their society.
- **Entry/Exit Tracking:** Guards can record vehicle entry and exit movements while validating vehicle approval and movement status.
- **Movement History:** Retrieve society-wide entry/exit history and vehicle-specific history, subject to the implemented access checks.
- **Centralized Exception Handling:** Return structured error responses for application errors and validation failures.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Style:** REST

## Architecture

The application follows a layered architecture:

```text
Client
  |
  v
Controllers
  |
  v
Services
  |
  v
Repositories
  |
  v
PostgreSQL Database
```

- **Controllers:** Handle HTTP requests and responses.
- **Services:** Implement business rules and access checks.
- **Repositories:** Retrieve and persist data using Spring Data JPA.
- **Entities:** Represent societies, buildings, flats, users, vehicles, gates, and entry/exit records.
- **DTOs and Mappers:** Separate API request/response structures from persistence entities.
- **Exception Handling:** Centralize application error responses.

## Data Model

The main domain entities are:

- **Society:** Represents a residential society.
- **Building:** Belongs to a society.
- **Flat:** Belongs to a building.
- **User:** Represents an administrator, security guard, or resident.
- **Vehicle:** Belongs to a resident and a society.
- **Gate:** Represents an entry/exit gate in a society.
- **EntryExitRecord:** Records a vehicle movement, the associated gate, the guard, the movement type, and its timestamp.

## API Endpoints

The following endpoints are implemented in the current backend.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/vehicles` | Register a vehicle |
| `GET` | `/api/vehicles/{registrationNumber}` | Retrieve vehicle details |
| `PATCH` | `/api/vehicles/{registrationNumber}/status` | Approve or reject a vehicle |
| `GET` | `/api/vehicles/pending` | List pending vehicle registrations |
| `GET` | `/api/vehicles/my-vehicles` | List vehicles belonging to a resident |
| `POST` | `/api/vehicles/entry-exit` | Record a vehicle entry or exit |
| `GET` | `/api/gates` | Retrieve gates for a guard's society |
| `GET` | `/api/entry-exit/history` | Retrieve entry/exit history for a society |
| `GET` | `/api/vehicles/{registrationNumber}/history` | Retrieve history for a specific vehicle |

Some endpoints currently accept `userId` as a request parameter for identifying the requesting user. This is a development-stage approach and must be replaced with authenticated identity and proper security controls before production deployment.

## Getting Started

### Prerequisites

Install the following:

- Java 21
- PostgreSQL
- Git

The repository includes the Maven Wrapper, so a separate Maven installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/Unknownempire/DwaarPal.git
cd DwaarPal
```

### 2. Create the database

Create a PostgreSQL database named:

```sql
CREATE DATABASE society_vehicle_manager;
```

### 3. Configure environment variables

Configure the following environment variables before starting the application:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/society_vehicle_manager` |
| `DB_USERNAME` | Your PostgreSQL username |
| `DB_PASSWORD` | Your PostgreSQL password |

Do not commit real credentials to the repository.

### 4. Start the application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

The current configuration uses Hibernate schema updates for development. Review database migration and configuration practices before deploying to production.

## Example Request

Retrieve a vehicle by its registration number:

```http
GET http://localhost:8080/api/vehicles/DL01AB1234
```

Register a vehicle using a JSON request body:

```http
POST http://localhost:8080/api/vehicles?userId=1
Content-Type: application/json

{
  "registrationNumber": "DL01AB1234",
  "type": "CAR",
  "company": "Toyota",
  "model": "Fortuner"
}
```

The vehicle registration request is associated with the specified user and starts with a pending approval status, according to the current business logic.

## Current Limitations and Future Improvements

- Implement Spring Security authentication and role-based authorization using the authenticated user rather than a client-provided `userId`.
- Add automated integration tests for authorization, vehicle movement rules, and error handling.
- Add pagination and filtering for movement history.
- Improve database migration and production configuration.
- Explore number plate recognition and a mobile interface for residents and security guards.

## Project Status

The backend's core vehicle registration, approval, gate listing, and entry/exit tracking APIs have been implemented. Authentication hardening, broader automated testing, and production readiness remain areas for improvement.

---

**DwaarPal** — A backend foundation for safer and more organized residential vehicle management.
