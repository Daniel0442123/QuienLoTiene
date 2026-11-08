-- HU-004 Segundo factor del administrador (CU-25).
-- secreto_totp guarda el secreto cifrado con AES-GCM (QLT_TOTP_LLAVE_CIFRADO); es null para clientes y tiendas.

alter table identidad.usuario add column secreto_totp varchar(255);

-- Paso intermedio entre la frase y el código TOTP. Solo se guarda el hash del token temporal.
create table identidad.desafio_segundo_factor (
    id                uuid        not null,
    usuario_id        uuid        not null,
    hash_token        varchar(64) not null,
    expira_en         timestamptz not null,
    intentos_fallidos integer     not null default 0,
    usado             boolean     not null default false,
    creado_en         timestamptz not null,
    actualizado_en    timestamptz not null,
    constraint pk_desafio_segundo_factor primary key (id),
    constraint uq_desafio_segundo_factor_hash unique (hash_token),
    constraint fk_desafio_segundo_factor_usuario foreign key (usuario_id) references identidad.usuario (id)
);
