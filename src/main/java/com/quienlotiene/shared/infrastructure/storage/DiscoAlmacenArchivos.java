package com.quienlotiene.shared.infrastructure.storage;

import com.quienlotiene.shared.domain.AlmacenArchivos;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/** Guarda los archivos en el disco del servidor, con nombre UUID, dentro de {@code quienlotiene.archivos.directorio}. */
@Component
@EnableConfigurationProperties(PropiedadesAlmacenArchivos.class)
public class DiscoAlmacenArchivos implements AlmacenArchivos {

    private static final Pattern SEGMENTO_PERMITIDO = Pattern.compile("[a-z0-9-]{1,64}");
    private static final Pattern EXTENSION_PERMITIDA = Pattern.compile("[a-z0-9]{1,5}");
    private static final int MAXIMO_SEGMENTOS = 5;

    private final Path directorioBase;

    public DiscoAlmacenArchivos(PropiedadesAlmacenArchivos propiedades) {
        this.directorioBase = Path.of(propiedades.directorio()).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(String carpeta, String extension, byte[] contenido) {
        if (!esCarpetaPermitida(carpeta)
                || !EXTENSION_PERMITIDA.matcher(extension).matches()) {
            throw new IllegalArgumentException("Carpeta o extensión no permitida para el almacén de archivos");
        }
        var ruta = carpeta + "/" + UUID.randomUUID() + "." + extension;
        var destino = resolver(ruta);
        var carpetaDestino = destino.getParent();
        if (carpetaDestino == null) {
            throw new IllegalArgumentException("La ruta del archivo no tiene carpeta");
        }
        try {
            Files.createDirectories(carpetaDestino);
            Files.write(destino, contenido);
            return ruta;
        } catch (IOException causa) {
            throw new UncheckedIOException("No se pudo guardar el archivo", causa);
        }
    }

    @Override
    public byte[] leer(String ruta) {
        try {
            return Files.readAllBytes(resolver(ruta));
        } catch (IOException causa) {
            throw new UncheckedIOException("No se pudo leer el archivo", causa);
        }
    }

    @Override
    public void borrar(String ruta) {
        try {
            Files.deleteIfExists(resolver(ruta));
        } catch (IOException causa) {
            throw new UncheckedIOException("No se pudo borrar el archivo", causa);
        }
    }

    /** Carpetas como {@code tiendas/<uuid>}: segmentos cortos de minúsculas, dígitos y guiones, sin "..". */
    private static boolean esCarpetaPermitida(String carpeta) {
        var segmentos = carpeta.split("/", -1);
        if (segmentos.length > MAXIMO_SEGMENTOS) {
            return false;
        }
        for (var segmento : segmentos) {
            if (!SEGMENTO_PERMITIDO.matcher(segmento).matches()) {
                return false;
            }
        }
        return true;
    }

    /** Impide que una ruta como {@code ../../etc/passwd} salga del directorio del almacén. */
    private Path resolver(String ruta) {
        var destino = directorioBase.resolve(ruta).normalize();
        if (!destino.startsWith(directorioBase)) {
            throw new IllegalArgumentException("La ruta sale del directorio del almacén de archivos");
        }
        return destino;
    }
}
