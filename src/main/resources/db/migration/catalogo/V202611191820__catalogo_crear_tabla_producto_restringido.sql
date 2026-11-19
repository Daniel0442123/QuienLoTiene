-- HU-023 y HU-038: lista de productos restringidos (diagrama 09: catalogo.producto_restringido). La palabra se guarda
-- normalizada (minúsculas, sin tildes); un producto es restringido si la contiene.
create table catalogo.producto_restringido (
    id             uuid        not null,
    palabra_clave  varchar(80) not null,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_producto_restringido primary key (id),
    constraint uq_producto_restringido_palabra unique (palabra_clave)
);

-- La lista inicial del MVP: antibióticos de venta con fórmula (los ejemplos de CP-SOL-023-01). El administrador la
-- amplía desde su panel.
insert into catalogo.producto_restringido (id, palabra_clave, creado_en, actualizado_en)
values ('5a1c3e2b-0001-4c7d-8e9f-1a2b3c4d0001', 'amoxicilina', now(), now()),
       ('5a1c3e2b-0002-4c7d-8e9f-1a2b3c4d0002', 'azitromicina', now(), now()),
       ('5a1c3e2b-0003-4c7d-8e9f-1a2b3c4d0003', 'ciprofloxacina', now(), now());
