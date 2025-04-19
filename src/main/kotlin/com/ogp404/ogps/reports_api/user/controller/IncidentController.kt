package com.ogp404.ogps.reports_api.incident.controller

import com.ogp404.ogps.reports_api.incident.controller.body.IncidentBody
import com.ogp404.ogps.reports_api.incident.service.IncidentService
import com.ogp404.ogps.reports_api.user.repository.entity.Incident
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

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun reportIncident(
        @RequestHeader("Authorization") authHeader: String,
        @RequestPart("incident") incidentBody: IncidentBody,
        @RequestPart("photos") photos: Array<MultipartFile>?
    ): ResponseEntity<Any> {
        return try {
            // Validamos el token
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            // Validamos tipo y tamaño de las fotos (si se enviaron)
            val allowedTypes = setOf("image/jpeg", "image/png", "image/jpg")
            val maxSize = 5 * 1024 * 1024 // 5 MB

            photos?.forEach { photo ->
                if (!allowedTypes.contains(photo.contentType)) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid file type: ${photo.contentType}")
                }
                if (photo.size > maxSize) {
                    throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File too large: ${photo.originalFilename}")
                }
            }

            // Guardamos las fotos y generamos los URLs
            val uploadDir = Paths.get("uploads")
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            val photoUrls = photos?.map { photo ->
                val fileName = "${UUID.randomUUID()}_${photo.originalFilename}"
                val filePath = uploadDir.resolve(fileName)
                Files.copy(photo.inputStream, filePath)
                "http://localhost:8080/uploads/$fileName"
            } ?: emptyList()

            // Llamamos al servicio para registrar el incidente
            val savedIncident: Incident = incidentService.reportIncident(token, incidentBody, photoUrls)

            ResponseEntity.status(HttpStatus.CREATED).body(savedIncident)
        } catch (ex: ResponseStatusException) {
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error reporting incident: ${ex.message}"))
        }
    }

    // Opcional: Mantén este endpoint si quieres permitir subir imágenes por separado
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