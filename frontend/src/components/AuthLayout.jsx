import Logo from './Logo'

export default function AuthLayout({ children }) {
  return (
    <div className="auth-wrap">
      <section className="auth-left">
        <div className="hero">
          <span className="badge">🚀 Full React frontend</span>
          <h2>Build together. Ship faster.</h2>
          <p>
            Complete React interface for repository management, authentication,
            team collaboration, activity tracking and profile visualization.
          </p>
          <div className="hero-actions">
            <button className="btn primary" type="button">Sign in</button>
            <button className="btn secondary" type="button">Create account</button>
          </div>
        </div>
      </section>

      <section className="auth-card">
        <div className="brand auth-brand">
          <Logo />
          <div>
            <h1>Push and Pray</h1>
            <p>Version control platform</p>
          </div>
        </div>
        {children}
        <div className="footer-note">
          React + Vite frontend. Sign in opens the full application dashboard.
        </div>
      </section>
    </div>
  )
}
