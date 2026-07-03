import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { parseError } from '../api/client';
import AuthLayout from '../components/AuthLayout';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ displayName: '', email: '', password: '' });
  const [fields, setFields] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFields({});
    setBusy(true);
    try {
      await register(form);
      navigate('/', { replace: true });
    } catch (err) {
      const parsed = parseError(err);
      setError(parsed.fields && Object.keys(parsed.fields).length ? '' : parsed.message);
      setFields(parsed.fields || {});
    } finally {
      setBusy(false);
    }
  };

  return (
    <AuthLayout>
      <p className="eyebrow mb-2">Start tracking</p>
      <h1 className="font-display text-3xl font-bold tracking-tight">Create your account</h1>
      <p className="mt-2 text-sm text-slate">One target, three checkpoints, zero excuses.</p>

      <form onSubmit={onSubmit} className="mt-8 space-y-4" noValidate>
        {error && (
          <div className="rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5 text-sm text-danger">
            {error}
          </div>
        )}
        <Field id="displayName" label="Name" value={form.displayName}
          onChange={set('displayName')} error={fields.displayName} autoComplete="name" />
        <Field id="email" label="Email" type="email" value={form.email}
          onChange={set('email')} error={fields.email} autoComplete="email" />
        <Field id="password" label="Password" type="password" value={form.password}
          onChange={set('password')} error={fields.password} autoComplete="new-password"
          hint="At least 8 characters." />
        <button type="submit" className="btn-primary" disabled={busy}>
          {busy ? 'Creating…' : 'Create account'}
        </button>
      </form>

      <p className="mt-6 text-sm text-slate">
        Already have an account?{' '}
        <Link to="/login" className="font-semibold text-cobalt hover:text-cobalt-600">
          Log in
        </Link>
      </p>
    </AuthLayout>
  );
}

function Field({ id, label, error, hint, ...props }) {
  return (
    <div>
      <label htmlFor={id} className="field-label">{label}</label>
      <input id={id} className="input" {...props} />
      {error
        ? <p className="mt-1 text-xs text-danger">{error}</p>
        : hint ? <p className="mt-1 text-xs text-slate">{hint}</p> : null}
    </div>
  );
}
