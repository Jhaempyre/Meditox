package com.example.meditox.models.wholesaler

import com.example.meditox.models.sync.PaginationInfo

data class GetWholesalersResponse(
    val wholesalers: List<WholesalerResponseDto>,
    val pagination: PaginationInfo
)
