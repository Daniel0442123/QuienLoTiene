-- HU-019 Responder solicitud y HU-024 elegir respuesta. Columnas según el diagrama 09 (solicitudes.respuesta), con
-- las convenciones de DECISIONES-BACKEND 3.2 y 10: dinero en pesos enteros y minutos con su unidad en el nombre.
-- "elegida" se guarda como el id de la orden que nació de la respuesta (orden_id no nulo = elegida).

create table solicitudes.respuesta (
    id                       uuid         not null,
    solicitud_id             uuid         not null,
    -- Tienda y su dueño del módulo tiendas; sin llave foránea porque es otro esquema (monolito modular)
    tienda_id                uuid         not null,
    dueno_id                 uuid         not null,
    tipo                     varchar(10)  not null,
    precio_pesos             bigint,
    cantidad_disponible      integer,
    descripcion_alternativa  varchar(300),
    tiempo_estimado_minutos  integer,
    -- Costo de domicilio a la ubicación de la solicitud; nulo si la tienda no lo lleva hasta allá
    costo_domicilio_pesos    bigint,
    -- Orden del módulo ordenes creada al elegir esta respuesta
    orden_id                 uuid,
    version                  bigint       not null default 0,
    creado_en                timestamptz  not null,
    actualizado_en           timestamptz  not null,
    constraint pk_respuesta primary key (id),
    constraint fk_respuesta_solicitud foreign key (solicitud_id) references solicitudes.solicitud (id),
    -- CP-SOL-019-04: una sola respuesta por tienda y solicitud
    constraint uq_respuesta_solicitud_tienda unique (solicitud_id, tienda_id),
    constraint uq_respuesta_orden unique (orden_id),
    constraint ck_respuesta_tipo check (tipo in ('SI', 'PARECIDO', 'NO')),
    constraint ck_respuesta_precio check (precio_pesos is null or precio_pesos > 0),
    constraint ck_respuesta_cantidad check (cantidad_disponible is null or cantidad_disponible between 1 and 999),
    constraint ck_respuesta_con_producto
        check (tipo = 'NO' or (precio_pesos is not null and cantidad_disponible is not null)),
    constraint ck_respuesta_alternativa check (tipo <> 'PARECIDO' or descripcion_alternativa is not null)
);

-- «Mis respuestas» de la tienda (HU-021) y precio sugerido (SC-011)
create index ix_respuesta_tienda_creado on solicitudes.respuesta (tienda_id, creado_en);

-- Dueño de cada tienda que recibió la solicitud, para avisarle por SSE al elegir o vencer (HU-021, HU-030).
-- Nulo en los envíos anteriores a esta migración.
alter table solicitudes.envio_solicitud add column dueno_id uuid;
