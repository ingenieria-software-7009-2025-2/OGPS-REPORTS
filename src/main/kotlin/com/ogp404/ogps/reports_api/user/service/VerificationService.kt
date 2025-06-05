package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.incident.repository.IncidentRepository
import com.ogp404.ogps.reports_api.user.domain.Verificacion
import com.ogp404.ogps.reports_api.user.repository.VerificationRepository
import com.ogp404.ogps.reports_api.user.repository.UserRepository

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class VerificationService(
    private val verificationRepository: VerificationRepository,
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun verifyIncident(idIncident: Int, idUser: Int): String {

        if (verificationRepository.existsByIdUserAndIdIncident(idUser, idIncident)) {
            return "Ya has verificado este incidente"
        }

        val user = userRepository.findById(idUser).orElseThrow {
           ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
        }

        val incident = incidentRepository.findById(idIncident).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Incidente no encontrado")
        }

        // Guardar la verificación como entidad (no domain)
        val verification = Verificacion(idUser = idUser, idIncident = idIncident)
        verificationRepository.save(verification)

        // Verificar si se alcanzaron las 3 verificaciones
        val count = verificationRepository.countByIdIncident(idIncident)
        if (count >= 3 && incident.status != "Verified") {
            incident.status = "Verified"
            incidentRepository.save(incident)
        }

        return "Verificación registrada exitosamente"
    }
}
