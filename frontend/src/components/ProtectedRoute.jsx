import { Navigate } from 'react-router-dom'
import { getAccessToken, isTokenExpired } from './auth'

export default function ProtectedRoute({ children }) {
  const token = getAccessToken()

  if (!token || isTokenExpired()) {
    return <Navigate to="/" replace />
  }

  return children
}
