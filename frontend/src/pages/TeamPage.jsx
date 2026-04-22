import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { userApi, repositoryApi } from '../api/api'
import { useUser } from '../context/UserContext'

const ROLE_COLORS = {
    MASTER:      { background: 'rgba(124,92,255,0.15)', color: '#a78bfa' },
    CONTRIBUTOR: { background: 'rgba(34,211,238,0.12)', color: '#22d3ee' },
    VIEWER:      { background: 'rgba(156,163,175,0.12)', color: '#9ca3af' },
    ADMIN:       { background: 'rgba(239,68,68,0.15)',   color: '#ef4444' },
    USER:        { background: 'rgba(99,102,241,0.15)',  color: '#818cf8' },
}

const ROLE_DESCRIPTIONS = {
    MASTER:      'Full control — can commit, manage members and delete the repository.',
    CONTRIBUTOR: 'Can commit directly or submit change requests.',
    VIEWER:      'Read-only access. Can clone and view history.',
}

function initials(name = '') {
    return name.slice(0, 2).toUpperCase() || '??'
}

function Avatar({ name, size = 44 }) {
    return (
        <div style={{
            width: size, height: size, borderRadius: Math.round(size * 0.36),
            display: 'grid', placeItems: 'center', flexShrink: 0, fontWeight: 700,
            fontSize: size * 0.32 + 'px',
            background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
            border: '1px solid var(--line)',
        }}>
            {initials(name)}
        </div>
    )
}

