package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.UserEntityRepository
import com.ogp404.ogps.reports_api.user.repository.AdminEntityRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import com.ogp404.ogps.reports_api.user.repository.entity.UserEntity
import com.ogp404.ogps.reports_api.user.repository.entity.AdminEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private var userRepository: UserRepository, 
    private val userEntityRepository: UserEntityRepository,
    private val adminEntityRepository: AdminEntityRepository) {

   @Transactional
    fun addUser(usuario: Usuario): Usuario {
        val personEntity = Person(
            id = usuario.id,
            userName = usuario.userName,
            password = usuario.password,
            mail = usuario.mail,
            token = usuario.token,
            firstName = usuario.firstName,
            lastName = usuario.lastName,
            role = usuario.role.ifEmpty { "User" }
        )

        val savedPerson = userRepository.save(personEntity)

        // Promt CLAUDE
        when (savedPerson.role) {
            "Administrator" -> {
                val adminEntity = AdminEntity(person = savedPerson)
                adminEntityRepository.save(adminEntity)
            }
            else -> {
                val userEntity = UserEntity(person =  savedPerson)
                userEntityRepository.save(userEntity)
            }
        }

        return Usuario(
            id = savedPerson.id,
            userName = savedPerson.userName,
            firstName = savedPerson.firstName,
            lastName = savedPerson.lastName,
            mail = savedPerson.mail,
            token = savedPerson.token,
            password = savedPerson.password,
            role = savedPerson.role
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
