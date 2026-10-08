package com.microservice.citas.mapper;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.doctor.DetalleEspecialidadDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import com.microservice.citas.model.Cita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CitaMapperService {


    @Mapping(target = "idCita", ignore = true)
    @Mapping(target = "estado", expression = "java(Estado_Cita.AGENDADA)")
    @Mapping(target = "reagendada", constant = "false")
    Cita createDtoToEntity(CreateCitaDto createDto);

    @Mapping(target = "nombre_Paciente", source = "pacienteDto.nombre")
    @Mapping(target = "apellido_Paciente", source = "pacienteDto.apellido" )
    @Mapping(target = "cedula_Paciente", source = "pacienteDto.cedula")
    @Mapping(target = "nombre_Doctor", source = "doctorDto.nombre")
    @Mapping(target = "apellido_Doctor", source = "doctorDto.apellido")
    @Mapping(target = "especialidades_Doctor", source = "doctorDto.especialidades")
    @Mapping(target = "idPaciente", source = "cita.idPaciente")
    @Mapping(target = "idDoctor", source = "cita.idDoctor")
    DetalleCitaDto entitysToDetalleDto(DetalleDoctorDto doctorDto, DetallePacienteDto pacienteDto, Cita cita);

    default String estapecialidadToName(DetalleEspecialidadDto especialidadDto){

       if (especialidadDto != null){
            return especialidadDto.nombreEspecialidad();
        }
       return null;
    }
}