function AddMemberModal({ repoId, existingUsernames, allUsers, onClose, onAdded }) {
    const [username, setUsername] = useState('')
    const [role,     setRole]     = useState('VIEWER')
    const [saving,   setSaving]   = useState(false)
    const [error,    setError]    = useState('')

    const suggestions = username.length >= 1
        ? allUsers.filter(u =>
            u.username.toLowerCase().includes(username.toLowerCase()) &&
            !existingUsernames.includes(u.username)
        ).slice(0, 5)
        : []

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!username.trim()) return
        setSaving(true)
        setError('')
        const res = await repositoryApi.addMember(repoId, username.trim(), role)
        if (res.success) {
            onAdded()
        } else {
            setError(res.error)
            setSaving(false)
        }
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-card" onClick={e => e.stopPropagation()}>
                <h3 style={{ margin: '0 0 6px' }}>Add member</h3>
                <p className="sub" style={{ marginTop: 0, marginBottom: 20, fontSize: '0.85rem' }}>
                    Search for a registered user and assign them a role.
                </p>

                {error && <div className="form-error" style={{ marginBottom: 14 }}>{error}</div>}

                <form className="form-grid" onSubmit={handleSubmit}>
                    <label className="field-label">
                        <span>Username</span>
                        <div className="field">
                            <span>👤</span>
                            <input
                                required
                                placeholder="Search username…"
                                value={username}
                                onChange={e => { setUsername(e.target.value); setError('') }}
                                autoFocus
                                autoComplete="off"
                            />
                        </div>
                        {suggestions.length > 0 && (
                            <div style={{
                                marginTop: 4, borderRadius: 14, overflow: 'hidden',
                                border: '1px solid var(--line)', background: 'var(--panel2)',
                            }}>
                                {suggestions.map(u => (
                                    <div
                                        key={u.username}
                                        style={{
                                            display: 'flex', alignItems: 'center', gap: 10,
                                            padding: '10px 14px', cursor: 'pointer',
                                            borderBottom: '1px solid var(--line)',
                                        }}
                                        onMouseDown={() => setUsername(u.username)}
                                    >
                                        <Avatar name={u.username} size={30} />
                                        <div>
                                            <strong style={{ fontSize: '0.85rem' }}>{u.username}</strong>
                                            <small style={{ display: 'block', color: 'var(--muted)', fontSize: '0.75rem' }}>{u.email}</small>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </label>

                    {/* Role selector */}
                    <label className="field-label">
                        <span>Role</span>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                            {['MASTER', 'CONTRIBUTOR', 'VIEWER'].map(r => (
                                <div
                                    key={r}
                                    onClick={() => setRole(r)}
                                    style={{
                                        display: 'flex', alignItems: 'flex-start', gap: 12,
                                        padding: '12px 14px', borderRadius: 14, cursor: 'pointer',
                                        border: `1px solid ${role === r ? 'rgba(124,92,255,0.4)' : 'var(--line)'}`,
                                        background: role === r ? 'rgba(124,92,255,0.07)' : 'rgba(255,255,255,0.02)',
                                        transition: 'all .15s',
                                    }}
                                >
                                    <div style={{
                                        width: 18, height: 18, borderRadius: '50%', marginTop: 2, flexShrink: 0,
                                        border: `2px solid ${role === r ? '#a78bfa' : 'rgba(255,255,255,0.2)'}`,
                                        background: role === r ? '#a78bfa' : 'transparent',
                                        display: 'grid', placeItems: 'center',
                                    }}>
                                        {role === r && <div style={{ width: 6, height: 6, borderRadius: '50%', background: '#fff' }} />}
                                    </div>
                                    <div>
                                        <strong style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '0.88rem', marginBottom: 3 }}>
                                            {r}
                                            <span className="pill" style={{ ...ROLE_COLORS[r], fontSize: '0.7rem', padding: '2px 8px' }}>{r}</span>
                                        </strong>
                                        <small style={{ color: 'var(--muted)', fontSize: '0.78rem', lineHeight: 1.5 }}>
                                            {ROLE_DESCRIPTIONS[r]}
                                        </small>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </label>

                    <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
                        <button className="btn primary" type="submit" disabled={saving || !username.trim()}>
                            {saving ? 'Adding…' : 'Add member'}
                        </button>
                        <button className="btn secondary" type="button" onClick={onClose}>Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    )
}

function RepoMembersPanel({ repo, allUsers, onChanged }) {
    const { user: currentUser } = useUser()
    const [members,    setMembers]    = useState([])
    const [loading,    setLoading]    = useState(true)
    const [showAdd,    setShowAdd]    = useState(false)
    const [kicking,    setKicking]    = useState(null)
    const [kickError,  setKickError]  = useState('')
    const [editingRole, setEditingRole] = useState(null)  // username being edited
    const [newRole,     setNewRole]     = useState('')
    const [savingRole,  setSavingRole]  = useState(null)

    const loadMembers = () => {
        setLoading(true)
        repositoryApi.getMembers(repo.id).then(data => {
            setMembers(data ?? [])
            setLoading(false)
        })
    }

    useEffect(() => { loadMembers() }, [repo.id])

    const handleKick = async (username) => {
        setKicking(username)
        setKickError('')
        const res = await repositoryApi.kickMember(repo.id, username)
        if (res.success) {
            loadMembers()
            onChanged()
        } else {
            setKickError(res.error)
        }
        setKicking(null)
    }

    const handleSaveRole = async (username) => {
        setSavingRole(username)
        setKickError('')
        // Remove then re-add with new role (uses existing endpoints)
        const kick = await repositoryApi.kickMember(repo.id, username)
        if (!kick.success) { setKickError(kick.error); setSavingRole(null); return }
        const add = await repositoryApi.addMember(repo.id, username, newRole)
        if (!add.success) { setKickError(add.error); setSavingRole(null); return }
        setEditingRole(null)
        setSavingRole(null)
        loadMembers()
        onChanged()
    }

    const isMaster = members.some(m => m.user === currentUser?.username && m.role === 'MASTER')

    return (
        <>
            {showAdd && (
                <AddMemberModal
                    repoId={repo.id}
                    existingUsernames={members.map(m => m.user)}
                    allUsers={allUsers}
                    onClose={() => setShowAdd(false)}
                    onAdded={() => { setShowAdd(false); loadMembers(); onChanged() }}
                />
            )}

            <div className="repo" style={{ marginTop: 10 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <div style={{
                            width: 34, height: 34, borderRadius: 12, display: 'grid', placeItems: 'center',
                            background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                            border: '1px solid var(--line)', fontSize: '1rem',
                        }}>📦</div>
                        <div>
                            <strong style={{ fontSize: '0.9rem' }}>{repo.name}</strong>
                            <small style={{ display: 'block', color: 'var(--muted)', fontSize: '0.78rem' }}>
                                {repo.description || 'No description'}
                            </small>
                        </div>
                    </div>
                    {isMaster && (
                        <button
                            className="btn primary"
                            style={{ padding: '6px 14px', fontSize: '0.82rem' }}
                            onClick={() => setShowAdd(true)}
                        >
                            ＋ Add member
                        </button>
                    )}
                </div>

                {kickError && <div className="form-error" style={{ marginBottom: 10 }}>{kickError}</div>}

                {loading && <p className="sub" style={{ margin: 0 }}>Loading…</p>}

                {!loading && members.length === 0 && (
                    <p className="sub" style={{ margin: 0 }}>No members yet.</p>
                )}

                {!loading && members.length > 0 && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {members.map(m => {
                            const isMe = m.user === currentUser?.username
                            return (
                                <div key={m.id} style={{
                                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                                    padding: '10px 12px', borderRadius: 14,
                                    background: 'rgba(255,255,255,0.03)', border: `1px solid ${isMe ? 'rgba(124,92,255,0.3)' : 'var(--line)'}`,
                                }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                        <Avatar name={m.user} size={34} />
                                        <div>
                                            <strong style={{ fontSize: '0.85rem' }}>
                                                {m.user}
                                                {isMe && <span style={{ fontSize: '0.72rem', marginLeft: 8, color: '#818cf8' }}>(you)</span>}
                                            </strong>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                        {editingRole === m.user ? (
                                            <>
                                                <select
                                                    value={newRole}
                                                    onChange={e => setNewRole(e.target.value)}
                                                    style={{
                                                        background: 'var(--panel2)', border: '1px solid var(--line)',
                                                        color: 'var(--text)', borderRadius: 10, padding: '4px 10px',
                                                        fontSize: '0.78rem', cursor: 'pointer',
                                                    }}
                                                >
                                                    {['MASTER','CONTRIBUTOR','VIEWER'].map(r => (
                                                        <option key={r} value={r}>{r}</option>
                                                    ))}
                                                </select>
                                                <button
                                                    className="btn primary"
                                                    style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                                                    disabled={savingRole === m.user || newRole === m.role}
                                                    onClick={() => handleSaveRole(m.user)}
                                                >
                                                    {savingRole === m.user ? '…' : 'Save'}
                                                </button>
                                                <button
                                                    className="btn secondary"
                                                    style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                                                    onClick={() => setEditingRole(null)}
                                                >
                                                    Cancel
                                                </button>
                                            </>
                                        ) : (
                                            <>
                                                <span className="pill" style={{ ...ROLE_COLORS[m.role], fontSize: '0.75rem' }}>{m.role}</span>
                                                {isMaster && !isMe && (
                                                    <>
                                                        <button
                                                            className="btn secondary"
                                                            style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                                                            onClick={() => { setEditingRole(m.user); setNewRole(m.role) }}
                                                        >
                                                            ✏️ Role
                                                        </button>
                                                        <button
                                                            className="btn secondary"
                                                            style={{ padding: '4px 10px', fontSize: '0.75rem', color: '#ef4444', borderColor: 'rgba(239,68,68,0.3)' }}
                                                            disabled={kicking === m.user}
                                                            onClick={() => handleKick(m.user)}
                                                        >
                                                            {kicking === m.user ? '…' : 'Remove'}
                                                        </button>
                                                    </>
                                                )}
                                            </>
                                        )}
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                )}
            </div>
        </>
    )
}

export default function TeamPage() {
    const { user: currentUser } = useUser()
    const [allUsers,  setAllUsers]  = useState([])
    const [repos,     setRepos]     = useState([])
    const [loading,   setLoading]   = useState(true)
    const [search,    setSearch]    = useState('')
    const [activeTab, setActiveTab] = useState('repositories')
    const [repoCount, setRepoCount] = useState({})
    const [repoMembers, setRepoMembers] = useState([])

    const loadData = () => {
        setLoading(true)
        Promise.all([userApi.getAll(), repositoryApi.getMy()]).then(([users, myRepos]) => {
            setAllUsers(users ?? [])
            setRepos(myRepos ?? [])
            // Load members for all repos to compute role counts
            Promise.all((myRepos ?? []).map(r => repositoryApi.getMembers(r.id))).then(results => {
                const all = results.flatMap(r => r ?? [])
                setRepoMembers(all)
                setLoading(false)
            })
        })
    }

    useEffect(() => { loadData() }, [])

    const filteredUsers = allUsers.filter(m =>
        m.username.toLowerCase().includes(search.toLowerCase()) ||
        m.email.toLowerCase().includes(search.toLowerCase())
    )

    return (
        <AppLayout subtitle="Team management">
            <section className="hero">
                <span className="badge">👥 Collaboration</span>
                <h2>Team management</h2>
                <p>Manage workspace members and control who has access to each repository.</p>
            </section>

            <section className="grid-4">
                <div className="card stat">
                    <span>Workspace members</span>
                    <strong>{loading ? '…' : allUsers.length}</strong>
                </div>
                <div className="card stat">
                    <span>Collaborators</span>
                    <strong>{loading ? '…' : new Set(repoMembers.filter(m => m.role === 'MASTER' || m.role === 'CONTRIBUTOR').map(m => m.user)).size}</strong>
                </div>
                <div className="card stat">
                    <span>Viewers</span>
                    <strong>{loading ? '…' : new Set(repoMembers.filter(m => m.role === 'VIEWER').map(m => m.user)).size}</strong>
                </div>
                <div className="card stat">
                    <span>Contributors</span>
                    <strong>{loading ? '…' : new Set(repoMembers.filter(m => m.role === 'CONTRIBUTOR').map(m => m.user)).size}</strong>
                </div>
            </section>

            {/* Tabs */}
            <div style={{ display: 'flex', gap: 8, marginBottom: -10 }}>
                {[
                    { key: 'repositories', label: '📦 Repository access' },
                    { key: 'workspace',    label: '🌐 Workspace members' },
                ].map(t => (
                    <button
                        key={t.key}
                        className={activeTab === t.key ? 'tab active' : 'tab'}
                        style={{ padding: '10px 18px', borderRadius: 14, border: '1px solid var(--line)', cursor: 'pointer' }}
                        onClick={() => setActiveTab(t.key)}
                    >
                        {t.label}
                    </button>
                ))}
            </div>

            {/* ── Workspace members tab ── */}
            {activeTab === 'workspace' && (
                <section className="card">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
                        <h3 style={{ margin: 0 }}>All workspace members</h3>
                        <div className="field" style={{ flex: 1, maxWidth: 300 }}>
                            <span>🔍</span>
                            <input
                                placeholder="Search by username or email…"
                                value={search}
                                onChange={e => setSearch(e.target.value)}
                            />
                        </div>
                    </div>

                    {loading && <p className="sub">Loading…</p>}
                    {!loading && filteredUsers.length === 0 && <p className="sub">No members found.</p>}

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {filteredUsers.map(member => {
                            const isMe = member.username === currentUser?.username
                            return (
                                <div
                                    className="member"
                                    key={member.username}
                                    style={isMe ? { border: '1px solid rgba(124,92,255,0.4)' } : {}}
                                >
                                    <div className="row-between">
                                        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                                            <Avatar name={member.username} />
                                            <div>
                                                <strong>
                                                    {member.username}
                                                    {isMe && <span style={{ fontSize: '0.75rem', marginLeft: 8, color: '#818cf8' }}>(you)</span>}
                                                </strong>
                                                <small>{member.email || 'No email'}</small>
                                                {member.joinedAt && (
                                                    <small style={{ display: 'block', opacity: 0.6 }}>
                                                        Joined {new Date(member.joinedAt).toLocaleDateString()}
                                                    </small>
                                                )}
                                            </div>
                                        </div>
                                        <span className="pill" style={ROLE_COLORS[member.systemRole] || ROLE_COLORS.USER}>
                      {member.systemRole}
                    </span>
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                </section>
            )}
            {activeTab === 'repositories' && (
                <section className="card">
                    <div style={{ marginBottom: 20 }}>
                        <h3 style={{ margin: '0 0 6px' }}>Repository members</h3>
                        <p className="sub" style={{ margin: 0, fontSize: '0.85rem' }}>
                            Expand each repository to manage who has access and what role they hold. Only MASTERs can add or remove members.
                        </p>
                    </div>

                    {loading && <p className="sub">Loading…</p>}
                    {!loading && repos.length === 0 && (
                        <p className="sub">You don't own any repositories yet.</p>
                    )}

                    {!loading && repos.length > 0 && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                            {repos.map(repo => (
                                <RepoMembersPanel
                                    key={repo.id}
                                    repo={repo}
                                    allUsers={allUsers}
                                    onChanged={loadData}
                                />
                            ))}
                        </div>
                    )}
                </section>
            )}
        </AppLayout>
    )
}