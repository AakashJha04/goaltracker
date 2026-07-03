import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import AppLayout from '../components/AppLayout';
import StatusPill from '../components/StatusPill';
import { parseError } from '../api/client';
import {
  createMilestone,
  deleteGoal,
  deleteMilestone,
  fetchGoal,
  fetchMilestones,
  updateGoalStatus,
  updateMilestone,
} from '../api/goals';
import { createReminder, deleteReminder, fetchReminders } from '../api/notifications';

const MAX_REMINDERS = 3;

function formatDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(iso) {
  return new Date(iso).toLocaleString(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' });
}

export default function GoalDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [newMilestone, setNewMilestone] = useState('');
  const [newReminderAt, setNewReminderAt] = useState('');
  const [error, setError] = useState('');

  const goalQuery = useQuery({ queryKey: ['goal', id], queryFn: () => fetchGoal(id) });
  const milestonesQuery = useQuery({ queryKey: ['milestones', id], queryFn: () => fetchMilestones(id) });
  const remindersQuery = useQuery({ queryKey: ['reminders', id], queryFn: () => fetchReminders(id) });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['goal', id] });
    queryClient.invalidateQueries({ queryKey: ['milestones', id] });
    queryClient.invalidateQueries({ queryKey: ['reminders', id] });
    queryClient.invalidateQueries({ queryKey: ['goals'] });
    queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
  };

  const addReminder = useMutation({
    mutationFn: (remindAt) => createReminder(id, remindAt),
    onSuccess: () => { setNewReminderAt(''); invalidate(); },
    onError: (err) => setError(parseError(err).message),
  });

  const removeReminder = useMutation({
    mutationFn: (reminderId) => deleteReminder(id, reminderId),
    onSuccess: invalidate,
    onError: (err) => setError(parseError(err).message),
  });

  const addMilestone = useMutation({
    mutationFn: (title) => createMilestone(id, title),
    onSuccess: () => { setNewMilestone(''); invalidate(); },
    onError: (err) => setError(parseError(err).message),
  });

  const toggleMilestone = useMutation({
    mutationFn: (m) => updateMilestone(m.id, { title: m.title, done: !m.done }),
    onSuccess: invalidate,
    onError: (err) => setError(parseError(err).message),
  });

  const removeMilestone = useMutation({
    mutationFn: (milestoneId) => deleteMilestone(milestoneId),
    onSuccess: invalidate,
    onError: (err) => setError(parseError(err).message),
  });

  const changeStatus = useMutation({
    mutationFn: (status) => updateGoalStatus(id, status),
    onSuccess: invalidate,
    onError: (err) => setError(parseError(err).message),
  });

  const removeGoal = useMutation({
    mutationFn: () => deleteGoal(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
      navigate('/goals');
    },
    onError: (err) => setError(parseError(err).message),
  });

  if (goalQuery.isLoading) {
    return <AppLayout><p className="text-slate">Loading…</p></AppLayout>;
  }
  if (goalQuery.isError) {
    return <AppLayout><p className="text-danger">This goal couldn't be found.</p></AppLayout>;
  }

  const goal = goalQuery.data;
  const milestones = milestonesQuery.data || [];
  const reminders = remindersQuery.data || [];

  return (
    <AppLayout>
      <Link to="/goals" className="text-sm font-medium text-cobalt hover:text-cobalt-600">← Back to goals</Link>

      {error && (
        <div className="mt-4 rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5 text-sm text-danger">
          {error}
        </div>
      )}

      <div className="mt-4 flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="font-display text-3xl font-bold tracking-tight">{goal.title}</h1>
            <StatusPill status={goal.status} />
          </div>
          <p className="mt-2 text-sm text-slate">
            {goal.category || 'Uncategorized'} · {goal.priority.charAt(0) + goal.priority.slice(1).toLowerCase()} priority
            {goal.targetDate && ` · Due ${formatDate(goal.targetDate)}`}
          </p>
          {goal.description && <p className="mt-3 max-w-xl text-sm leading-relaxed text-slate">{goal.description}</p>}
        </div>
        <div className="flex shrink-0 gap-2">
          <Link to={`/goals/${id}/edit`} className="btn-secondary">Edit</Link>
          {goal.status === 'ACTIVE' && (
            <button className="btn-secondary" onClick={() => changeStatus.mutate('ARCHIVED')}>Archive</button>
          )}
          {goal.status === 'ARCHIVED' && (
            <button className="btn-secondary" onClick={() => changeStatus.mutate('ACTIVE')}>Reactivate</button>
          )}
          <button
            className="rounded-xl border border-danger/30 px-4 py-2.5 text-[15px] font-semibold text-danger transition hover:bg-danger/5"
            onClick={() => { if (confirm('Delete this goal? This can\'t be undone.')) removeGoal.mutate(); }}
          >
            Delete
          </button>
        </div>
      </div>

      <div className="mt-6 flex items-center gap-3">
        <div className="h-2 flex-1 overflow-hidden rounded-full bg-line">
          <div className="h-full bg-cobalt transition-all" style={{ width: `${goal.progress}%` }} />
        </div>
        <span className="font-mono text-sm font-semibold text-ink">{goal.progress}%</span>
      </div>

      <div className="mt-10 rounded-xl2 border border-line bg-surface p-6 shadow-card">
        <h2 className="font-display text-lg font-bold">Milestones</h2>
        <p className="mt-1 text-sm text-slate">Checking these off updates your progress automatically.</p>

        <ul className="mt-5 space-y-2">
          {milestones.map((m) => (
            <li key={m.id} className="flex items-center gap-3 rounded-lg border border-line px-3.5 py-2.5">
              <input
                type="checkbox"
                checked={m.done}
                onChange={() => toggleMilestone.mutate(m)}
                className="h-4 w-4 rounded border-line text-cobalt focus:ring-cobalt"
              />
              <span className={`flex-1 text-sm ${m.done ? 'text-slate line-through' : 'text-ink'}`}>{m.title}</span>
              <button
                onClick={() => removeMilestone.mutate(m.id)}
                className="text-xs font-medium text-slate hover:text-danger"
                aria-label={`Remove ${m.title}`}
              >
                Remove
              </button>
            </li>
          ))}
          {milestones.length === 0 && (
            <li className="rounded-lg border border-dashed border-line px-3.5 py-3 text-sm text-slate">
              No milestones yet — break this goal into steps.
            </li>
          )}
        </ul>

        <form
          className="mt-4 flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (newMilestone.trim()) addMilestone.mutate(newMilestone.trim());
          }}
        >
          <input
            className="input"
            placeholder="Add a milestone"
            value={newMilestone}
            onChange={(e) => setNewMilestone(e.target.value)}
          />
          <button type="submit" className="btn-secondary shrink-0" disabled={addMilestone.isPending}>
            Add
          </button>
        </form>
      </div>

      <div className="mt-6 rounded-xl2 border border-line bg-surface p-6 shadow-card">
        <h2 className="font-display text-lg font-bold">Reminders</h2>
        <p className="mt-1 text-sm text-slate">Up to {MAX_REMINDERS} per goal. You'll get an in-app notification when one's due.</p>

        <ul className="mt-5 space-y-2">
          {reminders.map((r) => (
            <li key={r.id} className="flex items-center gap-3 rounded-lg border border-line px-3.5 py-2.5">
              <span className="flex-1 font-mono text-sm text-ink">{formatDateTime(r.remindAt)}</span>
              <span className={`pill ${r.status === 'SENT' ? 'bg-line text-slate' : 'bg-cobalt/10 text-cobalt'}`}>
                {r.status === 'SENT' ? 'Sent' : 'Pending'}
              </span>
              <button
                onClick={() => removeReminder.mutate(r.id)}
                className="text-xs font-medium text-slate hover:text-danger"
                aria-label={`Remove reminder for ${formatDateTime(r.remindAt)}`}
              >
                Remove
              </button>
            </li>
          ))}
          {reminders.length === 0 && (
            <li className="rounded-lg border border-dashed border-line px-3.5 py-3 text-sm text-slate">
              No reminders set.
            </li>
          )}
        </ul>

        {reminders.length < MAX_REMINDERS && (
          <form
            className="mt-4 flex gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              if (newReminderAt) addReminder.mutate(new Date(newReminderAt).toISOString());
            }}
          >
            <input
              type="datetime-local"
              className="input"
              value={newReminderAt}
              onChange={(e) => setNewReminderAt(e.target.value)}
            />
            <button type="submit" className="btn-secondary shrink-0" disabled={addReminder.isPending || !newReminderAt}>
              Add
            </button>
          </form>
        )}
      </div>
    </AppLayout>
  );
}
