create table doctores_especialidades(
    id_doctor UUID not null,
    id_especialidad int not null,

    primary key (id_doctor, id_especialidad),

    constraint doctores_especialidad_id_doctor foreign key (id_doctor) references doctores(id_doctor),
    constraint doctores_especialidad_id_especialidad foreign key (id_especialidad) references especialidades(id_especialidad)


);