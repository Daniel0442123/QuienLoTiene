-- HU-012 Configurar medios de pago (diagrama 09: tiendas.medio_pago_tienda). Solo se registran; no hay cobro en la app.

create table tiendas.medio_pago_tienda (
    tienda_id      uuid        not null,
    medio_pago     varchar(15) not null,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_medio_pago_tienda primary key (tienda_id, medio_pago),
    constraint fk_medio_pago_tienda_tienda foreign key (tienda_id) references tiendas.tienda (id),
    constraint ck_medio_pago_tienda_medio check (medio_pago in ('EFECTIVO', 'TRANSFERENCIA', 'DATAFONO'))
);
