package com.microservice.users.controller;


import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
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
import java.util.UUID;


@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    private final IPacienteService pacienteService;


    @PostMapping("/create")
    public ResponseEntity<DetallePacienteDto> create(@RequestBody @Valid CreatePacienteDto createPacienteDTO, UriComponentsBuilder uriComponentsBuilder) {
        DetallePacienteDto nuevoPaciente = pacienteService.create(createPacienteDTO);

        URI uri = uriComponentsBuilder.path("api/pacientes/create/{id}").buildAndExpand(nuevoPaciente.idPaciente()).toUri();

        return ResponseEntity.created(uri).body(nuevoPaciente);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DetallesPacienteProjection>> findAll(@PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        return ResponseEntity.ok(pacienteService.findAll(pageable));
    }

    @PutMapping("/update/{id_paciente}")
    public ResponseEntity<DetallePacienteDto> updatePaciente(@RequestBody UpdatePacienteDto updatePacienteDTO, @PathVariable UUID id_paciente) {
       DetallePacienteDto pacienteACtualizado = pacienteService.update(id_paciente, updatePacienteDTO);
        return ResponseEntity.ok(pacienteACtualizado);
    }

}
