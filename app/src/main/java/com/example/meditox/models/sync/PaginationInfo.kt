package com.example.meditox.models.sync

data class PaginationInfo(
    val current_page: Int,
    val total_pages: Int,
    val total_records: Int,
    val records_per_page: Int
)
