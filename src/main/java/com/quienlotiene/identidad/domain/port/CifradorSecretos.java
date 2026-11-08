package com.quienlotiene.identidad.domain.port;

/** Cifra los secretos TOTP antes de guardarlos (DECISIONES-BACKEND, sección 11: "el secreto se guarda cifrado"). */
public interface CifradorSecretos {

    String cifrar(String textoPlano);

    String descifrar(String textoCifrado);
}
