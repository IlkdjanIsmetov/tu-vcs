import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { repositoryApi, changeRequestApi } from '../api/api'
import { useUser } from '../context/UserContext'

const STATUS_STYLE = {
    PENDING:    { background: 'rgba(251,191,36,0.15)',  color: '#fbbf24', icon: '⏳' },
    APPROVED:   { background: 'rgba(34,197,94,0.12)',   color: '#22c55e', icon: '✅' },
    REJECTED:   { background: 'rgba(239,68,68,0.12)',   color: '#ef4444', icon: '❌' },
    CONFLICTED: { background: 'rgba(249,115,22,0.12)',  color: '#f97316', icon: '⚠️' },
}

function timeAgo(dateStr) {
    const diff = Date.now() - new Date(dateStr).getTime()
    const m = Math.floor(diff / 60000)
    if (m < 1)  return 'just now'
    if (m < 60) return `${m}m ago`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h}h ago`
    return `${Math.floor(h / 24)}d ago`
}

function CRCard({ cr, repoId, repoName, isMaster, onAction }) {
    const [acting,  setActing]  = useState(null)
    const [error,   setError]   = useState('')
    const [confirm, setConfirm] = useState(null) // 'approve' | 'reject'

    const handleAction = async (type) => {
        setActing(type)
        setError('')
        const res = type === 'approve'
            ? await changeRequestApi.approve(repoId, cr.id)
            : await changeRequestApi.reject(repoId, cr.id)
        if (res.success) {
            onAction()
        } else {
            setError(res.error)
            setActing(null)
        }
        setConfirm(null)
    }

    const st = STATUS_STYLE[cr.status] || STATUS_STYLE.PENDING
    const isPending = cr.status === 'PENDING'

    return (
        <div className="repo" style={{ position: 'relative' }}>
            <div className="row-between">
                <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start', flex: 1, minWidth: 0 }}>
                    <div style={{
                        width: 40, height: 40, borderRadius: 14, display: 'grid', placeItems: 'center',
                        background: st.background, border: `1px solid ${st.color}33`,
                        flexShrink: 0, fontSize: '1.1rem',
                    }}>
                        {st.icon}
                    </div>
                    <div style={{ minWidth: 0 }}>
                        <strong style={{ display: 'block', fontSize: '0.95rem', marginBottom: 3 }}>
                            {cr.title}
                        </strong>
                        {cr.description && (
                            <small style={{ color: 'var(--muted)', lineHeight: 1.5, display: 'block', marginBottom: 6 }}>
                                {cr.description}
                            </small>
                        )}
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                            <span className="pill">📦 {repoName}</span>
                            <span className="pill">👤 {cr.authorName}</span>
                            <span className="pill">📌 Base rev. {cr.baseRevisionNumber}</span>
                            <span className="pill">🕐 {timeAgo(cr.createdAt)}</span>
                            <span className="pill" style={st}>{cr.status}</span>
                        </div>
                    </div>
                </div>

                {isMaster && isPending && !confirm && (
                    <div style={{ display: 'flex', gap: 8, flexShrink: 0, marginLeft: 12 }}>
                        <button
                            className="btn secondary"
                            style={{ padding: '6px 14px', fontSize: '0.82rem', color: '#22c55e', borderColor: 'rgba(34,197,94,0.3)' }}
                            onClick={() => setConfirm('approve')}
                        >
                            ✅ Approve
                        </button>
                        <button
                            className="btn secondary"
                            style={{ padding: '6px 14px', fontSize: '0.82rem', color: '#ef4444', borderColor: 'rgba(239,68,68,0.3)' }}
                            onClick={() => setConfirm('reject')}
                        >
                            ❌ Reject
                        </button>
                    </div>
                )}
            </div>


            {confirm && (
                <div style={{
                    marginTop: 12, padding: '12px 14px', borderRadius: 14,
                    background: confirm === 'approve' ? 'rgba(34,197,94,0.07)' : 'rgba(239,68,68,0.07)',
                    border: `1px solid ${confirm === 'approve' ? 'rgba(34,197,94,0.25)' : 'rgba(239,68,68,0.25)'}`,
                }}>
                    <p style={{ margin: '0 0 10px', fontSize: '0.85rem', color: confirm === 'approve' ? '#22c55e' : '#ef4444' }}>
                        {confirm === 'approve'
                            ? `Approve "${cr.title}"? This will apply all changes to the repository.`
                            : `Reject "${cr.title}"? The author will need to resubmit.`}
                    </p>
                    <div style={{ display: 'flex', gap: 8 }}>
                        <button
                            className="btn"
                            style={{
                                padding: '6px 14px', fontSize: '0.82rem', color: '#fff',
                                background: confirm === 'approve' ? '#22c55e' : '#ef4444',
                            }}
                            disabled={!!acting}
                            onClick={() => handleAction(confirm)}
                        >
                            {acting ? '…' : `Confirm ${confirm}`}
                        </button>
                        <button className="btn secondary" style={{ padding: '6px 14px', fontSize: '0.82rem' }} onClick={() => setConfirm(null)}>
                            Cancel
                        </button>
                    </div>
                </div>
            )}

            {error && <div className="form-error" style={{ marginTop: 10 }}>{error}</div>}
        </div>
    )
}

export default function ChangeRequestsPage() {
    const { user: currentUser } = useUser()
    const [repos,     setRepos]     = useState([])
    const [crData,    setCrData]    = useState({})   // repoId -> { crs, isMaster }
    const [loading,   setLoading]   = useState(true)
    const [filter,    setFilter]    = useState('PENDING')  // PENDING | ALL
    const [repoFilter, setRepoFilter] = useState('all')

    const load = async () => {
        setLoading(true)
        const myRepos = await repositoryApi.getMy() ?? []
        setRepos(myRepos)

        const entries = await Promise.all(
            myRepos.map(async (repo) => {
                const crs = await changeRequestApi.getPending(repo.id)
                const members = await repositoryApi.getMembers(repo.id)
                const memberList = members?.content ?? members ?? []
                const isMaster = memberList.some(m => m.user === currentUser?.username && m.role === 'MASTER')
                return [repo.id, { crs: crs ?? [], isMaster }]
            })
        )
        setCrData(Object.fromEntries(entries))
        setLoading(false)
    }

    useEffect(() => { load() }, [])

    const allCrs = Object.values(crData).flatMap(d => d.crs)
    const totalPending = allCrs.filter(c => c.status === 'PENDING').length

    const displayRepos = repoFilter === 'all' ? repos : repos.filter(r => r.id === repoFilter)
    const displayCrs = displayRepos.flatMap(repo => {
        const data = crData[repo.id] || { crs: [], isMaster: false }
        return data.crs.map(cr => ({ cr, repo, isMaster: data.isMaster }))
    })

    return (
        <AppLayout subtitle="Change requests">
            <section className="hero">
                <span className="badge">🔁 Change requests</span>
                <h2>Review &amp; approve changes.</h2>
                <p>Manage pending change requests across your repositories. Approve or reject submissions from your team.</p>
            </section>

            <section className="grid-4">
                <div className="card stat">
                    <span>Pending</span>
                    <strong style={{ color: totalPending > 0 ? '#fbbf24' : undefined }}>
                        {loading ? '…' : totalPending}
                    </strong>
                </div>
                <div className="card stat">
                    <span>Approved</span>
                    <strong style={{ color: '#22c55e' }}>
                        {loading ? '…' : allCrs.filter(c => c.status === 'APPROVED').length}
                    </strong>
                </div>
                <div className="card stat">
                    <span>Rejected</span>
                    <strong style={{ color: '#ef4444' }}>
                        {loading ? '…' : allCrs.filter(c => c.status === 'REJECTED').length}
                    </strong>
                </div>
                <div className="card stat">
                    <span>Conflicted</span>
                    <strong style={{ color: '#f97316' }}>
                        {loading ? '…' : allCrs.filter(c => c.status === 'CONFLICTED').length}
                    </strong>
                </div>
            </section>

            <section className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20, flexWrap: 'wrap' }}>
                    <h3 style={{ margin: 0 }}>Change requests</h3>

                    <div className="field" style={{ minWidth: 180 }}>
                        <span>📦</span>
                        <select
                            value={repoFilter}
                            onChange={e => setRepoFilter(e.target.value)}
                            style={{ width: '100%', padding: '15px 0', background: 'transparent', border: 'none', outline: 'none', color: 'var(--text)', cursor: 'pointer' }}
                        >
                            <option value="all">All repositories</option>
                            {repos.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                        </select>
                    </div>
                </div>

                {loading && <p className="sub">Loading change requests…</p>}

                {!loading && displayCrs.length === 0 && (
                    <div style={{ textAlign: 'center', padding: '32px 0' }}>
                        <div style={{ fontSize: '2.5rem', marginBottom: 12 }}>🎉</div>
                        <p className="sub">No pending change requests. Everything is up to date.</p>
                    </div>
                )}

                {!loading && displayCrs.length > 0 && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {displayCrs.map(({ cr, repo, isMaster }) => (
                            <CRCard
                                key={cr.id}
                                cr={cr}
                                repoId={repo.id}
                                repoName={repo.name}
                                isMaster={isMaster}
                                onAction={load}
                            />
                        ))}
                    </div>
                )}
            </section>
        </AppLayout>
    )
}