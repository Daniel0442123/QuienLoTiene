-- Limpieza de datos de uso de la base de producción (scripts/limpieza/limpiar.sh).
--
-- Variables de psql:
--   modo     'pruebas': borra lo de las cuentas cuyo correo termina en @<dominio>
--            'total'  : borra todas las cuentas de clientes y tiendas y lo que cuelga de ellas
--   dominio  dominio de las cuentas de prueba (solo en modo 'pruebas'), por ejemplo prueba.quienlotiene.tech
--   aplicar  'si' borra; cualquier otro valor solo cuenta lo que se borraría y deshace la transacción
--
-- Nunca borra: la estructura ni el historial de Flyway, las categorías, las cuentas ADMIN ni la auditoría
-- del administrador (administracion.auditoria es evidencia de sus decisiones).
--
-- Salida (psql -At -F '|'): filas «CONTEO|tabla|n» y «ARCHIVO|ruta» (documentos que hay que borrar del disco).
-- Los módulos no tienen llaves foráneas entre esquemas, así que el orden de los borrados se arma aquí.

\set ON_ERROR_STOP on
begin;

select case when :'modo' in ('pruebas', 'total') then 1
            else 1 / 0 end as modo_valido \gset
select case when :'modo' = 'total' or :'dominio' ~ '^[a-z0-9.-]+\.[a-z]{2,}$' then 1
            else 1 / 0 end as dominio_valido \gset

create temp table u on commit drop as
select id from identidad.usuario
where rol <> 'ADMIN'
  and (:'modo' = 'total' or correo like '%@' || :'dominio');

create temp table t on commit drop as
select id from tiendas.tienda where dueno_id in (select id from u);

create temp table s on commit drop as
select id from solicitudes.solicitud where cliente_id in (select id from u);

create temp table o on commit drop as
select id from ordenes.orden
where cliente_id in (select id from u)
   or dueno_tienda_id in (select id from u)
   or tienda_id in (select id from t)
   or solicitud_id in (select id from s);

create temp table r on commit drop as
select id from solicitudes.respuesta
where solicitud_id in (select id from s)
   or tienda_id in (select id from t)
   or dueno_id in (select id from u)
   or orden_id in (select id from o);

create temp table conteo (tabla text, n bigint) on commit drop;

-- Órdenes
with b as (delete from ordenes.historial_estado where orden_id in (select id from o) returning 1)
insert into conteo select 'ordenes.historial_estado', count(*) from b;
with b as (delete from ordenes.orden where id in (select id from o) returning 1)
insert into conteo select 'ordenes.orden', count(*) from b;

-- Solicitudes
with b as (delete from solicitudes.respuesta where id in (select id from r) returning 1)
insert into conteo select 'solicitudes.respuesta', count(*) from b;
with b as (delete from solicitudes.envio_solicitud
           where solicitud_id in (select id from s)
              or tienda_id in (select id from t)
              or dueno_id in (select id from u)
           returning 1)
insert into conteo select 'solicitudes.envio_solicitud', count(*) from b;
with b as (delete from solicitudes.solicitud where id in (select id from s) returning 1)
insert into conteo select 'solicitudes.solicitud', count(*) from b;

-- Tiendas (las rutas de los documentos se devuelven para borrarlos del disco)
create temp table archivos on commit drop as
select ruta from tiendas.documento_verificacion where tienda_id in (select id from t);
with b as (delete from tiendas.documento_verificacion where tienda_id in (select id from t) returning 1)
insert into conteo select 'tiendas.documento_verificacion', count(*) from b;
with b as (delete from tiendas.horario where tienda_id in (select id from t) returning 1)
insert into conteo select 'tiendas.horario', count(*) from b;
with b as (delete from tiendas.medio_pago_tienda where tienda_id in (select id from t) returning 1)
insert into conteo select 'tiendas.medio_pago_tienda', count(*) from b;
with b as (delete from tiendas.tienda where id in (select id from t) returning 1)
insert into conteo select 'tiendas.tienda', count(*) from b;

-- Clientes e identidad
with b as (delete from clientes.direccion where cliente_id in (select id from u) returning 1)
insert into conteo select 'clientes.direccion', count(*) from b;
with b as (delete from identidad.autorizacion_datos where usuario_id in (select id from u) returning 1)
insert into conteo select 'identidad.autorizacion_datos', count(*) from b;
with b as (delete from identidad.token_refresco where usuario_id in (select id from u) returning 1)
insert into conteo select 'identidad.token_refresco', count(*) from b;
with b as (delete from identidad.desafio_segundo_factor where usuario_id in (select id from u) returning 1)
insert into conteo select 'identidad.desafio_segundo_factor', count(*) from b;
with b as (delete from identidad.usuario where id in (select id from u) returning 1)
insert into conteo select 'identidad.usuario', count(*) from b;

-- Eventos ya procesados de Spring Modulith: solo en modo total (los de pruebas no se pueden separar)
select (:'modo' = 'total') as es_total \gset
\if :es_total
with b as (delete from event_publication where completion_date is not null returning 1)
insert into conteo select 'event_publication (procesados)', count(*) from b;
\endif

select 'CONTEO', tabla, n from conteo;
select 'ARCHIVO', ruta from archivos;

select (:'aplicar' = 'si') as se_aplica \gset
\if :se_aplica
commit;
\else
rollback;
\endif
