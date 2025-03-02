package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.repository.PersonRepository
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.AdminRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import com.ogp404.ogps.reports_api.user.repository.entity.User
import com.ogp404.ogps.reports_api.user.repository.entity.Admin
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private var personRepository: PersonRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository) {

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

        val savedPerson = personRepository.save(personEntity)

        // Promt CLAUDE
        when (savedPerson.role) {
            "Administrator" -> {
                val adminEntity = Admin(person = savedPerson)
                adminRepository.save(adminEntity)
            }
            else -> {
                val userEntity = User(person =  savedPerson)
                userRepository.save(userEntity)
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
        return personRepository.findAll().map { person ->
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
        personRepository.save(user)
    }

    fun logout(token: String): Boolean {
        val userFound = personRepository.findByToken(token)

        return if (userFound != null) {
            userFound.token = null
            personRepository.save(userFound)
            true
        } else false
    }

    fun getInfoAboutMe(token: String): Usuario? {
        val userFound = personRepository.findByToken(token)

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
