# Goalkeeper — Web App (Phase 2)

Full-stack goal tracker. **Phase 1** delivered the auth foundation; **Phase 2** adds
goals, milestones, and a real dashboard.

```
goalkeeper-web/
├── backend/     Spring Boot 3 + Postgres + JWT auth
└── frontend/    React + Vite + Tailwind ("Trajectory" design system)
```

## What works now
- Register (auto-signs you in), log in, log out.
- **JWT access token** (in memory) + **rotating refresh token** in an httpOnly cookie;
  the session survives a page refresh and access tokens auto-renew silently.
- Passwords hashed with BCrypt; refresh tokens stored only as SHA-256 hashes.
- Protected route + auth guard on the front end; polished login/register/dashboard UI.
- **Goals**: create/edit/delete (soft delete), status (active/completed/archived),
  priority, category, target date. List view with status filter, search, pagination.
- **Milestones**: add/toggle/remove per goal; checking them off recomputes the goal's
  progress automatically.
- **Dashboard**: real stats (totals, completion rate), a status-split donut and
  category bar chart (Recharts), and an upcoming-deadlines list.

Reminders and notifications are Phase 3 from the system design.

---

## Run it (two terminals)

### 1. Backend
Prereqs: JDK 17+, Docker (for Postgres), Maven (or the `mvn` on your path).

> **Multiple JDKs installed?** Lombok's annotation processor (used for the entity
> getters/setters) can silently fail to run on very new JDKs it doesn't support yet,
> which surfaces as `cannot find symbol: method getX()` compile errors. Point
> `JAVA_HOME` at a JDK 17–21 install before running Maven, e.g.:
> `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`

```bash
cd backend
docker compose up -d          # Postgres on :5432
mvn spring-boot:run           # API on :8080  (Flyway creates the schema)
```
Check: `curl http://localhost:8080/health` → `{"status":"ok"}`

### 2. Frontend
Prereqs: Node 18+.

```bash
cd frontend
cp .env.example .env           # VITE_API_URL=http://localhost:8080
npm install
npm run dev                    # http://localhost:5173
```

Open http://localhost:5173, create an account, and you'll land on the dashboard.

---

## API (Phase 1 — auth)

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/auth/register` | `{email, password, displayName}` | 201, sets refresh cookie, returns access token |
| POST | `/auth/login` | `{email, password}` | sets refresh cookie |
| POST | `/auth/refresh` | — (cookie) | rotates refresh, returns new access token |
| POST | `/auth/logout` | — (cookie) | revokes + clears cookie |
| GET | `/auth/me` | — (Bearer) | current user |

## API (Phase 2 — goals, milestones, dashboard)

All routes require a Bearer access token and are scoped to the caller.

| Method | Path | Notes |
|---|---|---|
| GET | `/api/goals?status=&category=&search=&sort=&dir=&page=&size=` | paginated |
| POST | `/api/goals` | create |
| GET/PUT/DELETE | `/api/goals/{id}` | delete is a soft-delete |
| PATCH | `/api/goals/{id}/status` | `{status}` |
| PATCH | `/api/goals/{id}/progress` | `{progress}` (0-100) |
| GET/POST | `/api/goals/{id}/milestones` | max 50 per goal |
| PUT/DELETE | `/api/milestones/{id}` | toggling `done` recomputes goal progress |
| PATCH | `/api/goals/{id}/milestones/reorder` | `{orderedIds}` |
| GET | `/api/dashboard/stats` | counts, completion rate, upcoming deadlines, category breakdown |

Run the backend test suite with `mvn -q test` (needs `docker compose up -d` running
first — tests hit the same Postgres as local dev, using freshly registered test users
so they don't collide with your own data).

---

## Design notes (frontend)

The identity is **"Trajectory"** — a goal plotted as an arc rising to a target. Cobalt
structure with a single amber signal accent; Space Grotesk display, Inter body, IBM Plex
Mono for data labels. The auth screens spend all their boldness on the trajectory panel;
everything else stays quiet. Responsive to mobile, visible keyboard focus, reduced-motion
respected.

## Security notes
- Dev cookie is `SameSite=Lax` over http (localhost is same-site across ports). In
  production set `COOKIE_SECURE=true` and, if the API and app are on different domains,
  `SameSite=None`.
- Override `JWT_SECRET`, DB creds, and `FRONTEND_ORIGIN` via env vars in production
  (see `backend/src/main/resources/application.yml`).
- Phase 1 marks new accounts `emailVerified=true` because no SMTP is wired yet; Phase 3
  flips this to a real verification email.

## Next (from the system design)
Phase 3 — reminders + scheduler + in-app/email notifications (in-process async first).
Phase 4 — tags, activity log, Redis caching, Docker/CI polish.
