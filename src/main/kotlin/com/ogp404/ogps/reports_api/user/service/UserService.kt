package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private var userRepository: UserRepository) {

    fun addUser(usuario: Usuario): Usuario {
        // Convertimos el objeto del dominio al objeto que necesita nuestra BD
        val usuarioDB = Person(
            id = usuario.id,
            userName = usuario.userName,
            password = usuario.password,
            mail = usuario.mail,
            token = usuario.token,
            firstName = usuario.firstName,
            lastName = usuario.lastName,
            role = usuario.role.ifEmpty { "User" }  // Si no se especifica, es "User"
        )

        val result = userRepository.save(usuarioDB)

        // Convertimos el objeto de nuestra BD a un objeto de nuestro dominio.
        return Usuario(
            id = result.id,
            userName = result.userName,
            firstName = result.firstName,
            lastName = result.lastName,
            mail = result.mail,
            token = result.token,
            password = result.password,
            role = result.role,
        )
    }

    fun retrieveAllUser(): List<Usuario> {
        return userRepository.findAll().map { person ->
            Usuario(
                id = person.id,
                userName = person.userName,
                firstName = person.firstName,
                lastName = person.lastName,
                mail = person.mail,
                token = person.token,
                password = person.password,
                role = person.role,
            )
        }
    }

    /* fun login(mail: String, password: String): Usuario? {

        val userFound = userRepository.findByMailAndPassword(mail, password)

        return userFound?.let {
            val token = UUID.randomUUID().toString()
            updateTokenUser(it, token)
            Usuario(
                id = it.id.toString(),
                userName = it.userName,
                firstName = it.firstName,
                lastName = it.lastName,
                mail = it.mail,
                token = token,
                password = it.password,
                role = it.role,
            )
        }
    }*/

    fun updateTokenUser(user: Person, token: String) {
        user.token = token
        userRepository.save(user)
    }

    fun logout(token: String): Boolean {
        val userFound = userRepository.findByToken(token)

        return if (userFound != null) {
            userFound.token = null
            userRepository.save(userFound)
            true
        } else false
    }

    fun getInfoAboutMe(token: String): Usuario? {
        val userFound = userRepository.findByToken(token)

        return userFound?.let {
            Usuario(
                id = it.id,
                userName = it.userName,
                firstName = it.firstName,
                lastName = it.lastName,
                mail = it.mail,
                token = "*******",
                password = it.password,
                role = it.role,
            )
        }
    }
}
