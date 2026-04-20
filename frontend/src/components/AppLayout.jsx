import { NavLink, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import { logout } from './auth'
import { useUser } from '../context/UserContext'

const links = [
    { to: '/dashboard',    label: 'Dashboard',    icon: '🏠' },
    { to: '/repositories', label: 'Repositories', icon: '📦' },
    { to: '/activity',     label: 'Activity',     icon: '📈' },
    { to: '/team',         label: 'Team',         icon: '👥' },
    { to: '/profile',      label: 'Profile',      icon: '👤' },
]

export default function AppLayout({ subtitle, children }) {
    const navigate = useNavigate()
    const { user } = useUser()

    const handleLogout = () => {
        logout()
        navigate('/')
    }

    return (
        <div className="app-shell">
            <div className="glow a" />
            <div className="glow b" />
            <div className="glow c" />

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
                    {user && (
                        <div className="side-user">
                            <div className="avatar">{user.initials}</div>
                            <div className="side-user-info">
                                <strong>{user.fullName}</strong>
                                <small>{user.email}</small>
                            </div>
                        </div>
                    )}

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
                        <NavLink to="/repositories" className="side-link">➕ New repository</NavLink>
                        <NavLink to="/team"         className="side-link">🧩 Manage roles</NavLink>
                        <NavLink to="/activity"     className="side-link">📝 View commits</NavLink>
                    </div>

                </aside>

                <main className="main">{children}</main>
            </div>
        </div>
    )
}
