package com.quienlotiene.catalogo.api;

import java.util.UUID;

/** Una palabra de la lista de productos restringidos (HU-038, diagrama 09: {@code catalogo.producto_restringido}). */
public record ProductoRestringido(UUID id, String palabraClave) {}
