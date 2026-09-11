package com.microservice.users.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Integer idEspecialidad;

    @Column(name = "nombre_especialidad")
    private String nombreEspecialidad;

    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "especialidades")


    Set<Doctor> doctors = new HashSet<>();
}

