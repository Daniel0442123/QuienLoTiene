package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.SecretoTotp;

/** Comprueba un código de 6 dígitos (RFC 6238, pasos de 30 segundos, con tolerancia de un paso por desfase). */
public interface VerificadorCodigoTotp {

    boolean esValido(SecretoTotp secreto, String codigo);
}
