package com.ogp404.ogps.reports_api.incident.repository

import com.ogp404.ogps.reports_api.incident.domain.Evidence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EvidenceRepository : JpaRepository<Evidence, Long> {
    fun findAllByIncidentId(incidentId: Int): List<Evidence>
}