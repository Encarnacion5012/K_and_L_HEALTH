package com.microservice.users.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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

    private String cedula;

    private String  telefono;

    private String exequatur;

    private  String email;

    private Boolean activo;

    @Column(name = "fecha_desactivacion")
    private Instant fechaDesactivacion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "doctor")
    private List<HorarioAtencion> horarioAtencion;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name = "doctores_especialidades",
            joinColumns = @JoinColumn(name = "id_doctor"), inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    private Set<Especialidad> especialidades = new HashSet<>();


    public void helperRelacionEspecialidad(Set<Especialidad> especialidadesDoctor) {
        this.especialidades.addAll(especialidadesDoctor);
        especialidadesDoctor.forEach(especialidad -> {

            if(especialidad.getDoctors() == null){
                especialidad.setDoctors(new HashSet<>());
            }
            especialidad.doctors.add(this);
        });
    }

    public void helperRelacionHoracioATencios(List<HorarioAtencion> horarioAtencionDoctor) {
        if (this.horarioAtencion == null) {
            this.horarioAtencion = new ArrayList<>();
        }
        this.horarioAtencion.addAll(horarioAtencionDoctor);
        horarioAtencionDoctor.forEach(horarioAtencion -> {
            horarioAtencion.setDoctor(this);
        });
    }

    public void desactivarDoctor(){
        if (!this.activo){
            throw new IllegalStateException("Este doctor ya esta desactivado");
        }
        this.activo = false;
        this.fechaDesactivacion = Instant.now();
    }

    public void reactivarDoctor(){
        this.activo = true;
        this.fechaDesactivacion = null;
    }
}
