package com.microservice.citas.adapters;

import com.microservice.citas.clients.UserClient;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClientAdapter {
    private final UserClient userClient;

    @CircuitBreaker(name = "userClient", fallbackMethod = "fallbackfindDoctorById")
    public DetalleDoctorDto findDoctorById(UUID idDoctor){
        try {
            return userClient.findDoctorById(idDoctor);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("No se encontró el doctor con ID: " + idDoctor);
        }
    }

    @CircuitBreaker(name = "userClient", fallbackMethod = "fallbackfindByIdPaciente")
    public DetallePacienteDto findByIdPaciente( UUID idPaciente){
        try {
            return userClient.findByIdPaciente(idPaciente);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("No se encontró el paciente con ID: " + idPaciente);
        }
    }

    @CircuitBreaker(name = "userClient", fallbackMethod = "fallbackfindByCedulaPaciente")
    public DetallePacienteDto findByCedulaPaciente( String cedula){
        try {
            return userClient.findByCedulaPaciente(cedula);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("No se encontró el paciente con ID: " + cedula);
        }
    }



    //=============================================================================
    //FallBack
    //=============================================================================

    public DetallePacienteDto fallbackfindByCedulaPaciente(String cedula, Throwable ex){
        log.error("Circuit Breaker activado para pacientes {}: {}", cedula, ex.getMessage());
        throw new IllegalStateException("El servicio de usuarios/pacientes no está disponible temporalmente", ex);
    }

    public DetalleDoctorDto fallbackfindDoctorById(UUID idDoctor, Throwable ex){
        log.error("Circuit Breaker activado para doctor {}: {}", idDoctor, ex.getMessage());
        throw new IllegalStateException("El servicio de usuarios/doctores no está disponible temporalmente", ex);
    }

    public  DetallePacienteDto fallbackfindByIdPaciente(UUID idPaciente, Throwable ex){
        log.error("Circuit Breaker activado para paciente {}: {}", idPaciente, ex.getMessage());
        throw new IllegalStateException("El servicio de usuarios/pacientes no está disponible temporalmente", ex);
    }




}
