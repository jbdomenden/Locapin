async function loadPhotosTable(attractionId){
  const rows=await api.get('/admin/api/attractions/'+attractionId+'/photos')
  photosBody.innerHTML=rows.length?rows.map(r=>`<tr><td>${r.id}</td><td><img src='${r.imagePath}' width='80' style='border-radius:10px'></td><td><input value='${r.sortOrder}' data-id='${r.id}' class='input sort'></td><td><button class='btn alt' onclick='delPhoto(${r.id})'>Delete</button></td></tr>`).join(''):`<tr><td colspan='4'><div class='empty-state'>No photos uploaded yet.</div></td></tr>`
}

(async()=>{
  if(!document.getElementById('photosBody'))return
  const picker=document.getElementById('attractionPicker')
  const attractions=await api.get('/admin/api/attractions')
  picker.innerHTML = `<option value=''>Select attraction</option>` + attractions.map(a=>`<option value='${a.id}'>${a.name}</option>`).join('')

  const id=new URLSearchParams(location.search).get('attractionId')
  window._aid=id
  if(id){
    picker.value=id
    await loadPhotosTable(id)
  } else {
    photosBody.innerHTML="<tr><td colspan='4'><div class='empty-state'>Select an attraction to manage photos.</div></td></tr>"
  }

  picker.addEventListener('change', async ()=>{
    window._aid=picker.value||null
    if(!window._aid){
      photosBody.innerHTML="<tr><td colspan='4'><div class='empty-state'>Select an attraction to manage photos.</div></td></tr>"
      return
    }
    const url = new URL(location.href)
    url.searchParams.set('attractionId', window._aid)
    history.replaceState({}, '', url)
    await loadPhotosTable(window._aid)
  })
})();

async function uploadPhotos(){if(!window._aid){ui.toast('Choose attraction first.');return;}const f=document.getElementById('photoFiles').files;const form=new FormData();[...f].forEach(x=>form.append('files',x));await api.request('/admin/api/attractions/'+window._aid+'/photos',{method:'POST',body:form});await loadPhotosTable(window._aid);ui.toast('Photos uploaded')}
async function saveOrder(){const items=[...document.querySelectorAll('.sort')].map(x=>({id:+x.dataset.id,sortOrder:+x.value}));await api.patch('/admin/api/photos/reorder',{items});ui.toast('Order saved')}
async function delPhoto(id){if(!ui.confirm('Delete photo?'))return;await api.del('/admin/api/photos/'+id);await loadPhotosTable(window._aid)}
