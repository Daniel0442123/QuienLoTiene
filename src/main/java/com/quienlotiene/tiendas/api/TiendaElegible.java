package com.quienlotiene.tiendas.api;

import java.util.UUID;

/**
 * Tienda que recibe una solicitud. Lleva el id del dueño para que notificaciones le avise y la distancia para que el
 * cliente pueda ordenar las respuestas por cercanía.
 */
public record TiendaElegible(UUID tiendaId, UUID duenoId, String nombre, int distanciaEnMetros) {}
