import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { repositoryApi } from '../api/api'

const ROLE_COLORS = {
  MASTER:      { background: 'rgba(124,92,255,0.15)', color: '#a78bfa' },
  CONTRIBUTOR: { background: 'rgba(34,211,238,0.12)', color: '#22d3ee' },
  VIEWER:      { background: 'rgba(156,163,175,0.12)', color: '#9ca3af' },
}

function MembersPanel({ repoId }) {
  const [members, setMembers] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    repositoryApi.getMembers(repoId).then((data) => {
      setMembers(data)
      setLoading(false)
    })
  }, [repoId])

  if (loading) return <p className="sub" style={{ margin: '12px 0 0' }}>Loading members…</p>
  if (!members || members.length === 0)
    return <p className="sub" style={{ margin: '12px 0 0' }}>No members found.</p>

  return (
      <div style={{ marginTop: 14, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {members.map((m) => (
            <div key={m.id} style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '6px 12px', borderRadius: 12,
              background: 'rgba(255,255,255,0.04)', border: '1px solid var(--line)',
            }}>
              <div style={{
                width: 28, height: 28, borderRadius: 10, display: 'grid', placeItems: 'center',
                background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                fontSize: '0.7rem', fontWeight: 700,
              }}>
                {m.user.slice(0, 2).toUpperCase()}
              </div>
              <span style={{ fontSize: '0.85rem' }}>{m.user}</span>
              <span className="pill" style={{ ...(ROLE_COLORS[m.role] || {}), fontSize: '0.72rem', padding: '3px 8px' }}>
            {m.role}
          </span>
            </div>
        ))}
      </div>
  )
}

function EditModal({ repo, onClose, onSaved }) {
  const [form, setForm] = useState({
    description: repo.description || '',
    requiresApprovalByDefault: repo.requireApproval,
  })
  const [saving, setSaving] = useState(false)
  const [error,  setError]  = useState('')

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    const res = await repositoryApi.update(repo.id, form)
    if (res.success) {
      onSaved()
    } else {
      setError(res.error)
      setSaving(false)
    }
  }

  return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-card" onClick={(e) => e.stopPropagation()}>
          <h3>Edit — {repo.name}</h3>
          {error && <div className="form-error" style={{ marginBottom: 14 }}>{error}</div>}
          <form className="form-grid" onSubmit={handleSave}>
            <label className="field-label">
              <span>Description</span>
              <div className="field">
                <span>📝</span>
                <input
                    placeholder="Optional description"
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    autoFocus
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
              <button className="btn primary" type="submit" disabled={saving}>
                {saving ? 'Saving…' : 'Save changes'}
              </button>
              <button className="btn secondary" type="button" onClick={onClose}>Cancel</button>
            </div>
          </form>
        </div>
      </div>
  )
}

function DeleteConfirm({ repoName, onConfirm, onCancel, deleting }) {
  return (
      <div style={{
        marginTop: 10, padding: '14px', borderRadius: 14,
        background: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
      }}>
        <p style={{ margin: '0 0 12px', fontSize: '0.85rem', color: '#ef4444' }}>
          🚨 Delete <strong>{repoName}</strong>? This cannot be undone. All commits, members and data will be lost.
        </p>
        <div style={{ display: 'flex', gap: 8 }}>
          <button
              className="btn"
              style={{ padding: '6px 14px', fontSize: '0.82rem', background: '#ef4444', color: '#fff' }}
              onClick={onConfirm}
              disabled={deleting}
          >
            {deleting ? 'Deleting…' : 'Yes, delete'}
          </button>
          <button className="btn secondary" style={{ padding: '6px 14px', fontSize: '0.82rem' }} onClick={onCancel}>
            Cancel
          </button>
        </div>
      </div>
  )
}

