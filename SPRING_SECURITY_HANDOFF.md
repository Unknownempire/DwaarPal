# DwaarPal Spring Security Handoff

Use this note to continue the Spring Security learning and implementation work in another session. It records the current design and progress; recheck the listed files before making further changes because the workspace may have changed.

## How to Work With the User

- The user is a student and wants a senior-engineer mentor, not silent implementation.
- Work in small steps. Explain each component in simple terms, where it is used, who calls it, and why it is needed.
- The user usually edits the files themselves, then asks for a read-only review. Inspect the actual code before saying a step is complete.
- Work on one API at a time. When an API implementation is complete, test it in Postman before moving on. The user has used Postman for prior API checks and wants this workflow repeated for every completed API.
- If Postman testing reveals a bug, report the behavior and evidence to the user, work through a fix with them, then rerun the relevant Postman checks until the API behaves as expected. Do not treat an API as complete until this test/fix/retest loop is done.
- The user runs tests themselves unless they ask otherwise. Do not run automated tests or implementation commands unless asked; Postman test guidance and reviewing the user's reported results are welcome.
- Do not ask for or repeat secrets, passwords, or JWTs. Never place actual secret values in documentation.
- The product is a mobile app using a Spring Boot REST API. The user chose JWT bearer authentication.

## Repository Context

- Repository: `C:\Users\prana\projects\SVM\society-vehicle-manager\society-vehicle-manager`
- Java 21, Spring Boot, Spring MVC, Spring Data JPA, PostgreSQL, Maven Wrapper.
- Layering: `controller` → `service` → `repository` → PostgreSQL. User and society domain classes are in `entity`; request/response types are in `dto`.
- Users have global roles `ADMIN`, `GUARD`, `RESIDENT`; `Membership` connects a user to a society and may include a flat. Authorization therefore needs both role and society/resource checks.
- Public registration creates residents only. There is no general admin/guard provisioning endpoint in the reviewed code.

## Implemented Login/JWT Pieces

The files below were reviewed during the session. Confirm their latest state before continuing.

- `pom.xml`: Spring Security starter and OAuth2 Resource Server starter are present; Java is 21. The project also retains `spring-security-crypto`.
- `repository/UserRepository.java`: includes `findByPhone`.
- `security/SecurityConfig.java`:
  - `@EnableWebSecurity` is present; it is optional with the configured filter-chain bean but harmless.
  - Exposes `AuthenticationManager` from `AuthenticationConfiguration` and a BCrypt `PasswordEncoder` bean.
  - Reads `app.jwt.secret` from the environment-backed application property. The real value must remain outside source control.
  - Creates an HS256 `SecretKey`, `JwtEncoder`, and `JwtDecoder`.
  - Defines a stateless `SecurityFilterChain`; disables CSRF, form login, and HTTP Basic for this bearer-token API.
  - Permits `POST /api/auth/login`, `POST /api/users`, and the society/building/flat catalog GET routes.
  - Requires `ADMIN` for `GET /api/vehicles/pending` and `PATCH /api/vehicles/*/status`; other routes require authentication.
  - Uses a `JwtAuthenticationConverter` that reads the custom `authorities` claim with no added prefix. Tokens already contain authorities such as `ROLE_ADMIN`.
- `security/DwaarPalPrincipal.java`: Java record implementing `UserDetails`, carrying database `id`, phone username, password hash, authorities, and enabled status.
- `security/DwaarPalUserDetailsService.java`: looks up a user by phone and returns `DwaarPalPrincipal`; makes the role a `ROLE_...` authority and disables inactive accounts. There is an old commented-out builder block that can be cleaned up later.
- `service/UserService.java`: registration uses the shared `PasswordEncoder` bean to hash the password.
- `service/LoginService.java`: sends phone/password to `AuthenticationManager`, then asks `JwtService` to create a token and returns `LoginResponse`.
- `service/JwtService.java`: uses the `DwaarPalPrincipal` from successful authentication; JWT `sub` is now the database ID string, `authorities` contains the Spring authorities, issuer is `dwaar-pal`, and access-token lifetime is 900 seconds (15 minutes).
- `controller/AuthController.java`, `dto/LoginRequest.java`, `dto/LoginResponse.java`: `POST /api/auth/login` accepts phone/password and returns Bearer token plus expiry.
- `exception/GlobalExceptionHandler.java`: maps authentication failures to 401 with the generic message `Invalid phone number or password`; maps `ResponseStatusException` to the project `ErrorResponse`. Duplicate phone/email registration is detected in `UserService` and returns conflict.

