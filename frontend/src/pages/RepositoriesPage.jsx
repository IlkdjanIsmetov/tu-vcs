import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { repositoryApi } from '../api/api'

export default function RepositoriesPage() {
  const [repos,   setRepos]   = useState([])
  const [loading, setLoading] = useState(true)
  const [search,  setSearch]  = useState('')
  const [error,   setError]   = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [creating,   setCreating]   = useState(false)

  const [form, setForm] = useState({
    name: '', description: '', requiresApprovalByDefault: false,
  })

  const fetchRepos = async () => {
    setLoading(true)
    setError('')
    const data = await repositoryApi.getAll()
    if (data === null) {
      setError('Could not reach the server. Make sure the backend is running.')
    } else {
      setRepos(data)
    }
    setLoading(false)
  }

  useEffect(() => { fetchRepos() }, [])

  const handleSearch = async (e) => {
    const q = e.target.value
    setSearch(q)
    if (q.trim().length === 0) {
      fetchRepos()
    } else {
      const data = await repositoryApi.search(q)
      setRepos(data)
    }
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) return
    setCreating(true)
    setError('')
    const res = await repositoryApi.create({ repositoryName: form.name, description: form.description, requiresApprovalByDefault: form.requiresApprovalByDefault })
    if (res.success) {
      setShowCreate(false)
      setForm({ name: '', description: '', requiresApprovalByDefault: false })
      fetchRepos()
    } else {
      setError(res.error)
    }
    setCreating(false)
  }

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete repository "${name}"? This cannot be undone.`)) return
    await repositoryApi.delete(id)
    fetchRepos()
  }

  const displayed = repos

  return (
      <AppLayout subtitle="Repository management">
        <section className="hero">
          <span className="badge">📦 Repositories management</span>
          <h2>Repositories</h2>
          <p>Create, manage and track all repositories in your workspace.</p>
          <div className="hero-actions">
            <button className="btn primary" onClick={() => setShowCreate(true)}>
              ＋ New repository
            </button>
          </div>
        </section>

        {error && !showCreate && <div className="form-error" style={{ marginBottom: 16 }}>{error}</div>}

        {showCreate && (
            <div className="modal-overlay" onClick={() => setShowCreate(false)}>
              <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                <h3>Create repository</h3>
                {error && <div className="form-error">{error}</div>}
                <form className="form-grid" onSubmit={handleCreate}>
                  <label className="field-label">
                    <span>Name</span>
                    <div className="field">
                      <span>📦</span>
                      <input
                          required
                          placeholder="my-project"
                          value={form.name}
                          onChange={(e) => setForm({ ...form, name: e.target.value })}
                      />
                    </div>
                  </label>
                  <label className="field-label">
                    <span>Description</span>
                    <div className="field">
                      <span>📝</span>
                      <input
                          placeholder="Optional description"
                          value={form.description}
                          onChange={(e) => setForm({ ...form, description: e.target.value })}
                      />
                    </div>
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer' }}>
                    <input
                        type="checkbox"
                        checked={form.requiresApprovalByDefault}
                        onChange={(e) => setForm({ ...form, requiresApprovalByDefault: e.target.checked })}
                    />
                    <span style={{ fontSize: '0.9rem' }}>Require approval for commits</span>
                  </label>
                  <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
                    <button className="btn primary" type="submit" disabled={creating}>
                      {creating ? 'Creating…' : 'Create'}
                    </button>
                    <button className="btn secondary" type="button" onClick={() => setShowCreate(false)}>
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            </div>
        )}

        <section className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
            <h3 style={{ margin: 0 }}>All repositories</h3>
            <div className="field" style={{ flex: 1, maxWidth: 320 }}>
              <span>🔍</span>
              <input
                  placeholder="Search repositories…"
                  value={search}
                  onChange={handleSearch}
              />
            </div>
          </div>

          {loading && <p className="sub">Loading repositories…</p>}
          {!loading && displayed.length === 0 && (
              <p className="sub">No repositories found.</p>
          )}

          {!loading && displayed.length > 0 && (
              <div className="table">
                <div className="table-header">
                  <div>Name</div>
                  <div>Description</div>
                  <div>Revisions</div>
                  <div>Approval</div>
                  <div>Actions</div>
                </div>
                {displayed.map((repo) => (
                    <div className="table-row" key={repo.id}>
                      <div><strong>{repo.name}</strong></div>
                      <div style={{ color: 'var(--muted, #888)', fontSize: '0.85rem' }}>
                        {repo.description || '—'}
                      </div>
                      <div>
                        <span className="pill">Rev. {repo.revision ?? 0}</span>
                      </div>
                      <div>
                  <span className="pill" style={repo.requireApproval
                      ? { background: 'rgba(251,191,36,0.15)', color: '#fbbf24' }
                      : {}}>
                    {repo.requireApproval ? '🔒 Yes' : '✅ No'}
                  </span>
                      </div>
                      <div>
                        <button
                            className="btn secondary"
                            style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                            onClick={() => handleDelete(repo.id, repo.name)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                ))}
              </div>
          )}
        </section>
      </AppLayout>
  )
}
