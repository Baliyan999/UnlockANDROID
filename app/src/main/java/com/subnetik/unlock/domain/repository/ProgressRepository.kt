package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.domain.model.Resource

interface ProgressRepository {
    suspend fun syncProgress(): Resource<Unit>
}
