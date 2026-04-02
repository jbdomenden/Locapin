const api = {
  async request(url, opts = {}) {
    const method = opts.method || 'GET'
    const hasBody = Object.prototype.hasOwnProperty.call(opts, 'body')
    const isFormData = typeof FormData !== 'undefined' && opts.body instanceof FormData
    const headers = { ...(opts.headers || {}) }
    if (hasBody && !isFormData && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json'
    }

    let res
    try {
      res = await fetch(url, { ...opts, method, headers, credentials: 'include' })
    } catch {
      if (window.ui?.toast) ui.toast('Network error. Please try again.')
      throw new Error('Network error. Please try again.')
    }

    if (res.status === 401) {
      location.href = '/admin/login'
      throw new Error('Unauthenticated')
    }

    const j = await res.json().catch(() => ({}))
    if (!res.ok || j.success === false) {
      const message = j.message || 'Request failed'
      if (window.ui?.toast) ui.toast(message)
      throw new Error(message)
    }
    return j.data
  },
  get: (u) => api.request(u),
  post: (u, d) => api.request(u, { method: 'POST', body: JSON.stringify(d) }),
  put: (u, d) => api.request(u, { method: 'PUT', body: JSON.stringify(d) }),
  patch: (u, d) => api.request(u, { method: 'PATCH', body: JSON.stringify(d) }),
  del: (u) => api.request(u, { method: 'DELETE' })
}
