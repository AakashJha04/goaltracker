import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  fetchNotifications,
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
} from '../api/notifications';

// Polling, not a live socket — a deliberate first pass (see docs/goalkeeper-build-prompt.md
// Phase 3). A native EventSource can't attach the in-memory bearer token without putting
// it in the URL, so real-time delivery is a TODO for a cookie-authed SSE/WS upgrade.
const POLL_MS = 30_000;

function timeAgo(iso) {
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 60) return 'just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();

  const unreadQuery = useQuery({
    queryKey: ['notifications-unread-count'],
    queryFn: fetchUnreadCount,
    refetchInterval: POLL_MS,
  });

  const feedQuery = useQuery({
    queryKey: ['notifications-feed'],
    queryFn: () => fetchNotifications({ size: 10 }),
    enabled: open,
    refetchInterval: open ? POLL_MS : false,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['notifications-unread-count'] });
    queryClient.invalidateQueries({ queryKey: ['notifications-feed'] });
  };

  const markRead = useMutation({ mutationFn: markNotificationRead, onSuccess: invalidate });
  const markAll = useMutation({ mutationFn: markAllNotificationsRead, onSuccess: invalidate });

  const unread = unreadQuery.data || 0;

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="relative rounded-lg p-2 text-slate transition hover:bg-canvas hover:text-ink"
        aria-label={`Notifications${unread > 0 ? ` (${unread} unread)` : ''}`}
      >
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M10 2a5 5 0 0 0-5 5v2.5c0 .8-.3 1.6-.9 2.2L3 13h14l-1.1-1.3a3 3 0 0 1-.9-2.2V7a5 5 0 0 0-5-5Z"
            stroke="currentColor" strokeWidth="1.4" strokeLinejoin="round" />
          <path d="M8 16a2 2 0 0 0 4 0" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
        </svg>
        {unread > 0 && (
          <span className="absolute right-0.5 top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold text-white">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <>
          <button className="fixed inset-0 z-10 cursor-default" aria-hidden="true" onClick={() => setOpen(false)} />
          <div className="absolute right-0 z-20 mt-2 w-80 rounded-xl2 border border-line bg-surface shadow-card">
            <div className="flex items-center justify-between border-b border-line px-4 py-3">
              <h3 className="font-display text-sm font-bold">Notifications</h3>
              {unread > 0 && (
                <button onClick={() => markAll.mutate()} className="text-xs font-medium text-cobalt hover:text-cobalt-600">
                  Mark all read
                </button>
              )}
            </div>
            <div className="max-h-80 overflow-y-auto">
              {feedQuery.isLoading && <p className="p-4 text-sm text-slate">Loading…</p>}
              {feedQuery.data && feedQuery.data.items.length === 0 && (
                <p className="p-4 text-sm text-slate">You're all caught up.</p>
              )}
              {feedQuery.data?.items.map((n) => (
                <Link
                  key={n.id}
                  to={n.goalId ? `/goals/${n.goalId}` : '#'}
                  onClick={() => { if (!n.read) markRead.mutate(n.id); setOpen(false); }}
                  className={`block border-b border-line px-4 py-3 text-sm transition last:border-0 hover:bg-canvas ${
                    n.read ? '' : 'bg-cobalt/5'
                  }`}
                >
                  <div className="flex items-start gap-2">
                    {!n.read && <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-cobalt" />}
                    <div className="min-w-0">
                      <p className="font-medium text-ink">{n.title}</p>
                      {n.body && <p className="mt-0.5 truncate text-slate">{n.body}</p>}
                      <p className="mt-1 font-mono text-xs text-slate">{timeAgo(n.createdAt)}</p>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
