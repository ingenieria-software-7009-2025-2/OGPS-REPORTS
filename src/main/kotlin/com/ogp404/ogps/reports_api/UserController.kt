package com.ogp404.ogps.reports_api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user")
class UserController {

    @PostMapping("/create")
    fun createUser(@RequestBody create: User): ResponseEntity<User>{
        return ResponseEntity.ok(create)
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody login: User): ResponseEntity<User>{
        return ResponseEntity.ok(login)
    }

    @PostMapping("/logout")
    fun logoutUser(): ResponseEntity<String>{
        return ResponseEntity.ok("Sesión cerrada")
    }

    @GetMapping("/me")
    fun retriveUser(): ResponseEntity<User>{
        val meUser = User(userName = "BTCloutt1", mail = "geovanimoxito@gmail.com", password = "geovanibbcito20", token = "OGPRP")
        return ResponseEntity.ok(meUser)
    }

}

