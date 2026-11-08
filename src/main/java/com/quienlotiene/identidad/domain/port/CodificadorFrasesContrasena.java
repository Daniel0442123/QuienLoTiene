package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.FraseContrasena;

/** Calcula el hash de una frase de contraseña (Argon2id, NF-SEG-006). */
public interface CodificadorFrasesContrasena {

    String calcularHash(FraseContrasena frase);
}
