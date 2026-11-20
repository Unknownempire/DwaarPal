# DwaarPal Project Instructions

## Working with the user

- The user is learning backend engineering and wants a senior-engineer mentor. Work in small steps and explain what each component does, where it is used, who calls it, and why it exists.
- The user usually makes code edits themselves and asks for read-only review. Inspect the current code before reporting that a change is complete. Do not implement changes unless the user asks.
- Be candid and specific. Point out bugs, unsafe behavior, incomplete workflows, and architectural tradeoffs plainly, with code evidence and impact.
- Do not ask for or repeat secrets, passwords, or JWTs. Never place actual secret values in source files or documentation.

## API completion and Postman workflow

- Work on one API at a time. When its implementation is complete, test it in Postman before moving on.
- If a Postman check reveals a bug, report the observed behavior and relevant evidence to the user. Work through a fix with the user, then repeat the relevant Postman checks until they pass.
- Do not treat an API as complete until this test, bug-fix, and retest loop is complete.
- The user runs Postman and automated tests unless they explicitly ask otherwise. Do not run automated tests or implementation commands unless asked. Postman request guidance and reviewing reported results are welcome.

## Subagent reviews

When subagents are requested or useful, use two focused reviewers. Spawn both using the project custom agent named `config-agent` from `.codex/agents/config-agent.toml`; give each a separate bounded prompt for its review focus:

1. **Flow reviewer:** Trace the relevant user or API workflows end to end. Find bugs, broken or missing steps, inconsistent state transitions, authorization weaknesses, and confusing behavior.
2. **Test-gap reviewer:** Identify likely bugs and untested business rules or edge cases. Recommend the highest-value tests and explain what failures they would catch.

Ask reviewers to be brutally honest, evidence-based, and actionable. They should cite files and line numbers where possible, explain severity and user impact, and prioritize findings. Reviews are read-only unless the user explicitly asks a subagent to implement a clearly scoped change. Reconcile reviewer findings, check the evidence against the current code, and present actionable conclusions to the user.

## Project context

- DwaarPal is a Spring Boot REST API for residential society membership, vehicle approval, gates, and entry/exit records. The current repository is a backend API intended to support an API demonstration.
- Stack: Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, PostgreSQL, Flyway, and Maven Wrapper.
- Current architecture is layered: controllers call services, which use repositories to access PostgreSQL. DTOs define API inputs and outputs; entities represent persisted data.
- Current users have global roles (`ADMIN`, `GUARD`, `RESIDENT`); memberships link users to societies and may include a flat. Check both role and society/resource authorization when reviewing protected actions.
- Public registration creates resident accounts. Recheck the current code and migrations because this context may become stale.

## Review standards

- Distinguish confirmed bugs from risks or design questions. Do not present an unverified suspicion as a reproduced defect.
- For bug reports, state the triggering conditions, actual or expected result, impact, and evidence. For test recommendations, state the rule, scenario, and failure the test should detect.
- Prefer focused, incremental changes that keep business rules clear. Do not recommend microservices or extra abstractions without a concrete need and tradeoff.
