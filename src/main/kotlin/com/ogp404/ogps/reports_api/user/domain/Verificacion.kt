package com.ogp404.ogps.reports_api.user.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "verification",
    uniqueConstraints = [UniqueConstraint(columnNames = ["id_user", "id_incident"])]
)
data class Verificacion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_request")
    val id: Int = 0,

    @Column(name = "id_user", nullable = false)
    val idUser: Int,

    @Column(name = "id_incident", nullable = false)
    val idIncident: Int
)
