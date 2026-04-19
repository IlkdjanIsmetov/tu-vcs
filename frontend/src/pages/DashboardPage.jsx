import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { useUser } from '../context/UserContext'
import { repositoryApi } from '../api/api'

export default function DashboardPage() {
    const { user } = useUser()
    const [repos, setRepos]   = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        repositoryApi.getAll().then((data) => {
            setRepos(data ?? [])
            setLoading(false)
        })
    }, [])

    const myRepos  = repos.filter((r) => true)
    const totalRev = repos.reduce((sum, r) => sum + (r.revision || 0), 0)

    const stats = [
        { label: 'Repositories',    value: loading ? '…' : repos.length },
        { label: 'Total revisions', value: loading ? '…' : totalRev },
        { label: 'Approval repos',  value: loading ? '…' : repos.filter((r) => r.requireApproval).length },
        { label: 'Logged in as',    value: user?.username || '…' },
    ]

    return (
        <AppLayout subtitle="Build together. Ship faster.">
            <section className="hero">
                <span className="badge">TU-VCS</span>
                <h2>Welcome back{user ? `, ${user.firstName || user.username}` : ''}.</h2>
                <p>
                    Your web workspace for repository management, authentication,
                    team collaboration, activity tracking and profile visualization is ready.
                </p>
            </section>


            <section className="grid-4">
                {stats.map((item) => (
                    <div className="card stat" key={item.label}>
                        <span>{item.label}</span>
                        <strong>{item.value}</strong>
                    </div>
                ))}
            </section>

            <section className="grid-2">
                <div className="card">
                    <h3>Recent repositories</h3>
                    <p className="sub">Latest repositories in the workspace.</p>
                    {loading && <p className="sub" style={{ marginTop: 12 }}>Loading…</p>}
                    {!loading && repos.length === 0 && (
                        <p className="sub" style={{ marginTop: 12 }}>No repositories yet.</p>
                    )}
                    {repos.slice(0, 3).map((repo) => (
                        <div className="repo" style={{ marginTop: '12px' }} key={repo.id}>
                            <div className="repo-top">
                                <div>
                                    <h4>{repo.name}</h4>
                                    <p>{repo.description || 'No description.'}</p>
                                </div>
                                <span className="pill">{repo.requireApproval ? 'Approval' : 'Direct'}</span>
                            </div>
                            <div className="meta">
                                <span>Rev. {repo.revision ?? 0}</span>
                                <span>{repo.requireApproval ? '🔒 Protected' : '✅ Open'}</span>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="card">
                    <h3>Your account</h3>
                    <p className="sub">Current session details.</p>

                    {[
                        { label: 'Username',    value: user?.username    || '—' },
                        { label: 'Email',       value: user?.email       || '—' },
                        { label: 'Role',        value: user?.displayRole || '—' },
                        { label: 'Full name',   value: user?.fullName    || '—' },
                    ].map(({ label, value }) => (
                        <div className="repo" style={{ marginTop: '12px' }} key={label}>
                            <div className="row-between">
                                <strong>{label}</strong>
                                <span>{value}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            <section className="grid-3">
                <div className="card feature">
                    <strong>Authentication module</strong>
                    <p>Username/password login via Keycloak with JWT session handling and role-based access.</p>
                </div>
                <div className="card feature">
                    <strong>Repository lifecycle</strong>
                    <p>Create, commit, fetch, clone and delete repositories with revision tracking.</p>
                </div>
                <div className="card feature">
                    <strong>Team collaboration</strong>
                    <p>Add members as MASTER, CONTRIBUTOR or VIEWER with change request approval flows.</p>
                </div>
            </section>
        </AppLayout>
    )
}
