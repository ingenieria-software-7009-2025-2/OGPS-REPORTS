package com.ogp404.ogps.reports_api.user.controller

import com.ogp404.ogps.reports_api.user.controller.body.LoginUserBody
import com.ogp404.ogps.reports_api.user.controller.body.UserBody
import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.repository.PersonRepository
import com.ogp404.ogps.reports_api.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.web.server.ResponseStatusException

/**
 * Controlador para gestionar las operaciones relacionadas con los usuarios.
 */
@Tag(name = "User Management", description = "Endpoints for user registration, authentication, and profile management")
@Controller
@RequestMapping("/v1/users")
class UserController(var userService: UserService) {

    /**
     * @Operation: Anotacion de SpringDoc OpenAPI utilizada para documentar un endpoint en Swagger
     * summary: Proporciona una breve descripcion de una linea del endpoint
     * description: Informacion más detallada sobre lo que hace el endpoint
     * responses: Posibles respuestas HTTP del endpoint
    */

    // Docuementacion dell endpoint para crear usuario
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account in the system",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "User successfully registered",
                content = [Content(schema = Schema(implementation = Usuario::class))]
            )
        ]
    )
    /**
     * Endpoint para registrar un nuevo usuario.
     * @param userBody Datos del usuario que se recibirán en la petición.
     * @return ResponseEntity con la respuesta del servicio.
     */
    @PostMapping
    fun addUser(
        @SwaggerRequestBody(description = "User registration details")
        @RequestBody userBody: UserBody
    ): ResponseEntity<Any> {
        return try {
        // Convertir los datos del request a un objeto del dominio
        val usuario = Usuario(
            userName = userBody.userName,
            firstName = userBody.firstName,
            lastName = userBody.lastName,
            password = userBody.password,
            mail = userBody.mail,
            role = userBody.role
        )
        val response = userService.addUser(usuario)
        return ResponseEntity.ok(response)
    } catch (ex: ResponseStatusException)
    {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }
    }

    /**
     * Endpoint para iniciar sesión.
     * @param loginUserBody Datos del usuario (correo y contraseña) para autenticación.
     * @return ResponseEntity con la información del usuario si la autenticación es exitosa, o un error con un mensaje.
     */
    @PostMapping("/login")
    fun login(@RequestBody loginUserBody: LoginUserBody): ResponseEntity<Any> {
        return try {
            val result = userService.login(loginUserBody.mail, loginUserBody.password)
            ResponseEntity.ok(result)
        } catch (ex: ResponseStatusException) {
            ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
        }
    }

    // Docuementacion del enpoint para cerrar sesion
    @Operation(
        summary = "User Logout",
        description = "Invalidate current user session",
        responses = [
            ApiResponse(responseCode = "200", description = "Successfully logged out"),
            ApiResponse(responseCode = "400", description = "Logout failed")
        ]
    )
    /**
     * Endpoint para cerrar sesión.
     * @param token Token de autorización proporcionado en la cabecera.
     * @return ResponseEntity con mensaje de éxito o error en caso de fallo.
     */
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        val successLogout = userService.logout(token)
        return if (!successLogout) {
            ResponseEntity.badRequest().build()
        } else {
            ResponseEntity.ok("Sesión finalizada")
        }
    }


    // Docuementacion del endpoint para obtener datos del usuario
    @Operation(
        summary = "Get User Profile",
        description = "Retrieve information about the currently authenticated user",
        responses = [
            ApiResponse(responseCode = "200", description = "User profile retrieved"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    /**
     * Endpoint para obtener la información del usuario autenticado.
     * @param token Token de autorización.
     * @return ResponseEntity con la información del usuario o un estado 401 si no es válido.
     */
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") token: String): ResponseEntity<Usuario> {
        val response = userService.getInfoAboutMe(token)
        return if (response != null) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(401).build()
        }
    }


    // Docuementacion del endpoint para modificar usuario
    @Operation(
        summary = "Update User Profile",
        description = "Update information for the currently authenticated user",
        responses = [
            ApiResponse(responseCode = "200", description = "User profile updated"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @PutMapping("/me")
    fun updateMe(
        @SwaggerRequestBody(description = "Updated user details")
        @RequestHeader("Authorization") token: String,
        @RequestBody updatedUserBody:UserBody
    ): ResponseEntity<Usuario> {
        val updatedUser = Usuario(
            userName = updatedUserBody.userName,
            firstName = updatedUserBody.firstName,
            lastName = updatedUserBody.lastName,
            password = updatedUserBody.password,
            mail = updatedUserBody.mail,
            role = updatedUserBody.role
        )
        val successLogout = userService.updateMe(token, updatedUser)
        return if (successLogout != null) {
            //ResponseEntity.badRequest().build()
            ResponseEntity.ok(successLogout)

        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }






     /*
     * Endpoint para obtener la lista de todos los usuarios registrados.
     * @return ResponseEntity con la lista de usuarios.
     */
        /*
    @GetMapping
    fun getAllUsers(): ResponseEntity<Any> {
        val result = userService.retrieveAllUser()
        return ResponseEntity.ok(result)
    }*/
}

