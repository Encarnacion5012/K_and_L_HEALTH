package com.microservice.citas.controller;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.dto.ReagendarCitaDto;
import com.microservice.citas.model.Cita;
import com.microservice.citas.service.ICitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
    private final ICitaService  citaService;

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','RECEPCIONISTA')")
    @PostMapping("/create_cita")
    public ResponseEntity<DetalleCitaDto> createCita(@RequestBody CreateCitaDto createDto) {
        DetalleCitaDto cita = citaService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cita);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/findAll")
    public ResponseEntity<Page<DetalleCitaDto>> findAll( @PageableDefault(size = 10) Pageable pageable) {
        Page<DetalleCitaDto> citas = citaService.findAllAgendadaAndReagendada(pageable);
        return  ResponseEntity.ok().body(citas);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','RECEPCIONISTA')")
    @PutMapping("/cncelar_cita/{id_Cita}")
    public ResponseEntity<Void> cancelarCita(@PathVariable UUID id_Cita){
        citaService.cancelarCita(id_Cita);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','RECEPCIONISTA')")
    @PutMapping("/reagendar_cita/{id_Cita}")
    public ResponseEntity<Void> reagendarCita(@PathVariable UUID id_Cita, @RequestBody ReagendarCitaDto reagendarCitaDto) {
        citaService.reagendarCita(id_Cita, reagendarCitaDto);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @GetMapping("/find_by_idDoctor/{idDoctor}")
    public ResponseEntity<Page<DetalleCitaDto>> findByIdDoctor(@PathVariable UUID idDoctor, @PageableDefault(size = 10) Pageable pageable){
        Page<DetalleCitaDto> citas = citaService.findByIdDoctorOrIdPaciente(idDoctor,pageable );
        return ResponseEntity.ok().body(citas);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','RECEPCIONISTA')")
    @GetMapping("/find_by_idPaciente/{IdPaciente}")
    public ResponseEntity<Page<DetalleCitaDto>> findByIdPaciente(@PathVariable UUID IdPaciente, @PageableDefault(size = 10) Pageable pageable){
        Page<DetalleCitaDto> citas = citaService.findByIdDoctorOrIdPaciente(IdPaciente,pageable );
        return ResponseEntity.ok().body(citas);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @GetMapping("/find_by_id_General/{idCita}")
    public ResponseEntity<DetalleCitaDto> findByIdGeneral(@PathVariable UUID idCita){
        DetalleCitaDto cita = citaService.findByIdCitaGenaral(idCita);
        return ResponseEntity.ok().body(cita);
    }






}
