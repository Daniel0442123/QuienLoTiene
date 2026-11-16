-- HU-024 Crear orden, HU-027 estados y HU-028 recepción. Columnas según el diagrama 09 (ordenes.orden y
-- ordenes.historial_estado) con las convenciones de DECISIONES-BACKEND 3.2 y 10: dinero en pesos enteros. La dirección
-- de entrega va en columnas (no jsonb) y se copian el contacto del cliente y los datos de la tienda al crearla, para no
-- consultar otros módulos en cada pantalla. La reserva (reserva_vence, HU-025) llega en el Sprint 3.

create table ordenes.orden (
    id                        uuid         not null,
    -- Referencias lógicas a otros esquemas (solicitudes, identidad, tiendas); sin llave foránea (monolito modular)
    solicitud_id              uuid         not null,
    respuesta_id              uuid         not null,
    cliente_id                uuid         not null,
    tienda_id                 uuid         not null,
    dueno_tienda_id           uuid         not null,
    producto                  varchar(300) not null,
    cantidad                  integer      not null,
    precio_unitario_pesos     bigint       not null,
    costo_domicilio_pesos     bigint       not null,
    modalidad                 varchar(10)  not null,
    medio_pago                varchar(15)  not null,
    estado                    varchar(20)  not null,
    cliente_nombre            varchar(120) not null,
    cliente_telefono          varchar(20)  not null,
    tienda_nombre             varchar(120) not null,
    tienda_telefono           varchar(20)  not null,
    direccion_nombre          varchar(40),
    direccion_escrita         varchar(200),
    direccion_indicaciones    varchar(200),
    direccion_latitud         double precision,
    direccion_longitud        double precision,
    -- Bloqueo optimista: la tienda y el cliente cambian el estado en paralelo (sección 10)
    version                   bigint       not null default 0,
    creado_en                 timestamptz  not null,
    actualizado_en            timestamptz  not null,
    constraint pk_orden primary key (id),
    -- Una sola orden por respuesta y por solicitud (una sola elección)
    constraint uq_orden_respuesta unique (respuesta_id),
    constraint uq_orden_solicitud unique (solicitud_id),
    constraint ck_orden_cantidad check (cantidad between 1 and 999),
    constraint ck_orden_dinero check (precio_unitario_pesos > 0 and costo_domicilio_pesos >= 0),
    constraint ck_orden_modalidad check (modalidad in ('RECOGER', 'DOMICILIO')),
    constraint ck_orden_medio_pago check (medio_pago in ('EFECTIVO', 'TRANSFERENCIA', 'DATAFONO')),
    constraint ck_orden_estado check (estado in ('CONFIRMADO', 'PREPARANDO', 'EN_CAMINO', 'LISTO_PARA_RECOGER',
                                                 'ENTREGADO', 'RESUELTA', 'CANCELADO', 'VENCIDA')),
    constraint ck_orden_direccion check (modalidad = 'RECOGER' or direccion_escrita is not null)
);

-- Mis órdenes del cliente y de la tienda (HU-027)
create index ix_orden_cliente_creado on ordenes.orden (cliente_id, creado_en);
create index ix_orden_dueno_tienda_creado on ordenes.orden (dueno_tienda_id, creado_en);

create table ordenes.historial_estado (
    id             uuid        not null,
    orden_id       uuid        not null,
    estado         varchar(20) not null,
    cambiado_en    timestamptz not null,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_historial_estado primary key (id),
    constraint fk_historial_estado_orden foreign key (orden_id) references ordenes.orden (id)
);

create index ix_historial_estado_orden on ordenes.historial_estado (orden_id, cambiado_en);
