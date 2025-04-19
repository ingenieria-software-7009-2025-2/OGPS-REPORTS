package com.ogp404.ogps.reports_api.incident.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "incident")
data class Incidente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident")
    val id: Int? = null,

    @Column(name = "id_user")
    val userId: Int?,

    @Column(name = "id_admin")
    val adminId: Int?,

    @Column(name = "latitude", nullable = false)
    val latitude: Double,

    @Column(name = "longitude", nullable = false)
    val longitude: Double,

    @Column(name = "category", nullable = false)
    val category: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "status", nullable = false)
    val status: String,

    @Column(name = "report_date", nullable = false)
    val reportDate: LocalDate
)