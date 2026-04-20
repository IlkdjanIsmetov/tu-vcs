import { useState } from 'react'
import AppLayout from '../components/AppLayout'
import { useUser } from '../context/UserContext'
import { profileApi } from '../api/api'
import { logout } from '../components/auth'

const ACTION_WARN = {
  password: {
    color: '#f59e0b',
    bg: 'rgba(245,158,11,0.10)',
    border: 'rgba(245,158,11,0.30)',
    icon: '⚠️',
    text: 'A password-reset link will be sent to your registered email address. Check your inbox (and spam folder) after confirming.',
  },
  logout: {
    color: '#22d3ee',
    bg: 'rgba(34,211,238,0.08)',
    border: 'rgba(34,211,238,0.25)',
    icon: 'ℹ️',
    text: 'This will end your current session on this device. You will need to log in again to continue.',
  },
  delete: {
    color: '#ef4444',
    bg: 'rgba(239,68,68,0.10)',
    border: 'rgba(239,68,68,0.30)',
    icon: '🚨',
    text: 'This action is permanent and cannot be undone. All your data, repositories and memberships will be lost. Please be absolutely sure before proceeding.',
  },
}

function WarningBox({ type }) {
  const w = ACTION_WARN[type]
  return (
      <div style={{
        marginTop: 10,
        padding: '12px 14px',
        borderRadius: 14,
        background: w.bg,
        border: `1px solid ${w.border}`,
        color: w.color,
        fontSize: '0.82rem',
        lineHeight: 1.6,
        display: 'flex',
        gap: 10,
        alignItems: 'flex-start',
      }}>
        <span style={{ fontSize: '1rem', flexShrink: 0 }}>{w.icon}</span>
        <span>{w.text}</span>
      </div>
  )
}

function ActionRow({ icon, label, description, buttonLabel, buttonStyle, onClick, warningType, expanded, onToggle }) {
  return (
      <div className="repo" style={{ marginTop: 10 }}>
        <div className="row-between">
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <span style={{ fontSize: '1.2rem' }}>{icon}</span>
            <div>
              <strong style={{ display: 'block', marginBottom: 2 }}>{label}</strong>
              <small style={{ color: 'var(--muted)', fontSize: '0.8rem' }}>{description}</small>
            </div>
          </div>
          <button
              className="btn secondary"
              style={{ padding: '7px 14px', fontSize: '0.82rem', whiteSpace: 'nowrap', ...buttonStyle }}
              onClick={onToggle || onClick}
          >
            {buttonLabel}
          </button>
        </div>
        {expanded && warningType && <WarningBox type={warningType} />}
        {expanded && onClick && (
            <div style={{ display: 'flex', gap: 8, marginTop: 12, justifyContent: 'flex-end' }}>
              <button className="btn secondary" style={{ padding: '7px 14px', fontSize: '0.82rem' }} onClick={onToggle}>
                Cancel
              </button>
              <button
                  className="btn"
                  style={{ padding: '7px 16px', fontSize: '0.82rem', ...buttonStyle }}
                  onClick={onClick}
              >
                Confirm
              </button>
            </div>
        )}
      </div>
  )
}

