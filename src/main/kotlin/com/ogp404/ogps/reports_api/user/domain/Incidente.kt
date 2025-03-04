package com.ogp404.ogps.reports_api.user.domain

import java.sql.Timestamp

data class Incidente(
    val idIncident: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val category: String = "",
    val description: String = "",
    val status: String = "Reported",
    val reportDate: Timestamp
)