## Actor-ID Migration Already Completed

The subject changed from phone to numeric database ID. Two admin vehicle endpoints were migrated:

- `GET /api/vehicles/pending?societyId=...`
- `PATCH /api/vehicles/{registrationNumber}/status?societyId=...&updateStatus=...`

Their controller methods receive `@AuthenticationPrincipal Jwt`, parse `jwt.getSubject()` as a `long`, and pass the ID to `VehicleService`. The service loads the actor with `findById` and retains role, active-membership, and target-society checks. Client requests no longer need `userId` for these two routes.

The user said the ID-subject code is working. Earlier Postman checks showed: registration returned 201; login returned 200 with token; a valid token reached a protected vehicle lookup (404 for a nonexistent vehicle); a wrong token returned 401; after restarting with the admin matcher active, a resident token was denied on pending-vehicles. Recheck after the subject migration with a fresh token. Existing tokens have the old phone subject and may fail numeric parsing; local users must log in again. Tokens expire after 15 minutes.

## Next Work: Continue Replacing Client-Supplied Actor IDs

Search controllers/services for `userId` and `adminUserId`. Remaining reviewed areas include:

- `VehicleController`: vehicle entry/exit (`userId`) and `my-vehicles` (`userId`). Vehicle creation uses `membershipId`; its service must also confirm that the selected membership belongs to the authenticated resident, not merely that it is active and resident-owned.
- `MembershipController`: apply, mine, pending list, decision, and leave use `userId`/`adminUserId`.
- `GateController`: gate listing uses `userId` and `societyId`.
- `EntryExitController`: society history uses `userId`.
- Corresponding `VehicleService`, `MembershipService`, `GateService`, and `EntryExitService` methods still accept actor IDs. `EntryExitService` also has a vehicle-history service method with a user ID; confirm whether a controller exposes it.

For each endpoint, use the validated JWT principal and its numeric subject as the actor ID; retain the service's domain checks for role, active membership, ownership, and society scope. Do not treat a role check alone as resource authorization. Remove actor-ID query parameters once the server derives the actor. Handle one coherent endpoint at a time and let the user test it in Postman before proceeding.

## Important Remaining Security Work

1. Add appropriate role rules for the other role-specific routes (guard movement/gates, resident membership/vehicle functions, admin decisions/history). Only pending-vehicle listing and vehicle approval currently have explicit role matchers.
2. Keep resource-level checks: admin/guard membership in the requested society, resident ownership of membership/vehicle, gate belonging to the same society, and active account/membership state.
3. Plan secure creation/provisioning of ADMIN and GUARD accounts; public registration must not accept a client-selected privileged role.
4. JWT filter failures (missing/expired/invalid token) are handled by Spring Security outside MVC advice, so their response body may differ from `ErrorResponse`. Add an `AuthenticationEntryPoint`/`AccessDeniedHandler` if consistent API JSON is desired.
5. Consider issuer validation and ensure JWT HMAC secret is at least 32 random bytes. The environment variable name is `DWAARPAL_JWT_SECRET`; application config uses a placeholder. Never read or echo the secret value.
6. JWT payloads are signed, not encrypted. The subject is now an internal ID rather than a phone number, which reduces exposed personal data.
7. Repeated login attempts/rate limiting, refresh tokens, account lockout, and DB uniqueness-race handling are not implemented.

## Postman Guidance

- Login: `POST {{baseUrl}}/api/auth/login` with the registered phone and password.
- Use a fresh response token in Authorization → Bearer Token. Do not paste tokens into chat/screenshots; a bearer token is a credential.
- Pending list no longer takes `userId`: `GET {{baseUrl}}/api/vehicles/pending?societyId=1`.
- Vehicle approval no longer takes `userId`; retain `societyId` and `updateStatus` query values.
- Valid token on a protected route should proceed to the controller; absent/invalid/expired token should be rejected with 401; authenticated role mismatch should be 403.

## Other Artifact

`spring-security-login-flow.html` was created earlier as a visual walkthrough. It may need updating because it predates the switch from phone-based JWT subjects to database-ID subjects.