export default function RepositoriesPage() {
  const navigate = useNavigate()
  const [repos,   setRepos]   = useState([])
  const [loading, setLoading] = useState(true)
  const [search,  setSearch]  = useState('')
  const [error,   setError]   = useState('')
  const [expanded, setExpanded] = useState(null)
  const [editRepo, setEditRepo] = useState(null)
  const [deleteId, setDeleteId] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const fetchRepos = async () => {
    setLoading(true)
    setError('')
    const data = await repositoryApi.getMy()
    if (data === null) {
      setError('Could not reach the server. Make sure the backend is running.')
    } else {
      setRepos(data)
    }
    setLoading(false)
  }

  useEffect(() => { fetchRepos() }, [])

  const handleDelete = async (id) => {
    setDeleting(true)
    await repositoryApi.delete(id)
    setDeleteId(null)
    setDeleting(false)
    fetchRepos()
  }

  const toggleExpand = (id) => setExpanded(prev => prev === id ? null : id)

  const displayed = repos.filter((r) =>
      r.name.toLowerCase().includes(search.toLowerCase()) ||
      (r.description || '').toLowerCase().includes(search.toLowerCase())
  )

  return (
      <AppLayout subtitle="Repository management">
        <section className="hero">
          <span className="badge">📦 Repository management</span>
          <h2>Repositories</h2>
          <p>All repositories you own or collaborate on. Create new repository or edit exciting one.</p>
        </section>

        {error && <div className="form-error" style={{ marginBottom: 16 }}>{error}</div>}

        {editRepo && (
            <EditModal
                repo={editRepo}
                onClose={() => setEditRepo(null)}
                onSaved={() => { setEditRepo(null); fetchRepos() }}
            />
        )}

        <section className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
            <h3 style={{ margin: 0 }}>My repositories</h3>
            <div className="field" style={{ flex: 1, maxWidth: 320 }}>
              <span>🔍</span>
              <input
                  placeholder="Search by name or description…"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button
                className="btn primary"
                style={{ whiteSpace: 'nowrap' }}
                onClick={() => navigate('/repositories/new')}
            >
              ＋ New
            </button>
          </div>

          {loading && <p className="sub">Loading repositories…</p>}
          {!loading && displayed.length === 0 && (
              <p className="sub">
                {search
                    ? 'No repositories match your search.'
                    : 'No repositories yet. Create one using the sidebar or the button above.'}
              </p>
          )}

          {!loading && displayed.length > 0 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {displayed.map((repo) => (
                    <div key={repo.id} className="repo">

                      {/* Main row */}
                      <div className="row-between">
                        <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start', flex: 1, minWidth: 0 }}>
                          <div style={{
                            width: 40, height: 40, borderRadius: 14, display: 'grid', placeItems: 'center',
                            background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                            border: '1px solid var(--line)', flexShrink: 0, fontSize: '1.1rem',
                          }}>📦</div>
                          <div style={{ minWidth: 0 }}>
                            <strong style={{ display: 'block', fontSize: '0.95rem', marginBottom: 4 }}>
                              {repo.name}
                            </strong>
                            <small style={{ color: 'var(--muted)', lineHeight: 1.5 }}>
                              {repo.description || <em>No description</em>}
                            </small>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 8 }}>
                              <span className="pill">📌 Rev. {repo.revision ?? 0}</span>
                              <span className="pill" style={repo.requireApproval
                                  ? { background: 'rgba(251,191,36,0.15)', color: '#fbbf24' }
                                  : { background: 'rgba(34,197,94,0.12)', color: '#22c55e' }}>
                          {repo.requireApproval ? '🔒 Approval required' : '✅ Direct commits'}
                        </span>
                            </div>
                          </div>
                        </div>

                        <div style={{ display: 'flex', gap: 8, flexShrink: 0, alignItems: 'center', marginLeft: 12 }}>
                          <button
                              className="btn secondary"
                              style={{ padding: '5px 12px', fontSize: '0.8rem' }}
                              onClick={() => { setDeleteId(null); toggleExpand(repo.id) }}
                          >
                            {expanded === repo.id ? '▲' : '▼'} Members
                          </button>
                          <button
                              className="btn secondary"
                              style={{ padding: '5px 12px', fontSize: '0.8rem' }}
                              onClick={() => { setDeleteId(null); setEditRepo(repo) }}
                          >
                            ✏️ Edit
                          </button>
                          <button
                              className="btn secondary"
                              style={{
                                padding: '5px 12px', fontSize: '0.8rem',
                                color: '#ef4444', borderColor: 'rgba(239,68,68,0.3)',
                              }}
                              onClick={() => setDeleteId(prev => prev === repo.id ? null : repo.id)}
                          >
                            🗑️
                          </button>
                        </div>
                      </div>

                      {expanded === repo.id && (
                          <MembersPanel repoId={repo.id} />
                      )}

                      {deleteId === repo.id && (
                          <DeleteConfirm
                              repoName={repo.name}
                              onConfirm={() => handleDelete(repo.id)}
                              onCancel={() => setDeleteId(null)}
                              deleting={deleting}
                          />
                      )}
                    </div>
                ))}
              </div>
          )}
        </section>
      </AppLayout>
  )
}