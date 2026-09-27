package com.microservice.users.mapper;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.model.Doctor;
import org.mapstruct.*;

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
    @Mapping(target = "fechaDesactivacion",  ignore = true)
    Doctor registerDtoToEntity(CreateDcotorDto dto);


    DetalleDoctorDto entityToDto(Doctor doctor);



    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idDoctor", ignore = true)
    @Mapping(target = "idAuthentication",  ignore = true)
    @Mapping(target = "nombre", source = "dto.userUpdate.firstName")
    @Mapping(target = "apellido" , source = "dto.userUpdate.lastName")
    @Mapping(target = "email", source = "dto.userUpdate.email")
    @Mapping(target = "especialidades", ignore = true)
    @Mapping(target = "horarioAtencion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaDesactivacion",  ignore = true)
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "exequatur", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "sexo", ignore = true)
    void update(UpdateDoctorDto dto,  @MappingTarget Doctor doctor);
}
