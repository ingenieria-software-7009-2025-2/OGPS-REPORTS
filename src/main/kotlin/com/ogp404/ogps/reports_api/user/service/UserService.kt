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

    /*fun retrieveAllUser(): List<Usuario> {
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
    }*/

    fun login(mail: String, password: String): Usuario? {
        val userFound = personRepository.findByMailAndPassword(mail, password)

        return if (userFound != null) {
            val token = UUID.randomUUID().toString()
            updateTokenUser(userFound, token)
            Usuario(
                id = userFound.id,
                userName = userFound.userName,
                firstName = userFound.firstName,
                lastName = userFound.lastName,
                mail = userFound.mail,
                token = token,
                password = userFound.password,
                role = userFound.role,
            )
        } else{
            userFound
        }
    }

    fun updateTokenUser(user: Person, token: String) {
        user.token = token
        personRepository.save(user)  // Guarda la entidad de la persona con el token actualizado.
    }


    fun logout(token: String): Boolean {
        val userFound = personRepository.findByToken(token)

         if (userFound != null) {
            userFound.token = null
            personRepository.save(userFound)
         return true
        } else return false
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
                token = "*****",
                password = it.password,
                role = it.role,
            )
        }
    }

    @Transactional
    fun updateMe(token: String, usuarioActualizado: Usuario): Usuario?{
        val userFound = personRepository.findByToken(token)?: return null

            userFound.userName = usuarioActualizado.userName.ifEmpty { userFound.userName }
            userFound.firstName = usuarioActualizado.firstName.ifEmpty { userFound.firstName }
            userFound.lastName = usuarioActualizado.lastName.ifEmpty { userFound.lastName }
            userFound.password = usuarioActualizado.password.ifEmpty { userFound.password }
            userFound.mail = usuarioActualizado.mail.ifEmpty { userFound.mail }
            userFound.role = usuarioActualizado.role.ifEmpty { userFound.role }

            val updatedPerson = personRepository.save(userFound)

        return Usuario(
            id = updatedPerson.id,
            userName = updatedPerson.userName,
            firstName = updatedPerson.firstName,
            lastName = updatedPerson.lastName,
            mail = updatedPerson.mail,
            token = "***", // Ocultar token por seguridad
            password = "***", // No devolver la contraseña
            role = updatedPerson.role
        )
    }

}
