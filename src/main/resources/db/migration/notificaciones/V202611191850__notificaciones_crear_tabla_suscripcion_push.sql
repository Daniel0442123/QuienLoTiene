-- HU-031 Notificaciones push web (diagrama 28): una fila por navegador suscrito.
create table notificaciones.suscripcion_push (
    id          uuid          not null,
    -- Referencia lógica a identidad; sin llave foránea (monolito modular)
    usuario_id  uuid          not null,
    endpoint    varchar(1000) not null,
    p256dh      varchar(100)  not null,
    auth        varchar(50)   not null,
    creado_en   timestamptz   not null,
    constraint pk_suscripcion_push primary key (id),
    -- Un navegador tiene un solo endpoint; si cambia de cuenta, la fila se reemplaza
    constraint uq_suscripcion_push_endpoint unique (endpoint)
);

create index ix_suscripcion_push_usuario on notificaciones.suscripcion_push (usuario_id);
