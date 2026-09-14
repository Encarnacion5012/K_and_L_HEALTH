package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDTO;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;

public interface IDoctorService {
    DetalleDoctorDto createDoctor(CreateDcotorDTO createDTO);
}
