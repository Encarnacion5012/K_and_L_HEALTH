package com.microservice.citas.service;

import com.microservice.citas.clients.DoctorClient;
import com.microservice.citas.clients.PacienteClient;
import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.doctor.DetalleHorarioAtencionDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CitaServiceImp implements ICitaService{
    private final  DoctorClient doctorClient;
    private final PacienteClient pacienteClient;


    @Transactional
    @Override
    public DetalleCitaDto create(CreateCitaDto createCitaDto) {

        try {
            DetalleDoctorDto doctor = doctorClient.findById(createCitaDto.idDoctor());
            DetallePacienteDto pacienteDto = pacienteClient.findByIdPaciente(createCitaDto.idPaciente());

            DayOfWeek diaCIta = createCitaDto.fechaHoaraCita().getDayOfWeek();
            LocalTime horaCIta = createCitaDto.fechaHoaraCita().toLocalTime();
            DetalleHorarioAtencionDto horarioDia = doctor.horarioAtencion().stream()
                    .filter(h-> h.diaDeSemana().equals(diaCIta))
                    .findFirst()
                    .orElseThrow(()-> new IllegalArgumentException("EL doctor no trabaja los dias: " + diaCIta));

            boolean horaValida = !horaCIta.isBefore(horarioDia.horaInicio()) &&  !horaCIta.isAfter(horarioDia.horaFin());

            if(!horaValida) {
                throw new RuntimeException("La hora a la cual esta agendando la cita no es valida, por favor ingrese una hora valida");
            }



        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        return null;
    }
}
