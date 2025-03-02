package com.ogp404.ogps.reports_api.user.repository

import com.ogp404.ogps.reports_api.user.repository.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int>{

}
