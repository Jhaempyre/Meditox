package com.example.meditox.models.sync

data class GetGlobalDrugsResponse(
    val drugs: List<GlobalDrugDto>,
    val pagination: PaginationInfo
)
