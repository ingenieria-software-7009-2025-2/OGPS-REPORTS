package com.ogp404.ogps.reports_api.user.repository.entity

import com.ogp404.ogps.reports_api.incident.domain.Evidence
import jakarta.persistence.*
import java.sql.Timestamp
import javax.management.Descriptor


@Entity
@Table(name = "person")
class Person constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_person")
    val id: Int =0,

    @Column(name = "user_name")
    var userName: String ="",

    @Column(name = "token")
    var token: String? = null,

    @Column(name = "mail")
    var mail: String = "",

    @Column(name = "first_name")
    var firstName: String = "",

    @Column(name = "last_name")
    var lastName: String = "",

    @Column(name = "password")
    var password: String= "",

    @Column(name = "role")
    var role: String="",
)

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    val idUser: Int = 0,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "id_person", referencedColumnName = "id_person")
    var person: Person
)

@Entity
@Table(name = "administrator")
class Admin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    val idAdmin: Int = 0,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "id_person", referencedColumnName = "id_person")
    var person: Person
)

@Entity
@Table(name = "incident")
class Incident(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident")
    val idIncident: Int = 0,

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    var user: User? = null,

    @ManyToOne
    @JoinColumn(name = "id_admin", referencedColumnName = "id_admin")
    var admin: Admin? = null,

    @Column(name = "latitude")
    var latitude: Double,

    @Column(name = "longitude")
    var longitude: Double,

    @Column(name = "title")
    var title: String = "",

    @Column(name = "category")
    var category: String = "",

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "status")
    var status: String = "",

    @Column(name = "report_date")
    val reportDate: Timestamp = Timestamp(System.currentTimeMillis()),

    // Nueva relación para evidencias
    @OneToMany(mappedBy = "incidentId", cascade = [CascadeType.ALL], orphanRemoval = true)
    var evidences: MutableList<Evidence> = mutableListOf()
)

@Entity
@Table(
    name = "verification",
    uniqueConstraints = [UniqueConstraint(columnNames = ["id_user", "id_incident"])]
)
class Verification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_request")
    val id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "id_incident", referencedColumnName = "id_incident", nullable = false)
    val incident: Incident
)
