package com.quienlotiene.shared.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("HU-002")
@DisplayName("HU-002 Almacén de archivos en disco")
class DiscoAlmacenArchivosTest {

    @TempDir
    Path directorio;

    private DiscoAlmacenArchivos almacen() {
        return new DiscoAlmacenArchivos(new PropiedadesAlmacenArchivos(directorio.toString()));
    }

    @Test
    @DisplayName("Guarda con nombre generado, lo lee y lo borra")
    void guardaLeeYBorra() {
        // Dado
        var almacen = almacen();

        // Cuando
        var ruta = almacen.guardar("tiendas/abc-123", "pdf", new byte[] {1, 2, 3});

        // Entonces
        assertThat(ruta).matches("tiendas/abc-123/[0-9a-f-]{36}\\.pdf");
        assertThat(Files.exists(directorio.resolve(ruta))).isTrue();
        assertThat(almacen.leer(ruta)).containsExactly(1, 2, 3);
        almacen.borrar(ruta);
        assertThat(Files.exists(directorio.resolve(ruta))).isFalse();
        assertThatCode(() -> almacen.borrar(ruta)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No deja salir del directorio del almacén")
    void impideRutasFueraDelDirectorio() {
        var almacen = almacen();

        assertThatThrownBy(() -> almacen.guardar("../fuera", "pdf", new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacen.guardar("tiendas", "p/df", new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacen.leer("../../etc/passwd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacen.leer("tiendas/no-existe.pdf")).isInstanceOf(RuntimeException.class);
    }
}
