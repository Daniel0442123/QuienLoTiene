package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.FraseContrasena;

/** Lista de contraseñas comunes o filtradas contra la que se revisa cada frase nueva (NIST 800-63B). */
public interface ListaFrasesComunes {

    boolean contiene(FraseContrasena frase);
}
