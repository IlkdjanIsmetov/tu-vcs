import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage           from './pages/LoginPage'
import DashboardPage       from './pages/DashboardPage'
import ProfilePage         from './pages/ProfilePage'
import RepositoriesPage    from './pages/RepositoriesPage'
import NewRepositoryPage   from './pages/NewRepositoryPage'
import TeamPage            from './pages/TeamPage'
import ChangeRequestsPage  from './pages/ChangeRequestsPage'
import HistoryPage         from './pages/HistoryPage'

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
            <Route path="/repositories/new" element={
                <ProtectedRoute><NewRepositoryPage /></ProtectedRoute>
            } />
            <Route path="/change-requests" element={
                <ProtectedRoute><ChangeRequestsPage /></ProtectedRoute>
            } />
            <Route path="/history" element={
                <ProtectedRoute><HistoryPage /></ProtectedRoute>
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