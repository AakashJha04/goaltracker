import { useAuth } from '../auth/AuthContext';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const firstName = user?.displayName?.split(' ')[0] || 'there';

  return (
    <div className="min-h-screen bg-canvas">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-2.5">
            <svg width="22" height="22" viewBox="0 0 22 22" aria-hidden="true">
              <circle cx="11" cy="11" r="9" fill="none" stroke="#2647CE" strokeWidth="1.6" />
              <circle cx="11" cy="11" r="4" fill="none" stroke="#2647CE" strokeWidth="1.6" />
              <circle cx="11" cy="11" r="1.4" fill="#E8A23B" />
            </svg>
            <span className="font-display text-lg font-bold tracking-tight">Goalkeeper</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="hidden text-sm text-slate sm:inline">{user?.email}</span>
            <button onClick={logout}
              className="rounded-lg border border-line px-3 py-1.5 text-sm font-medium text-ink transition hover:bg-canvas">
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-6 py-12">
        <p className="eyebrow mb-2">Dashboard</p>
        <h1 className="font-display text-3xl font-bold tracking-tight">
          Good to see you, {firstName}.
        </h1>
        <p className="mt-2 text-slate">Your account is set. Here's where your goals will live.</p>

        <div className="mt-10 rounded-xl2 border border-line bg-surface p-10 text-center shadow-card">
          <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-full bg-cobalt/8">
            <svg width="26" height="26" viewBox="0 0 26 26" aria-hidden="true">
              <path d="M4 20 C 10 20, 16 14, 22 5" fill="none" stroke="#2647CE" strokeWidth="2.4" strokeLinecap="round" />
              <circle cx="22" cy="5" r="3" fill="#E8A23B" />
            </svg>
          </div>
          <h2 className="font-display text-xl font-bold">No goals yet</h2>
          <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-slate">
            Goal creation, milestones, and reminders arrive next. For now, the foundation is
            live: your account, secure login, and session are working end to end.
          </p>
          <button
            disabled
            className="mt-6 inline-flex items-center justify-center rounded-xl border border-dashed border-cobalt/40 px-4 py-2.5 text-sm font-semibold text-cobalt/70"
          >
            Add a goal — coming in Phase 2
          </button>
        </div>
      </main>
    </div>
  );
}
