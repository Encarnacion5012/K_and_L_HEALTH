package com.microservice.citas.service;

import com.microservice.citas.adapters.UserClientAdapter;
import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.dto.ReagendarCitaDto;
import com.microservice.citas.exception.custom.CancelarCitaException;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import com.microservice.citas.mapper.CitaMapperService;
import com.microservice.citas.model.Cita;
import com.microservice.citas.model.Estado_Cita;
import com.microservice.citas.repository.CitaRepository;
import com.microservice.citas.service.validaciones.createCita.ValidarCreacionCita;
import com.microservice.citas.service.validaciones.reajendar.ValidarReagendaCita;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j

public class CitaServiceImp implements ICitaService{
    private final UserClientAdapter userClientAdapter;
    private final CitaRepository citaRepository;
    private final List<ValidarCreacionCita> validacionesAendaCitas;
    private final List<ValidarReagendaCita> validacionesReagendaCitas;
    private final CitaMapperService citaMapperService;

    @Transactional
    @Override
    public DetalleCitaDto create(CreateCitaDto createCitaDto) {

            DetallePacienteDto pacienteDto = getPacienteDetailsHelper(createCitaDto.idPaciente());
            DetalleDoctorDto doctor = getDocotorDetailsHelper(createCitaDto.idDoctor());

            validacionesAendaCitas.forEach(v-> v.validarCreacionCita(createCitaDto, doctor));

            Cita cita = citaMapperService.createDtoToEntity(createCitaDto);

            citaRepository.saveAndFlush(cita);

            return citaMapperService.entitysToDetalleDto(doctor, pacienteDto, cita);
    }

    @Override
    public Page<DetalleCitaDto> findAllAgendadaAndReagendada(Pageable pageable) {
        Page<Cita> citas = citaRepository.findAllAgendadaAndReagendada(pageable);

        return pageCitaToPageDetalleCitaDtoHelper(citas);
    }

    @Override
    @Transactional
    public void cancelarCita(UUID id_Cita) {
            Cita cita = findByidHelper(id_Cita);

            cita.cancelarCita();

            citaRepository.flush();

            if (cita.getEstado() == Estado_Cita.CANCELADA){
                throw new  CancelarCitaException("La cita ya se encuentra calcelada");
            }
    }

    @Override
    @Transactional
    public void reagendarCita(UUID id_Cita, ReagendarCitaDto reagendarCitaDto) {

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
    }

    @Override
    public Page<DetalleCitaDto> findByidPaciente(UUID idPaciente, Pageable pageable) {

        Page<Cita> citas = citaRepository.findByIdPaciente(idPaciente, pageable);
        DetallePacienteDto paciente = getPacienteDetailsHelper(idPaciente);

        Set<UUID> id_Dotores = pageCitasToSetIdDoctoresHelper(citas);

        Map<UUID,DetalleDoctorDto> mapDotors = setOfIdDotoresToMapHelper(id_Dotores);

        return  citas.map(c-> citaMapperService.entitysToDetalleDto(
                mapDotors.get(c.getIdDoctor()),
                paciente,
                c
        ));
    }

    @Override
    public Page<DetalleCitaDto> findByidDoctor(UUID idDoctor, Pageable pageable) {
        Page<Cita> citas =  citaRepository.findByIdDoctor(idDoctor, pageable);
        DetalleDoctorDto doctor =  getDocotorDetailsHelper(idDoctor);

        Set<UUID> idPacientes = citas.map(Cita::getIdPaciente).toSet();

        Map<UUID, DetallePacienteDto> mapPacientes = setOfIdPacientesToMapHelper(idPacientes);

        return citas.map(c-> citaMapperService.entitysToDetalleDto(
                doctor,
                mapPacientes.get(c.getIdPaciente()),
                c
        ));
    }

    @Override
    public DetalleCitaDto findByIdCitaGenaral(UUID idCita) {
        Cita cita = findByidHelper(idCita);
        DetallePacienteDto paciente = getPacienteDetailsHelper(cita.getIdPaciente());
        DetalleDoctorDto  doctor = getDocotorDetailsHelper(cita.getIdDoctor());

        return citaMapperService.entitysToDetalleDto(doctor,paciente,cita);
    }

    @Override
    public Page<DetalleCitaDto> findByCedulaPaciente(String cedulaPaciente, Pageable pageable) {
        DetallePacienteDto paciente = userClientAdapter.findByCedulaPaciente(cedulaPaciente);
        Page<Cita> citas = citaRepository.findByIdPaciente(paciente.idPaciente(), pageable);

        Set<UUID> idDoctors = pageCitasToSetIdDoctoresHelper(citas);

        Map<UUID, DetalleDoctorDto> mapDoctores = setOfIdDotoresToMapHelper(idDoctors);


        return citas.map( c-> citaMapperService.entitysToDetalleDto(
                mapDoctores.get(c.getIdDoctor()),
                paciente,
                c
        ));
    }

    //====================================================================================
    //Helpers Internos
    //====================================================================================
    private Cita findByidHelper(UUID id_Cita){
        return  citaRepository.findById(id_Cita)
                .orElseThrow(()-> new EntityNotFoundException("Cita no encontrada con id: " + id_Cita));
    }

    @CircuitBreaker(name = "userClient", fallbackMethod = "fallbackgetDocotorDetailsHelper")
    private DetalleDoctorDto getDocotorDetailsHelper(UUID idDoctor){
        return userClientAdapter.findDoctorById(idDoctor);
    }

    @CircuitBreaker(name = "userClient", fallbackMethod = "fallbackgetPacienteDetailsHelper")
    private DetallePacienteDto getPacienteDetailsHelper(UUID idPaciente){
        return userClientAdapter.findByIdPaciente(idPaciente);
    }

    private Map<UUID, DetalleDoctorDto> setOfIdDotoresToMapHelper(Set<UUID> idDoctores){
        return idDoctores.stream()
                .collect(Collectors.toMap(id-> id, this::getDocotorDetailsHelper));
    }

    private Map<UUID, DetallePacienteDto> setOfIdPacientesToMapHelper(Set<UUID> idPacientes){
        return idPacientes.stream()
                .collect(Collectors.toMap(id-> id, this::getPacienteDetailsHelper));
    }

    private Set<UUID> pageCitasToSetIdPacientesHelper (Page<Cita> citas){
        return citas.map(Cita::getIdPaciente).toSet();
    }
    private Set<UUID> pageCitasToSetIdDoctoresHelper (Page<Cita> citas){
        return citas.map(Cita::getIdDoctor).toSet();
    }

    private Page<DetalleCitaDto> pageCitaToPageDetalleCitaDtoHelper(Page<Cita> citas) {

        Set<UUID>id_Doctores = pageCitasToSetIdDoctoresHelper(citas);
        Set<UUID> id_Pacientes = pageCitasToSetIdPacientesHelper(citas);

        Map<UUID, DetalleDoctorDto> doctoresMap = setOfIdDotoresToMapHelper(id_Doctores);

        Map<UUID, DetallePacienteDto> pacientesMap = setOfIdPacientesToMapHelper(id_Pacientes);

        return citas.map(c-> citaMapperService.entitysToDetalleDto(
                doctoresMap.get(c.getIdDoctor()),
                pacientesMap.get(c.getIdPaciente()),
                c
        ));
    }
}
