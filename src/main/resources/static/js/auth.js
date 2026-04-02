(async () => {
  if (location.pathname === '/admin/login') {
    const form = document.getElementById('loginForm')
    const emailInput = document.getElementById('email')
    const passwordInput = document.getElementById('password')
    const submitBtn = document.getElementById('loginSubmit')
    const togglePasswordBtn = document.getElementById('togglePassword')
    const forgotPasswordBtn = document.getElementById('forgotPasswordBtn')
    const createAccountBtn = document.getElementById('createAccountBtn')

    togglePasswordBtn?.addEventListener('click', () => {
      const reveal = passwordInput.type === 'password'
      passwordInput.type = reveal ? 'text' : 'password'
      togglePasswordBtn.textContent = reveal ? 'Hide' : 'Show'
      togglePasswordBtn.setAttribute('aria-label', reveal ? 'Hide password' : 'Show password')
    })

    forgotPasswordBtn?.addEventListener('click', () => {
      ui.toast('Please contact your super admin to reset your password.')
    })

    createAccountBtn?.addEventListener('click', () => {
      ui.toast('New admin accounts are created by the super admin.')
    })

    form?.addEventListener('submit', async (e) => {
      e.preventDefault()
      const originalLabel = submitBtn?.textContent || 'Login'

      if (submitBtn) {
        submitBtn.disabled = true
        submitBtn.textContent = 'Signing in...'
      }

      try {
        await api.post('/admin/auth/login', {
          email: emailInput.value,
          password: passwordInput.value
        })
        location.href = '/admin/dashboard'
      } catch (err) {
        ui.toast(err.message)
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false
          submitBtn.textContent = originalLabel
        }
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
