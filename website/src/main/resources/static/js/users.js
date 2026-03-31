async function loadUsers(){
  if(!document.getElementById('usersBody')) return
  const q = new URLSearchParams()
  if(userSearch.value) q.set('q', userSearch.value)
  if(roleFilter.value) q.set('role', roleFilter.value)
  if(statusFilter.value) q.set('status', statusFilter.value)
  const rows = await api.get('/admin/api/users' + (q.toString() ? '?' + q.toString() : ''))
  usersBody.innerHTML = rows.length ? rows.map(r => `<tr><td>${r.fullName}</td><td>${r.email}</td><td><span class='badge'>${r.role}</span></td><td><span class='badge ${r.status==='ACTIVE'?'active':'inactive'}'>${r.status}</span></td><td><a class='btn ghost' href='/admin/users/${r.id}/edit'>Edit</a></td></tr>`).join('') : `<tr><td colspan='5'><div class='empty-state'>No admin users found.</div></td></tr>`
}
(async()=>{await loadUsers()})()
