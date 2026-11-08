package com.quienlotiene.identidad.domain.model;

/** Estado de la cuenta de un usuario. Una cuenta suspendida no puede iniciar sesión (HU-003, HU-037). */
public enum EstadoCuenta {
    ACTIVA,
    SUSPENDIDA
}
