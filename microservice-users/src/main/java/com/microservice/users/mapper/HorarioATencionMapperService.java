package com.microservice.users.mapper;

import com.microservice.users.dto.doctor.horaiosAtencion.CreateHorarioAtencion;
import com.microservice.users.model.HorarioAtencion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HorarioATencionMapperService {


    @Mapping(target = "idHorario", ignore = true)
    @Mapping(target = "doctor", ignore = true)

    HorarioAtencion registerDtoToEntity(CreateHorarioAtencion dto);
}
