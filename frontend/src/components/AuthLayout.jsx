import Logo from './Logo'

export default function AuthLayout({
  children,
  selectedTab,
  onSelectTab,
  showForm,
}) {
  return (
    <div className={showForm ? 'auth-wrap auth-wrap--split' : 'auth-wrap auth-wrap--center'}>
      <section className="auth-left">
        <div className="hero">
        <span className="badge">TU-VCS</span>
          <h2>Build together. Ship faster.</h2>
           <p>
             Web workspace for repository management, authentication,
             team collaboration, activity tracking and profile visualization.
           </p>

          {!showForm && (
            <div className="hero-actions">
              <button
                className="btn primary"
                type="button"
                onClick={() => onSelectTab('signin')}
              >
                Sign in
              </button>

              <button
                className="btn secondary"
                type="button"
                onClick={() => onSelectTab('signup')}
              >
                Create account
              </button>
            </div>
          )}
        </div>
      </section>

      {showForm && (
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
              Your data is securely protected with Keycloak authentication and access control.
          </div>
        </section>
      )}
    </div>
  )
}