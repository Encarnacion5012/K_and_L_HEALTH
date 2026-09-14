package com.microservice.users.mapper;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.CreatePacienteDTO;
import com.microservice.users.dto.paciente.UpdatePacienteDTO;
import com.microservice.users.model.Paciente;
import org.mapstruct.*;

@Mapper(componentModel = "spring")

public interface PacienteMapperService {

    @Mapping(target = "idAuthentication", ignore = true)
    @Mapping(target = "idPaciente",  ignore = true)
    @Mapping(target = "nombre", source = "registroDTO.userRegisterDto.firstName")
    @Mapping(target = "apellido", source = "registroDTO.userRegisterDto.lastName")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "email", source = "registroDTO.userRegisterDto.email")

    Paciente createDTOtoEntity(CreatePacienteDTO registroDTO);



    DetallePacienteDTO entityToDTO(Paciente paciente);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nombre", source = "dto.userUpdate.firstName")
    @Mapping(target = "apellido", source = "dto.userUpdate.lastName")
    @Mapping(target = "email", source = "dto.userUpdate.email")
    void updatePaciente(UpdatePacienteDTO dto, @MappingTarget Paciente paciente);
}
