import { api } from './client';

export async function fetchNotifications({ page = 0, size = 20 } = {}) {
  const { data } = await api.get('/api/notifications', { params: { page, size } });
  return data;
}

export async function fetchUnreadCount() {
  const { data } = await api.get('/api/notifications/unread-count');
  return data.count;
}

export async function markNotificationRead(id) {
  const { data } = await api.patch(`/api/notifications/${id}/read`);
  return data;
}

export async function markAllNotificationsRead() {
  await api.post('/api/notifications/read-all');
}

export async function fetchReminders(goalId) {
  const { data } = await api.get(`/api/goals/${goalId}/reminders`);
  return data;
}

export async function createReminder(goalId, remindAt, channel = 'IN_APP') {
  const { data } = await api.post(`/api/goals/${goalId}/reminders`, { remindAt, channel });
  return data;
}

export async function deleteReminder(goalId, reminderId) {
  await api.delete(`/api/goals/${goalId}/reminders/${reminderId}`);
}
