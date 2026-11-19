-- HU-033 Mini chat (diagrama 09: mensajeria.mensaje). Los mensajes no se editan ni se borran.
create table mensajeria.mensaje (
    id               uuid          not null,
    -- Referencias lógicas a ordenes e identidad; sin llave foránea (monolito modular)
    orden_id         uuid          not null,
    autor_id         uuid          not null,
    autor_es_cliente boolean       not null,
    texto            varchar(1000) not null,
    creado_en        timestamptz   not null,
    constraint pk_mensaje primary key (id),
    constraint ck_mensaje_texto check (length(btrim(texto)) between 1 and 1000)
);

-- Conversación de una orden en orden de llegada
create index ix_mensaje_orden_creado on mensajeria.mensaje (orden_id, creado_en);
