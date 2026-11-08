-- HU-003 Iniciar y cerrar sesión: tokens de refresco opacos y rotativos (D-15).
-- Solo se guarda el hash SHA-256 del token; el valor viaja en una cookie HttpOnly.

create table identidad.token_refresco (
    id             uuid        not null,
    usuario_id     uuid        not null,
    hash_token     varchar(64) not null,
    expira_en      timestamptz not null,
    revocado       boolean     not null default false,
    creado_en      timestamptz not null,
    actualizado_en timestamptz not null,
    constraint pk_token_refresco primary key (id),
    constraint uq_token_refresco_hash unique (hash_token),
    constraint fk_token_refresco_usuario foreign key (usuario_id) references identidad.usuario (id)
);

-- Para cerrar todas las sesiones de un usuario
create index ix_token_refresco_usuario on identidad.token_refresco (usuario_id) where revocado = false;
