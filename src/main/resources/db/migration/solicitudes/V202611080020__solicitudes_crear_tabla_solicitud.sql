-- HU-016 Publicar solicitud. Columnas según el diagrama 09-entidad-relacion, con estos ajustes a las convenciones
-- (DECISIONES-BACKEND 3.2 y 10): radio en metros enteros (radio_metros) y creado_en como fecha de publicación.
-- La foto y la marca de restringida llegan con sus historias (HU-016 foto, HU-023).

create table solicitudes.solicitud (
    id             uuid                   not null,
    -- Usuario del módulo identidad; sin llave foránea porque es otro esquema (monolito modular)
    cliente_id     uuid                   not null,
    -- Categoría del módulo catalogo; se valida por su api cuando exista
    categoria_id   uuid                   not null,
    producto       varchar(150)           not null,
    cantidad       integer                not null,
    modalidad      varchar(10)            not null,
    urgente        boolean                not null default false,
    ubicacion      geography(Point, 4326) not null,
    radio_metros   integer                not null,
    fue_ampliado   boolean                not null default false,
    restringida    boolean                not null default false,
    estado         varchar(10)            not null,
    vence_en       timestamptz            not null,
    -- Bloqueo optimista: la tienda responde y el reloj la vence en paralelo (sección 10)
    version        bigint                 not null default 0,
    creado_en      timestamptz            not null,
    actualizado_en timestamptz            not null,
    constraint pk_solicitud primary key (id),
    constraint ck_solicitud_cantidad check (cantidad between 1 and 999),
    constraint ck_solicitud_modalidad check (modalidad in ('RECOGER', 'DOMICILIO')),
    constraint ck_solicitud_estado check (estado in ('ABIERTA', 'RESUELTA', 'VENCIDA'))
);

-- Difusión por radio con ST_DWithin (HU-017)
create index ix_solicitud_ubicacion on solicitudes.solicitud using gist (ubicacion);
-- Tarea que vence las solicitudes abiertas (CU-29)
create index ix_solicitud_estado_vence on solicitudes.solicitud (estado, vence_en);
-- Límite de 10 por hora por cliente (HU-016)
create index ix_solicitud_cliente_creado on solicitudes.solicitud (cliente_id, creado_en);
