const KEYCLOAK_URL = "http://localhost:8081";
const BACKEND_URL  = "http://localhost:8080";
const REALM        = "vcs-realm";
const CLIENT_ID    = "vcs-spring-client";


export const login = async (username, password) => {
  const url = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`;
  const params = new URLSearchParams();
  params.append('client_id',  CLIENT_ID);
  params.append('grant_type', 'password');
  params.append('username',   username);
  params.append('password',   password);
  params.append('scope',      'openid');

  try {
    const response = await fetch(url, {
      method:  'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:    params,
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('access_token', data.access_token);
      return { success: true };
    }
    return { success: false, error: 'Wrong username or password.' };
  } catch {
    return { success: false, error: 'Keycloak server not reachable.' };
  }
};


export const registerUser = async (userData) => {
  try {
    const response = await fetch(`${BACKEND_URL}/api/auth/register`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username:  userData.username,
        email:     userData.email,
        password:  userData.password,
        firstName: userData.fullName.split(' ')[0],
        lastName:  userData.fullName.split(' ').slice(1).join(' ') || '',
      }),
    });

    if (response.ok) return { success: true };

    const err = await response.json().catch(() => ({}));
    return { success: false, error: err.message || 'Registration failed.' };
  } catch {
    return { success: false, error: 'Backend server not reachable.' };
  }
};

export const forgotPassword = async (email) => {
  try {
    await fetch(`${BACKEND_URL}/api/auth/forgot-password?email=${encodeURIComponent(email)}`, {
      method: 'POST',
    });
    return { success: true };
  } catch {
    return { success: true };
  }
};

export const getAccessToken = () => localStorage.getItem('access_token');

export const isTokenExpired = () => {
  const token = getAccessToken();
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return Date.now() >= payload.exp * 1000;
  } catch {
    return true;
  }
};

export const getUserInfo = () => {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const roles   = payload?.realm_access?.roles ?? [];

    let displayRole = 'User';
    if (roles.includes('admin'))      displayRole = 'Admin';
    else if (roles.includes('maintainer')) displayRole = 'Maintainer';
    else if (roles.includes('contributor')) displayRole = 'Contributor';

    const firstName = payload.given_name  || '';
    const lastName  = payload.family_name || '';
    const fullName  = payload.name || `${firstName} ${lastName}`.trim() || payload.preferred_username || 'Unknown';


    const initials = fullName
        .split(' ')
        .filter(Boolean)
        .slice(0, 2)
        .map((w) => w[0].toUpperCase())
        .join('');

    return {
      username:    payload.preferred_username || '',
      email:       payload.email              || '',
      fullName,
      firstName,
      lastName,
      initials:    initials || '??',
      roles,
      displayRole,
    };
  } catch {
    return null;
  }
};

export const logout = () => {
  localStorage.removeItem('access_token');
  window.location.href = '/';
};
