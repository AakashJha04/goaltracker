import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL,
  withCredentials: true, // send/receive the httpOnly refresh cookie
});

// Access token lives in memory only (not localStorage) to reduce XSS exposure.
let accessToken = null;
export const setAccessToken = (t) => { accessToken = t; };
export const getAccessToken = () => accessToken;

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

// On a 401, try one silent refresh then replay the request.
let refreshInFlight = null;
const AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh'];

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const config = error.config || {};
    const status = error.response?.status;
    const isAuthCall = AUTH_PATHS.some((p) => (config.url || '').includes(p));

    if (status === 401 && !config._retry && !isAuthCall) {
      config._retry = true;
      try {
        if (!refreshInFlight) {
          refreshInFlight = api.post('/auth/refresh').finally(() => { refreshInFlight = null; });
        }
        const { data } = await refreshInFlight;
        setAccessToken(data.accessToken);
        config.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(config);
      } catch (e) {
        setAccessToken(null);
      }
    }
    return Promise.reject(error);
  }
);

// Normalize backend error bodies into a friendly message + field map.
export function parseError(error) {
  const data = error.response?.data;
  return {
    message: data?.message || 'Something went wrong. Please try again.',
    fields: data?.fields || {},
  };
}

// ----- auth calls -----

export async function register(payload) {
  const { data } = await api.post('/auth/register', payload);
  setAccessToken(data.accessToken);
  return data.user;
}

export async function login(payload) {
  const { data } = await api.post('/auth/login', payload);
  setAccessToken(data.accessToken);
  return data.user;
}

export async function refreshSession() {
  const { data } = await api.post('/auth/refresh');
  setAccessToken(data.accessToken);
  return data.user;
}

export async function logout() {
  try { await api.post('/auth/logout'); } finally { setAccessToken(null); }
}
