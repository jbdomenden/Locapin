package locapin.admin.services

object PhilippineCities {
    private val cities = listOf(
        "Alaminos City", "Angeles City", "Antipolo City", "Bacolod City", "Bacoor City",
        "Bago City", "Baguio City", "Bais City", "Balanga City", "Batac City",
        "Batangas City", "Bayawan City", "Baybay City", "Biñan City", "Bislig City",
        "Bogo City", "Borongan City", "Butuan City", "Cabadbaran City", "Cabanatuan City",
        "Cabuyao City", "Cadiz City", "Cagayan de Oro City", "Calamba City", "Calapan City",
        "Calbayog City", "Caloocan City", "Candon City", "Canlaon City", "Carcar City",
        "Catbalogan City", "Cauayan City", "Cavite City", "Cebu City", "City of Manila",
        "Cotabato City", "Dagupan City", "Danao City", "Dapitan City", "Dasmariñas City",
        "Davao City", "Digos City", "Dipolog City", "Dumaguete City", "El Salvador City",
        "Escalante City", "Gapan City", "General Santos City", "General Trias City", "Gingoog City",
        "Guihulngan City", "Himamaylan City", "Ilagan City", "Iligan City", "Iloilo City",
        "Imus City", "Iriga City", "Isabela City", "Kabankalan City", "Kidapawan City",
        "Koronadal City", "La Carlota City", "Lamitan City", "Laoag City", "Lapu-Lapu City",
        "Las Piñas City", "Legazpi City", "Ligao City", "Lipa City", "Lucena City",
        "Maasin City", "Makati City", "Malabon City", "Malaybalay City", "Malolos City",
        "Mandaluyong City", "Mandaue City", "Manila City", "Marawi City", "Marikina City",
        "Masbate City", "Mati City", "Meycauayan City", "Muñoz City", "Muntinlupa City",
        "Naga City", "Navotas City", "Olongapo City", "Ormoc City", "Oroquieta City",
        "Ozamiz City", "Pagadian City", "Palayan City", "Panabo City", "Parañaque City",
        "Pasay City", "Pasig City", "Passi City", "Puerto Princesa City", "Quezon City",
        "Roxas City", "Sagay City", "Samal City", "San Carlos City", "San Fernando City",
        "San Jose City", "San Jose del Monte City", "San Juan City", "San Pablo City", "San Pedro City",
        "Santa Rosa City", "Santiago City", "Silay City", "Sipalay City", "Sorsogon City",
        "Surigao City", "Tabaco City", "Tabuk City", "Tacloban City", "Tacurong City",
        "Tagaytay City", "Tagbilaran City", "Taguig City", "Tagum City", "Talisay City",
        "Tanauan City", "Tandag City", "Tangub City", "Tanjay City", "Tarlac City",
        "Tayabas City", "Toledo City", "Trece Martires City", "Tuguegarao City", "Urdaneta City",
        "Valencia City", "Valenzuela City", "Victorias City", "Vigan City", "Zamboanga City"
    ).sorted()

    fun suggest(query: String?, limit: Int = 15): List<String> {
        val normalized = query?.trim()?.lowercase().orEmpty()
        if (normalized.isEmpty()) return cities.take(limit)
        return cities
            .asSequence()
            .filter { it.lowercase().contains(normalized) }
            .take(limit)
            .toList()
    }
}

