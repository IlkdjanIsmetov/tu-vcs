import AppLayout from '../components/AppLayout'
import { useUser } from '../context/UserContext'

export default function ProfilePage() {
  const { user } = useUser()

  if (!user) {
    return (
        <AppLayout subtitle="User profile">
          <section className="hero">
            <h2>Unable to load profile.</h2>
            <p>Your session may have expired. Please log in again.</p>
          </section>
        </AppLayout>
    )
  }

  return (
      <AppLayout subtitle="User profile">
        <section className="hero">
          <span className="badge">👤 Profile and preferences</span>
          <h2>User and security overview.</h2>
          <p>Structured profile page with account settings, role visibility and authentication status.</p>
        </section>

        <section className="grid-2">

          <div className="card">
            <h3>Account details</h3>
            <div className="member">
              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <div className="avatar">{user.initials}</div>
                <div>
                  <strong>{user.fullName}</strong>
                  <small>{user.displayRole} · {user.email}</small>
                </div>
              </div>
            </div>

            <div className="repo" style={{ marginTop: '12px' }}>
              <div className="row-between">
                <strong>Username</strong>
                <span>{user.username}</span>
              </div>
            </div>

            <div className="repo" style={{ marginTop: '12px' }}>
              <div className="row-between">
                <strong>Email</strong>
                <span>{user.email || '—'}</span>
              </div>
            </div>

            <div className="repo" style={{ marginTop: '12px' }}>
              <div className="row-between">
                <strong>Current role</strong>
                <span className="pill">{user.displayRole}</span>
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
            <h3>Roles &amp; permissions</h3>
            <p className="sub">All roles assigned to your account in this realm.</p>

            {user.roles.length > 0 ? (
                user.roles.map((role) => (
                    <div className="repo" style={{ marginTop: '12px' }} key={role}>
                      <div className="row-between">
                        <strong>{role}</strong>
                        <span className="pill">Active</span>
                      </div>
                    </div>
                ))
            ) : (
                <div className="repo" style={{ marginTop: '12px' }}>
                  <p className="sub">No roles assigned.</p>
                </div>
            )}
          </div>

        </section>
      </AppLayout>
  )
}
