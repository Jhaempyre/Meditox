package com.example.meditox.models.sync

data class GetGlobalSupplementsResponse(
    val supplements: List<GlobalSupplementDto>,
    val pagination: PaginationInfo
)
