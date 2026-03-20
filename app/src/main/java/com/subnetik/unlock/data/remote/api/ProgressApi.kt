package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.progress.ProgressSyncRequest
import com.subnetik.unlock.data.remote.dto.progress.StudentFullProgressResponse
import retrofit2.http.*

interface ProgressApi {
    @POST("progress/sync")
    suspend fun syncProgress(@Body request: ProgressSyncRequest)

    @GET("progress/student/{userId}")
    suspend fun getStudentProgress(@Path("userId") userId: Int): StudentFullProgressResponse
}
