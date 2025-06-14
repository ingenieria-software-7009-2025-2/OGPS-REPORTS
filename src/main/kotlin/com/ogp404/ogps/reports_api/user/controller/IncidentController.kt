package com.ogp404.ogps.reports_api.incident.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ogp404.ogps.reports_api.incident.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.incident.service.IncidentService
import com.ogp404.ogps.reports_api.user.controller.body.VerificationBody
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
import com.ogp404.ogps.reports_api.user.service.VerificationService
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
    private val incidentService: IncidentService,
    private val verificationService: VerificationService
) {
    private val logger = LoggerFactory.getLogger(IncidentController::class.java)

    @PostMapping("/{id}/verify")
    fun verifyIncident(
        @PathVariable id: Int,
        @RequestBody body: VerificationBody
    ): ResponseEntity<String> {
        val result = verificationService.verifyIncident(id, body.idUser)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}/verification-count")
    fun getVerificationCount(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            logger.info("Getting verification count for incident: $id")
            val count = verificationService.getVerificationCount(id)
            logger.info("Verification count for incident $id: $count")

            ResponseEntity.ok(count)
        } catch (ex: ResponseStatusException) {
            logger.error("ResponseStatusException: ${ex.statusCode} - ${ex.reason}", ex)
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            logger.error("Error getting verification count: ${ex.message}", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error getting verification count: ${ex.message}"))
        }
    }

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
            logger.info("Max allowed size for photos: $maxSize bytes (5 MB)")

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

            val photoUrls = photos?.map { photo ->
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
            } ?: emptyList()
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

    @GetMapping("/my-reports")
    fun getMyIncidents(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            val incidents = incidentService.getIncidentsByUserToken(token)
            ResponseEntity.ok(incidents)
        } catch (ex: ResponseStatusException) {
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error retrieving incidents: ${ex.message}"))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteIncident(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable("id") incidentId: Int
    ): ResponseEntity<Any> {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }
            logger.info("Attempting to delete incident with ID: $incidentId")

            incidentService.deleteIncident(token, incidentId)
            ResponseEntity.ok(mapOf("message" to "Incident deleted successfully"))
        } catch (ex: ResponseStatusException) {
            logger.error("ResponseStatusException: ${ex.statusCode} - ${ex.reason}", ex)
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            logger.error("Error deleting incident: ${ex.message}", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error deleting incident: ${ex.message}"))
        }
    }

    @GetMapping("/nearby")
    fun getNearbyIncidents(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "5.0") radius: Double
    ): ResponseEntity<Any> {
        return try {
            // Validamos el token
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            // Validamos los parámetros
            if (latitude < -90.0 || latitude > 90.0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid latitude. Must be between -90 and 90")
            }
            if (longitude < -180.0 || longitude > 180.0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid longitude. Must be between -180 and 180")
            }
            if (radius <= 0.0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Radius must be greater than 0")
            }

            logger.info("Getting nearby incidents at coordinates ($latitude, $longitude) with radius $radius km")
            val nearbyIncidents = incidentService.getNearbyIncidents(token, latitude, longitude, radius)

            if (nearbyIncidents.isEmpty()) {
                logger.info("No incidents found near coordinates ($latitude, $longitude) within $radius km")
            } else {
                logger.info("Found ${nearbyIncidents.size} incidents near coordinates ($latitude, $longitude) within $radius km")
            }

            ResponseEntity.ok(nearbyIncidents)
        } catch (ex: ResponseStatusException) {
            logger.error("ResponseStatusException: ${ex.statusCode} - ${ex.reason}", ex)
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            logger.error("Error retrieving nearby incidents: ${ex.message}", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error retrieving nearby incidents: ${ex.message}"))
        }
    }

    @GetMapping("/all")
    fun getAllIncidents(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            val incidents = incidentService.getAllIncidents(token)
            ResponseEntity.ok(incidents)
        } catch (ex: ResponseStatusException) {
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error retrieving all incidents: ${ex.message}"))
        }
    }

    @PutMapping("/{id}/status", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateIncidentStatus(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable("id") incidentId: Int,
        @RequestParam("status") status: String,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("photos", required = false) photos: Array<MultipartFile>?
    ): ResponseEntity<Any> {
        return try {
            // Validamos el token
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }
            logger.info("Token received for status update: $token")
            logger.info("Updating incident $incidentId with status: $status, description: $description")

            // Validamos que el status no esté vacío
            if (status.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Status is required")
            }

            // Validamos y procesamos las fotos si existen
            var photoUrls: List<String> = emptyList()
            if (photos != null && photos.isNotEmpty()) {
                // Validamos tipo y tamaño de las fotos
                val allowedTypes = setOf("image/jpeg", "image/png", "image/jpg")
                val maxSize = 5 * 1024 * 1024 // 5 MB
                
                photos.forEach { photo ->
                    logger.info("Processing photo for status update: ${photo.originalFilename}, size: ${photo.size}, type: ${photo.contentType}")
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

                photoUrls = photos.map { photo ->
                    try {
                        val fileName = "${UUID.randomUUID()}_${photo.originalFilename}"
                        val filePath = uploadDir.resolve(fileName)
                        Files.copy(photo.inputStream, filePath)
                        logger.info("Saved status update photo to: $filePath")
                        "http://localhost:8080/uploads/$fileName"
                    } catch (e: Exception) {
                        logger.error("Failed to save photo ${photo.originalFilename}: ${e.message}", e)
                        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save photo: ${e.message}")
                    }
                }
                logger.info("Generated photo URLs for status update: $photoUrls")
            }

            // Llamamos al servicio para actualizar el incidente
            val updatedIncident = incidentService.updateIncidentStatus(token, incidentId, status, description, photoUrls)
            logger.info("Incident status updated successfully: $updatedIncident")

            ResponseEntity.ok(updatedIncident)
        } catch (ex: ResponseStatusException) {
            logger.error("ResponseStatusException: ${ex.statusCode} - ${ex.reason}", ex)
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            logger.error("Error updating incident status: ${ex.message}", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error updating incident status: ${ex.message}"))
        }
    }
}

