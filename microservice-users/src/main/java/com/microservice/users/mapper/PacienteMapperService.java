package com.microservice.users.mapper;

import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.model.Paciente;
import org.mapstruct.*;

@Mapper(componentModel = "spring")

public interface PacienteMapperService {

    @Mapping(target = "idAuthentication", ignore = true)
    @Mapping(target = "idPaciente",  ignore = true)
    @Mapping(target = "nombre", source = "registroDTO.userRegisterDto.firstName")
    @Mapping(target = "apellido", source = "registroDTO.userRegisterDto.lastName")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "email", source = "registroDTO.userRegisterDto.email")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaDesactivacion", ignore = true)

    Paciente createDTOtoEntity(CreatePacienteDto registroDTO);



    DetallePacienteDto entityToDTO(Paciente paciente);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nombre", source = "dto.userUpdate.firstName")
    @Mapping(target = "apellido", source = "dto.userUpdate.lastName")
    @Mapping(target = "email", source = "dto.userUpdate.email")
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "fechaDesactivacion", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "idAuthentication",  ignore = true)
    @Mapping(target = "idPaciente",  ignore = true)
    void updatePaciente(UpdatePacienteDto dto, @MappingTarget Paciente paciente);
}
