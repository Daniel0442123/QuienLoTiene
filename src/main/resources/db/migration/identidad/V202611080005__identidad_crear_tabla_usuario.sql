-- HU-001 Registrar cliente: cuentas de usuario (cliente, tienda y administrador).
-- Columnas según el diagrama 09-entidad-relacion; secreto_totp llega con HU-004.

create table identidad.usuario (
    id              uuid         not null,
    nombre          varchar(120) not null,
    -- Siempre en minúsculas (lo normaliza el objeto de valor Correo)
    correo          varchar(254) not null,
    telefono        varchar(20)  not null,
    -- Hash Argon2id de la frase de contraseña; la frase nunca se guarda
    hash_contrasena varchar(255) not null,
    rol             varchar(10)  not null,
    estado          varchar(12)  not null,
    creado_en       timestamptz  not null,
    actualizado_en  timestamptz  not null,
    constraint pk_usuario primary key (id),
    constraint uq_usuario_correo unique (correo),
    constraint ck_usuario_rol check (rol in ('CLIENTE', 'TIENDA', 'ADMIN')),
    constraint ck_usuario_estado check (estado in ('ACTIVA', 'SUSPENDIDA'))
);
