package com.ogp404.ogps.reports_api.report.domain

import jakarta.persistence.*

@Entity
@Table(name = "reports")
data class Report(
    @EmbeddedId
    val id: ReportId
)