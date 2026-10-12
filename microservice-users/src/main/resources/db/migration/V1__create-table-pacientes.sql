CREATE TABLE pacientes(
    id_paciente UUID primary key not null,
    id_authentication UUID unique not null ,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    cedula varchar(20) not null unique,
    sexo varchar(20) not null,
    email varchar(120) not null,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    fecha_nacimiento date not null,
    telefono varchar(20),
    direccion varchar(100),
    activo boolean not null default true,
    fecha_desactivacion TIMESTAMPTZ DEFAULT NULL,

    constraint fecha_hora_desactivacion_valida check ( fecha_desactivacion <= NOW() ),

    constraint sexo_valido CHECK (sexo IN ('M', 'F'))


)