create table citas(
    id_cita UUID not null primary key,
    id_paciente UUID not null,
    id_doctor UUID not null,
    fecha_hoara_cita TIMESTAMPTZ not null ,
    motivo varchar(130) not null,
    estado varchar(20) not null DEFAULT 'AGENDADA',
    constraint estado_valido check (estado IN ('AGENDADA', 'CANCELADA', 'ATENDIDA', 'REAGENDADA')),
    constraint fecha_hoara_cita_futura check ( fecha_hoara_cita > NOW())

)