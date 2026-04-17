import AppLayout from '../components/AppLayout'
import { profile } from '../data/mockData'

export default function ProfilePage() {
  return (
    <AppLayout subtitle="User profile">
      <section className="hero">
        <span className="badge">👤 Profile and preferences</span>
        <h2>User and security overview.</h2>
        <p>Structured profile page prepared for account settings, role visibility and authentication status.</p>
      </section>

      <section className="grid-2">
        <div className="card">
          <h3>Account details</h3>
          <div className="member">
            <div style={{ display: 'flex', gap: '12px' }}>
              <div className="avatar">AP</div>
              <div>
                <strong>{profile.name}</strong>
                <small>{profile.role} · {profile.email}</small>
              </div>
            </div>
          </div>
          <div className="repo" style={{ marginTop: '12px' }}>
            <div className="row-between">
              <strong>Current role</strong>
              <span className="pill">{profile.role}</span>
            </div>
          </div>
          <div className="repo" style={{ marginTop: '12px' }}>
            <div className="row-between">
              <strong>Authentication</strong>
              <span className="pill">Active session</span>
            </div>
          </div>
        </div>

        <div className="card">
          <h3>Workspace summary</h3>
          <div className="repo">
            <div className="row-between">
              <strong>Owned repositories</strong>
              <span>{profile.ownedRepositories}</span>
            </div>
          </div>
          <div className="repo" style={{ marginTop: '12px' }}>
            <div className="row-between">
              <strong>Collaborative projects</strong>
              <span>{profile.collaborativeProjects}</span>
            </div>
          </div>
          <div className="repo" style={{ marginTop: '12px' }}>
            <div className="row-between">
              <strong>Pending approvals</strong>
              <span>{profile.pendingApprovals}</span>
            </div>
          </div>
        </div>
      </section>
    </AppLayout>
  )
}
