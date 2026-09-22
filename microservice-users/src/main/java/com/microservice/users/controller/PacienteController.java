package com.microservice.users.controller;


import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import com.microservice.users.service.pacientes.IPacienteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
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
        Page<DetallesPacienteProjection> pacientes = pacienteService.findAllActivo(pageable);
        return ResponseEntity.ok(pacientes);
    }

    @PutMapping("/update/{id_paciente}")
    public ResponseEntity<DetallePacienteDto> updatePaciente(@RequestBody UpdatePacienteDto updatePacienteDTO, @PathVariable UUID id_paciente) {
       DetallePacienteDto pacienteACtualizado = pacienteService.update(id_paciente, updatePacienteDTO);
        return ResponseEntity.ok(pacienteACtualizado);
    }

    @PutMapping("/descativate_paciente/{id_paciente}")
    public ResponseEntity<Void> descativatePaciente(@PathVariable UUID id_paciente) {
        pacienteService.desactivarPaciente(id_paciente);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reactivate_paciente/{id_paciente}")
    public ResponseEntity<Void> reactivatePaciente(@PathVariable UUID id_paciente) {
        pacienteService.reactivarPaciente(id_paciente);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find_by_id/{id_paciente}")
    public ResponseEntity<DetallesPacienteProjection> findByIdPaciente(@PathVariable UUID id_paciente) {
        DetallesPacienteProjection paciente = pacienteService.getByIdPaciente(id_paciente);
        return ResponseEntity.ok(paciente);
    }

    @GetMapping("/find_by_email/{email_paciente}")
    public ResponseEntity<DetallesPacienteProjection> findByEmailPaciente(@PathVariable @Email(message = "Intrudusca un formato valido de email: expample@example.example")
                                                                      String email_paciente) {
        DetallesPacienteProjection paciente = pacienteService.findByEmail(email_paciente);
        return ResponseEntity.ok(paciente);
    }

    @GetMapping("/find_by_telefono/{telefono}")
    public ResponseEntity<List<DetallesPacienteProjection>> findByTelefonoPaciente(@PathVariable String telefono) {
        List<DetallesPacienteProjection> pacietes = pacienteService.findAllByTelefono(telefono);
        return ResponseEntity.ok(pacietes);
    }

    @GetMapping("/find_by_cedula/{cedula}")
    public ResponseEntity<DetallesPacienteProjection> findByCedulaPaciente(@PathVariable String cedula) {
        DetallesPacienteProjection paciente = pacienteService.findByCedula(cedula);
        return ResponseEntity.ok(paciente);
    }

}
