import { api } from './client';

export async function fetchActivity(goalId, { page = 0, size = 20 } = {}) {
  const { data } = await api.get(`/api/goals/${goalId}/activity`, { params: { page, size } });
  return data;
}
