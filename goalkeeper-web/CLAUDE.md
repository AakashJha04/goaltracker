# CLAUDE.md

Project conventions and guardrails for Claude Code. Read this before making changes.

## What this is
Goalkeeper — a full-stack goal-tracking web app.
- `backend/` — Spring Boot 3, Java 17, PostgreSQL, Flyway, JWT auth.
- `frontend/` — React 18 + Vite + Tailwind ("Trajectory" design system).
- `docs/goalkeeper-system-design.md` — the architecture (data model, API, auth, notifications, scale).
- `docs/goalkeeper-build-prompt.md` — the phased build plan.

Current status: **Phases 1–4 complete** (auth, goals/milestones/dashboard,
reminders/notifications, tags/activity/caching/Docker/CI). See `README.md` for what's
implemented and `## Known follow-ups` there for what's intentionally left open.

## Run commands
Backend:  `cd backend && docker compose up -d && mvn spring-boot:run`   → http://localhost:8080
Frontend: `cd frontend && npm install && npm run dev`                   → http://localhost:5173
Health:   `curl http://localhost:8080/health`
Build check before committing: `cd backend && mvn -q test` and `cd frontend && npm run build`.

## Architecture conventions
- Backend package root: `com.aakash.goalkeeper`. Group by feature (`auth/`, `user/`,
  `goal/`, `notification/`), each with entity / repository / service / controller / `dto/`.
- Follow the existing `AuthService` + `AuthController` + `AuthDtos` pattern for new features.
- DTOs (records) cross the API boundary — never serialize JPA entities directly.
- Reuse `common/ApiException` + `GlobalExceptionHandler`; error envelope is
  `{timestamp, status, message, fields?}`. Don't invent a new error shape.
- Frontend: axios lives in `src/api/client.js` (in-memory access token + silent-refresh
  interceptor — leave it as-is). Auth is in `src/auth/`. Use TanStack Query for server data.

## Hard rules — do not break
1. **Never edit an existing Flyway migration.** Add new `V2__…`, `V3__…` files. Schema is
   Flyway-owned; `ddl-auto` stays `none`.
2. **Scope every query to the authenticated user** (id from the security context). Verify
   ownership on every read and write — one user must never touch another's data. Add a test.
3. **Don't touch the auth/security setup** (`security/`, cookie/JWT config) unless the task
   is explicitly about auth.
4. **Stay in the Trajectory design system** — reuse tokens in `tailwind.config.js`
   (cobalt / amber / navy; Space Grotesk / Inter / IBM Plex Mono). No second visual language.
5. List endpoints are always **paginated** and validated.
6. Keep the app runnable after every change; fix build/console errors before moving on.

## Working style
- For a multi-part task, propose the migration + entity/service design first, then implement
  after I confirm. Work one phase at a time.
- Conventional commits (`feat(goals): …`, `fix(auth): …`). Run the build before committing.
- UI copy is written from the user's side: plain verbs, sentence case, actionable empty and
  error states.
