CREATE TABLE pacientes(
    id_paciente UUID primary key not null,
    id_keycloak UUID unique not null ,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    cedula varchar(20) not null unique,
    sexo varchar(20) not null,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    fecha_nacimiento date not null,
    telefono varchar(20),
    direccion varchar(100),

    constraint sexo_valido CHECK (sexo IN ('M', 'F'))


)