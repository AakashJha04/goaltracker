import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { parseError } from '../api/client';
import AuthLayout from '../components/AuthLayout';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err) {
      setError(parseError(err).message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <AuthLayout>
      <p className="eyebrow mb-2">Welcome back</p>
      <h1 className="font-display text-3xl font-bold tracking-tight">Log in</h1>
      <p className="mt-2 text-sm text-slate">Pick up where your goals left off.</p>

      <form onSubmit={onSubmit} className="mt-8 space-y-4" noValidate>
        {error && (
          <div className="rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5 text-sm text-danger">
            {error}
          </div>
        )}
        <div>
          <label htmlFor="email" className="field-label">Email</label>
          <input id="email" type="email" autoComplete="email" className="input"
            value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="password" className="field-label">Password</label>
          <input id="password" type="password" autoComplete="current-password" className="input"
            value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <button type="submit" className="btn-primary" disabled={busy}>
          {busy ? 'Logging in…' : 'Log in'}
        </button>
      </form>

      <p className="mt-6 text-sm text-slate">
        New here?{' '}
        <Link to="/register" className="font-semibold text-cobalt hover:text-cobalt-600">
          Create an account
        </Link>
      </p>
    </AuthLayout>
  );
}
