package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.lead.LeadCreateRequest
import com.subnetik.unlock.data.remote.dto.lead.LeadResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LeadApi {
    @POST("leads")
    suspend fun createLead(@Body request: LeadCreateRequest): LeadResponse
}
