import { NavLink, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import { logout } from './auth'

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: '🏠' },
  { to: '/repositories', label: 'Repositories', icon: '📦' },
  { to: '/activity', label: 'Activity', icon: '📈' },
  { to: '/team', label: 'Team', icon: '👥' },
  { to: '/profile', label: 'Profile', icon: '👤' },
]

export default function AppLayout({ subtitle, children }) {
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className="app-shell">
      <div className="glow a"></div>
      <div className="glow b"></div>
      <div className="glow c"></div>

      <header className="topbar">
        <div className="brand">
          <Logo />
          <div>
            <h1>Push and Pray</h1>
            <p>{subtitle}</p>
          </div>
        </div>

        <nav className="topnav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {link.label}
            </NavLink>
          ))}
          <button type="button" onClick={handleLogout}>Logout</button>
        </nav>
      </header>

      <div className="layout">
        <aside className="sidebar">
          <div className="side-group">
            <div className="side-label">Workspace</div>
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) => `side-link${isActive ? ' active' : ''}`}
              >
                <span>{link.icon}</span>
                <span>{link.label}</span>
              </NavLink>
            ))}
          </div>

          <div className="side-group">
            <div className="side-label">Actions</div>
            <a className="side-link" href="/">➕ New repository</a>
            <a className="side-link" href="/">🧩 Manage roles</a>
            <a className="side-link" href="/">📝 View commits</a>
          </div>
        </aside>

        <main className="main">{children}</main>
      </div>

      <nav className="mobile-bottom">
        {links.slice(0, 4).map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            <div>{link.icon}</div>
            {link.label}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
