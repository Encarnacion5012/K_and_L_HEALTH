package com.microservice.citas.controller;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.model.Cita;
import com.microservice.citas.service.ICitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
    private final ICitaService  citaService;

    @PostMapping("/create_cita")
    public ResponseEntity<DetalleCitaDto> createCita(@RequestBody CreateCitaDto createDto) {
        DetalleCitaDto cita = citaService.create(createDto);
        return ResponseEntity.ok().body(cita);
    }
}
