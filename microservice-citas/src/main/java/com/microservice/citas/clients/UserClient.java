package com.microservice.citas.clients;

import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "mcsv-users", path = "/api")
public interface UserClient {

    @GetMapping("/doctor/find_by_id/{id_Doctor}")
    DetalleDoctorDto findDoctorById(@PathVariable UUID id_Doctor);

    @GetMapping("/pacientes/find_by_id/{id_paciente}")
    DetallePacienteDto findByIdPaciente(@PathVariable UUID id_paciente);

    @GetMapping("/pacientes/find_by_cedula/{cedula}")
    DetallePacienteDto findByCedulaPaciente(@PathVariable String cedula);
}
