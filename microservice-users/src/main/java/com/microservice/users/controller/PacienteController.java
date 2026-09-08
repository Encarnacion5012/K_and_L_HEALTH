package com.microservice.users.controller;


import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.service.pacientes.IPacienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    private final IPacienteService pacienteService;


    @PostMapping("/create")
    public ResponseEntity<DetallePacienteDTO> create(@RequestBody @Valid RegistroPacienteDTO registroPacienteDTO, UriComponentsBuilder uriComponentsBuilder) {
        DetallePacienteDTO nuevoPaciente = pacienteService.create(registroPacienteDTO);

        URI uri = uriComponentsBuilder.path("api/pacientes/create/{id}").buildAndExpand(nuevoPaciente.idPaciente()).toUri();

        return ResponseEntity.created(uri).body(nuevoPaciente);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DetallePacienteDTO>> findAll(@PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        return ResponseEntity.ok(pacienteService.findAll(pageable));
    }

}
