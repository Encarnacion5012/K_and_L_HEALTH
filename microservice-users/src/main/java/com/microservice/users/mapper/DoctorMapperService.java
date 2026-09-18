package com.microservice.users.mapper;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
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
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaDesactivacion", constant = "null")
    Doctor registerDtoToEntity(CreateDcotorDto dto);


    DetalleDoctorDto entityToDto(Doctor doctor);
}
