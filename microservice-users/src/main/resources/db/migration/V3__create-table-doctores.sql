create table doctores(
    id_doctor UUID not null primary key,
    id_authentication UUID not null unique,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    fecha_nacimiento date not null,
    sexo varchar(20) not null,
    telefono varchar(20) not null,

    id_especialidad int not null,

    hora_ingreso time,
    hora_salida time,

    constraint fk_escialidad_doctores foreign key (id_especialidad) references especialidades (id_especialidad),

    constraint valid_sexo CHECK ( sexo IN ('M','F'))

)