package com.example.meditox.models.chemist

import com.example.meditox.models.sync.PaginationInfo

data class GetStockListResponse(
    val stock_entries: List<StockEntryResponse>,
    val pagination: PaginationInfo
)
