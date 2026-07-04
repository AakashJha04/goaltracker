import { api } from './client';

export async function fetchTags() {
  const { data } = await api.get('/api/tags');
  return data;
}

export async function createTag(name) {
  const { data } = await api.post('/api/tags', { name });
  return data;
}

export async function deleteTag(id) {
  await api.delete(`/api/tags/${id}`);
}

export async function fetchTagsForGoal(goalId) {
  const { data } = await api.get(`/api/goals/${goalId}/tags`);
  return data;
}

export async function attachTag(goalId, tagId) {
  await api.post(`/api/goals/${goalId}/tags/${tagId}`);
}

export async function detachTag(goalId, tagId) {
  await api.delete(`/api/goals/${goalId}/tags/${tagId}`);
}
