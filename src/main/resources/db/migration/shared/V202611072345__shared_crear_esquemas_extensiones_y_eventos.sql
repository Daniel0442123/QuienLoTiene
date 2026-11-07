-- Primera migración: extensiones, un esquema por módulo y las tablas del registro de eventos de Spring Modulith.
-- DECISIONES-BACKEND secciones 4.3, 10 y 10.1. Una migración publicada en main no se modifica nunca.

-- Extensiones de PostgreSQL que usa el sistema
create extension if not exists postgis;   -- ubicaciones geography(Point, 4326) y ST_DWithin
create extension if not exists pg_trgm;   -- búsqueda aproximada de texto
create extension if not exists unaccent;  -- búsqueda sin tildes

-- Un esquema por módulo, con el mismo nombre del módulo (D-11)
create schema if not exists identidad;
create schema if not exists clientes;
create schema if not exists tiendas;
create schema if not exists catalogo;
create schema if not exists solicitudes;
create schema if not exists ordenes;
create schema if not exists mensajeria;
create schema if not exists reputacion;
create schema if not exists notificaciones;
create schema if not exists administracion;

-- Registro persistente de eventos entre módulos (spring-modulith-starter-jpa 2.x).
-- Los nombres y columnas los define Spring Modulith; no se renombran.
create table if not exists event_publication (
    id                     uuid                     not null,
    listener_id            text                     not null,
    event_type             text                     not null,
    serialized_event       text                     not null,
    publication_date       timestamp with time zone not null,
    completion_date        timestamp with time zone,
    status                 text,
    completion_attempts    integer                  not null default 0,
    last_resubmission_date timestamp with time zone,
    constraint pk_event_publication primary key (id)
);

create index if not exists idx_event_publication_serialized_event_hash
    on event_publication using hash (serialized_event);
create index if not exists idx_event_publication_completion_date
    on event_publication (completion_date);

create table if not exists event_publication_archive (
    id                     uuid                     not null,
    listener_id            text                     not null,
    event_type             text                     not null,
    serialized_event       text                     not null,
    publication_date       timestamp with time zone not null,
    completion_date        timestamp with time zone,
    status                 text,
    completion_attempts    integer                  not null default 0,
    last_resubmission_date timestamp with time zone,
    constraint pk_event_publication_archive primary key (id)
);

create index if not exists idx_event_publication_archive_serialized_event_hash
    on event_publication_archive using hash (serialized_event);
create index if not exists idx_event_publication_archive_completion_date
    on event_publication_archive (completion_date);
