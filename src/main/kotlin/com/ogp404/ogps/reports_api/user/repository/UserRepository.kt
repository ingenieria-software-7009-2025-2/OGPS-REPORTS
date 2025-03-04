package com.ogp404.ogps.reports_api.user.repository

import com.ogp404.ogps.reports_api.user.repository.entity.User
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int>{
    @Query(value = "SELECT * FROM user u WHERE u.person_id = ?", nativeQuery = true)
    fun findByPerson(person: Person): User?
}
