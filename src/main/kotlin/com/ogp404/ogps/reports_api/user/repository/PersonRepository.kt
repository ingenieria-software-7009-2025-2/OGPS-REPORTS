package com.ogp404.ogps.reports_api.user.repository

import com.ogp404.ogps.reports_api.user.repository.entity.Person
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : CrudRepository<Person, Int> {
    fun findByMail(mail: String): Person?

    fun findByMailAndPassword(mail: String, password: String): Person?

    fun findByToken(token: String): Person?

    fun findByUserName(userName: String): Person?
}