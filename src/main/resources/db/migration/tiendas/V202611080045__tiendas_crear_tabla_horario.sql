-- HU-009 Configurar perfil y horario: una franja por día, en hora de Colombia (America/Bogota).
-- dia_semana sigue ISO 8601: 1 = lunes ... 7 = domingo. Un día sin fila es un día cerrado.

create table tiendas.horario (
    id             uuid        not null,
    tienda_id      uuid        not null,
    dia_semana     smallint    not null,
    abre           time        not null,
    cierra         time        not null,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_horario primary key (id),
    constraint fk_horario_tienda foreign key (tienda_id) references tiendas.tienda (id),
    constraint uq_horario_tienda_dia unique (tienda_id, dia_semana),
    constraint ck_horario_dia check (dia_semana between 1 and 7),
    constraint ck_horario_franja check (abre < cierra)
);
