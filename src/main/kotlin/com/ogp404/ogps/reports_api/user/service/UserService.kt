package com.ogp404.ogps.reports_api.user.service

import com.ogp404.ogps.reports_api.user.domain.Usuario
import com.ogp404.ogps.reports_api.user.repository.PersonRepository
import com.ogp404.ogps.reports_api.user.repository.UserRepository
import com.ogp404.ogps.reports_api.user.repository.AdminRepository
import com.ogp404.ogps.reports_api.user.repository.entity.Person
import com.ogp404.ogps.reports_api.user.repository.entity.User
import com.ogp404.ogps.reports_api.user.repository.entity.Admin
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class
UserService(
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

    fun login(mail: String, password: String): Usuario {
        if (!isValidEmail(mail)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo con formato inválido")
        }

        val userFound = personRepository.findByMail(mail)

        if (userFound == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Correo no registrado")
        }

        if (userFound.password != password) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta")
        }

        val token = UUID.randomUUID().toString()
        updateTokenUser(userFound, token)

        return Usuario(
            id = userFound.id,
            userName = userFound.userName,
            firstName = userFound.firstName,
            lastName = userFound.lastName,
            mail = userFound.mail,
            token = token,
            password = userFound.password,
            role = userFound.role,
        )
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|outlook|hotmail)\\.com$".toRegex(RegexOption.IGNORE_CASE)
        return email.matches(emailRegex)
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
    fun updateMe(token: String, usuarioActualizado: Usuario): Usuario? {
        val userFound = personRepository.findByToken(token) ?: return null

        val emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|outlook|hotmail)\\.com$".toRegex()
        val passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$".toRegex()
        val nameRegex = "^[a-zA-ZÁÉÍÓÚáéíóúÑñ]{2,}$".toRegex()
        val userNameRegex = "^[a-zA-Z0-9._-]{3,}$".toRegex()

        usuarioActualizado.userName.takeIf { it.isNotBlank() }?.let { newUserName ->
            if (!newUserName.matches(userNameRegex)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid username format, must be at least 3 characters and can only contain letters, numbers, hyphens, or periods"
                )
            }
            if (newUserName == userFound.userName) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You already have this information")
            }
            val existingUserName = personRepository.findByUserName(newUserName)
            if (existingUserName != null && existingUserName.id != userFound.id) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already in use")
            }
            userFound.userName = newUserName
        }

        usuarioActualizado.mail.takeIf { it.isNotBlank() }?.let { newMail ->
            if (!newMail.matches(emailRegex)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email format, only gmail, outlook, and hotmail domains are accepted"
                )
            }
            if (newMail == userFound.mail) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You already have this information")
            }
            val existingUser = personRepository.findByMail(newMail)
            if (existingUser != null && existingUser.id != userFound.id) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
            }
            userFound.mail = newMail
        }

        usuarioActualizado.password.takeIf { it.isNotBlank() }?.let { newPassword ->
            if (!newPassword.matches(passwordRegex)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Weak password, must contain at least 1 uppercase letter, 1 number, 1 special character, and be at least 8 characters long"
                )
            }
            if (newPassword == userFound.password) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You already have this information")
            }
            userFound.password = newPassword
        }

        usuarioActualizado.firstName.takeIf { it.isNotBlank() }?.let { newFirstName ->
            if (!newFirstName.matches(nameRegex)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid first name, use at least 2 letters and no special characters"
                )
            }
            if (newFirstName == userFound.firstName) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You already have this information")
            }
            userFound.firstName = newFirstName
        }

        usuarioActualizado.lastName.takeIf { it.isNotBlank() }?.let { newLastName ->
            if (!newLastName.matches(nameRegex)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid last name, use at least 2 letters and no special characters"
                )
            }
            if (newLastName == userFound.lastName) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You already have this information")
            }
            userFound.lastName = newLastName
        }

        val updatedPerson = personRepository.save(userFound)
        return Usuario(
            id = updatedPerson.id,
            userName = updatedPerson.userName,
            firstName = updatedPerson.firstName,
            lastName = updatedPerson.lastName,
            mail = updatedPerson.mail,
            token = "***",
            password = "***",
            role = updatedPerson.role
        )
    }

}