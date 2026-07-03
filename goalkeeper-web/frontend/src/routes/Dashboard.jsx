import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import AppLayout from '../components/AppLayout';
import { useAuth } from '../auth/AuthContext';
import { fetchDashboardStats } from '../api/dashboard';

const STATUS_COLORS = { Active: '#2647CE', Completed: '#E8A23B', Archived: '#C5453B' };

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

function StatTile({ label, value }) {
  return (
    <div className="rounded-xl2 border border-line bg-surface p-5 shadow-card">
      <p className="eyebrow">{label}</p>
      <p className="font-display mt-1 text-3xl font-bold tracking-tight">{value}</p>
    </div>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const firstName = user?.displayName?.split(' ')[0] || 'there';
  const { data: stats, isLoading, isError } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: fetchDashboardStats,
  });

  const isEmpty = stats && stats.totalGoals === 0;

  const statusData = stats
    ? [
        { name: 'Active', value: stats.activeGoals },
        { name: 'Completed', value: stats.completedGoals },
        { name: 'Archived', value: stats.archivedGoals },
      ].filter((d) => d.value > 0)
    : [];

  const categoryData = stats
    ? Object.entries(stats.categoryBreakdown).map(([category, count]) => ({ category, count }))
    : [];

  return (
    <AppLayout>
      <p className="eyebrow mb-2">Dashboard</p>
      <h1 className="font-display text-3xl font-bold tracking-tight">Good to see you, {firstName}.</h1>
      <p className="mt-2 text-slate">Here's where your goals stand.</p>

      {isLoading && <p className="mt-10 text-slate">Loading…</p>}
      {isError && <p className="mt-10 text-danger">Couldn't load your stats. Try refreshing.</p>}

      {isEmpty && (
        <div className="mt-10 rounded-xl2 border border-line bg-surface p-10 text-center shadow-card">
          <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-full bg-cobalt/8">
            <svg width="26" height="26" viewBox="0 0 26 26" aria-hidden="true">
              <path d="M4 20 C 10 20, 16 14, 22 5" fill="none" stroke="#2647CE" strokeWidth="2.4" strokeLinecap="round" />
              <circle cx="22" cy="5" r="3" fill="#E8A23B" />
            </svg>
          </div>
          <h2 className="font-display text-xl font-bold">No goals yet</h2>
          <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-slate">
            Start with something you want to be true a few months from now.
          </p>
          <Link to="/goals/new" className="btn-primary mt-6 inline-flex w-auto px-6">Add a goal</Link>
        </div>
      )}

      {stats && !isEmpty && (
        <>
          <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
            <StatTile label="Total goals" value={stats.totalGoals} />
            <StatTile label="Active" value={stats.activeGoals} />
            <StatTile label="Completed" value={stats.completedGoals} />
            <StatTile label="Completion rate" value={`${stats.completionRate}%`} />
          </div>

          <div className="mt-6 grid gap-4 lg:grid-cols-2">
            <div className="rounded-xl2 border border-line bg-surface p-6 shadow-card">
              <h2 className="font-display text-lg font-bold">Status split</h2>
              <div className="mt-2 h-56">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={statusData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={80} paddingAngle={2}>
                      {statusData.map((d) => (
                        <Cell key={d.name} fill={STATUS_COLORS[d.name]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="mt-2 flex flex-wrap justify-center gap-4 text-sm">
                {statusData.map((d) => (
                  <span key={d.name} className="flex items-center gap-1.5 text-slate">
                    <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: STATUS_COLORS[d.name] }} />
                    {d.name} ({d.value})
                  </span>
                ))}
              </div>
            </div>

            <div className="rounded-xl2 border border-line bg-surface p-6 shadow-card">
              <h2 className="font-display text-lg font-bold">By category</h2>
              <div className="mt-2 h-56">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={categoryData} layout="vertical" margin={{ left: 8 }}>
                    <XAxis type="number" hide />
                    <YAxis type="category" dataKey="category" width={100} tick={{ fontSize: 12, fill: '#5B6270' }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="#2647CE" radius={[0, 6, 6, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          <div className="mt-6 rounded-xl2 border border-line bg-surface p-6 shadow-card">
            <h2 className="font-display text-lg font-bold">Upcoming deadlines</h2>
            {stats.upcomingDeadlines.length === 0 ? (
              <p className="mt-2 text-sm text-slate">Nothing on the calendar right now.</p>
            ) : (
              <ul className="mt-4 divide-y divide-line">
                {stats.upcomingDeadlines.map((g) => (
                  <li key={g.id}>
                    <Link to={`/goals/${g.id}`} className="flex items-center justify-between py-3 text-sm hover:text-cobalt">
                      <span className="font-medium text-ink">{g.title}</span>
                      <span className="font-mono text-slate">{formatDate(g.targetDate)}</span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </AppLayout>
  );
}
