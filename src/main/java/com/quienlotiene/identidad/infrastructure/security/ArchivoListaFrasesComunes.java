package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Lista de contraseñas comunes y filtradas de 15 o más caracteres, leída una sola vez de
 * {@code identidad/frases-comunes.txt}. Las de menos de 15 ya las rechaza la regla de longitud. La comparación no
 * distingue mayúsculas ni espacios a los lados. Para ampliar la lista basta con agregar líneas al archivo.
 */
@Component
class ArchivoListaFrasesComunes implements ListaFrasesComunes {

    static final String RUTA_ARCHIVO = "identidad/frases-comunes.txt";

    private final Set<String> frasesComunes;

    ArchivoListaFrasesComunes() {
        this(new ClassPathResource(RUTA_ARCHIVO));
    }

    ArchivoListaFrasesComunes(Resource archivo) {
        this.frasesComunes = leer(archivo);
    }

    @Override
    public boolean contiene(FraseContrasena frase) {
        return frasesComunes.contains(normalizar(frase.valor()));
    }

    int cantidadFrases() {
        return frasesComunes.size();
    }

    private static Set<String> leer(Resource archivo) {
        try (var lector = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            return lector.lines()
                    .map(ArchivoListaFrasesComunes::normalizar)
                    .filter(linea -> !linea.isEmpty() && !linea.startsWith("#"))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException causa) {
            throw new UncheckedIOException("No se pudo leer la lista de frases comunes " + RUTA_ARCHIVO, causa);
        }
    }

    private static String normalizar(String texto) {
        return texto.strip().toLowerCase(Locale.ROOT);
    }
}
