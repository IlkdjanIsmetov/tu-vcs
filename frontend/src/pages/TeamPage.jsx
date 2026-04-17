import AppLayout from '../components/AppLayout'
import { teamMembers } from '../data/mockData'

export default function TeamPage() {
  return (
    <AppLayout subtitle="Team and access roles">
      <section className="hero">
        <span className="badge">👥 Collaboration</span>
        <h2>Manage users and roles.</h2>
        <p>Visual structure for contributors, maintainers and administrators with access-oriented workflows.</p>
        <div className="hero-actions">
          <button className="btn primary">Invite member</button>
          <button className="btn secondary">Edit permissions</button>
        </div>
      </section>

      <section className="grid-2">
        {teamMembers.map((member) => (
          <div className="member" key={member.name}>
            <div className="row-between">
              <div style={{ display: 'flex', gap: '12px' }}>
                <div className="avatar">{member.initials}</div>
                <div>
                  <strong>{member.name}</strong>
                  <small>{member.description}</small>
                </div>
              </div>
              <span className="pill">{member.role}</span>
            </div>
          </div>
        ))}
      </section>
    </AppLayout>
  )
}
