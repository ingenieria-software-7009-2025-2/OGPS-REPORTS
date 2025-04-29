package com.ogp404.ogps.reports_api.incident.service

import com.ogp404.ogps.reports_api.incident.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.incident.domain.Evidence
import com.ogp404.ogps.reports_api.incident.repository.EvidenceRepository
import com.ogp404.ogps.reports_api.incident.repository.IncidentRepository
import com.ogp404.ogps.reports_api.report.domain.Report
import com.ogp404.ogps.reports_api.report.domain.ReportId
import com.ogp404.ogps.reports_api.report.repository.ReportRepository
import com.ogp404.ogps.reports_api.user.repository.PersonRepository
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import com.ogp404.ogps.reports_api.user.repository.entity.User
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import kotlin.math.*

@Service
class IncidentService(
    private val personRepository: PersonRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val evidenceRepository: EvidenceRepository,
    private val reportRepository: ReportRepository,
    private val entityManager: EntityManager
) {

    private val logger = LoggerFactory.getLogger(IncidentService::class.java)

    private val validCategories = listOf(
        "Potholes and Defects",
        "Street Lighting",
        "Traffic Accidents",
        "Obstacles",
        "Other"
    )

    @Transactional
    fun reportIncident(token: String, incidentBody: IncidentBody, photoUrls: List<String>): Incident {
        // Validamos el token y obtenemos al usuario asociado
        val person: Person = personRepository.findByToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        if (person.role != "User") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with role 'User' can report incidents")
        }

        val user: User = userRepository.findByPersonId(person.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for person with id: \${person.id}")

        // Validamos los campos obligatorios del incidente
        if (incidentBody.category.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Category is required")
        }
        if (incidentBody.title.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Title is required")
        }
        if (incidentBody.reportDate == null) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Report date is required")
        }
        if (incidentBody.latitude !in -90.0..90.0) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid latitude")
        }
        if (incidentBody.longitude !in -180.0..180.0) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid longitude")
        }
        if (incidentBody.category !in validCategories) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid category")
        }
        if (incidentBody.description?.length ?: 0 > 1000) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Description cannot exceed 1000 characters")
        }

        // Creamos la entidad Incidente con los datos proporcionados
        val incidentToSave = Incident(
            user = user,
            admin = null,
            latitude = incidentBody.latitude,
            longitude = incidentBody.longitude,
            category = incidentBody.category,
            title = incidentBody.title,
            description = incidentBody.description ?: "",
            status = "Reported",
            reportDate = Timestamp(incidentBody.reportDate.time)
        )

        val savedIncident = try {
            incidentRepository.save(incidentToSave).also {
                incidentRepository.flush()
                entityManager.flush()
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving incident: \${e.message}")
        }

        // Registramos la relación en la tabla intermedia 'Report'
        val report = Report(
            id = ReportId(
                userId = user.idUser,
                incidentId = savedIncident.idIncident
            )
        )

        try {
            reportRepository.save(report).also {
                reportRepository.flush()
                entityManager.flush()
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving report: \${e.message}")
        }

        // Guardamos las evidencias asociadas con los URLs generados
        photoUrls.forEach { photoUrl ->
            val evidence = Evidence(
                incidentId = savedIncident.idIncident,
                photoUrl = photoUrl
            )
            try {
                evidenceRepository.save(evidence).also {
                    evidenceRepository.flush()
                    entityManager.flush()
                }
            } catch (e: Exception) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving evidence: \${e.message}")
            }
        }

        return savedIncident
    }

    fun getIncidentsByUserToken(token: String): List<Map<String, Any>> {
        val person = personRepository.findByToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        if (person.role != "User") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can view their incidents")
        }

        val user = userRepository.findByPersonId(person.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this person")

        val incidents = incidentRepository.findAllByUserIdUser(user.idUser)
        return incidents.map { incident ->
            val evidences = evidenceRepository.findByIncidentId(incident.idIncident)
            mapOf(
                "idIncident" to incident.idIncident,
                "title" to incident.title,
                "status" to incident.status,
                "reportDate" to incident.reportDate,
                "latitude" to incident.latitude,
                "longitude" to incident.longitude,
                "category" to incident.category,
                "description" to incident.description,
                "evidences" to evidences.map { evidence ->
                    mapOf(
                        "id" to evidence.id,
                        "photoUrl" to evidence.photoUrl
                    )
                }
            )
        }
    }

    @Transactional
    fun deleteIncident(token: String, incidentId: Int) {
        val person = personRepository.findByToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        if (person.role != "User") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can delete their incidents")
        }

        val user = userRepository.findByPersonId(person.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val incident = incidentRepository.findById(incidentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found") }

        if (incident.user?.idUser != user.idUser) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own incidents")
        }

        try {
            // Eliminar evidencias asociadas
            evidenceRepository.deleteByIncidentId(incidentId)
            // Eliminar registros en la tabla Report
            reportRepository.deleteByIdIncidentId(incidentId)
            // Eliminar el incidente
            incidentRepository.deleteById(incidentId)
            incidentRepository.flush()
            entityManager.flush()
        } catch (e: Exception) {
            logger.error("Error deleting incident with ID: $incidentId, message: ${e.message}", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting incident: ${e.message}")
        }
    }
    fun getNearbyIncidents(token: String, latitude: Double, longitude: Double, radius: Double): List<Map<String, Any?>> {
        // Validamos el token
        val person = personRepository.findByToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        // Obtener todos los incidentes
        val allIncidents = incidentRepository.findAll()

        // Filtrar por distancia
        val nearbyIncidents = allIncidents.filter { incident ->
            val distance = calculateDistance(
                latitude, longitude,
                incident.latitude, incident.longitude
            )
            distance <= radius
        }

        // Para cada incidente, obtener sus evidencias
        return nearbyIncidents.map { incident ->
            val evidences = evidenceRepository.findAllByIncidentId(incident.idIncident)

            mapOf(
                "id" to incident.idIncident,
                "userId" to incident.user?.idUser,
                "adminId" to incident.admin?.idAdmin,
                "latitude" to incident.latitude,
                "longitude" to incident.longitude,
                "category" to incident.category,
                "title" to incident.title,
                "description" to incident.description,
                "status" to incident.status,
                "reportDate" to incident.reportDate,
                "distance" to calculateDistance(latitude, longitude, incident.latitude, incident.longitude),
                "photos" to evidences.map { evidence -> mapOf("id" to evidence.id, "url" to evidence.photoUrl) }
            )
        }.sortedBy { it["distance"] as Double }
    }

    fun getAllIncidents(token: String): List<Map<String, Any?>> {
        // Validamos el token
        val person = personRepository.findByToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        // Obtener todos los incidentes
        val allIncidents = incidentRepository.findAll()

        // Para cada incidente, obtener sus evidencias
        return allIncidents.map { incident ->
            val evidences = evidenceRepository.findAllByIncidentId(incident.idIncident)

            mapOf(
                "id" to incident.idIncident,
                "userId" to incident.user?.idUser,
                "adminId" to incident.admin?.idAdmin,
                "latitude" to incident.latitude,
                "longitude" to incident.longitude,
                "category" to incident.category,
                "title" to incident.title,
                "description" to incident.description,
                "status" to incident.status,
                "reportDate" to incident.reportDate,
                "photos" to evidences.map { evidence -> mapOf("id" to evidence.id, "url" to evidence.photoUrl) }
            )
        }
    }

    // Función auxiliar para calcular la distancia entre dos coordenadas utilizando la fórmula de Haversine
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}

