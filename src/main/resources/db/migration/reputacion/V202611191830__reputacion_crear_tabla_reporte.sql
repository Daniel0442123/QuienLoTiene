-- HU-035 Reportar problema y HU-037 Gestionar reportes (diagrama 09: reputacion.reporte). Motivos de SC-011 punto 5.
create table reputacion.reporte (
    id               uuid          not null,
    -- Referencias lógicas a ordenes, tiendas e identidad; sin llave foránea (monolito modular)
    orden_id         uuid          not null,
    tienda_id        uuid          not null,
    autor_id         uuid          not null,
    autor_es_cliente boolean       not null,
    motivo           varchar(30)   not null,
    descripcion      varchar(1000),
    estado           varchar(10)   not null,
    nota_resolucion  varchar(500),
    resuelto_en      timestamptz,
    creado_en        timestamptz   not null,
    actualizado_en   timestamptz   not null,
    constraint pk_reporte primary key (id),
    constraint ck_reporte_motivo check (motivo in ('DEMORA_ENTREGA', 'PRODUCTO_DIFERENTE', 'COBRO_DIFERENTE', 'OTRO')),
    constraint ck_reporte_estado check (estado in ('ABIERTO', 'RESUELTO')),
    constraint ck_reporte_resolucion check (estado = 'ABIERTO' or nota_resolucion is not null)
);

-- Bandeja del administrador (HU-037) y conteo por tienda
create index ix_reporte_estado_creado on reputacion.reporte (estado, creado_en);
create index ix_reporte_tienda on reputacion.reporte (tienda_id);
