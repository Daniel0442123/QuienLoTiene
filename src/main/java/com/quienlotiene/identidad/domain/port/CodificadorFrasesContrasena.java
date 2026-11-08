package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.FraseContrasena;

/** Calcula y verifica el hash de una frase de contraseña (Argon2id, NF-SEG-006). */
public interface CodificadorFrasesContrasena {

    String calcularHash(FraseContrasena frase);

    /** Compara la frase que escribió el usuario con el hash guardado. */
    boolean coincide(String fraseIngresada, String hashGuardado);

    /**
     * Hace el mismo trabajo que {@link #coincide} contra un hash ficticio. Se llama cuando el correo no existe, para
     * que la respuesta tarde lo mismo y no delate qué correos tienen cuenta.
     */
    void verificarContraHashFicticio(String fraseIngresada);
}
