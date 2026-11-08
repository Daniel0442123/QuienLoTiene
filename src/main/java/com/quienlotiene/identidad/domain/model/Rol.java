package com.quienlotiene.identidad.domain.model;

/** Rol del usuario; viaja en el claim {@code roles} del token de acceso. */
public enum Rol {
    CLIENTE,
    TIENDA,
    ADMIN
}
