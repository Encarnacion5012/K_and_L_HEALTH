package com.microservice.citas.service;

import com.microservice.citas.clients.UserClient;
import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.dto.ReagendarCitaDto;
import com.microservice.citas.exception.custom.AgendarException;
import com.microservice.citas.exception.custom.CancelarCitaException;
import com.microservice.citas.exception.custom.ReagendarException;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import com.microservice.citas.mapper.CitaMapperService;
import com.microservice.citas.model.Cita;
import com.microservice.citas.model.Estado_Cita;
import com.microservice.citas.repository.CitaRepository;
import com.microservice.citas.service.validaciones.createCita.ValidarCreacionCita;
import com.microservice.citas.service.validaciones.reajendar.ValidarReagendaCita;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImp implements ICitaService{
    private final UserClient userClient;
    private final CitaRepository citaRepository;
    private final List<ValidarCreacionCita> validacionesAendaCitas;
    private final List<ValidarReagendaCita> validacionesReagendaCitas;
    private final CitaMapperService citaMapperService;

    @Transactional
    @Override
    public DetalleCitaDto create(CreateCitaDto createCitaDto) {

        try {

            DetallePacienteDto pacienteDto = getPacienteDetailsHelper(createCitaDto.idPaciente());
            DetalleDoctorDto doctor = getDocotorDetailsHelper(createCitaDto.idDoctor());

            validacionesAendaCitas.forEach(v-> v.validarCreacionCita(createCitaDto, doctor));

            Cita cita = citaMapperService.createDtoToEntity(createCitaDto);

            citaRepository.saveAndFlush(cita);

            return citaMapperService.entitysToDetalleDto(doctor, pacienteDto, cita);


        }catch (Exception e){
            throw new AgendarException(e.getMessage(), e);
        }


    }

    @Override
    public Page<DetalleCitaDto> findAllAgendadaAndReagendada(Pageable pageable) {
        Page<Cita> citas = citaRepository.findAllAgendadaAndReagendada(pageable);

        return pageCitaToPageDetalleCitaDtoHelper(citas);
    }

    @Override
    public void cancelarCita(UUID id_Cita) {
        try{
            Cita cita = findByidHelper(id_Cita);

            cita.cancelarCita();

            citaRepository.flush();

        }catch (Exception e){
            throw new CancelarCitaException("Ha ocurrido un error al desactivar la cita con id: " + id_Cita,e);
        }
    }

    @Override
    public void reagendarCita(UUID id_Cita, ReagendarCitaDto reagendarCitaDto) {
        try {
            Cita cita = citaRepository.findByIdAndEstadoAgendadaAndReagendada(id_Cita)
                    .orElseThrow(() -> new EntityNotFoundException("Cita no encontrada con el id: " + id_Cita));

            cita.setEstado(Estado_Cita.REAGENDADA);

            validacionesReagendaCitas.forEach(vr-> vr.validarReagendaCita(
                    id_Cita,
                    cita,
                    reagendarCitaDto.nuevaFechaHora()
            ));

            cita.setFechaHoraCita(reagendarCitaDto.nuevaFechaHora());

            citaRepository.flush();

        }catch (Exception e){
            throw new ReagendarException("Ha ocurrido un error al Reagendar la cita con id: " +  id_Cita,e);
        }
    }

    private Cita findByidHelper(UUID id_Cita){
        return  citaRepository.findById(id_Cita)
                .orElseThrow(()-> new EntityNotFoundException("Cita no encontrada con id: " + id_Cita));
    }

    private DetalleDoctorDto getDocotorDetailsHelper(UUID idDoctor){
        return userClient.findDoctorById(idDoctor);
    }

    private DetallePacienteDto getPacienteDetailsHelper(UUID idPaciente){
        return userClient.findByIdPaciente(idPaciente);
    }

    private Page<DetalleCitaDto> pageCitaToPageDetalleCitaDtoHelper(Page<Cita> citas) {

      Set<UUID>id_Docotores = citas.stream()
              .map(Cita::getIdDoctor)
              .collect(Collectors.toSet());
      Set<UUID> id_Pacientes = citas.stream()
              .map(Cita::getIdPaciente)
              .collect(Collectors.toSet());

      Map<UUID, DetalleDoctorDto> doctoresMap = id_Docotores.stream()
              .collect(Collectors.toMap(id-> id, this::getDocotorDetailsHelper));

      Map<UUID, DetallePacienteDto> pacientesMap = id_Pacientes.stream()
              .collect(Collectors.toMap(id-> id, this::getPacienteDetailsHelper));

        return citas.map(c-> citaMapperService.entitysToDetalleDto(
                doctoresMap.get(c.getIdDoctor()),
                pacientesMap.get(c.getIdPaciente()),
                c
        ));
    }

    @Override
    public Page<DetalleCitaDto> findByIdDoctorOrIdPaciente(UUID id,  Pageable pageable) {

        Page<Cita> citas = citaRepository.findBYIdDoctorOrIdPaciente(id, pageable);

        return pageCitaToPageDetalleCitaDtoHelper(citas);
    }

    @Override
    public DetalleCitaDto findByIdCitaGenaral(UUID idCita) {
        Cita cita;
        DetallePacienteDto paciente;
        DetalleDoctorDto doctor;
        try{
            cita = findByidHelper(idCita);
            paciente = getPacienteDetailsHelper(cita.getIdPaciente());
            doctor = getDocotorDetailsHelper(cita.getIdDoctor());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return citaMapperService.entitysToDetalleDto(doctor,paciente,cita);
    }
}
