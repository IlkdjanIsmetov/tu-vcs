import Logo from './Logo'

export default function AuthLayout({
                                       children,
                                       selectedTab,
                                       onSelectTab,
                                       showForm,
                                   }) {
    return (
        <div className={showForm ? 'auth-wrap auth-wrap--split' : 'auth-wrap auth-wrap--center'}
             style={{ transition: 'all 0.4s ease', ...(!showForm ? { maxWidth: '1200px' } : {}) }}>

            <section className="auth-left">
                <div className="hero">
                    <span className="badge">TU-VCS</span>
                    <h2 style={!showForm ? { whiteSpace: "nowrap" } : {}}>Build together. Ship faster.</h2>
                    <p>
                        Web workspace for repository management, team collaboration, activity tracking and profile visualization.
                    </p>

                    {!showForm && (
                        <div className="hero-actions">
                            <button
                                className="btn primary"
                                type="button"
                                onClick={() => onSelectTab('login')}
                            >
                                Log in
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

            <section
                className="auth-card"
                style={{
                    opacity: showForm ? 1 : 0,
                    transform: showForm ? 'translateX(0)' : 'translateX(40px)',
                    transition: 'opacity 0.35s ease, transform 0.35s ease',
                    pointerEvents: showForm ? 'auto' : 'none',
                    display: showForm ? undefined : 'none',
                }}
            >
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

        </div>
    )
}