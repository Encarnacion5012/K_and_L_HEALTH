package com.microservice.citas.clients;


import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "mcsv-users", path = "/api/pacientes")
public interface PacienteClient {

    @GetMapping("/find_by_id/{id_paciente}")
    DetallePacienteDto findByIdPaciente(@PathVariable UUID id_paciente);
}
