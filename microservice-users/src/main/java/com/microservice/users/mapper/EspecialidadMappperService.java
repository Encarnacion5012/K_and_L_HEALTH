package com.microservice.users.mapper;

import com.microservice.users.dto.especialidad.CreateEspecialidadDto;
import com.microservice.users.dto.especialidad.DetalleEspecialidadDto;
import com.microservice.users.model.Especialidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EspecialidadMappperService {

    @Mapping(target = "doctors" ,  ignore = true)
    @Mapping(target = "idEspecialidad",  ignore = true)

    Especialidad createDtoTOEntity(CreateEspecialidadDto dto);

    DetalleEspecialidadDto entityToDto(Especialidad especialidad);
}
