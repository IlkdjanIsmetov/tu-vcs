import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { login, registerUser, forgotPassword } from '../components/auth'
import { useUser } from '../context/UserContext'

export default function LoginPage() {
  const [tab,     setTab]     = useState('login')
  const [showForm, setShowForm] = useState(false)
  const [error,   setError]   = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { refreshUser } = useUser()

  const [formData, setFormData] = useState({
    username: '', password: '', email: '', firstName: '', lastName: '',
  })

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    setError('')
    setMessage('')
  }

  const handleTabChange = (newTab) => {
    setTab(newTab)
    setShowForm(true)
    setError('')
    setMessage('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    setLoading(true)

    if (tab === 'login') {
      const res = await login(formData.username, formData.password)
      if (res.success) {
        refreshUser()
        navigate('/dashboard')
      } else {
        setError(res.error)
      }

    } else if (tab === 'signup') {
      if (!formData.firstName.trim()) {
        setError('First name is required.')
        setLoading(false)
        return
      }
      const res = await registerUser(formData)
      if (res.success) {
        setMessage('Account created! You can now log in.')
        handleTabChange('login')
      } else {
        setError(res.error)
      }

    } else if (tab === 'forgot') {
      if (!formData.email.trim()) {
        setError('Please enter your email address.')
        setLoading(false)
        return
      }
      await forgotPassword(formData.email)
      setMessage('If that email exists, you will receive reset instructions shortly.')
    }

    setLoading(false)
  }

  return (
      <AuthLayout showForm={showForm} selectedTab={tab} onSelectTab={handleTabChange}>

        <div className="tabs">
          <button
              type="button"
              className={tab === 'login' ? 'tab active' : 'tab'}
              onClick={() => handleTabChange('login')}
          >
            Log in
          </button>
          <button
              type="button"
              className={tab === 'signup' ? 'tab active' : 'tab'}
              onClick={() => handleTabChange('signup')}
          >
            Create account
          </button>
        </div>

        <form className="form-grid" onSubmit={handleSubmit}>

          {error   && <div className="form-error">{error}</div>}
          {message && <div className="form-success">{message}</div>}

          {tab === 'signup' && (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                <label className="field-label">
                  <span>First name</span>
                  <div className="field">
                    <span>🪪</span>
                    <input
                        name="firstName"
                        required
                        placeholder="First name"
                        value={formData.firstName}
                        onChange={handleChange}
                    />
                  </div>
                </label>
                <label className="field-label">
                  <span>Last name</span>
                  <div className="field">
                    <span>🪪</span>
                    <input
                        name="lastName"
                        required
                        placeholder="Last name"
                        value={formData.lastName}
                        onChange={handleChange}
                    />
                  </div>
                </label>
              </div>
          )}

          {(tab === 'signup' || tab === 'forgot') && (
              <label className="field-label">
                <span>Email</span>
                <div className="field">
                  <span>📧</span>
                  <input
                      name="email"
                      type="email"
                      required
                      placeholder="Enter email"
                      value={formData.email}
                      onChange={handleChange}
                  />
                </div>
              </label>
          )}

          {(tab === 'login' || tab === 'signup') && (
              <label className="field-label">
                <span>Username</span>
                <div className="field">
                  <span>👤</span>
                  <input
                      name="username"
                      required
                      placeholder="Enter username"
                      value={formData.username}
                      onChange={handleChange}
                  />
                </div>
              </label>
          )}

          {(tab === 'login' || tab === 'signup') && (
              <label className="field-label">
                <span>Password</span>
                <div className="field">
                  <span>🔐</span>
                  <input
                      name="password"
                      type="password"
                      required
                      placeholder="••••••••"
                      value={formData.password}
                      onChange={handleChange}
                  />
                </div>
              </label>
          )}

          {tab === 'login' && (
              <div className="helper-row">
                <span>Secure authentication</span>
                <span
                    style={{ cursor: 'pointer', textDecoration: 'underline' }}
                    onClick={() => handleTabChange('forgot')}
                >
              Forgot password?
            </span>
              </div>
          )}

          <button className="btn primary block" type="submit" disabled={loading}>
            {loading ? 'Please wait…' : (
                tab === 'login'  ? 'Access platform' :
                    tab === 'signup' ? 'Create account'  :
                        'Send reset email'
            )}
          </button>

          {tab === 'forgot' && (
              <button
                  type="button"
                  className="btn secondary block"
                  onClick={() => handleTabChange('login')}
              >
                Back to log in
              </button>
          )}

        </form>
      </AuthLayout>
  )
}