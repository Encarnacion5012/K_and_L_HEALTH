package com.microservice.users.mapper;

import com.microservice.users.dto.doctor.CreateDcotorDTO;
import com.microservice.users.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapperService {

    @Mapping(target = "idDoctor", ignore = true)
    @Mapping(target = "idAuthentication",  ignore = true)
    @Mapping(target = "nombre", source = "dto.userRegister.firstName")
    @Mapping(target = "apellido" , source = "dto.userRegister.lastName")
    @Mapping(target = "email", source = "dto.userRegister.email")
    @Mapping(target = "especialidades", ignore = true)
    @Mapping(target = "horarioAtencion", ignore = true)
    Doctor registerDtoToEntity(CreateDcotorDTO dto);
}
