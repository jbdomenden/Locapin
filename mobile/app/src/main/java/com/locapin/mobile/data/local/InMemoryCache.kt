package com.locapin.mobile.data.local

import com.locapin.mobile.domain.model.Category
import com.locapin.mobile.domain.model.Destination
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryCache @Inject constructor() {
    var lastDestinations: List<Destination> = emptyList()
    var lastCategories: List<Category> = emptyList()
}
