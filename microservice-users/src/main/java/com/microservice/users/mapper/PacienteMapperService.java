package com.microservice.users.mapper;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PacienteMapperService {

    @Mapping(target = "idAuthentication", ignore = true)
    @Mapping(target = "idPaciente",  ignore = true)
    @Mapping(target = "nombre", source = "registroDTO.userRegisterDto.firstName")
    @Mapping(target = "apellido", source = "registroDTO.userRegisterDto.lastName")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")

    Paciente createDTOtoEntity(RegistroPacienteDTO registroDTO);



    DetallePacienteDTO entityToDTO(Paciente paciente);
}
