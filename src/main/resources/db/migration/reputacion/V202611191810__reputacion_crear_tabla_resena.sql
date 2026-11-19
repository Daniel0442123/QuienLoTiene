-- HU-034 Calificar tienda (diagrama 09: reputacion.resena). Una reseña por orden; el promedio se calcula al consultar.
create table reputacion.resena (
    id             uuid         not null,
    -- Referencias lógicas a ordenes, tiendas e identidad; sin llave foránea (monolito modular)
    orden_id       uuid         not null,
    tienda_id      uuid         not null,
    cliente_id     uuid         not null,
    estrellas      smallint     not null,
    comentario     varchar(500),
    creado_en      timestamptz  not null,
    actualizado_en timestamptz  not null,
    constraint pk_resena primary key (id),
    constraint uq_resena_orden unique (orden_id),
    constraint ck_resena_estrellas check (estrellas between 1 and 5)
);

-- Detalle de la tienda (HU-014): promedio y reseñas recientes
create index ix_resena_tienda_creado on reputacion.resena (tienda_id, creado_en desc);
