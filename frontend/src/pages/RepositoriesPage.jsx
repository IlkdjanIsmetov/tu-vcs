import AppLayout from '../components/AppLayout'
import { repositories } from '../data/mockData'

export default function RepositoriesPage() {
  return (
    <AppLayout subtitle="Repository management">
      <section className="hero">
        <span className="badge">📦 Repository management</span>
        <h2>Repositories and metadata.</h2>
        <p>
          Structured views for repository visibility, ownership,
          update history and operational status.
        </p>
        <div className="hero-actions">
          <button className="btn primary">New repository</button>
          <button className="btn secondary">Import project</button>
        </div>
      </section>

      <section className="card">
        <h3>All repositories</h3>
        <p className="sub">Professional layout prepared for create, update, fetch and visibility operations.</p>
        <div className="table">
          <div className="table-header">
            <div>Name</div>
            <div>Visibility</div>
            <div>Language</div>
            <div>Updated</div>
          </div>
          {repositories.map((repo) => (
            <div className="table-row" key={repo.name}>
              <div>{repo.name}</div>
              <div><span className="pill">{repo.visibility}</span></div>
              <div>{repo.language}</div>
              <div>{repo.updated}</div>
            </div>
          ))}
        </div>
      </section>
    </AppLayout>
  )
}
