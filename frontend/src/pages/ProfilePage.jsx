import { useState } from 'react'
import AppLayout from '../components/AppLayout'
import { useUser } from '../context/UserContext'
import { profileApi } from '../api/api'

export default function ProfilePage() {
  const { user, refreshUser } = useUser()

  const [editing, setEditing] = useState(false)
  const [saving,  setSaving]  = useState(false)
  const [success, setSuccess] = useState('')
  const [error,   setError]   = useState('')

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

  const handleCancel = () => {
    setEditing(false)
    setError('')
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')

    const res = await profileApi.update(form)

    if (res.success) {
      setSuccess('Profile updated! Please log out and log back in to see the changes reflected everywhere.')
      setEditing(false)
    } else {
      setError(res.error)
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
          <span className="badge">👤 Profile</span>
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
                    { label: 'Role',       value: user.displayRole },
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
            <h3>Roles &amp; permissions</h3>
            <p className="sub">All Keycloak roles assigned to your account.</p>

            <div className="repo" style={{ marginTop: 12 }}>
              <div className="row-between">
                <strong>Authentication</strong>
                <span className="pill" style={{ background: 'rgba(34,197,94,0.15)', color: '#22c55e' }}>
                ✅ Active session
              </span>
              </div>
            </div>

            {user.roles.filter((r) => !r.startsWith('default-roles')).map((role) => (
                <div className="repo" style={{ marginTop: 10 }} key={role}>
                  <div className="row-between">
                    <strong>{role}</strong>
                    <span className="pill">Active</span>
                  </div>
                </div>
            ))}

            {user.roles.filter((r) => !r.startsWith('default-roles')).length === 0 && (
                <div className="repo" style={{ marginTop: 12 }}>
                  <p className="sub">No custom roles assigned.</p>
                </div>
            )}

            <div style={{ marginTop: 20, padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: 8 }}>
              <p className="sub" style={{ margin: 0, fontSize: '0.8rem' }}>
                ℹ️ Username cannot be changed here. To change your password, use "Forgot password" on the login page.
              </p>
            </div>
          </div>

        </section>
      </AppLayout>
  )
}
