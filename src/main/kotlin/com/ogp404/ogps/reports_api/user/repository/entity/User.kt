package com.ogp404.ogps.reports_api.user.repository.entity

import jakarta.persistence.*


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