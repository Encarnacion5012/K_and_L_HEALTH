package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDTO;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDoctorService {
    DetalleDoctorDto createDoctor(CreateDcotorDTO createDTO);
    Page<DetallesDoctorProjection> findAll(Pageable pageable);
}
