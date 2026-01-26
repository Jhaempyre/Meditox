package com.example.meditox.models.sync

import com.google.gson.annotations.SerializedName

data class GetGlobalSurgicalConsumablesResponse(
    @SerializedName("surgicalConsumables")
    val surgicalConsumables: List<GlobalSurgicalConsumableDto>,
    val pagination: PaginationInfo
)
