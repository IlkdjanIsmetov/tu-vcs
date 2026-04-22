import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { repositoryApi, historyApi } from '../api/api'

function timeAgo(date) {
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

export default function HistoryPage() {
    const [repos,      setRepos]      = useState([])
    const [selectedId, setSelectedId] = useState(null)
    const [history,    setHistory]    = useState([])
    const [loadingRepos, setLoadingRepos] = useState(true)
    const [loadingHist,  setLoadingHist]  = useState(false)
    const [search,     setSearch]     = useState('')

    useEffect(() => {
        repositoryApi.getMy().then(data => {
            const r = data ?? []
            setRepos(r)
            setLoadingRepos(false)
            if (r.length > 0) setSelectedId(r[0].id)
        })
    }, [])

    useEffect(() => {
        if (!selectedId) return
        setLoadingHist(true)
        setHistory([])
        historyApi.getHistory(selectedId).then(data => {
            setHistory(data ?? [])
            setLoadingHist(false)
        })
    }, [selectedId])

    const selectedRepo = repos.find(r => r.id === selectedId)

    const totalRevisions = repos.reduce((s, r) => s + (r.revision ?? 0), 0)
    const uniqueAuthors  = [...new Set(history.map(h => h.username))].length

    const filtered = history.filter(h =>
        (h.message || '').toLowerCase().includes(search.toLowerCase()) ||
        (h.username || '').toLowerCase().includes(search.toLowerCase())
    )

    return (
        <AppLayout subtitle="Commit history">
            <section className="hero">
                <span className="badge">📜 Commit history</span>
                <h2>Revision timeline.</h2>
                <p>Browse the full commit history for each repository. See who committed what and when.</p>
            </section>

            <section className="grid-4">
                <div className="card stat">
                    <span>Total revisions</span>
                    <strong>{loadingRepos ? '…' : totalRevisions}</strong>
                </div>
                <div className="card stat">
                    <span>This repository</span>
                    <strong>{loadingHist ? '…' : history.length}</strong>
                </div>
                <div className="card stat">
                    <span>Contributors</span>
                    <strong>{loadingHist ? '…' : uniqueAuthors}</strong>
                </div>
                <div className="card stat">
                    <span>Latest revision</span>
                    <strong>{loadingHist || history.length === 0 ? '…' : `#${history[0].revisionNumber}`}</strong>
                </div>
            </section>

            <div className="grid-2" style={{ alignItems: 'start' }}>

                <div className="card">
                    <h3 style={{ margin: '0 0 16px' }}>Repositories</h3>
                    {loadingRepos && <p className="sub">Loading…</p>}
                    {!loadingRepos && repos.length === 0 && <p className="sub">No repositories found.</p>}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {repos.map(repo => (
                            <div
                                key={repo.id}
                                onClick={() => setSelectedId(repo.id)}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: 12,
                                    padding: '12px 14px', borderRadius: 16, cursor: 'pointer',
                                    border: `1px solid ${selectedId === repo.id ? 'rgba(124,92,255,0.4)' : 'var(--line)'}`,
                                    background: selectedId === repo.id ? 'rgba(124,92,255,0.07)' : 'rgba(255,255,255,0.02)',
                                    transition: 'all .15s',
                                }}
                            >
                                <div style={{
                                    width: 36, height: 36, borderRadius: 12, display: 'grid', placeItems: 'center',
                                    background: 'linear-gradient(135deg,rgba(124,92,255,.20),rgba(34,211,238,.14))',
                                    border: '1px solid var(--line)', flexShrink: 0, fontSize: '1rem',
                                }}>📦</div>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <strong style={{ display: 'block', fontSize: '0.88rem' }}>{repo.name}</strong>
                                    <small style={{ color: 'var(--muted)', fontSize: '0.76rem' }}>
                                        {repo.revision ?? 0} revision{repo.revision !== 1 ? 's' : ''}
                                    </small>
                                </div>
                                {selectedId === repo.id && (
                                    <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#a78bfa', flexShrink: 0 }} />
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                <div className="card">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
                        <h3 style={{ margin: 0 }}>
                            {selectedRepo ? selectedRepo.name : 'Select a repository'}
                        </h3>
                        {history.length > 0 && (
                            <div className="field" style={{ flex: 1, maxWidth: 260 }}>
                                <span>🔍</span>
                                <input
                                    placeholder="Search commits…"
                                    value={search}
                                    onChange={e => setSearch(e.target.value)}
                                />
                            </div>
                        )}
                    </div>

                    {loadingHist && <p className="sub">Loading history…</p>}

                    {!loadingHist && history.length === 0 && selectedId && (
                        <div style={{ textAlign: 'center', padding: '32px 0' }}>
                            <div style={{ fontSize: '2rem', marginBottom: 10 }}>📭</div>
                            <p className="sub">No commits yet in this repository.</p>
                        </div>
                    )}

                    {!loadingHist && filtered.length === 0 && history.length > 0 && (
                        <p className="sub">No commits match your search.</p>
                    )}

                    {!loadingHist && filtered.length > 0 && (
                        <div style={{ position: 'relative' }}>
                            <div style={{
                                position: 'absolute', left: 19, top: 0, bottom: 0, width: 2,
                                background: 'linear-gradient(180deg, rgba(124,92,255,0.4), transparent)',
                                borderRadius: 2,
                            }} />

                            <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
                                {filtered.map((commit, i) => (
                                    <div key={commit.revisionNumber} style={{ display: 'flex', gap: 16, alignItems: 'flex-start', paddingBottom: 20 }}>
                                        <div style={{
                                            width: 40, height: 40, borderRadius: '50%', flexShrink: 0, zIndex: 1,
                                            display: 'grid', placeItems: 'center',
                                            background: i === 0
                                                ? 'linear-gradient(135deg,#7c5cff,#22d3ee)'
                                                : 'var(--panel2)',
                                            border: `2px solid ${i === 0 ? '#a78bfa' : 'var(--line)'}`,
                                            fontSize: '0.85rem', fontWeight: 700,
                                        }}>
                                            {i === 0 ? '★' : `#${commit.revisionNumber}`}
                                        </div>

                                        <div style={{
                                            flex: 1, padding: '10px 14px', borderRadius: 14,
                                            background: i === 0 ? 'rgba(124,92,255,0.06)' : 'rgba(255,255,255,0.02)',
                                            border: `1px solid ${i === 0 ? 'rgba(124,92,255,0.2)' : 'var(--line)'}`,
                                        }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
                                                <strong style={{ fontSize: '0.88rem', lineHeight: 1.4 }}>
                                                    {commit.message || <em style={{ color: 'var(--muted)' }}>No message</em>}
                                                </strong>
                                                {i === 0 && (
                                                    <span className="pill" style={{ background: 'rgba(124,92,255,0.15)', color: '#a78bfa', fontSize: '0.7rem', flexShrink: 0 }}>
                            latest
                          </span>
                                                )}
                                            </div>
                                            <div style={{ display: 'flex', gap: 10, marginTop: 8, flexWrap: 'wrap' }}>
                                                <span className="pill" style={{ fontSize: '0.72rem' }}>👤 {commit.username}</span>
                                                <span className="pill" style={{ fontSize: '0.72rem' }}>📌 Rev. #{commit.revisionNumber}</span>
                                                <span className="pill" style={{ fontSize: '0.72rem' }}>🕐 {timeAgo(commit.createdAt)}</span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </AppLayout>
    )
}