import { useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import AppLayout from '../components/AppLayout';
import { createGoal, fetchGoal, updateGoal } from '../api/goals';
import { parseError } from '../api/client';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'];

function toDateInputValue(iso) {
  return iso ? iso.slice(0, 10) : '';
}

export default function GoalForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: existing, isLoading } = useQuery({
    queryKey: ['goal', id],
    queryFn: () => fetchGoal(id),
    enabled: isEdit,
  });

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [targetDate, setTargetDate] = useState('');
  const [initialized, setInitialized] = useState(!isEdit);
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');

  if (existing && !initialized) {
    setTitle(existing.title);
    setDescription(existing.description || '');
    setCategory(existing.category || '');
    setPriority(existing.priority);
    setTargetDate(toDateInputValue(existing.targetDate));
    setInitialized(true);
  }

  const mutation = useMutation({
    mutationFn: (payload) => (isEdit ? updateGoal(id, payload) : createGoal(payload)),
    onSuccess: (goal) => {
      queryClient.invalidateQueries({ queryKey: ['goals'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
      navigate(`/goals/${goal.id}`);
    },
    onError: (err) => {
      const parsed = parseError(err);
      setFormError(parsed.message);
      setFieldErrors(parsed.fields);
    },
  });

  const onSubmit = (e) => {
    e.preventDefault();
    setFormError('');
    setFieldErrors({});
    mutation.mutate({
      title,
      description: description || null,
      category: category || null,
      priority,
      targetDate: targetDate ? new Date(targetDate + 'T00:00:00Z').toISOString() : null,
    });
  };

  if (isEdit && isLoading) {
    return (
      <AppLayout>
        <p className="text-slate">Loading…</p>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <Link to={isEdit ? `/goals/${id}` : '/goals'} className="text-sm font-medium text-cobalt hover:text-cobalt-600">
        ← Back
      </Link>
      <h1 className="font-display mt-3 text-2xl font-bold tracking-tight">
        {isEdit ? 'Edit goal' : 'New goal'}
      </h1>

      <form onSubmit={onSubmit} className="mt-8 max-w-xl space-y-4" noValidate>
        {formError && (
          <div className="rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5 text-sm text-danger">
            {formError}
          </div>
        )}
        <div>
          <label htmlFor="title" className="field-label">What are you working toward?</label>
          <input id="title" className="input" value={title} onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Run a marathon" required />
          {fieldErrors.title && <p className="mt-1 text-sm text-danger">{fieldErrors.title}</p>}
        </div>
        <div>
          <label htmlFor="description" className="field-label">Description</label>
          <textarea id="description" className="input" rows={3} value={description}
            onChange={(e) => setDescription(e.target.value)} placeholder="Optional details" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="category" className="field-label">Category</label>
            <input id="category" className="input" value={category} onChange={(e) => setCategory(e.target.value)}
              placeholder="e.g. Fitness" />
          </div>
          <div>
            <label htmlFor="priority" className="field-label">Priority</label>
            <select id="priority" className="input" value={priority} onChange={(e) => setPriority(e.target.value)}>
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>
        </div>
        <div>
          <label htmlFor="targetDate" className="field-label">Target date</label>
          <input id="targetDate" type="date" className="input" value={targetDate}
            onChange={(e) => setTargetDate(e.target.value)} />
        </div>
        <div className="flex gap-3 pt-2">
          <button type="submit" className="btn-primary w-auto px-6" disabled={mutation.isPending}>
            {mutation.isPending ? 'Saving…' : isEdit ? 'Save changes' : 'Create goal'}
          </button>
          <Link to={isEdit ? `/goals/${id}` : '/goals'} className="btn-secondary">Cancel</Link>
        </div>
      </form>
    </AppLayout>
  );
}
