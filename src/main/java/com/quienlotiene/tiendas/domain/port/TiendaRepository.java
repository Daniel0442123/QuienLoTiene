package com.quienlotiene.tiendas.domain.port;

import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.Tienda;
import java.util.List;

/** Guarda las tiendas y sus documentos en el esquema {@code tiendas}. */
public interface TiendaRepository {

    void guardarNueva(Tienda tienda, List<DocumentoVerificacion> documentos);
}
