create table doctores(
    id_doctor UUID not null primary key,
    id_authentication UUID not null unique,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    fecha_nacimiento date not null,
    cedula varchar(20) not null unique,
    sexo varchar(20) not null,
    telefono varchar(20) not null,
    email varchar(120) not null,


    hora_ingreso time,
    hora_salida time,



    constraint valid_sexo CHECK ( sexo IN ('M','F'))

)