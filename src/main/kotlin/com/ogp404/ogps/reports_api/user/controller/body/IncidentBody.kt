package com.ogp404.ogps.reports_api.incident.controller.body

import java.util.Date

data class IncidentBody(
    val category: String,
    val title: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val reportDate: Date,
)