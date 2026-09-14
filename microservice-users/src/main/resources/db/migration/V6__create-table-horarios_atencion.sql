create table horarios_atencion(
    id_horario int not null primary key,

    id_doctor UUID not null,

    dia_de_semana SMALLINT not null,

    hora_inicio time not null ,
    hora_fin time not null ,

    duracion_cita_minutos int not null,

    constraint fk_horarios_atencionid_doctor foreign key (id_doctor) references doctores(id_doctor)


);

create unique index if not exists horarios_atencion_id_doctor on horarios_atencion (id_doctor);