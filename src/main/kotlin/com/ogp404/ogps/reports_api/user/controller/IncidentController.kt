package com.ogp404.ogps.reports_api.user.controller

import com.ogp404.ogps.reports_api.user.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.user.controller.body.LoginUserBody
import com.ogp404.ogps.reports_api.user.controller.body.UserBody
import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.domain.Incidente
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import com.ogp404.ogps.reports_api.user.service.UserService
import com.ogp404.ogps.reports_api.user.service.IncidentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


    /**
     En proceso de desarrollo, pendiente para Iteración02
     * Controlador para gestionar las operaciones relacionadas con los usuarios.

    @Controller
    @RequestMapping("/v1/incident")
    class IncidentController(var incidentService: IncidentService) {

        /**
         * Endpoint para registrar un nuevo usuario.
         * @param userBody Datos del usuario que se recibirán en la petición.
         * @return ResponseEntity con la respuesta del servicio.
         */
        @PostMapping
        fun reportIncident(@RequestBody incidentBody: IncidentBody): ResponseEntity<Any> {
            // Convertir los datos del request a un objeto del dominio
            val incidente = Incidente(
                latitude = incidentBody.latitude,
                longitude = incidentBody.longitude,
                category = incidentBody.category,
                description  = incidentBody.description,
                status = incidentBody.status,
                reportDate= incidentBody.reportDate
            )

            val response = incidentService.reportIncident()
            return if (response != null) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(401).build()
            }
        }

    }
     */