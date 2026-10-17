alter table citas drop constraint estado_valido;
alter table citas add constraint estado_valido check (estado IN ('AGENDADA', 'CANCELADA', 'COMPLETADA', 'REAGENDADA', 'NOASISTIDA'));