(async () => {
  if (location.pathname === '/admin/login') {
    document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
      e.preventDefault()
      try {
        await api.post('/admin/auth/login', { email: email.value, password: password.value })
        location.href = '/admin/dashboard'
      } catch {
        // api.request already surfaces backend error messages.
      }
    })
    return
  }

  try {
    const me = await api.get('/admin/auth/me')
    const nameHolder = document.getElementById('topbarIdentity')
    if (nameHolder) nameHolder.textContent = me.email || 'Admin'
  } catch {
    location.href = '/admin/login'
  }
})()

function logout() {
  api.post('/admin/auth/logout', {}).finally(() => (location.href = '/admin/login'))
}
