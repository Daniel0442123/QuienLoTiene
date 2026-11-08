package com.quienlotiene.tiendas.application.dto;

import java.util.UUID;

/** Datos del dueño, de la tienda y sus documentos (HU-002, CU-15). Los archivos llegan como bytes sin validar. */
public record RegistrarTiendaComando(
        String nombreDueno,
        String correo,
        String telefono,
        String fraseContrasena,
        boolean aceptaPoliticaDatos,
        String versionPoliticaDatos,
        String nombreTienda,
        UUID categoriaId,
        double latitud,
        double longitud,
        byte[] fotoLocal,
        byte[] rut) {

    /** Copias de los archivos: el comando no comparte los arreglos con quien lo creó. */
    public RegistrarTiendaComando {
        fotoLocal = copiar(fotoLocal);
        rut = copiar(rut);
    }

    @Override
    public byte[] fotoLocal() {
        return copiar(fotoLocal);
    }

    @Override
    public byte[] rut() {
        return copiar(rut);
    }

    private static byte[] copiar(byte[] contenido) {
        return contenido == null ? null : contenido.clone();
    }

    @Override
    public String toString() {
        return "RegistrarTiendaComando[***]";
    }
}
