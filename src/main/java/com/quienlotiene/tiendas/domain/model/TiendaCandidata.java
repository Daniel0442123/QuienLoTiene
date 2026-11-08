package com.quienlotiene.tiendas.domain.model;

/** Tienda que quedó dentro del radio de una solicitud, con su distancia al punto del cliente. */
public record TiendaCandidata(Tienda tienda, double distanciaEnMetros) {}
