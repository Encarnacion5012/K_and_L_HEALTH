package com.microservice.users.controller;



import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import com.microservice.users.service.doctor.IDoctorService;
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
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<DetalleDoctorDto> cretaeDoctor(@RequestBody CreateDcotorDto dto) {
        DetalleDoctorDto newDoctor = doctorService.createDoctor(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(newDoctor);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DetallesDoctorProjection>> findAll(@PageableDefault(size = 10, sort = "idDoctor") Pageable pageable) {
        Page<DetallesDoctorProjection> doctores = doctorService.findAllACtivo(pageable);
        return ResponseEntity.ok(doctores);
    }

    @GetMapping("/especiality/{especiality_Name}")
    public ResponseEntity<Page<DetallesDoctorProjection>>  findByEspeciality(@PathVariable String especiality_Name, @PageableDefault(size = 10, sort = {"nombre"}) Pageable pageable) {
        Page<DetallesDoctorProjection> doctores = doctorService.findALlByEspecialidad(especiality_Name, pageable);
        return ResponseEntity.ok(doctores);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/exequatur/{exequatur}")
    public ResponseEntity<DetallesDoctorProjection> findByExequatur(@PathVariable String exequatur) {
        DetallesDoctorProjection doctor = doctorService.findByExequatur(exequatur);
        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/desctivate/{id_doctor}")
    public ResponseEntity<Void> desactivateDoctor(@PathVariable UUID id_doctor) {
        doctorService.desactivarDoctor(id_doctor);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reactivate/{id_doctor}")
    public ResponseEntity<Void> reactivateDoctor(@PathVariable UUID id_doctor) {
        doctorService.reactivarDoctor(id_doctor);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/find_by_cedula/{cedula_doctor}")
    public ResponseEntity<DetallesDoctorProjection> findByCedula(@PathVariable String cedula_doctor) {
        DetallesDoctorProjection doctor = doctorService.findByCedula(cedula_doctor);
        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/find_by_id/{id_Doctor}")
    public ResponseEntity<DetallesDoctorProjection> findById(@PathVariable UUID id_Doctor) {
        DetallesDoctorProjection doctor = doctorService.findAllByIdDoctor(id_Doctor);
        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id_doctor}")
    public ResponseEntity<DetalleDoctorDto> updateDotor(@PathVariable UUID id_doctor, @RequestBody UpdateDoctorDto updateDTO) {
        DetalleDoctorDto doctorActualizado =  doctorService.updateDoctor(id_doctor, updateDTO);
        return ResponseEntity.ok(doctorActualizado);
    }
}
