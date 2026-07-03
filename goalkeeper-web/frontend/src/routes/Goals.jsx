import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import AppLayout from '../components/AppLayout';
import StatusPill from '../components/StatusPill';
import { fetchGoals } from '../api/goals';

const STATUS_FILTERS = [
  { value: '', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'ARCHIVED', label: 'Archived' },
];

function formatDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

export default function Goals() {
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['goals', { status, search, page }],
    queryFn: () => fetchGoals({ status: status || undefined, search: search || undefined, page }),
    placeholderData: (prev) => prev,
  });

  return (
    <AppLayout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="eyebrow mb-2">Goals</p>
          <h1 className="font-display text-3xl font-bold tracking-tight">Everything you're working toward</h1>
        </div>
        <Link to="/goals/new" className="btn-primary w-auto px-5">Add a goal</Link>
      </div>

      <div className="mt-8 flex flex-wrap items-center gap-3">
        <div className="flex gap-1 rounded-xl border border-line bg-surface p-1">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.value}
              onClick={() => { setStatus(f.value); setPage(0); }}
              className={`rounded-lg px-3 py-1.5 text-sm font-medium transition ${
                status === f.value ? 'bg-cobalt text-white' : 'text-slate hover:text-ink'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
        <input
          className="input max-w-xs"
          placeholder="Search goals…"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        />
      </div>

      {isLoading && <p className="mt-10 text-slate">Loading…</p>}
      {isError && <p className="mt-10 text-danger">Couldn't load your goals. Try refreshing.</p>}

      {data && data.items.length === 0 && (
        <div className="mt-10 rounded-xl2 border border-line bg-surface p-10 text-center shadow-card">
          <h2 className="font-display text-xl font-bold">No goals here yet</h2>
          <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-slate">
            {search || status ? 'Nothing matches these filters.' : 'Start with something you want to be true a few months from now.'}
          </p>
          <Link to="/goals/new" className="btn-primary mt-6 inline-flex w-auto px-6">Add your first goal</Link>
        </div>
      )}

      {data && data.items.length > 0 && (
        <ul className="mt-8 space-y-3">
          {data.items.map((goal) => (
            <li key={goal.id}>
              <Link
                to={`/goals/${goal.id}`}
                className="flex items-center justify-between gap-4 rounded-xl2 border border-line bg-surface p-5 shadow-card transition hover:border-cobalt/40"
              >
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="truncate font-display text-base font-bold">{goal.title}</h3>
                    <StatusPill status={goal.status} />
                  </div>
                  <p className="mt-1 text-sm text-slate">
                    {goal.category || 'Uncategorized'}
                    {goal.targetDate && ` · Due ${formatDate(goal.targetDate)}`}
                  </p>
                </div>
                <div className="flex shrink-0 items-center gap-3">
                  <div className="h-1.5 w-24 overflow-hidden rounded-full bg-line">
                    <div className="h-full bg-cobalt" style={{ width: `${goal.progress}%` }} />
                  </div>
                  <span className="font-mono text-sm text-slate">{goal.progress}%</span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {data && data.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-3">
          <button className="btn-secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span className="text-sm text-slate">Page {data.page + 1} of {data.totalPages}</span>
          <button className="btn-secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      )}
    </AppLayout>
  );
}
