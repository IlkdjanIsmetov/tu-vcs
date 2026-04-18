import AppLayout from '../components/AppLayout'
import { activity } from '../data/mockData'

export default function ActivityPage() {
  return (
    <AppLayout subtitle="Activity tracking">
      <section className="hero">
        <span className="badge">📈 Real-time updates</span>
        <h2>Track repository activity.</h2>
        <p>Monitor commit history, permission changes, repository updates and collaboration events.</p>
      </section>

      <section className="card">
        <h3>Activity timeline</h3>
        {activity.map((item) => (
          <div className="feed" style={{ marginTop: '12px' }} key={item.title}>
            <div className="feed-ico">{item.icon}</div>
            <div>
              <strong>{item.title}</strong>
              <small>{item.text}</small>
            </div>
          </div>
        ))}
      </section>
    </AppLayout>
  )
}