export default function ProfilePage() {
  const { user } = useUser()

  const [editing, setEditing]   = useState(false)
  const [saving,  setSaving]    = useState(false)
  const [success, setSuccess]   = useState('')
  const [error,   setError]     = useState('')
  const [expanded, setExpanded] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState('')

  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName:  user?.lastName  || '',
    email:     user?.email     || '',
  })

  const handleEdit = () => {
    setForm({
      firstName: user?.firstName || '',
      lastName:  user?.lastName  || '',
      email:     user?.email     || '',
    })
    setEditing(true)
    setSuccess('')
    setError('')
  }

  const handleCancel = () => { setEditing(false); setError('') }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    const res = await profileApi.update(form)
    if (res.success) {
      setSuccess('Profile updated! Log out and back in to see the changes reflected everywhere.')
      setEditing(false)
    } else {
      setError(res.error)
    }
    setSaving(false)
  }

  const toggle = (key) => {
    setExpanded(prev => prev === key ? null : key)
    setDeleteConfirm('')
  }

  const handleChangePassword = async () => {
    setExpanded(null)
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const res = await fetch('/api/auth/change-password', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access_token')}`,
        },
      })
      const data = await res.json().catch(() => ({}))
      if (res.ok) {
        setSuccess(data.message || 'Password reset email sent. Check your inbox.')
      } else {
        setError(data.message || 'Failed to send reset email.')
      }
    } catch {
      setError('Could not reach the server.')
    }
    setSaving(false)
  }

  const handleDeleteAccount = async () => {
    setExpanded(null)
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const res = await fetch('/api/auth/delete-account', {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access_token')}`,
        },
      })
      if (res.ok) {
        logout()
      } else {
        const data = await res.json().catch(() => ({}))
        setError(data.message || 'Failed to delete account.')
      }
    } catch {
      setError('Could not reach the server.')
    }
    setSaving(false)
  }

  if (!user) {
    return (
        <AppLayout subtitle="User profile">
          <section className="hero">
            <h2>Unable to load profile.</h2>
            <p>Your session may have expired. Please log in again.</p>
          </section>
        </AppLayout>
    )
  }

  return (
      <AppLayout subtitle="User profile">
        <section className="hero">
          <span className="badge">👤 Profile and preferences</span>
          <h2>Profile information</h2>
          <p>View and edit your account details. Changes are saved directly to Keycloak.</p>
        </section>

        {success && <div className="form-success" style={{ marginBottom: 16 }}>{success}</div>}
        {error   && <div className="form-error"   style={{ marginBottom: 16 }}>{error}</div>}

        <section className="grid-2">

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ margin: 0 }}>Account details</h3>
              {!editing && (
                  <button className="btn secondary" style={{ padding: '6px 14px' }} onClick={handleEdit}>
                    ✏️ Edit
                  </button>
              )}
            </div>

            <div className="member" style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', gap: 14, alignItems: 'center' }}>
                <div className="avatar" style={{ width: 52, height: 52, fontSize: '1.1rem' }}>
                  {user.initials}
                </div>
                <div>
                  <strong style={{ fontSize: '1rem' }}>{user.fullName}</strong>
                  <small>{user.displayRole} · {user.email || '—'}</small>
                </div>
              </div>
            </div>

            {!editing && (
                <>
                  {[
                    { label: 'Username',   value: user.username    },
                    { label: 'First name', value: user.firstName   },
                    { label: 'Last name',  value: user.lastName    },
                    { label: 'Email',      value: user.email       },
                  ].map(({ label, value }) => (
                      <div className="repo" style={{ marginTop: 10 }} key={label}>
                        <div className="row-between">
                          <strong>{label}</strong>
                          <span>{value || '—'}</span>
                        </div>
                      </div>
                  ))}
                </>
            )}

            {editing && (
                <form className="form-grid" onSubmit={handleSave}>
                  <label className="field-label">
                    <span>First name</span>
                    <div className="field">
                      <span>🪪</span>
                      <input
                          value={form.firstName}
                          onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                          placeholder="First name"
                      />
                    </div>
                  </label>
                  <label className="field-label">
                    <span>Last name</span>
                    <div className="field">
                      <span>🪪</span>
                      <input
                          value={form.lastName}
                          onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                          placeholder="Last name"
                      />
                    </div>
                  </label>
                  <label className="field-label">
                    <span>Email</span>
                    <div className="field">
                      <span>📧</span>
                      <input
                          type="email"
                          value={form.email}
                          onChange={(e) => setForm({ ...form, email: e.target.value })}
                          placeholder="Email address"
                      />
                    </div>
                  </label>
                  <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
                    <button className="btn primary" type="submit" disabled={saving}>
                      {saving ? 'Saving…' : 'Save changes'}
                    </button>
                    <button className="btn secondary" type="button" onClick={handleCancel}>
                      Cancel
                    </button>
                  </div>
                </form>
            )}
          </div>

          <div className="card">
            <h3 style={{ margin: '0 0 4px' }}>Account actions</h3>
            <p className="sub" style={{ marginTop: 0, marginBottom: 4 }}>
              Manage security and account lifecycle. Some actions are irreversible — read the warnings carefully.
            </p>

            <ActionRow
                icon="🔑"
                label="Change password"
                description="Reset your password via the Keycloak login page."
                buttonLabel="Change"
                buttonStyle={{ color: '#f59e0b', borderColor: 'rgba(245,158,11,0.30)' }}
                warningType="password"
                expanded={expanded === 'password'}
                onToggle={() => toggle('password')}
                onClick={handleChangePassword}
            />

            <ActionRow
                icon="🚪"
                label="Log out"
                description="End your current session on this device."
                buttonLabel="Log out"
                warningType="logout"
                expanded={expanded === 'logout'}
                onToggle={() => toggle('logout')}
                onClick={logout}
            />

            <div className="repo" style={{ marginTop: 10 }}>
              <div className="row-between">
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                  <span style={{ fontSize: '1.2rem' }}>🗑️</span>
                  <div>
                    <strong style={{ display: 'block', marginBottom: 2 }}>Delete account</strong>
                    <small style={{ color: 'var(--muted)', fontSize: '0.8rem' }}>
                      Permanently remove your account and all associated data.
                    </small>
                  </div>
                </div>
                <button
                    className="btn secondary"
                    style={{ padding: '7px 14px', fontSize: '0.82rem', color: '#ef4444', borderColor: 'rgba(239,68,68,0.30)', whiteSpace: 'nowrap' }}
                    onClick={() => toggle('delete')}
                >
                  Delete
                </button>
              </div>

              {expanded === 'delete' && (
                  <>
                    <div style={{
                      marginTop: 10, padding: '12px 14px', borderRadius: 14,
                      background: 'rgba(239,68,68,0.10)', border: '1px solid rgba(239,68,68,0.30)',
                      color: '#ef4444', fontSize: '0.82rem', lineHeight: 1.6,
                      display: 'flex', gap: 10, alignItems: 'flex-start',
                    }}>
                      <span style={{ fontSize: '1rem', flexShrink: 0 }}>🚨</span>
                      <span>This action is <strong>permanent and cannot be undone</strong>. All your data, repositories and memberships will be lost.</span>
                    </div>

                    <div style={{ marginTop: 12 }}>
                      <p style={{ margin: '0 0 8px', fontSize: '0.82rem', color: 'var(--muted)' }}>
                        Type <strong style={{ color: '#ef4444' }}>DELETE</strong> to confirm:
                      </p>
                      <div className="field" style={{ borderColor: deleteConfirm === 'DELETE' ? 'rgba(239,68,68,0.5)' : undefined }}>
                        <input
                            value={deleteConfirm}
                            onChange={(e) => setDeleteConfirm(e.target.value)}
                            placeholder="Type DELETE here"
                            style={{ color: deleteConfirm === 'DELETE' ? '#ef4444' : undefined }}
                        />
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: 8, marginTop: 12, justifyContent: 'flex-end' }}>
                      <button className="btn secondary" style={{ padding: '7px 14px', fontSize: '0.82rem' }} onClick={() => toggle('delete')}>
                        Cancel
                      </button>
                      <button
                          className="btn"
                          disabled={deleteConfirm !== 'DELETE' || saving}
                          style={{
                            padding: '7px 16px', fontSize: '0.82rem',
                            color: '#fff',
                            background: deleteConfirm === 'DELETE' ? '#ef4444' : 'rgba(239,68,68,0.3)',
                            cursor: deleteConfirm === 'DELETE' ? 'pointer' : 'not-allowed',
                            opacity: deleteConfirm === 'DELETE' ? 1 : 0.6,
                          }}
                          onClick={handleDeleteAccount}
                      >
                        {saving ? 'Deleting…' : 'Confirm delete'}
                      </button>
                    </div>
                  </>
              )}
            </div>

            <div style={{
              marginTop: 20,
              padding: '12px 14px',
              background: 'rgba(255,255,255,0.03)',
              borderRadius: 14,
              border: '1px solid var(--line)',
            }}>
              <p className="sub" style={{ margin: 0, fontSize: '0.8rem' }}>
                ℹ️ Username cannot be changed directly. To transfer ownership of repositories, contact an administrator.
              </p>
            </div>
          </div>

        </section>
      </AppLayout>
  )
}