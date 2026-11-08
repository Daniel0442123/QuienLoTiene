package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Usa el {@code PasswordEncoder} Argon2id de {@code SeguridadConfig}. */
@Component
class Argon2CodificadorFrasesContrasena implements CodificadorFrasesContrasena {

    private final PasswordEncoder codificador;

    Argon2CodificadorFrasesContrasena(PasswordEncoder codificador) {
        this.codificador = codificador;
    }

    @Override
    public String calcularHash(FraseContrasena frase) {
        return codificador.encode(frase.valor());
    }
}
