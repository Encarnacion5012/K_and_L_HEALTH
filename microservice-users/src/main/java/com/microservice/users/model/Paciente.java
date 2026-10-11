package com.microservice.users.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id_paciente")
    private UUID idPaciente;

    @Column(name = "id_authentication")
    private UUID idAuthentication;

    private String nombre;

    private String apellido;

    private String cedula;

    private String direccion;

    private String sexo;

    private String email;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String telefono;

    private boolean activo;

    @Column(name = "fecha_desactivacion")
    private Instant fechaDesactivacion;


    public void desactivarPaciente(){
        if (!this.activo){
            throw new IllegalStateException("Paciente ya esta desactivado");
        }
        this.activo = false;
        this.fechaDesactivacion = Instant.now();
    }

    public void reactivarPaciente(){
        this.activo = true;
        this.fechaDesactivacion = null;
    }

}
