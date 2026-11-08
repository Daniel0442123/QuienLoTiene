-- HU-036 Aprobar o rechazar tiendas: motivo del rechazo (lo ve el dueño) y fecha de la revisión.

alter table tiendas.tienda add column motivo_rechazo varchar(300);
alter table tiendas.tienda add column fecha_revision timestamptz;
