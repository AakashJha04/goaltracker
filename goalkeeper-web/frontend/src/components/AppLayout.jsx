import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function NavItem({ to, children }) {
  return (
    <NavLink
      to={to}
      end
      className={({ isActive }) =>
        `rounded-lg px-3 py-1.5 text-sm font-medium transition ${
          isActive ? 'bg-cobalt/10 text-cobalt' : 'text-slate hover:bg-canvas hover:text-ink'
        }`
      }
    >
      {children}
    </NavLink>
  );
}

export default function AppLayout({ children }) {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-canvas">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2.5">
              <svg width="22" height="22" viewBox="0 0 22 22" aria-hidden="true">
                <circle cx="11" cy="11" r="9" fill="none" stroke="#2647CE" strokeWidth="1.6" />
                <circle cx="11" cy="11" r="4" fill="none" stroke="#2647CE" strokeWidth="1.6" />
                <circle cx="11" cy="11" r="1.4" fill="#E8A23B" />
              </svg>
              <span className="font-display text-lg font-bold tracking-tight">Goalkeeper</span>
            </div>
            <nav className="hidden items-center gap-1 sm:flex">
              <NavItem to="/">Dashboard</NavItem>
              <NavItem to="/goals">Goals</NavItem>
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <span className="hidden text-sm text-slate sm:inline">{user?.email}</span>
            <button
              onClick={logout}
              className="rounded-lg border border-line px-3 py-1.5 text-sm font-medium text-ink transition hover:bg-canvas"
            >
              Log out
            </button>
          </div>
        </div>
        <nav className="flex items-center gap-1 border-t border-line px-6 py-2 sm:hidden">
          <NavItem to="/">Dashboard</NavItem>
          <NavItem to="/goals">Goals</NavItem>
        </nav>
      </header>

      <main className="mx-auto max-w-5xl px-6 py-12">{children}</main>
    </div>
  );
}
