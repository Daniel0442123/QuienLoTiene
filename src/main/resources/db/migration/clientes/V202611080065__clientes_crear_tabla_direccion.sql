-- HU-007 Gestionar perfil y direcciones del cliente. Columnas según el diagrama 09-entidad-relacion.

create table clientes.direccion (
    id                uuid                   not null,
    -- Usuario del módulo identidad (rol CLIENTE); sin llave foránea porque es otro esquema (monolito modular)
    cliente_id        uuid                   not null,
    nombre            varchar(40)            not null,
    ubicacion         geography(Point, 4326) not null,
    direccion_escrita varchar(200)           not null,
    indicaciones      varchar(200),
    telefono          varchar(20)            not null,
    creado_en         timestamptz            not null,
    actualizado_en    timestamptz            not null,
    constraint pk_direccion primary key (id)
);

create index ix_direccion_cliente on clientes.direccion (cliente_id, creado_en);
