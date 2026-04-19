import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage       from './pages/LoginPage'
import DashboardPage   from './pages/DashboardPage'
import ActivityPage    from './pages/ActivityPage'
import ProfilePage     from './pages/ProfilePage'
import RepositoriesPage from './pages/RepositoriesPage'
import TeamPage        from './pages/TeamPage'

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<LoginPage />} />

            <Route path="/dashboard" element={
                <ProtectedRoute><DashboardPage /></ProtectedRoute>
            } />
            <Route path="/repositories" element={
                <ProtectedRoute><RepositoriesPage /></ProtectedRoute>
            } />
            <Route path="/activity" element={
                <ProtectedRoute><ActivityPage /></ProtectedRoute>
            } />
            <Route path="/team" element={
                <ProtectedRoute><TeamPage /></ProtectedRoute>
            } />
            <Route path="/profile" element={
                <ProtectedRoute><ProfilePage /></ProtectedRoute>
            } />

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}
