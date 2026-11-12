-- Categorías de tienda y de solicitud (diagrama 09-entidad-relacion). Las usan el registro de tienda (HU-002) y la
-- publicación de solicitudes (HU-016). La gestión por el administrador llega con HU-038; mientras tanto, las del MVP
-- se siembran aquí con id fijo para que el frontend y las pruebas las reconozcan.

create table catalogo.categoria (
    id             uuid        not null,
    nombre         varchar(60) not null,
    activa         boolean     not null default true,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_categoria primary key (id),
    constraint uq_categoria_nombre unique (nombre)
);

insert into catalogo.categoria (id, nombre, activa, creado_en, actualizado_en)
values ('3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001', 'Farmacia', true, now(), now()),
       ('3f6b1c2a-0002-4a8e-9b1d-5c2e7a9f0002', 'Ferretería', true, now(), now());
