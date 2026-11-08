-- HU-005 Aceptar política de datos (Ley 1581, NF-SEG-014): versión aceptada y fecha de aceptación.
-- Columnas según el diagrama 09-entidad-relacion. Las filas no se editan ni se borran: son la prueba de la autorización.

create table identidad.autorizacion_datos (
    id               uuid        not null,
    usuario_id       uuid        not null,
    version_politica varchar(10) not null,
    aceptada_en      timestamptz not null,
    creado_en        timestamptz not null,
    actualizado_en   timestamptz not null,
    constraint pk_autorizacion_datos primary key (id),
    constraint fk_autorizacion_datos_usuario foreign key (usuario_id) references identidad.usuario (id)
);

create index ix_autorizacion_datos_usuario on identidad.autorizacion_datos (usuario_id);
