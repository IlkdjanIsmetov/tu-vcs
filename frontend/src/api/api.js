import { getAccessToken, logout } from '../components/auth'

const BACKEND = ''

async function request(path, options = {}) {
  const token = getAccessToken()

  let res
  try {
    res = await fetch(`${BACKEND}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {}),
      },
    })
  } catch {
    return null
  }

  if (res.status === 401) {
    logout()
    return null
  }

  return res
}

export const repositoryApi = {
  getAll: async () => {
    const res = await request('/api/repositories/all')
    if (!res) return null
    if (!res.ok) return []
    return res.json()
  },

  getMy: async () => {
    const res = await request('/api/repositories/my')
    if (!res || !res.ok) return []
    return res.json()
  },

  search: async (q) => {
    const res = await request(`/api/repositories/search?q=${encodeURIComponent(q)}`)
    if (!res || !res.ok) return []
    return res.json()
  },

  create: async (data) => {
    const res = await request('/api/repositories/create', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true, data: await res.json() }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to create repository.' }
  },

  delete: async (id) => {
    const res = await request(`/api/repositories/${id}`, { method: 'DELETE' })
    if (!res) return { success: false }
    return { success: res.ok }
  },

  getMembers: async (id) => {
    const res = await request(`/api/repositories/${id}/allMembers`)
    if (!res || !res.ok) return []
    const data = await res.json()
    return data?.content ?? data ?? []
  },

  update: async (id, data) => {
    const res = await request(`/api/repositories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to update repository.' }
  },

  addMember: async (repoId, username, role) => {
    const res = await request(
        `/api/repositories/${repoId}/addMember?username=${encodeURIComponent(username)}&role=${role}`,
        { method: 'POST' }
    )
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to add member.' }
  },

  kickMember: async (repoId, username) => {
    const res = await request(
        `/api/repositories/${repoId}/kickMember?username=${encodeURIComponent(username)}`,
        { method: 'DELETE' }
    )
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to remove member.' }
  },
}

export const userApi = {
  getAll: async () => {
    const res = await request('/api/users')
    if (!res || !res.ok) return []
    return res.json()
  },
}

export const profileApi = {
  update: async (data) => {
    const res = await request('/api/auth/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to update profile.' }
  },

  changePassword: async () => {
    const res = await request('/api/auth/change-password', { method: 'POST' })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true, data: await res.json().catch(() => ({})) }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to send reset email.' }
  },

  deleteAccount: async () => {
    const res = await request('/api/auth/delete-account', { method: 'DELETE' })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to delete account.' }
  },
}

export const changeRequestApi = {
  getPending: async (repoId) => {
    const res = await request(`/api/repositories/${repoId}/change-request/pending/requests`)
    if (!res || !res.ok) return []
    return res.json()
  },

  getPendingCount: async (repoId) => {
    const res = await request(`/api/repositories/${repoId}/change-request/pending/count`)
    if (!res || !res.ok) return 0
    return res.json()
  },

  approve: async (repoId, crId) => {
    const res = await request(`/api/repositories/${repoId}/change-request/${crId}/approve`, { method: 'POST' })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to approve.' }
  },

  reject: async (repoId, crId) => {
    const res = await request(`/api/repositories/${repoId}/change-request/${crId}/reject`, { method: 'POST' })
    if (!res) return { success: false, error: 'Not authenticated.' }
    if (res.ok) return { success: true }
    const err = await res.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to reject.' }
  },
}

export const historyApi = {
  getHistory: async (repoId) => {
    const res = await request(`/api/repositories/${repoId}/history`)
    if (!res || !res.ok) return []
    return res.json()
  },
}