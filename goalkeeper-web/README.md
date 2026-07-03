# Goalkeeper — Web App (Phase 1)

Full-stack goal tracker. **Phase 1 delivers the auth foundation, runnable end to end:**
register / log in / stay logged in / log out, backed by PostgreSQL, with a designed
React front end.

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

The dashboard is intentionally a shell — goals, milestones, reminders, and notifications
are Phases 2–3 from the system design.

---

## Run it (two terminals)

### 1. Backend
Prereqs: JDK 17+, Docker (for Postgres), Maven (or the `mvn` on your path).

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

## API (Phase 1)

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/auth/register` | `{email, password, displayName}` | 201, sets refresh cookie, returns access token |
| POST | `/auth/login` | `{email, password}` | sets refresh cookie |
| POST | `/auth/refresh` | — (cookie) | rotates refresh, returns new access token |
| POST | `/auth/logout` | — (cookie) | revokes + clears cookie |
| GET | `/auth/me` | — (Bearer) | current user |

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
Phase 2 — goals + milestones CRUD, list with filter/sort/pagination, dashboard stats.
Phase 3 — reminders + scheduler + in-app/email notifications (in-process async first).
