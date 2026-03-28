package com.subnetik.unlock.data.remote.dto.lead

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeadCreateRequest(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val message: String? = null,
    @SerialName("language_level") val languageLevel: String? = null,
    @SerialName("preferred_time") val preferredTime: String? = null,
    val format: String? = null,
    val promocode: String? = null,
    @SerialName("final_price") val finalPrice: String? = null,
    val source: String = "lead",
)

@Serializable
data class LeadResponse(
    val id: Int? = null,
    val status: String? = null,
)
