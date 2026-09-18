package com.microservice.users.service.especialidad;


import com.microservice.users.dto.especialidad.CreateEspecialidadDto;
import com.microservice.users.dto.especialidad.DetalleEspecialidadDto;
import com.microservice.users.mapper.EspecialidadMappperService;
import com.microservice.users.model.Especialidad;
import com.microservice.users.projections.doctor.DetallesEspecialidadProjection;
import com.microservice.users.repository.EspecialidadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements IEspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final EspecialidadMappperService especialidadMappperService;


    @Transactional
    @Override
    public DetalleEspecialidadDto createEspcialidad(CreateEspecialidadDto createEspecialidadDto) {

        Especialidad especialidad = especialidadMappperService.createDtoTOEntity(createEspecialidadDto);

        especialidadRepository.save(especialidad);
        return especialidadMappperService.entityToDto(especialidad);


    }

    @Override
    public List<DetallesEspecialidadProjection> findAll() {
        return especialidadRepository.findAllProjection();
    }
}
