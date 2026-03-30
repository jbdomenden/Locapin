document.addEventListener('DOMContentLoaded', () => {
  const citySelect = document.getElementById('citySelect');
  const areaSelect = document.getElementById('areaSelect');
  if (citySelect && areaSelect) {
    citySelect.addEventListener('change', async () => {
      const cityId = citySelect.value;
      if (!cityId) return;
      const res = await fetch(`/admin/api/areas/by-city/${cityId}`);
      const areas = await res.json();
      areaSelect.innerHTML = '';
      areas.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = a.name;
        areaSelect.appendChild(opt);
      });
    });
  }

  const photoInput = document.getElementById('photoInput');
  const previewContainer = document.getElementById('previewContainer');
  if (photoInput && previewContainer) {
    photoInput.addEventListener('change', () => {
      previewContainer.innerHTML = '';
      [...photoInput.files].forEach(file => {
        const img = document.createElement('img');
        img.className = 'thumb';
        img.src = URL.createObjectURL(file);
        previewContainer.appendChild(img);
      });
    });
  }

  document.querySelectorAll('form[action*="/delete"], form[action*="/archive"]').forEach(form => {
    form.addEventListener('submit', (e) => {
      if (!confirm('Are you sure you want to continue?')) e.preventDefault();
    });
  });
});
