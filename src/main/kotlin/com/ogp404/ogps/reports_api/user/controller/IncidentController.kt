package com.ogp404.ogps.reports_api.incident.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ogp404.ogps.reports_api.incident.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.incident.service.IncidentService
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/v1/incidents")
class IncidentController(
    private val incidentService: IncidentService
) {
    private val logger = LoggerFactory.getLogger(IncidentController::class.java)

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun reportIncident(
        @RequestHeader("Authorization") authHeader: String,
        @RequestPart("incident") incidentJson: String,
        @RequestPart("photos") photos: Array<MultipartFile>?
    ): ResponseEntity<Any> {
        return try {
            // Validamos el token
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }
            logger.info("Token received: $token")

            // Log del JSON recibido
            logger.info("Received incident JSON: $incidentJson")

            // Deserializamos el JSON manualmente
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.registerModule(KotlinModule.Builder().build())
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            val incidentBody: IncidentBody = objectMapper.readValue(incidentJson, IncidentBody::class.java)
            logger.info("Deserialized incidentBody: $incidentBody")

            // Validamos que se haya enviado al menos una foto
            if (photos == null || photos.isEmpty()) {
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "At least one photo is required")
            }

            // Validamos tipo y tamaño de las fotos
            val allowedTypes = setOf("image/jpeg", "image/png", "image/jpg")
            val maxSize = 5 * 1024 * 1024 // 5 MB

            photos.forEach { photo ->
                logger.info("Processing photo: ${photo.originalFilename}, size: ${photo.size}, type: ${photo.contentType}")
                if (!allowedTypes.contains(photo.contentType)) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid file type: ${photo.contentType}")
                }
                if (photo.size > maxSize) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File too large: ${photo.originalFilename}")
                }
                if (photo.originalFilename.isNullOrBlank()) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File name cannot be empty")
                }
            }

            // Guardamos las fotos y generamos los URLs
            val uploadDir = Paths.get("uploads")
            if (!Files.exists(uploadDir)) {
                try {
                    Files.createDirectories(uploadDir)
                    logger.info("Created upload directory: $uploadDir")
                } catch (e: Exception) {
                    logger.error("Failed to create upload directory: ${e.message}", e)
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create upload directory")
                }
            }

            val photoUrls = photos.map { photo ->
                try {
                    val fileName = "${UUID.randomUUID()}_${photo.originalFilename}"
                    val filePath = uploadDir.resolve(fileName)
                    Files.copy(photo.inputStream, filePath)
                    logger.info("Saved photo to: $filePath")
                    "http://localhost:8080/uploads/$fileName"
                } catch (e: Exception) {
                    logger.error("Failed to save photo ${photo.originalFilename}: ${e.message}", e)
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save photo: ${e.message}")
                }
            }
            logger.info("Generated photo URLs: $photoUrls")

            // Llamamos al servicio para registrar el incidente
            logger.info("Calling incidentService.reportIncident with token: $token, incidentBody: $incidentBody, photoUrls: $photoUrls")
            val savedIncident: Incident = incidentService.reportIncident(token, incidentBody, photoUrls)
            logger.info("Incident saved successfully: $savedIncident")

            ResponseEntity.status(HttpStatus.CREATED).body(savedIncident)
        } catch (ex: ResponseStatusException) {
            logger.error("ResponseStatusException: ${ex.statusCode} - ${ex.reason}", ex)
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            logger.error("Error processing incident: ${ex.message}", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error reporting incident: ${ex.message}"))
        }
    }

    @PostMapping("/upload-photos", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadPhotos(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam("photos") photos: Array<MultipartFile>?
    ): ResponseEntity<Any> {
        return try {
            // Validamos el token
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            // Validamos que se hayan enviado fotos
            if (photos == null || photos.isEmpty()) {
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No photos uploaded")
            }

            // Validamos tipo y tamaño de las fotos
            val allowedTypes = setOf("image/jpeg", "image/png", "image/jpg")
            val maxSize = 5 * 1024 * 1024 // 5 MB

            photos.forEach { photo ->
                if (!allowedTypes.contains(photo.contentType)) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid file type: ${photo.contentType}")
                }
                if (photo.size > maxSize) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File too large: ${photo.originalFilename}")
                }
                if (photo.originalFilename.isNullOrBlank()) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File name cannot be empty")
                }
            }

            // Guardamos las fotos y generamos los URLs
            val uploadDir = Paths.get("uploads")
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            val photoUrls = photos.map { photo ->
                val fileName = "${UUID.randomUUID()}_${photo.originalFilename}"
                val filePath = uploadDir.resolve(fileName)
                Files.copy(photo.inputStream, filePath)
                "http://localhost:8080/uploads/$fileName"
            }

            ResponseEntity.ok(mapOf("photoUrls" to photoUrls))
        } catch (ex: ResponseStatusException) {
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(mapOf("error" to "Error uploading images: ${ex.message}"))
        }
    }
}