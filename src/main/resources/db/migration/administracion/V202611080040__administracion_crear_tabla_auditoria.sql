-- RNF-013 Auditoría de acciones sensibles: quién, qué, sobre qué y cuándo. Solo ids, sin datos personales.
-- Las filas no se editan ni se borran.

create table administracion.auditoria (
    id               uuid         not null,
    administrador_id uuid         not null,
    accion           varchar(40)  not null,
    recurso_id       uuid         not null,
    detalle          varchar(300),
    creado_en        timestamptz  not null,
    actualizado_en   timestamptz  not null,
    constraint pk_auditoria primary key (id)
);

create index ix_auditoria_recurso on administracion.auditoria (recurso_id, creado_en);
