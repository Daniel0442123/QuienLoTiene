-- HU-002 Registrar tienda. Columnas según el diagrama 09-entidad-relacion; las de domicilio, horario y medios de pago
-- llegan con HU-009, HU-010 y HU-012.

create table tiendas.tienda (
    id             uuid                   not null,
    -- Usuario del módulo identidad (rol TIENDA); sin llave foránea porque es otro esquema (monolito modular)
    dueno_id       uuid                   not null,
    -- Categoría del módulo catalogo; se valida por su api cuando exista
    categoria_id   uuid                   not null,
    nombre         varchar(120)           not null,
    telefono       varchar(20)            not null,
    ubicacion      geography(Point, 4326) not null,
    estado         varchar(12)            not null,
    abierta        boolean                not null default false,
    version        bigint                 not null default 0,
    creado_en      timestamptz            not null,
    actualizado_en timestamptz            not null,
    constraint pk_tienda primary key (id),
    constraint uq_tienda_dueno unique (dueno_id),
    constraint ck_tienda_estado check (estado in ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'SUSPENDIDA'))
);

-- Difusión por radio con ST_DWithin (HU-017)
create index ix_tienda_ubicacion on tiendas.tienda using gist (ubicacion);
-- Bandeja del administrador (HU-036) y difusión (solo APROBADA)
create index ix_tienda_estado on tiendas.tienda (estado);

-- Documentos que revisa el administrador. La ruta apunta al almacén de archivos, nunca a una URL pública.
create table tiendas.documento_verificacion (
    id             uuid        not null,
    tienda_id      uuid        not null,
    tipo           varchar(40) not null,
    ruta           text        not null,
    tipo_contenido varchar(40) not null,
    tamano_bytes   integer     not null,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_documento_verificacion primary key (id),
    constraint fk_documento_verificacion_tienda foreign key (tienda_id) references tiendas.tienda (id),
    constraint ck_documento_verificacion_tipo check (tipo in ('FOTO_LOCAL', 'RUT'))
);

create index ix_documento_verificacion_tienda on tiendas.documento_verificacion (tienda_id);
