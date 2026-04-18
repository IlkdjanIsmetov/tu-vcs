export const AUTH_KEY = 'push_pray_react_auth'

export function login() {
  localStorage.setItem(AUTH_KEY, 'true')
}

export function logout() {
  localStorage.removeItem(AUTH_KEY)
}

export function isAuthenticated() {
  return localStorage.getItem(AUTH_KEY) === 'true'
}
