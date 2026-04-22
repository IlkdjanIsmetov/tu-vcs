import { NavLink, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import { logout } from './auth'
import { useUser } from '../context/UserContext'

const workspaceLinks = [
    { to: '/dashboard',    label: 'Dashboard',    icon: '🏠' },
    { to: '/repositories', label: 'Repositories', icon: '📦' },
    { to: '/activity',     label: 'Activity',     icon: '📈' },
    { to: '/profile',      label: 'Profile',      icon: '👤' },
]

const topNavLinks = [
    { to: '/dashboard',    label: 'Dashboard' },
    { to: '/repositories', label: 'Repositories' },
    { to: '/activity',     label: 'Activity' },
    { to: '/team',         label: 'Team' },
    { to: '/profile',      label: 'Profile' },
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
                    {topNavLinks.map((link) => (
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
                        {workspaceLinks.map((link) => (
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
                        <NavLink
                            to="/repositories/new"
                            className={({ isActive }) => `side-link${isActive ? ' active' : ''}`}
                        >
                            <span>➕</span>
                            <span>New repository</span>
                        </NavLink>
                        <NavLink
                            to="/team"
                            className={({ isActive }) => `side-link${isActive ? ' active' : ''}`}
                        >
                            <span>👥</span>
                            <span>Manage team</span>
                        </NavLink>
                        <NavLink
                            to="/activity"
                            className={({ isActive }) => `side-link${isActive ? ' active' : ''}`}
                        >
                            <span>📝</span>
                            <span>View commits</span>
                        </NavLink>
                    </div>
                </aside>

                <main className="main">{children}</main>
            </div>
        </div>
    )
}