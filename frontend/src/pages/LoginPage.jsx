import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { login } from '../components/auth'

export default function LoginPage() {
  const [tab, setTab] = useState('signin')
  const navigate = useNavigate()

  const handleSubmit = (event) => {
    event.preventDefault()
    login()
    navigate('/dashboard')
  }

  return (
    <AuthLayout>
      <div className="tabs">
        <button className={tab === 'signin' ? 'tab active' : 'tab'} onClick={() => setTab('signin')}>
          Sign in
        </button>
        <button className={tab === 'signup' ? 'tab active' : 'tab'} onClick={() => setTab('signup')}>
          Create account
        </button>
      </div>

      {tab === 'signin' ? (
        <form className="form-grid" onSubmit={handleSubmit}>
          <label className="field-label">
            <span>Username</span>
            <div className="field">
              <span>👤</span>
              <input required placeholder="Enter username" />
            </div>
          </label>

          <label className="field-label">
            <span>Password</span>
            <div className="field">
              <span>🔐</span>
              <input required type="password" placeholder="Enter password" />
            </div>
          </label>

          <div className="helper-row">
            <span>Secure authentication</span>
            <span>Forgot password?</span>
          </div>

          <button className="btn primary block" type="submit">Access platform</button>
        </form>
      ) : (
        <form className="form-grid" onSubmit={handleSubmit}>
          <label className="field-label">
            <span>Full name</span>
            <div className="field">
              <span>🪪</span>
              <input required placeholder="Enter full name" />
            </div>
          </label>

          <label className="field-label">
            <span>Email</span>
            <div className="field">
              <span>📧</span>
              <input required type="email" placeholder="Enter email" />
            </div>
          </label>

          <label className="field-label">
            <span>Username</span>
            <div className="field">
              <span>👤</span>
              <input required placeholder="Choose username" />
            </div>
          </label>

          <label className="field-label">
            <span>Role</span>
            <div className="field">
              <span>🧩</span>
              <select defaultValue="Contributor">
                <option>Contributor</option>
                <option>Maintainer</option>
                <option>Administrator</option>
              </select>
            </div>
          </label>

          <label className="field-label">
            <span>Password</span>
            <div className="field">
              <span>🔐</span>
              <input required type="password" placeholder="Create password" />
            </div>
          </label>

          <button className="btn primary block" type="submit">Create account</button>
        </form>
      )}
    </AuthLayout>
  )
}
