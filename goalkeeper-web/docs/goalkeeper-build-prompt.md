# Build Prompt — Goalkeeper (Phases 2–4)

> Paste everything below the line into Claude Code (in VS Code), with the repo open.
> Work through it phase by phase and let Claude run the app between phases.

---

You are working in an existing full-stack monorepo, `goalkeeper-web/`, with two apps:
`backend/` (Spring Boot 3, Java 17) and `frontend/` (React 18 + Vite + Tailwind).

**Before writing any code, read these to match existing conventions:**
- `README.md` (run instructions, what Phase 1 already delivers)
- `docs/goalkeeper-system-design.md` (the full architecture — data model, API, auth, notifications, scalability)
- `backend/src/main/java/com/aakash/goalkeeper/**` (follow the existing package + service/controller/DTO patterns)
- `frontend/src/**` (reuse the "Trajectory" design system and the existing auth/query patterns)

## What already exists (Phase 1 — do not break it)
- Auth: register / login / refresh (rotating httpOnly cookie) / logout / `GET /auth/me`.
  JWT access tokens, BCrypt, `JwtAuthenticationFilter`, `SecurityConfig` (stateless).
- Postgres via Flyway. Migration `V1__init_auth.sql` created `users` + `refresh_tokens`.
  **Never edit V1** — add new `V2__…`, `V3__…` migrations.
- `common/ApiException` + `GlobalExceptionHandler` produce a consistent error envelope
  (`{timestamp, status, message, fields?}`). Reuse it.
- Frontend: `api/client.js` (axios with silent-refresh interceptor, in-memory access token),
  `auth/AuthContext.jsx`, `auth/ProtectedRoute.jsx`, and the Trajectory tokens in
  `tailwind.config.js` (cobalt/amber/navy, Space Grotesk / Inter / IBM Plex Mono).

## Cross-cutting rules (apply to every phase)
1. **Scope all data to the authenticated user.** Every goal/milestone/reminder/notification
   query filters by the user id from the security context. A user must never read or mutate
   another user's data — verify ownership on every write. Add a test for this.
2. **Migrations only via Flyway** (new versioned files). No `ddl-auto` changes.
3. **DTOs over the wire**, never expose JPA entities directly. Validate inputs with
   `jakarta.validation`. Return the existing error envelope on failure.
4. **Pagination** on list endpoints — prefer keyset/seek pagination (or `Pageable`), never
   unbounded results.
5. **Design:** stay inside the existing Trajectory system. Reuse the tokens and component
   patterns; don't introduce a second visual language. Responsive to mobile, visible
   keyboard focus, reduced-motion respected. Write UI copy from the user's side
   (plain verbs, sentence case, actionable empty/error states).
6. **Keep it runnable.** After each phase the app must start clean (`mvn spring-boot:run`
   + `npm run dev`) with no console errors. Fix, then move on.
7. **Server state on the frontend:** introduce TanStack Query for data fetching/caching;
   keep the existing axios client + auth as-is.
8. **Commit per phase** with clear messages (e.g. `feat(goals): CRUD + list + detail`).
   Run the build before committing.

---

## Phase 2 — Goals, milestones, dashboard

**Backend**
- Migration `V2__goals.sql`: `goals` and `milestones` tables per the system design
  (goals: id UUID, user_id, title, description, category, priority, status, progress int,
  target_date, deleted, created_at, updated_at; milestones: id, goal_id, title, done,
  position). Index `goals(user_id, status, target_date)` and `milestones(goal_id)`.
- Entities + repositories in a `goal/` package, service + controller following the
  `AuthService`/`AuthController` style.
- Endpoints:
  - `GET /api/goals` — filter (status, category, search), sort, paginate.
  - `POST /api/goals`, `GET/PUT/DELETE /api/goals/{id}` (soft-delete).
  - `PATCH /api/goals/{id}/status`, `PATCH /api/goals/{id}/progress`.
  - Milestones: `GET/POST /api/goals/{id}/milestones`, `PUT/DELETE /api/milestones/{id}`,
    reorder support. Checking milestones off should be able to auto-compute goal progress.
  - `GET /api/dashboard/stats` — counts by status, completion rate, upcoming deadlines,
    category breakdown.
- Tests: a service/web test proving cross-user access is blocked, plus goal CRUD happy path.

**Frontend**
- Routes: goals list, goal detail (with milestones), create/edit goal.
- Dashboard: replace the placeholder with real stats + charts (use Recharts) — completion
  rate, status split, upcoming deadlines. Keep the Trajectory look.
- List: filter/sort/search UI, status pills (reuse the palette: cobalt/amber/danger),
  pagination, and an inviting empty state.
- Forms with inline validation surfacing the backend `fields` errors.

## Phase 3 — Reminders & notifications (in-process async first)

Per the system design, start with the in-process path (no Kafka yet), structured so a
Kafka upgrade is a later swap, not a rewrite.

**Backend**
- Migration `V3__reminders_notifications.sql`: `reminders` (id, goal_id, remind_at, channel,
  status) and `notifications` (id, user_id, goal_id?, type, title, body, read, created_at).
  Index `reminders(status, remind_at)` and `notifications(user_id, read, created_at desc)`.
- Reminder endpoints under a goal (enforce a max of 3 per goal): create/list/delete.
- A `@Scheduled` job (every minute) that selects due, unsent reminders using a locked/
  `SKIP LOCKED` query so multiple instances don't double-send, then publishes an
  application event.
- An async listener (`@Async` + `ApplicationEventPublisher`) that: writes a notification
  row, and sends email via `JavaMailSender` — but if no SMTP is configured, log the email
  to the console instead so it runs locally with zero setup. Isolate delivery behind a
  `NotificationChannel` interface so Kafka consumers can replace it later.
- Notification endpoints: `GET /api/notifications` (paginated), `PATCH /api/notifications/{id}/read`,
  `POST /api/notifications/read-all`. Add real-time delivery via SSE or STOMP WebSocket;
  if that's heavy, poll every ~30s as a first pass and leave a clear TODO for SSE.

**Frontend**
- Reminder UI on the goal detail screen (add up to 3 date-times, list, remove).
- A notification bell in the header with an unread count and a dropdown feed; mark-read
  and mark-all-read. Live updates via the SSE/WS stream (or polling fallback).

## Phase 4 — Polish & scale

- Tags: `tags` + `goal_tags` (migration `V4__tags.sql`), attach/detach on goals, filter by tag.
- Per-goal activity log.
- Redis caching for `GET /api/dashboard/stats` with event-based invalidation (keep it
  optional/config-gated so the app still runs without Redis).
- `Dockerfile` for backend + frontend, a root `docker-compose.yml` running Postgres +
  backend + frontend, and a GitHub Actions workflow that builds both on push.
- Actuator + basic request logging.

---

## Definition of done (per phase)
- Backend builds (`mvn -q -DskipTests=false test`) and starts; frontend builds
  (`npm run build`) and runs.
- New endpoints validated, paginated, and ownership-checked.
- UI matches the Trajectory system and is responsive.
- A short note appended to `README.md` describing what the phase added and any new env vars.
- One commit per phase with a descriptive message.

Start with **Phase 2**. Show me your migration and the goal entity/service design first,
then implement once I confirm.
