package com.ogp404.ogps.reports_api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/mascota")
class MascotaController {

    @GetMapping
    fun retrieveMascota(): ResponseEntity<Mascota>{

        val miMascota = Mascota(tipo = "Perro", nombre = "Pancracio", peso = "10kg" )
        return ResponseEntity.ok(miMascota)
    }

    @PostMapping
    fun createMascota(@RequestBody mascotaBody: MascotaBody): ResponseEntity<Mascota>{

        val miMascota = Mascota(
            tipo = mascotaBody.tipo,
            nombre = mascotaBody.nombre,
            peso = mascotaBody.peso )
        return ResponseEntity.ok(miMascota)
    }

}