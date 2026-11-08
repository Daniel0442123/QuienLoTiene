package com.quienlotiene.identidad.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Administrador que se crea al arrancar si no existe (HU-004). Todo sale de variables de entorno {@code QLT_ADMIN_*};
 * si falta el correo no se crea nada. El secreto TOTP lo genera quien despliega y lo carga en su aplicación
 * autenticadora: la API nunca lo muestra ni lo registra.
 */
@ConfigurationProperties("quienlotiene.identidad.administrador-inicial")
public record PropiedadesAdministradorInicial(
        String nombre, String correo, String telefono, String fraseContrasena, String secretoTotp) {

    public boolean estaConfigurado() {
        return correo != null && !correo.isBlank();
    }

    @Override
    public String toString() {
        return "PropiedadesAdministradorInicial[***]";
    }
}
