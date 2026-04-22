import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { repositoryApi } from '../api/api'

export default function NewRepositoryPage() {
    const navigate = useNavigate()
    const [saving, setSaving] = useState(false)
    const [error,  setError]  = useState('')

    const [form, setForm] = useState({
        name: '',
        description: '',
        requiresApprovalByDefault: false,
    })

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!form.name.trim()) return
        setSaving(true)
        setError('')
        const res = await repositoryApi.create({
            repositoryName: form.name,
            description: form.description,
            requiresApprovalByDefault: form.requiresApprovalByDefault,
        })
        if (res.success) {
            navigate('/repositories')
        } else {
            setError(res.error)
            setSaving(false)
        }
    }

    const nameValid = /^[a-z0-9-]+$/.test(form.name) || form.name === ''
    const canSubmit = form.name.trim() && nameValid && !saving

    return (
        <AppLayout subtitle="Repository management">
            <section className="hero">
                <span className="badge">📦 Repository management</span>
                <h2>New repository</h2>
                <p>Set up a new repository for your project. You can add members and configure settings after creation.</p>
            </section>

            <section className="grid-2" style={{ alignItems: 'start' }}>

                {/* ── Left: Form ── */}
                <div className="card">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
                        <div style={{
                            width: 44, height: 44, borderRadius: 14, display: 'grid', placeItems: 'center',
                            background: 'linear-gradient(135deg,rgba(124,92,255,.28),rgba(34,211,238,.18))',
                            border: '1px solid rgba(124,92,255,.3)', fontSize: '1.2rem', flexShrink: 0,
                        }}>📦</div>
                        <div>
                            <h3 style={{ margin: 0, fontSize: '1rem' }}>Repository details</h3>
                            <small style={{ color: 'var(--muted)' }}>Fill in the fields below to get started</small>
                        </div>
                    </div>

                    {error && <div className="form-error" style={{ marginBottom: 16 }}>{error}</div>}

                    <form className="form-grid" onSubmit={handleSubmit}>

                        {/* Name */}
                        <label className="field-label">
                            <div style={{ display: 'flex', alignItems: 'center', gap: 4, margin: '0 0 8px 4px' }}>
                                <span style={{ color: 'var(--muted)', fontSize: '0.84rem' }}>Repository name</span>
                                <span style={{ color: '#ef4444', fontSize: '0.84rem' }}>*</span>
                            </div>
                            <div className="field" style={!nameValid && form.name ? { borderColor: 'rgba(239,68,68,0.5)' } : {}}>
                                <input
                                    required
                                    placeholder="my-project"
                                    value={form.name}
                                    onChange={(e) => setForm({ ...form, name: e.target.value.toLowerCase() })}
                                    autoFocus
                                    style={{ fontFamily: 'monospace', fontSize: '0.9rem' }}
                                />
                            </div>
                            {!nameValid && form.name
                                ? <small style={{ color: '#ef4444', fontSize: '0.78rem', marginTop: 4 }}>
                                    Only lowercase letters, numbers and hyphens allowed.
                                </small>
                                : <small style={{ color: 'var(--muted)', fontSize: '0.70rem', marginTop: 4, marginLeft:8}}>
                                      Lowercase letters, numbers and hyphens only. Must be unique.
                                </small>
                            }
                        </label>

                        {/* Description */}
                        <label className="field-label">
                            <span>Description (optional)</span>
                            <div className="field">
                                <span>📝</span>
                                <input
                                    placeholder="A short description of what this repo is for"
                                    value={form.description}
                                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                                />
                            </div>
                        </label>

                        {/* Divider */}
                        <div style={{ borderTop: '1px solid var(--line)', margin: '4px 0' }} />

                        {/* Approval toggle */}
                        <div
                            className="repo"
                            style={{ cursor: 'pointer', userSelect: 'none',
                                background: form.requiresApprovalByDefault
                                    ? 'rgba(251,191,36,0.06)' : undefined,
                                borderColor: form.requiresApprovalByDefault
                                    ? 'rgba(251,191,36,0.25)' : undefined,
                            }}
                            onClick={() => setForm({ ...form, requiresApprovalByDefault: !form.requiresApprovalByDefault })}
                        >
                            <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start' }}>
                                <div style={{
                                    width: 20, height: 20, borderRadius: 6, flexShrink: 0, marginTop: 2,
                                    border: `2px solid ${form.requiresApprovalByDefault ? '#fbbf24' : 'rgba(255,255,255,0.2)'}`,
                                    background: form.requiresApprovalByDefault ? '#fbbf24' : 'transparent',
                                    display: 'grid', placeItems: 'center', transition: 'all .15s',
                                }}>
                                    {form.requiresApprovalByDefault && (
                                        <svg width="11" height="8" viewBox="0 0 11 8" fill="none">
                                            <path d="M1 4L4 7L10 1" stroke="#000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                    )}
                                </div>
                                <div>
                                    <strong style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                                        🔒 Require approval for commits
                                        {form.requiresApprovalByDefault && (
                                            <span className="pill" style={{ background: 'rgba(251,191,36,0.15)', color: '#fbbf24', fontSize: '0.7rem', padding: '2px 8px' }}>
                        Enabled
                      </span>
                                        )}
                                    </strong>
                                    <small style={{ color: 'var(--muted)', fontSize: '0.8rem', lineHeight: 1.6 }}>
                                        Commits go through a change-request flow and need a MASTER to approve before being applied.
                                    </small>
                                </div>
                            </div>
                        </div>

                        {/* Actions */}
                        <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
                            <button
                                className="btn primary"
                                type="submit"
                                disabled={!canSubmit}
                                style={{ opacity: canSubmit ? 1 : 0.5 }}
                            >
                                {saving ? 'Creating…' : '✓ Create repository'}
                            </button>
                            <button
                                className="btn secondary"
                                type="button"
                                onClick={() => navigate('/repositories')}
                            >
                                Cancel
                            </button>
                        </div>

                    </form>
                </div>

                {/* ── Right: Live preview + tips ── */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>

                    {/* Preview card */}
                    <div className="card">
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 700 }}>
                Preview
              </span>
                        </div>

                        <div className="repo">
                            <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start' }}>
                                <div style={{
                                    width: 40, height: 40, borderRadius: 14, display: 'grid', placeItems: 'center',
                                    background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                                    border: '1px solid var(--line)', flexShrink: 0, fontSize: '1rem',
                                }}>📦</div>
                                <div style={{ minWidth: 0 }}>
                                    <strong style={{ display: 'block', fontSize: '0.95rem', marginBottom: 4, color: form.name ? 'var(--text)' : 'var(--muted)' }}>
                                        {form.name || 'repository-name'}
                                    </strong>
                                    <small style={{ color: 'var(--muted)', lineHeight: 1.5, display: 'block' }}>
                                        {form.description || <em>No description</em>}
                                    </small>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 10 }}>
                                        <span className="pill">📌 Rev. 0</span>
                                        <span className="pill" style={form.requiresApprovalByDefault
                                            ? { background: 'rgba(251,191,36,0.15)', color: '#fbbf24' }
                                            : { background: 'rgba(34,197,94,0.12)', color: '#22c55e' }}>
                      {form.requiresApprovalByDefault ? '🔒 Approval required' : '✅ Direct commits'}
                    </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Tips */}
                    <div className="card">
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 14 }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 700 }}>
                Tips
              </span>
                        </div>
                        {[
                            { icon: '🔤', title: 'Naming', text: 'Use short, descriptive names with hyphens. e.g. frontend-app or api-service.' },
                            { icon: '👥', title: 'Members', text: 'After creating, you can invite team members and assign them MASTER, CONTRIBUTOR or VIEWER roles.' },
                            { icon: '🔒', title: 'Approval flow', text: 'Enable approval if you want all changes reviewed before they are committed to the repo.' },
                        ].map(({ icon, title, text }) => (
                            <div key={title} style={{ display: 'flex', gap: 12, marginBottom: 14, alignItems: 'flex-start' }}>
                <span style={{
                    width: 32, height: 32, borderRadius: 10, display: 'grid', placeItems: 'center',
                    background: 'rgba(124,92,255,0.1)', border: '1px solid rgba(124,92,255,0.2)',
                    flexShrink: 0, fontSize: '0.9rem',
                }}>{icon}</span>
                                <div>
                                    <strong style={{ display: 'block', fontSize: '0.85rem', marginBottom: 3 }}>{title}</strong>
                                    <small style={{ color: 'var(--muted)', fontSize: '0.8rem', lineHeight: 1.6 }}>{text}</small>
                                </div>
                            </div>
                        ))}
                    </div>

                </div>
            </section>
        </AppLayout>
    )
}