window.ui = {
  toast(msg) {
    const el = document.createElement('div')
    el.className = 'toast'
    el.textContent = msg
    document.body.appendChild(el)
    setTimeout(() => el.remove(), 2600)
  },
  confirm(msg) { return window.confirm(msg) }
}

function setupUserMenu() {
  const menu = document.querySelector('[data-user-menu]')
  if (!menu) return
  const btn = menu.querySelector('[data-user-menu-btn]')
  btn?.addEventListener('click', (e) => {
    e.stopPropagation()
    menu.classList.toggle('open')
    if (menu.classList.contains('open')) {
      menu.querySelector('.dropdown-item')?.focus()
    }
  })

  document.addEventListener('click', (e) => {
    if (!menu.contains(e.target)) menu.classList.remove('open')
  })
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') menu.classList.remove('open')
  })
}

function setupChangePasswordModal() {
  const modal = document.getElementById('changePasswordModal')
  if (!modal) return
  const openBtn = document.querySelector('[data-change-password]')
  const closeEls = modal.querySelectorAll('[data-close-modal]')
  const form = document.getElementById('changePasswordForm')
  const errorEl = document.getElementById('changePasswordError')

  const close = () => {
    modal.classList.remove('open')
    errorEl.textContent = ''
    form.reset()
  }

  openBtn?.addEventListener('click', () => {
    modal.classList.add('open')
    document.getElementById('currentPassword')?.focus()
  })
  closeEls.forEach(el => el.addEventListener('click', close))
  modal.addEventListener('click', (e) => { if (e.target === modal) close() })

  form?.addEventListener('submit', async (e) => {
    e.preventDefault()
    errorEl.textContent = ''
    const payload = {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
      confirmNewPassword: confirmNewPassword.value
    }
    if (!payload.currentPassword || !payload.newPassword || !payload.confirmNewPassword) {
      errorEl.textContent = 'All fields are required.'
      return
    }
    if (payload.newPassword.length < 8) {
      errorEl.textContent = 'New password must be at least 8 characters.'
      return
    }
    if (payload.newPassword !== payload.confirmNewPassword) {
      errorEl.textContent = 'New password and confirmation do not match.'
      return
    }

    try {
      await api.post('/admin/auth/change-password', payload)
      close()
      ui.toast('Password updated successfully.')
    } catch (err) {
      errorEl.textContent = err.message || 'Unable to update password.'
    }
  })
}

function markActiveNav() {
  const current = location.pathname
  document.querySelectorAll('.nav-link').forEach(link => {
    if (current.startsWith(link.getAttribute('href'))) link.classList.add('active')
  })
}

document.addEventListener('DOMContentLoaded', () => {
  setupUserMenu()
  setupChangePasswordModal()
  markActiveNav()
})
