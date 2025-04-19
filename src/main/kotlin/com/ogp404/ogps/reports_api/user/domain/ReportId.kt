package com.ogp404.ogps.reports_api.report.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ReportId(
    @Column(name = "id_user")
    val userId: Int = 0,

    @Column(name = "id_incident")
    val incidentId: Int = 0
) : java.io.Serializable