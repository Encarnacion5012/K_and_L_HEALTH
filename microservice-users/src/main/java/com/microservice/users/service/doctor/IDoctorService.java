package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IDoctorService {
    DetalleDoctorDto createDoctor(CreateDcotorDto createDTO);
    Page<DetallesDoctorProjection> findAllACtivo(Pageable pageable);
    void desactivarDoctor(UUID idDoctor);
    void reactivarDoctor(UUID idDoctor);
    DetallesDoctorProjection findAllByIdDoctor(UUID idDoctor);
    Page<DetallesDoctorProjection> findALlByEspecialidad (String nombreEspecialidad, Pageable pageable);
    DetallesDoctorProjection findByExequatur(String exequatur);
    DetallesDoctorProjection findBYIdAuthentication(UUID idAuthentication);
    DetallesDoctorProjection findByCedula(String cedula);
    DetalleDoctorDto updateDoctor(UUID idDoctor, UpdateDoctorDto updateDTO);

}
