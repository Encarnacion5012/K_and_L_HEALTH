create unique index if not exists paciente_cedula on pacientes (cedula);
create unique index if not exists doctores_cedula on doctores (cedula);

create unique index if not exists paciente_id_paciente on pacientes (id_paciente);
create unique index if not exists doctores_id_doctor on doctores (id_doctor);

create unique index if not exists paciente_id_authentication on pacientes (id_authentication);
create unique index if not exists doctores_id_authentication on doctores (id_authentication);

create unique index if not exists paciente_email on pacientes (email);
create unique index if not exists doctores_email on doctores (email);

