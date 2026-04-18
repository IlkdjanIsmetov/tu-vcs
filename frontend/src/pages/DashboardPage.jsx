import AppLayout from '../components/AppLayout'
import { activity, repositories, stats } from '../data/mockData'

export default function DashboardPage() {
  return (
    <AppLayout subtitle="Build together. Ship faster.">
      <section className="hero">
        <span className="badge">TU-VCS</span>
        <h2>Build together. Ship faster.</h2>
        <p>
          Web workspace for repository management, authentication,
          team collaboration, activity tracking and profile visualization.
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
          <h3>Repositories</h3>
          <p className="sub">Key repositories in the current workspace.</p>
          {repositories.slice(0, 2).map((repo) => (
            <div className="repo" style={{ marginTop: '12px' }} key={repo.name}>
              <div className="repo-top">
                <div>
                  <h4>{repo.name}</h4>
                  <p>{repo.description}</p>
                </div>
                <span className="pill">{repo.visibility}</span>
              </div>
              <div className="meta">
                <span>{repo.language}</span>
                <span>⭐ {repo.stars}</span>
                <span>{repo.updated}</span>
              </div>
            </div>
          ))}
        </div>

        <div className="card">
          <h3>Recent activity</h3>
          <p className="sub">Latest actions across repositories and users.</p>
          {activity.slice(0, 3).map((item) => (
            <div className="feed" style={{ marginTop: '12px' }} key={item.title}>
              <div className="feed-ico">{item.icon}</div>
              <div>
                <strong>{item.title}</strong>
                <small>{item.text}</small>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="grid-3">
        <div className="card feature">
          <strong>Authentication module</strong>
          <p>Prepared for username/password login, session handling and role-based access integration.</p>
        </div>
        <div className="card feature">
          <strong>Repository lifecycle</strong>
          <p>Supports create, update, fetch, clone, delete and metadata-oriented repository views.</p>
        </div>
        <div className="card feature">
          <strong>Team collaboration</strong>
          <p>Designed for contributors, maintainers and administrators with clear workflow separation.</p>
        </div>
      </section>
    </AppLayout>
  )
}
