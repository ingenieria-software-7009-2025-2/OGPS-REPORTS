package com.ogp404.ogps.reports_api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/usuario")
class UsuarioController {

    @PostMapping("/create")
    fun createUsuario(@RequestBody create: Usuario): ResponseEntity<Usuario>{
        return ResponseEntity.ok(create)
    }

    @PostMapping("/login")
    fun loginUsuario(@RequestBody login: Usuario): ResponseEntity<Usuario>{
        return ResponseEntity.ok(login)
    }

    @PostMapping("/logout")
    fun logoutUsuario(): ResponseEntity<String>{
        return ResponseEntity.ok("Sesión cerrada")
    }

    @GetMapping("/me")
    fun retriveUsuario(): ResponseEntity<Usuario>{
        val meUsuario = Usuario(userName = "BTCloutt1", mail = "geovanimoxito@gmail.com", password = "geovanibbcito20", token = "OGPRP")
        return ResponseEntity.ok(meUsuario)
    }

}

