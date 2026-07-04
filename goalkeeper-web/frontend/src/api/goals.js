import { api } from './client';

export async function fetchGoals({ status, category, search, tag, sort = 'createdAt', dir = 'desc', page = 0, size = 20 } = {}) {
  const { data } = await api.get('/api/goals', {
    params: { status, category, search, tag, sort, dir, page, size },
  });
  return data;
}

export async function fetchGoal(id) {
  const { data } = await api.get(`/api/goals/${id}`);
  return data;
}

export async function createGoal(payload) {
  const { data } = await api.post('/api/goals', payload);
  return data;
}

export async function updateGoal(id, payload) {
  const { data } = await api.put(`/api/goals/${id}`, payload);
  return data;
}

export async function deleteGoal(id) {
  await api.delete(`/api/goals/${id}`);
}

export async function updateGoalStatus(id, status) {
  const { data } = await api.patch(`/api/goals/${id}/status`, { status });
  return data;
}

export async function updateGoalProgress(id, progress) {
  const { data } = await api.patch(`/api/goals/${id}/progress`, { progress });
  return data;
}

export async function fetchMilestones(goalId) {
  const { data } = await api.get(`/api/goals/${goalId}/milestones`);
  return data;
}

export async function createMilestone(goalId, title) {
  const { data } = await api.post(`/api/goals/${goalId}/milestones`, { title });
  return data;
}

export async function updateMilestone(id, payload) {
  const { data } = await api.put(`/api/milestones/${id}`, payload);
  return data;
}

export async function deleteMilestone(id) {
  await api.delete(`/api/milestones/${id}`);
}

export async function reorderMilestones(goalId, orderedIds) {
  await api.patch(`/api/goals/${goalId}/milestones/reorder`, { orderedIds });
}
