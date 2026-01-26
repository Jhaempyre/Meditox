package com.example.meditox.models.sync

import com.google.gson.annotations.SerializedName

data class GetGlobalMedicalDevicesResponse(
    @SerializedName("medicalDevices")
    val medicalDevices: List<GlobalMedicalDeviceDto>,
    val pagination: PaginationInfo
)
