package com.microservice.users.controller;


import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.service.pacientes.IPacienteService;
import com.sun.tools.rngom.util.Uri;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    private IPacienteService pacienteService;


    @PostMapping("/create")
    public ResponseEntity<DetallePacienteDTO> create(@RequestBody @Valid RegistroPacienteDTO registroPacienteDTO, UriComponentsBuilder uriComponentsBuilder) {
        DetallePacienteDTO nuevoPaciente = pacienteService.create(registroPacienteDTO);

        URI uri = uriComponentsBuilder.path("api/pacientes/create/{id}").buildAndExpand(nuevoPaciente.idPaciente()).toUri();

        return ResponseEntity.created(uri).body(nuevoPaciente);
    }

}
