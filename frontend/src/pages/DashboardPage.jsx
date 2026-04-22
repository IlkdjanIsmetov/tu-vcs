import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { useUser } from '../context/UserContext'
import { repositoryApi, changeRequestApi, historyApi, userApi } from '../api/api'

function timeAgo(date) {
    if (!date) return '—'
    const diff = Date.now() - new Date(date).getTime()
    const m = Math.floor(diff / 60000)
    if (m < 1)  return 'just now'
    if (m < 60) return `${m}m ago`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h}h ago`
    const d = Math.floor(h / 24)
    if (d < 30) return `${d}d ago`
    return new Date(date).toLocaleDateString()
}

export default function DashboardPage() {
    const { user }   = useUser()
    const navigate   = useNavigate()

    const [repos,        setRepos]        = useState([])
    const [pendingCrs,   setPendingCrs]   = useState([])
    const [recentCommits, setRecentCommits] = useState([])
    const [teamSize,     setTeamSize]     = useState(null)
    const [loading,      setLoading]      = useState(true)

    useEffect(() => {
        const load = async () => {
            const [myRepos, allUsers] = await Promise.all([
                repositoryApi.getMy(),
                userApi.getAll(),
            ])

            const repos = myRepos ?? []
            setRepos(repos)
            setTeamSize(allUsers?.length ?? 0)

            const [crResults, historyResults] = await Promise.all([
                Promise.all(repos.map(r => changeRequestApi.getPending(r.id).then(crs => ({ repo: r, crs: crs ?? [] })))),
                Promise.all(repos.map(r => historyApi.getHistory(r.id).then(h => ({ repo: r, history: h ?? [] })))),
            ])

            const allPending = crResults.flatMap(({ repo, crs }) =>
                crs.map(cr => ({ ...cr, repoName: repo.name, repoId: repo.id }))
            )
            setPendingCrs(allPending)

            const allCommits = historyResults.flatMap(({ repo, history }) =>
                history.map(h => ({ ...h, repoName: repo.name }))
            ).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5)
            setRecentCommits(allCommits)

            setLoading(false)
        }
        load()
    }, [])

    const totalRevisions = repos.reduce((s, r) => s + (r.revision ?? 0), 0)
    const protectedRepos = repos.filter(r => r.requireApproval).length
    const hour = new Date().getHours()
    const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'

    return (
        <AppLayout subtitle="Dashboard">
            <section className="hero">
                <span className="badge">🏠 Dashboard</span>
                <h2>{greeting}{user ? `, ${user.firstName || user.username}` : ''}!</h2>
                <p>Here's what's happening across your workspace right now.</p>

            </section>

            <section className="grid-4">
                <div className="card stat" style={{ cursor: 'pointer' }} onClick={() => navigate('/repositories')}>
                    <span>Repositories</span>
                    <strong>{loading ? '…' : repos.length}</strong>
                    <small style={{ color: 'var(--muted)', fontSize: '0.78rem', marginTop: 4, display: 'block' }}>
                        {loading ? '' : `${protectedRepos} protected`}
                    </small>
                </div>
                <div className="card stat" style={{ cursor: 'pointer' }} onClick={() => navigate('/history')}>
                    <span>Total commits</span>
                    <strong>{loading ? '…' : totalRevisions}</strong>
                    <small style={{ color: 'var(--muted)', fontSize: '0.78rem', marginTop: 4, display: 'block' }}>
                        across all repos
                    </small>
                </div>
                <div className="card stat"
                     style={{ cursor: 'pointer', ...(pendingCrs.length > 0 ? { borderColor: 'rgba(251,191,36,0.35)' } : {}) }}
                     onClick={() => navigate('/change-requests')}
                >
                    <span>Pending reviews</span>
                    <strong style={{ color: pendingCrs.length > 0 ? '#fbbf24' : undefined }}>
                        {loading ? '…' : pendingCrs.length}
                    </strong>
                    <small style={{ color: 'var(--muted)', fontSize: '0.78rem', marginTop: 4, display: 'block' }}>
                        {loading ? '' : pendingCrs.length > 0 ? 'need your attention' : 'all caught up'}
                    </small>
                </div>
                <div className="card stat" style={{ cursor: 'pointer' }} onClick={() => navigate('/team')}>
                    <span>Team members</span>
                    <strong>{teamSize === null ? '…' : teamSize}</strong>
                    <small style={{ color: 'var(--muted)', fontSize: '0.78rem', marginTop: 4, display: 'block' }}>
                        in your workspace
                    </small>
                </div>
            </section>

            <section className="grid-2" style={{ alignItems: 'start' }}>

                {/* ── Left: Pending change requests ── */}
                <div className="card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                        <h3 style={{ margin: 0 }}>Change requests</h3>
                        <button
                            className="btn secondary"
                            style={{ padding: '5px 12px', fontSize: '0.8rem' }}
                            onClick={() => navigate('/change-requests')}
                        >
                            View all →
                        </button>
                    </div>

                    {loading && <p className="sub">Loading…</p>}

                    {!loading && pendingCrs.length === 0 && (
                        <div style={{ textAlign: 'center', padding: '24px 0' }}>
                            <div style={{ fontSize: '2rem', marginBottom: 8 }}>🎉</div>
                            <p className="sub" style={{ margin: 0 }}>No pending change requests.</p>
                        </div>
                    )}

                    {!loading && pendingCrs.slice(0, 4).map(cr => (
                        <div key={cr.id} className="repo" style={{ marginTop: 10 }}>
                            <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                                <div style={{
                                    width: 36, height: 36, borderRadius: 12, display: 'grid', placeItems: 'center',
                                    background: 'rgba(251,191,36,0.12)', border: '1px solid rgba(251,191,36,0.3)',
                                    flexShrink: 0, fontSize: '1rem',
                                }}>⏳</div>
                                <div style={{ minWidth: 0, flex: 1 }}>
                                    <strong style={{ display: 'block', fontSize: '0.88rem', marginBottom: 3 }}>{cr.title}</strong>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
                                        <span className="pill" style={{ fontSize: '0.72rem' }}>📦 {cr.repoName}</span>
                                        <span className="pill" style={{ fontSize: '0.72rem' }}>👤 {cr.authorName}</span>
                                        <span className="pill" style={{ fontSize: '0.72rem' }}>🕐 {timeAgo(cr.createdAt)}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}

                    {!loading && pendingCrs.length > 4 && (
                        <p className="sub" style={{ marginTop: 12, fontSize: '0.82rem', textAlign: 'center' }}>
                            +{pendingCrs.length - 4} more —{' '}
                            <span style={{ color: 'var(--accent)', cursor: 'pointer' }} onClick={() => navigate('/change-requests')}>
                view all
              </span>
                        </p>
                    )}
                </div>

                {/* ── Right: Recent commits ── */}
                <div className="card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                        <h3 style={{ margin: 0 }}>Recent commits</h3>
                        <button
                            className="btn secondary"
                            style={{ padding: '5px 12px', fontSize: '0.8rem' }}
                            onClick={() => navigate('/history')}
                        >
                            Full history →
                        </button>
                    </div>

                    {loading && <p className="sub">Loading…</p>}

                    {!loading && recentCommits.length === 0 && (
                        <div style={{ textAlign: 'center', padding: '24px 0' }}>
                            <div style={{ fontSize: '2rem', marginBottom: 8 }}>📭</div>
                            <p className="sub" style={{ margin: 0 }}>No commits yet.</p>
                        </div>
                    )}

                    {!loading && recentCommits.map((commit, i) => (
                        <div key={`${commit.repoName}-${commit.revisionNumber}`} className="feed" style={{ marginTop: i === 0 ? 0 : 10 }}>
                            <div style={{
                                width: 36, height: 36, borderRadius: 12, display: 'grid', placeItems: 'center',
                                background: i === 0
                                    ? 'linear-gradient(135deg,rgba(124,92,255,.28),rgba(34,211,238,.18))'
                                    : 'rgba(255,255,255,0.04)',
                                border: `1px solid ${i === 0 ? 'rgba(124,92,255,0.35)' : 'var(--line)'}`,
                                flexShrink: 0, fontSize: '0.78rem', fontWeight: 700, color: i === 0 ? '#a78bfa' : 'var(--muted)',
                            }}>
                                #{commit.revisionNumber}
                            </div>
                            <div style={{ minWidth: 0 }}>
                                <strong style={{ display: 'block', fontSize: '0.85rem', marginBottom: 3 }}>
                                    {commit.message || <em style={{ color: 'var(--muted)' }}>No message</em>}
                                </strong>
                                <small>
                                    <span style={{ color: 'var(--muted)' }}>📦 {commit.repoName}</span>
                                    {' · '}
                                    <span style={{ color: 'var(--muted)' }}>👤 {commit.username}</span>
                                    {' · '}
                                    <span style={{ color: 'var(--muted)' }}>{timeAgo(commit.createdAt)}</span>
                                </small>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            {/* ── My repositories ── */}
            <section className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                    <h3 style={{ margin: 0 }}>My repositories</h3>
                    <button
                        className="btn secondary"
                        style={{ padding: '5px 12px', fontSize: '0.8rem' }}
                        onClick={() => navigate('/repositories')}
                    >
                        View all →
                    </button>
                </div>

                {loading && <p className="sub">Loading…</p>}

                {!loading && repos.length === 0 && (
                    <div style={{ textAlign: 'center', padding: '24px 0' }}>
                        <div style={{ fontSize: '2rem', marginBottom: 8 }}>📦</div>
                        <p className="sub" style={{ margin: 0, marginBottom: 12 }}>No repositories yet.</p>
                        <button className="btn primary" onClick={() => navigate('/repositories/new')}>
                            Create your first repository
                        </button>
                    </div>
                )}

                {!loading && repos.length > 0 && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: 10 }}>
                        {repos.slice(0, 6).map(repo => (
                            <div key={repo.id} style={{
                                padding: '14px 16px', borderRadius: 18,
                                background: 'var(--panel2)', border: '1px solid var(--line)',
                                display: 'flex', flexDirection: 'column', gap: 8,
                            }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                    <div style={{
                                        width: 34, height: 34, borderRadius: 12, display: 'grid', placeItems: 'center',
                                        background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                                        border: '1px solid var(--line)', flexShrink: 0, fontSize: '0.95rem',
                                    }}>📦</div>
                                    <strong style={{ fontSize: '0.88rem', flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                        {repo.name}
                                    </strong>
                                </div>
                                {repo.description && (
                                    <small style={{ color: 'var(--muted)', fontSize: '0.78rem', lineHeight: 1.5 }}>
                                        {repo.description.length > 60 ? repo.description.slice(0, 60) + '…' : repo.description}
                                    </small>
                                )}
                                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                                    <span className="pill" style={{ fontSize: '0.72rem' }}>📌 Rev. {repo.revision ?? 0}</span>
                                    <span className="pill" style={{
                                        fontSize: '0.72rem',
                                        ...(repo.requireApproval
                                            ? { background: 'rgba(251,191,36,0.12)', color: '#fbbf24' }
                                            : { background: 'rgba(34,197,94,0.10)', color: '#22c55e' })
                                    }}>
                    {repo.requireApproval ? '🔒' : '✅'} {repo.requireApproval ? 'Approval' : 'Direct'}
                  </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* ── App preview ── */}
            <section className="card">
                <div style={{ marginBottom: 20 }}>
                    <h3 style={{ margin: '0 0 6px' }}>What you can do</h3>
                    <p className="sub" style={{ margin: 0, fontSize: '0.85rem' }}>
                        A quick overview of everything available in your workspace.
                    </p>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 12 }}>
                    {[
                        {
                            icon: '📦', title: 'Repositories',
                            color: 'rgba(124,92,255,0.10)', border: 'rgba(124,92,255,0.22)', textColor: '#a78bfa',
                            desc: 'Create and manage version-controlled repositories. Track every revision, clone snapshots and configure access settings.',
                            action: () => navigate('/repositories'),
                        },
                        {
                            icon: '🔁', title: 'Change Requests',
                            color: 'rgba(251,191,36,0.08)', border: 'rgba(251,191,36,0.20)', textColor: '#fbbf24',
                            desc: 'Contributors submit change requests for review. MASTERs approve or reject them before changes are committed.',
                            action: () => navigate('/change-requests'),
                        },
                        {
                            icon: '📜', title: 'Commit History',
                            color: 'rgba(34,211,238,0.08)', border: 'rgba(34,211,238,0.20)', textColor: '#22d3ee',
                            desc: 'Browse a full timeline of every revision across your repositories — who committed what and when.',
                            action: () => navigate('/history'),
                        },
                        {
                            icon: '👥', title: 'Team Management',
                            color: 'rgba(45,212,191,0.08)', border: 'rgba(45,212,191,0.20)', textColor: '#2dd4bf',
                            desc: 'Invite workspace members to your repos. Assign MASTER, CONTRIBUTOR or VIEWER roles per repository.',
                            action: () => navigate('/team'),
                        },
                        {
                            icon: '👤', title: 'Profile',
                            color: 'rgba(99,102,241,0.08)', border: 'rgba(99,102,241,0.20)', textColor: '#818cf8',
                            desc: 'Update your name and email, change your password or delete your account — all synced directly with Keycloak.',
                            action: () => navigate('/profile'),
                        },
                        {
                            icon: '🔒', title: 'Access control',
                            color: 'rgba(239,68,68,0.07)', border: 'rgba(239,68,68,0.18)', textColor: '#ef4444',
                            desc: 'Every repository can require approval before commits are applied. Protect your main branch with a one-click toggle.',
                            action: () => navigate('/repositories'),
                        },
                        {
                            icon: '💻', title: 'CLI — commit & pull',
                            color: 'rgba(15,23,42,0.6)', border: 'rgba(148,163,184,0.18)', textColor: '#94a3b8',
                            desc: 'Use the terminal client to commit files, pull latest changes, check status and view diffs — just like Git but connected to Push and Pray.',
                            action: null,
                        },
                        {
                            icon: '⚡', title: 'CLI — all commands',
                            color: 'rgba(15,23,42,0.6)', border: 'rgba(148,163,184,0.18)', textColor: '#94a3b8',
                            desc: 'login · create · clone · commit · pull · push · status · diff · switch · history · approve · reject · add-member · kick-member',
                            action: null,
                        },
                    ].map(({ icon, title, color, border, textColor, desc, action }) => (
                        <div
                            key={title}
                            onClick={action || undefined}
                            style={{
                                padding: '18px', borderRadius: 20, cursor: 'pointer',
                                background: color, border: `1px solid ${border}`,
                                transition: 'transform .15s, box-shadow .15s',
                                display: 'flex', flexDirection: 'column', gap: 10,
                            }}
                            onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,0.25)' }}
                            onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none' }}
                        >
                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                <div style={{
                                    width: 40, height: 40, borderRadius: 13, display: 'grid', placeItems: 'center',
                                    background: 'rgba(255,255,255,0.05)', border: `1px solid ${border}`,
                                    fontSize: '1.1rem', flexShrink: 0,
                                }}>{icon}</div>
                                <strong style={{ fontSize: '0.92rem', color: textColor }}>{title}</strong>
                            </div>
                            <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.8rem', lineHeight: 1.65 }}>{desc}</p>
                        </div>
                    ))}
                </div>
            </section>
            {/* ── Footer ── */}
            <section className="card" style={{ textAlign: 'center', padding: '28px 24px' }}>
                <div style={{ marginBottom: 16 }}>
                    <span className="badge" style={{ marginBottom: 12, display: 'inline-flex' }}>🎓 TU-VCS Project</span>
                    <h3 style={{ margin: '12px 0 6px', fontSize: '1rem' }}>Built by the Push and Pray team</h3>
                    <p className="sub" style={{ margin: 0, fontSize: '0.85rem' }}>
                        A full-stack version control system — Spring Boot backend, React frontend and a Java CLI client.
                    </p>
                </div>

                <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
                    <a
                        href="https://github.com/IlkdjanIsmetov/tu-vcs"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: 8,
                            padding: '10px 20px', borderRadius: 14,
                            background: 'rgba(255,255,255,0.05)', border: '1px solid var(--line)',
                            color: 'var(--text)', fontSize: '0.85rem', fontWeight: 600,
                            textDecoration: 'none', transition: 'all .15s',
                        }}
                        onMouseEnter={e => { e.currentTarget.style.background = 'rgba(124,92,255,0.12)'; e.currentTarget.style.borderColor = 'rgba(124,92,255,0.35)' }}
                        onMouseLeave={e => { e.currentTarget.style.background = 'rgba(255,255,255,0.05)'; e.currentTarget.style.borderColor = 'var(--line)' }}
                    >
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
                        </svg>
                        Backend &amp; Frontend
                    </a>

                    <a
                        href="https://github.com/IlkdjanIsmetov/tu-vcs-client"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: 8,
                            padding: '10px 20px', borderRadius: 14,
                            background: 'rgba(255,255,255,0.05)', border: '1px solid var(--line)',
                            color: 'var(--text)', fontSize: '0.85rem', fontWeight: 600,
                            textDecoration: 'none', transition: 'all .15s',
                        }}
                        onMouseEnter={e => { e.currentTarget.style.background = 'rgba(34,211,238,0.10)'; e.currentTarget.style.borderColor = 'rgba(34,211,238,0.35)' }}
                        onMouseLeave={e => { e.currentTarget.style.background = 'rgba(255,255,255,0.05)'; e.currentTarget.style.borderColor = 'var(--line)' }}
                    >
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
                        </svg>
                        CLI Client
                    </a>
                </div>

                <p style={{ margin: '16px 0 0', color: 'var(--muted)', fontSize: '0.78rem' }}>
                    Open source · Made with A LOT of effort
                </p>
            </section>
        </AppLayout>
    )
}