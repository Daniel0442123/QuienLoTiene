-- HU-025 Reservar para recogida y HU-029 Cancelar orden (contrato del Sprint 3, decisiones 1 y 2).
-- reserva_vence se llena cuando la tienda marca LISTO_PARA_RECOGER; la tarea de CU-30 vence las que pasaron.
alter table ordenes.orden
    add column reserva_vence      timestamptz,
    add column motivo_cancelacion varchar(200),
    add column cancelada_por      varchar(10),
    add constraint ck_orden_cancelada_por check (cancelada_por is null or cancelada_por in ('CLIENTE', 'TIENDA')),
    add constraint ck_orden_cancelacion check (estado <> 'CANCELADO' or cancelada_por is not null);

-- CU-30: la tarea de cada minuto busca solo las recogidas listas con reserva
create index ix_orden_reserva_vence on ordenes.orden (reserva_vence) where estado = 'LISTO_PARA_RECOGER';
