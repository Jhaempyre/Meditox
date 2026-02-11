package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.GlobalChemist.CreateGlobalCosmeticRequest
import com.example.meditox.models.GlobalChemist.GlobalCosmeticApiResponse
import com.example.meditox.models.sync.GetGlobalDrugsResponse
import com.example.meditox.models.GlobalDrug.CreateGlobalDrugRequest
import com.example.meditox.models.GlobalDrug.GlobalDrugApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

import com.example.meditox.models.api.CreateGlobalGeneralFmcgRequest
import com.example.meditox.models.api.GlobalGeneralFmcgApiResponse
import com.example.meditox.models.sync.GetGlobalCosmeticsResponse
import com.example.meditox.models.sync.GetGlobalGeneralFmcgResponse
import com.example.meditox.models.sync.GetGlobalMedicalDevicesResponse
import com.example.meditox.models.sync.GetGlobalSupplementsResponse
import com.example.meditox.models.sync.GetGlobalSurgicalConsumablesResponse

interface GlobalDataSyncApiService {

    @POST("api/v3/global/fmcg/add")
    suspend fun createGlobalGeneralFmcg(
        @Body request: CreateGlobalGeneralFmcgRequest
    ): Response<ApiResponse<GlobalGeneralFmcgApiResponse>>

    @POST("api/v3/global/drugs/add")
    suspend fun createGlobalDrug(
        @Body request: CreateGlobalDrugRequest
    ): Response<ApiResponse<GlobalDrugApiResponse>>

    @POST("api/v3/global/cosmetics/add")
    suspend fun createGlobalCosmetic(
        @Body request: CreateGlobalCosmeticRequest
    ): Response<ApiResponse<GlobalCosmeticApiResponse>>
    
    @GET("api/v3/global/drugs")
    suspend fun getGlobalDrugs(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("drug_schedule") drugSchedule: String? = null,
        @Query("sort_by") sortBy: String = "brandName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalDrugsResponse>>

    @GET("api/v3/global/cosmetics")
    suspend fun getGlobalCosmetics(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalCosmeticsResponse>>

    @GET("api/v3/global/fmcg")
    suspend fun getGlobalGeneralFmcg(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalGeneralFmcgResponse>>

    @GET("api/v3/global/medical-devices")
    suspend fun getGlobalMedicalDevices(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalMedicalDevicesResponse>>

    @GET("api/v3/global/supplements")
    suspend fun getGlobalSupplements(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalSupplementsResponse>>

    @GET("api/v3/global/surgical-consumables")
    suspend fun getGlobalSurgicalConsumables(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalSurgicalConsumablesResponse>>
}
