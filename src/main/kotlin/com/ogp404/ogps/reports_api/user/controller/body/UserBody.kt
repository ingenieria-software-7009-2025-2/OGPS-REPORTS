package com.ogp404.ogps.reports_api.user.controller.body

data class UserBody(
    val userName: String,   // Nombre de usuario (user_name en la BD)
    val firstName: String,  // Nombre (first_name en la BD)
    val lastName: String,   // Apellido (last_name en la BD)
    val password: String,   // Contraseña (password en la BD)
    val mail: String,       // Correo electrónico (mail en la BD)
    val role: String // Puede ser "User" o "Administrator" (por defecto "User")
)
