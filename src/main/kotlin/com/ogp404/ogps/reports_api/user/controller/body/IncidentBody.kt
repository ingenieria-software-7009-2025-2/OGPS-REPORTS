package com.ogp404.ogps.reports_api.incident.controller.body

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class IncidentBody(
    val category: String = "",
    val title: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val reportDate: Date = Date(),
    val description: String? = null
)