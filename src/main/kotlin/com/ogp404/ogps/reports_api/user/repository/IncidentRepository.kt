package com.ogp404.ogps.reports_api.incident.repository

import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IncidentRepository : JpaRepository<Incident, Int>{
    fun findAllByUserIdUser(userId: Int): List<Incident>
    override fun deleteById(incidentId: Int)
}