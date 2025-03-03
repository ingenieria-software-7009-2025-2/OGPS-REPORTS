package com.ogp404.ogps.reports_api.user.controller.body

import com.ogp404.ogps.reports_api.user.repository.entity.Admin
import java.sql.Timestamp

data class IncidentBody(
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String,
    val status: String,
    val reportDate: Timestamp
)
