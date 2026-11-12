package com.quienlotiene.catalogo.infrastructure.web;

import java.util.UUID;

/** Una categoría en la respuesta de {@code GET /api/v1/categorias}. */
public record CategoriaResponse(UUID id, String nombre) {}
