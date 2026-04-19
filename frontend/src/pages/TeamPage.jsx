import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { userApi } from '../api/api'
import { useUser } from '../context/UserContext'

const ROLE_COLORS = {
    ADMIN: { background: 'rgba(239,68,68,0.15)', color: '#ef4444' },
    USER:  { background: 'rgba(99,102,241,0.15)', color: '#818cf8' },
}

function initials(username = '') {
    return username.slice(0, 2).toUpperCase() || '??'
}

export default function TeamPage() {
    const { user: currentUser } = useUser()
    const [members,  setMembers]  = useState([])
    const [loading,  setLoading]  = useState(true)
    const [search,   setSearch]   = useState('')

    useEffect(() => {
        userApi.getAll().then((data) => {
            setMembers(data)
            setLoading(false)
        })
    }, [])

    const filtered = members.filter((m) =>
        m.username.toLowerCase().includes(search.toLowerCase()) ||
        m.email.toLowerCase().includes(search.toLowerCase())
    )

    return (
        <AppLayout subtitle="Team and access roles">
            <section className="hero">
                <span className="badge">👥 Collaboration</span>
                <h2>Manage users and roles.</h2>
                <p>All registered users in the workspace with their system roles and access levels.</p>
            </section>

            {/* ── Stats row ── */}
            <section className="grid-4">
                <div className="card stat">
                    <span>Total members</span>
                    <strong>{loading ? '…' : members.length}</strong>
                </div>
                <div className="card stat">
                    <span>Admins</span>
                    <strong>{loading ? '…' : members.filter((m) => m.systemRole === 'ADMIN').length}</strong>
                </div>
                <div className="card stat">
                    <span>Users</span>
                    <strong>{loading ? '…' : members.filter((m) => m.systemRole === 'USER').length}</strong>
                </div>
                <div className="card stat">
                    <span>Your role</span>
                    <strong>{currentUser?.displayRole || '…'}</strong>
                </div>
            </section>

            <section className="card">
                {/* ── Search ── */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
                    <h3 style={{ margin: 0 }}>All members</h3>
                    <div className="field" style={{ flex: 1, maxWidth: 300 }}>
                        <span>🔍</span>
                        <input
                            placeholder="Search by username or email…"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                </div>

                {loading && <p className="sub">Loading members…</p>}
                {!loading && filtered.length === 0 && (
                    <p className="sub">No members found.</p>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {filtered.map((member) => {
                        const isMe = member.username === currentUser?.username
                        const roleStyle = ROLE_COLORS[member.systemRole] || ROLE_COLORS.USER
                        return (
                            <div
                                className="member"
                                key={member.username}
                                style={isMe ? { border: '1px solid rgba(99,102,241,0.4)' } : {}}
                            >
                                <div className="row-between">
                                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                                        <div className="avatar">{initials(member.username)}</div>
                                        <div>
                                            <strong>
                                                {member.username}
                                                {isMe && (
                                                    <span style={{ fontSize: '0.75rem', marginLeft: 8, color: '#818cf8' }}>
                            (you)
                          </span>
                                                )}
                                            </strong>
                                            <small>{member.email || 'No email'}</small>
                                            {member.joinedAt && (
                                                <small style={{ display: 'block', opacity: 0.5 }}>
                                                    Joined {new Date(member.joinedAt).toLocaleDateString()}
                                                </small>
                                            )}
                                        </div>
                                    </div>
                                    <span className="pill" style={roleStyle}>
                    {member.systemRole}
                  </span>
                                </div>
                            </div>
                        )
                    })}
                </div>
            </section>
        </AppLayout>
    )
}
