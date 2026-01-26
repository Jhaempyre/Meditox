package com.example.meditox.models.sync

data class GetGlobalCosmeticsResponse(
    val cosmetics: List<GlobalCosmeticDto>,
    val pagination: PaginationInfo
)
