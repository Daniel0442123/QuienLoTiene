-- HU-017 Difundir a tiendas elegibles: a qué tiendas llegó cada solicitud y a qué distancia estaban.
-- Lo usan la bandeja de la tienda (HU-018) y la validación de respuestas (HU-019).

create table solicitudes.envio_solicitud (
    id               uuid        not null,
    solicitud_id     uuid        not null,
    -- Tienda del módulo tiendas; sin llave foránea porque es otro esquema (monolito modular)
    tienda_id        uuid        not null,
    distancia_metros integer     not null,
    creado_en        timestamptz not null,
    actualizado_en   timestamptz not null,
    constraint pk_envio_solicitud primary key (id),
    constraint fk_envio_solicitud_solicitud foreign key (solicitud_id) references solicitudes.solicitud (id),
    constraint uq_envio_solicitud_tienda unique (solicitud_id, tienda_id)
);

-- Bandeja de la tienda: sus solicitudes más recientes
create index ix_envio_solicitud_tienda on solicitudes.envio_solicitud (tienda_id, creado_en);
