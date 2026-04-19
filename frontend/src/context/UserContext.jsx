import { createContext, useContext, useState, useEffect } from 'react'
import { getUserInfo, getAccessToken, isTokenExpired } from '../components/auth'

const UserContext = createContext(null)

export function UserProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = getAccessToken()
    if (token && !isTokenExpired()) {
      setUser(getUserInfo())
    } else {
      setUser(null)
    }
    setLoading(false)
  }, [])

  // Call this after login so all pages immediately get user info
  const refreshUser = () => {
    setUser(getUserInfo())
  }

  return (
    <UserContext.Provider value={{ user, loading, refreshUser }}>
      {children}
    </UserContext.Provider>
  )
}

// Convenience hook
export function useUser() {
  return useContext(UserContext)
}
