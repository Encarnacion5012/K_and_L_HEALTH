package com.microservice.citas.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor



@Entity
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id_cita")
    private UUID idCita;

    @Column(name = "id_paciente")
    private UUID idPaciente;

    @Column(name = "id_doctor")
    private UUID idDoctor;

    @Column(name = "fecha_hoara_cita")
    private LocalDateTime fechaHoaraCita;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private Estado_Cita estado;

    private boolean reagendada;

    public void cancelarCita() {
        this.estado = Estado_Cita.CANCELADA;
    }

}
