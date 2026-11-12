package com.quienlotiene.catalogo.application.dto;

import java.util.UUID;

/** Lo que ve el usuario de una categoría al elegirla en un formulario. */
public record CategoriaResumen(UUID id, String nombre) {}
