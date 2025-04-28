package com.ogp404.ogps.reports_api.incident.repository

import com.ogp404.ogps.reports_api.incident.domain.Evidence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EvidenceRepository : JpaRepository<Evidence, Int> {
    fun deleteByIncidentId(incidentId: Int)
    fun findByIncidentId(incidentId: Int): List<Evidence>
}