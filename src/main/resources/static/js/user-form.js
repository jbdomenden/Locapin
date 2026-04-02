const MODULES = ['DASHBOARD','AREAS','ATTRACTIONS','PHOTOS','PLANS','USER_MANAGEMENT']
function renderPermissionGrid(data={}){
  permissionGrid.innerHTML = `<table class='table'><thead><tr><th>Module</th><th>Read</th><th>Create</th><th>Update</th><th>Delete</th></tr></thead><tbody>${MODULES.map(m=>`<tr><td>${m}</td>${['read','create','update','delete'].map(a=>`<td><input type='checkbox' data-module='${m}' data-action='${a}' ${(data[m]?.[a]?'checked':'')}></td>`).join('')}</tr>`).join('')}</tbody></table>`
}
function collectPermissions(){
  return MODULES.map(m=>({moduleKey:m,canRead:!!document.querySelector(`[data-module='${m}'][data-action='read']`)?.checked,canCreate:!!document.querySelector(`[data-module='${m}'][data-action='create']`)?.checked,canUpdate:!!document.querySelector(`[data-module='${m}'][data-action='update']`)?.checked,canDelete:!!document.querySelector(`[data-module='${m}'][data-action='delete']`)?.checked}))
}
(async()=>{
  const form=document.getElementById('adminUserForm'); if(!form) return
  renderPermissionGrid()
  const id=location.pathname.split('/')[3]
  if(id && id!=='new'){
    const user=await api.get('/admin/api/users/'+id)
    fullName.value=user.fullName; email.value=user.email; role.value=user.role; status.value=user.status
    const perms=await api.get('/admin/api/users/'+id+'/permissions')
    const map={}; perms.forEach(p=>map[p.moduleKey]={read:p.canRead,create:p.canCreate,update:p.canUpdate,delete:p.canDelete});
    renderPermissionGrid(map)
  }
  form.addEventListener('submit',async(e)=>{
    e.preventDefault()
    const payload={fullName:fullName.value,email:email.value,role:role.value,status:status.value,permissions:collectPermissions()}
    if(id && id!=='new') await api.put('/admin/api/users/'+id,payload)
    else await api.post('/admin/api/users',{...payload,password:password.value,confirmPassword:confirmPassword.value})
    location.href='/admin/users'
  })
})()
