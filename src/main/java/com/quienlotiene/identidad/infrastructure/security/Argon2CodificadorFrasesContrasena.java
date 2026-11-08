package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Usa el {@code PasswordEncoder} Argon2id de {@code SeguridadConfig}. */
@Component
class Argon2CodificadorFrasesContrasena implements CodificadorFrasesContrasena {

    private final PasswordEncoder codificador;

    /** Hash con los mismos parámetros que los reales, calculado una vez al arrancar, para igualar los tiempos. */
    private final String hashFicticio;

    Argon2CodificadorFrasesContrasena(PasswordEncoder codificador) {
        this.codificador = codificador;
        this.hashFicticio = codificador.encode("frase ficticia para igualar el tiempo de respuesta");
    }

    @Override
    public String calcularHash(FraseContrasena frase) {
        return codificador.encode(frase.valor());
    }

    @Override
    public boolean coincide(String fraseIngresada, String hashGuardado) {
        return fraseIngresada != null && codificador.matches(fraseIngresada, hashGuardado);
    }

    @Override
    public void verificarContraHashFicticio(String fraseIngresada) {
        codificador.matches(fraseIngresada == null ? "" : fraseIngresada, hashFicticio);
    }
}
