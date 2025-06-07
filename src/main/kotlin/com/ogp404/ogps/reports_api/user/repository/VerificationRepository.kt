package com.ogp404.ogps.reports_api.user.repository


import com.ogp404.ogps.reports_api.user.domain.Verificacion
import com.ogp404.ogps.reports_api.user.repository.entity.Verification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationRepository : JpaRepository<Verificacion, Int> {
    fun countByIdIncident(idIncident: Int): Int
    fun existsByIdUserAndIdIncident(idUser: Int, idIncident: Int): Boolean
}