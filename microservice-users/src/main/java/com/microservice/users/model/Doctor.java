package com.microservice.users.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "doctores")

public class Doctor {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id_doctor")
    private UUID idDoctor;

    @Column(name = "id_authentication")
    private UUID idAuthentication;

    private String nombre;

    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String sexo;

    private String  telefono;

    @Column(name = "hora_ingreso")
    private LocalTime horaIngreso;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    private  String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Especialidad especialidad;
}
