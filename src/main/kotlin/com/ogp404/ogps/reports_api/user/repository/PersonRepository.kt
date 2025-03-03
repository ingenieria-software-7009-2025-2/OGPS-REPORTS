package com.ogp404.ogps.reports_api.user.repository

import com.ogp404.ogps.reports_api.user.repository.entity.Person
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PersonRepository : CrudRepository<Person, Int> {

    @Query(value = "SELECT * FROM person WHERE correo=?1", nativeQuery = true)
    fun findByMail(mail: String): Person?

    @Query(value = "SELECT * FROM person WHERE mail=?1 AND password=?2", nativeQuery = true)
    fun findByMailAndPassword(mail: String, password: String): Person?

    @Query(value = "SELECT * FROM person WHERE token=?1", nativeQuery = true)
    fun findByToken(token: String): Person?
}