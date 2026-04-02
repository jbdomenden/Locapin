(async()=>{
  if(!document.getElementById('dashboardStats')) return
  const s = await api.get('/admin/api/dashboard/stats')
  const cards = [
    ['Cities', s.totalCities, '🏙️', 'Managed urban destinations'],
    ['Areas', s.totalAreas, '🗺️', 'Mapped local zones'],
    ['Attractions', s.totalAttractions, '📍', 'Published attractions'],
    ['Photos', s.totalPhotos, '🖼️', 'Media assets in gallery'],
    ['Users', s.totalUsers, '👥', 'Registered app users'],
    ['Premium Subscribers', s.totalPremiumSubscribers, '💎', 'Active premium members']
  ]
  dashboardStats.innerHTML = cards.map(([label, value, icon, foot]) =>
    `<div class='stat-card'><div class='stat-label'>${icon} ${label}</div><div class='stat-value'>${value}</div><div class='stat-foot'>${foot}</div></div>`
  ).join('')

  document.getElementById('summaryPanel').innerHTML = `
    <div class='grid cols-2'>
      <div class='surface-body'>
        <h3>Attraction Summary</h3>
        <p class='page-subtitle'>Active admin snapshot.</p>
      </div>
      <div class='surface-body'>
        <div><span class='badge active'>Active Content</span> ${s.totalAttractions}</div>
        <div style='margin-top:8px'><span class='badge featured'>Featured Candidates</span> ${s.featuredAttractions ?? 0}</div>
      </div>
    </div>`
})();
