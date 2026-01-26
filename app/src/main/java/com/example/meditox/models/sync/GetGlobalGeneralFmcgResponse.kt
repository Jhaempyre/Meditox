package com.example.meditox.models.sync

import com.google.gson.annotations.SerializedName

data class GetGlobalGeneralFmcgResponse(
    @SerializedName("generalFmcg")
    val generalFmcg: List<GlobalGeneralFmcgDto>,
    val pagination: PaginationInfo
)
