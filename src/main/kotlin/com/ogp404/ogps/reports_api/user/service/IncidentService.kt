package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.user.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.user.domain.Incidente
import com.ogp404.ogps.reports_api.user.repository.IncidentRepository
import com.ogp404.ogps.reports_api.user.repository.PersonRepository
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.AdminRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import com.ogp404.ogps.reports_api.user.repository.entity.User
import com.ogp404.ogps.reports_api.user.repository.entity.Admin
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties.Token
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IncidentService (
    private val incidentRepository: IncidentRepository,
    private var personRepository: PersonRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository) {

    @Transactional
    fun reportIncident(token: String, incidente: Incidente): Incidente? {
        val userFound = personRepository.findByToken(token) ?: return null
        //val userFound1 = userRepository.findByPerson(userFound)

        return if (userFound.role == "User") {
            val incident = Incident(
                idIncident = incidente.idIncident,
                user = null,
                admin = null,
                latitude = incidente.latitude,
                longitude = incidente.longitude,
                category = incidente.category,
                description = incidente.description,
                status = incidente.status.ifEmpty { "Reported" },
                reportDate = incidente.reportDate
            )

            val savedIncident = incidentRepository.save(incident)

            // Convertimos la entidad `Incident` de la BD a un objeto `Incidente` del dominio antes de retornarlo
            Incidente(
                idIncident = savedIncident.idIncident,
                latitude = savedIncident.latitude,
                longitude = savedIncident.longitude,
                category = savedIncident.category,
                description = savedIncident.description,
                status = savedIncident.status,
                reportDate = savedIncident.reportDate
            )
        } else {
            null
        }
    }
}