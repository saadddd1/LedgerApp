package com.example.ledger.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// --- Auth ---
data class SendCodeRequest(
    @SerializedName("email") val email: String
)
data class SendCodeResponse(
    @SerializedName("success") val success: Boolean
)

data class VerifyCodeRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)
data class VerifyCodeResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("isVip") val isVip: Boolean,
    @SerializedName("vipExpireAt") val vipExpireAt: Long?
)

// --- Sync ---
data class SyncUploadRequest(
    @SerializedName("dataJson") val dataJson: String
)
data class SyncResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

data class SyncDownloadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("dataJson") val dataJson: String?,
    @SerializedName("updatedAt") val updatedAt: Long?,
    @SerializedName("message") val message: String?
)

// --- VIP ---
data class VipPayRequest(
    @SerializedName("planId") val planId: String,
    @SerializedName("payMethod") val payMethod: String
)
data class VipPayResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("payUrl") val payUrl: String?,
    @SerializedName("vipExpireAt") val vipExpireAt: Long?
)

interface ApiService {
    @POST("/api/auth/send-code")
    suspend fun sendCode(@Body request: SendCodeRequest): SendCodeResponse

    @POST("/api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse

    @POST("/api/sync/upload")
    suspend fun uploadSyncData(
        @Header("Authorization") token: String,
        @Body request: SyncUploadRequest
    ): SyncResponse

    @GET("/api/sync/download")
    suspend fun downloadSyncData(
        @Header("Authorization") token: String
    ): SyncDownloadResponse

    @POST("/api/vip/pay")
    suspend fun createVipOrder(
        @Header("Authorization") token: String,
        @Body request: VipPayRequest
    ): VipPayResponse
}
