-- HU-010 Configurar domicilio con tarifa fija (diagrama 09: hace_domicilio, radio de cobertura y tarifa).
-- Radio en metros y dinero en pesos enteros (DECISIONES-BACKEND 3.2 y 10). Los tipos POR_DISTANCIA y
-- GRATIS_DESDE_MONTO y su columna monto_minimo_gratis llegan con HU-011.

alter table tiendas.tienda add column hace_domicilio boolean not null default false;
alter table tiendas.tienda add column radio_cobertura_metros integer;
alter table tiendas.tienda add column tipo_tarifa varchar(20);
alter table tiendas.tienda add column valor_tarifa_pesos bigint;

alter table tiendas.tienda add constraint ck_tienda_tarifa_no_negativa check (valor_tarifa_pesos >= 0);
alter table tiendas.tienda add constraint ck_tienda_tipo_tarifa check (tipo_tarifa in ('FIJA'));
alter table tiendas.tienda add constraint ck_tienda_domicilio_completo
    check (not hace_domicilio or (radio_cobertura_metros is not null and tipo_tarifa is not null
                                  and valor_tarifa_pesos is not null));
