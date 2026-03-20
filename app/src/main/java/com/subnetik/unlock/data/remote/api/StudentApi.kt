package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.student.StudentScheduleData
import com.subnetik.unlock.data.remote.dto.student.StudentTokenWallet
import retrofit2.http.GET

interface StudentApi {
    @GET("homework/my/wallet")
    suspend fun getWallet(): StudentTokenWallet

    @GET("homework/my/schedule")
    suspend fun getSchedule(): StudentScheduleData
}
