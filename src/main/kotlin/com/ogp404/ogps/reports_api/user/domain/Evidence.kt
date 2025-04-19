package com.ogp404.ogps.reports_api.incident.domain

import jakarta.persistence.*

@Entity
@Table(name = "evidence")
data class Evidence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidence")
    val id: Int? = null,

    @Column(name = "id_incident", nullable = false)
    val incidentId: Int,

    @Column(name = "url_photo", nullable = false)
    val photoUrl: String
)