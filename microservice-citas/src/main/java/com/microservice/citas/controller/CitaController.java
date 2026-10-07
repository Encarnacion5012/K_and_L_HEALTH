package com.microservice.citas.controller;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.service.ICitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
    private final ICitaService  citaService;

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','RECEPCIONISTA')")
    @PostMapping("/create_cita")
    public ResponseEntity<DetalleCitaDto> createCita(@RequestBody CreateCitaDto createDto) {
        DetalleCitaDto cita = citaService.create(createDto);
        return ResponseEntity.ok().body(cita);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/findAll")
    public ResponseEntity<Page<DetalleCitaDto>> findAll( @PageableDefault(size = 10) Pageable pageable) {
        Page<DetalleCitaDto> citas = citaService.findAllAgendadaAndReagendada(pageable);
        return  ResponseEntity.ok().body(citas);
    }


}
