import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { repositoryApi } from '../api/api'

export default function ActivityPage() {
    const [repos,   setRepos]   = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        repositoryApi.getAll().then((data) => {
            setRepos(data ?? [])
            setLoading(false)
        })
    }, [])

    const activity = repos.flatMap((repo) => {
        const events = []
        if (repo.revision > 0) {
            events.push({
                icon: '📝',
                title: `${repo.name} — revision ${repo.revision}`,
                text:  `Latest committed revision in this repository.`,
                repo:  repo.name,
            })
        }
        if (repo.requireApproval) {
            events.push({
                icon: '🔒',
                title: `${repo.name} — approval required`,
                text:  `All commits to this repository require maintainer approval.`,
                repo:  repo.name,
            })
        }
        return events
    })

    const totalRevisions = repos.reduce((s, r) => s + (r.revision ?? 0), 0)

    return (
        <AppLayout subtitle="Activity tracking">
            <section className="hero">
                <span className="badge">📈 Real-time updates</span>
                <h2>Track repository activity.</h2>
                <p>Monitor commit history, revision counts and repository status across your workspace.</p>
            </section>

            <section className="grid-4">
                <div className="card stat">
                    <span>Repositories</span>
                    <strong>{loading ? '…' : repos.length}</strong>
                </div>
                <div className="card stat">
                    <span>Total revisions</span>
                    <strong>{loading ? '…' : totalRevisions}</strong>
                </div>
                <div className="card stat">
                    <span>Protected repos</span>
                    <strong>{loading ? '…' : repos.filter((r) => r.requireApproval).length}</strong>
                </div>
                <div className="card stat">
                    <span>Open repos</span>
                    <strong>{loading ? '…' : repos.filter((r) => !r.requireApproval).length}</strong>
                </div>
            </section>

            <section className="card">
                <h3>Repository status</h3>
                <p className="sub">Current revision and protection status for each repository.</p>

                {loading && <p className="sub" style={{ marginTop: 12 }}>Loading…</p>}
                {!loading && repos.length === 0 && (
                    <p className="sub" style={{ marginTop: 12 }}>No repositories found.</p>
                )}

                {!loading && repos.length > 0 && (
                    <div className="table" style={{ marginTop: 16 }}>
                        <div className="table-header">
                            <div>Repository</div>
                            <div>Description</div>
                            <div>Revisions</div>
                            <div>Status</div>
                        </div>
                        {repos.map((repo) => (
                            <div className="table-row" key={repo.id}>
                                <div><strong>{repo.name}</strong></div>
                                <div style={{ color: 'var(--muted, #888)', fontSize: '0.85rem' }}>
                                    {repo.description || '—'}
                                </div>
                                <div>
                                    <span className="pill">Rev. {repo.revision ?? 0}</span>
                                </div>
                                <div>
                  <span
                      className="pill"
                      style={repo.requireApproval
                          ? { background: 'rgba(251,191,36,0.15)', color: '#fbbf24' }
                          : { background: 'rgba(34,197,94,0.15)', color: '#22c55e' }}
                  >
                    {repo.requireApproval ? '🔒 Protected' : '✅ Open'}
                  </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {activity.length > 0 && (
                <section className="card">
                    <h3>Activity feed</h3>
                    <p className="sub">Events derived from repository state.</p>
                    {activity.map((item, i) => (
                        <div className="feed" style={{ marginTop: '12px' }} key={i}>
                            <div className="feed-ico">{item.icon}</div>
                            <div>
                                <strong>{item.title}</strong>
                                <small>{item.text}</small>
                            </div>
                        </div>
                    ))}
                </section>
            )}
        </AppLayout>
    )
}
