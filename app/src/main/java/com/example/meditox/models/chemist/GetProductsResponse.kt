package com.example.meditox.models.chemist

import com.example.meditox.models.sync.PaginationInfo

data class GetProductsResponse(
    val products: List<ChemistProductListItem>,
    val pagination: PaginationInfo,
    val summary: ProductsSummary
)
