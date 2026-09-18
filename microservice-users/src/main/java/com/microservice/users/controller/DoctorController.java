package com.microservice.users.controller;



import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import com.microservice.users.service.doctor.IDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;


    @PostMapping("/create")
    public ResponseEntity<DetalleDoctorDto> cretaeDoctor(@RequestBody CreateDcotorDto dto) {
        return ResponseEntity.ok(doctorService.createDoctor(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DetallesDoctorProjection>> findAll(@PageableDefault(size = 10, sort = "idDoctor") Pageable pageable) {
        return ResponseEntity.ok(doctorService.findAll(pageable));
    }
}
