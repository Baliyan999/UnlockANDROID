package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.payment.ReceiptUploadResponse
import com.subnetik.unlock.data.remote.dto.payment.StudentPaymentInfoResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface PaymentApi {
    @GET("payments/my-info")
    suspend fun getMyPaymentInfo(): StudentPaymentInfoResponse

    @Multipart
    @POST("payments/receipts/upload")
    suspend fun uploadReceipt(@Part receipt: MultipartBody.Part): ReceiptUploadResponse
}
