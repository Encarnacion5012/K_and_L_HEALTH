package com.microservice.users.controller;

import com.microservice.users.dto.especialidad.CreateEspecialidadDto;
import com.microservice.users.dto.especialidad.DetalleEspecialidadDto;
import com.microservice.users.model.Especialidad;
import com.microservice.users.projections.doctor.DetallesEspecialidadProjection;
import com.microservice.users.service.especialidad.IEspecialidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad")
@RequiredArgsConstructor
public class EspecialidadController {
    private final IEspecialidadService especialidadService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<DetallesEspecialidadProjection>> findAll(){

        List<DetallesEspecialidadProjection> especialidades =  especialidadService.findAll();
       return ResponseEntity.ok( especialidades);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<DetalleEspecialidadDto> create(@RequestBody CreateEspecialidadDto createEspecialidadDto){
        DetalleEspecialidadDto newEspecialidad = especialidadService.createEspcialidad(createEspecialidadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEspecialidad);
    }
}
