create table citas(
    id_cita UUID not null primary key,
    id_paciente UUID not null,
    id_doctor UUID not null,
    fecha_hora_cita TIMESTAMPTZ not null ,
    motivo varchar(130) not null,
    estado varchar(20) not null DEFAULT 'AGENDADA',
    reagendada boolean not null default false,
    constraint estado_valido check (estado IN ('AGENDADA', 'CANCELADA', 'COMPLETADA', 'REAGENDADA')),
    constraint fecha_hora_cita_futura check ( fecha_hora_cita > NOW())

)