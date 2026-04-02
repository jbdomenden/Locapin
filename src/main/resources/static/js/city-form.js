(async () => {
  const form = document.getElementById('cityForm');
  if (!form) return;

  const cityId = location.pathname.split('/')[3];
  const cityNameInput = document.getElementById('name');
  const isPremiumInput = document.getElementById('isPremium');
  const suggestionList = document.getElementById('phCitiesSuggestions');

  const renderSuggestions = (items) => {
    suggestionList.innerHTML = '';
    items.forEach((city) => {
      const option = document.createElement('option');
      option.value = city;
      suggestionList.appendChild(option);
    });
  };

  const loadSuggestions = async (query) => {
    const q = (query || '').trim();
    if (q.length < 2) {
      renderSuggestions([]);
      return;
    }
    const suggestions = await api.get(`/admin/api/reference/ph-cities?q=${encodeURIComponent(q)}`);
    renderSuggestions(suggestions || []);
  };

  let cityLookupDebounce;
  cityNameInput.addEventListener('input', () => {
    clearTimeout(cityLookupDebounce);
    cityLookupDebounce = setTimeout(() => {
      loadSuggestions(cityNameInput.value).catch(() => renderSuggestions([]));
    }, 180);
  });

  if (cityId && cityId !== 'new') {
    const city = await api.get(`/admin/api/cities/${cityId}`);
    cityNameInput.value = city.name;
    isPremiumInput.checked = city.isPremium;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = {
      name: cityNameInput.value,
      isPremium: isPremiumInput.checked
    };
    if (cityId && cityId !== 'new') {
      await api.put(`/admin/api/cities/${cityId}`, payload);
    } else {
      await api.post('/admin/api/cities', payload);
    }
    location.href = '/admin/cities';
  });
})();
