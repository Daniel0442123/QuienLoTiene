package com.quienlotiene.catalogo.api;

import java.util.UUID;

/** Una categoría como la ve el administrador (HU-038). */
public record CategoriaAdministrada(UUID id, String nombre, boolean activa) {}
