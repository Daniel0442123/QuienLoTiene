package com.quienlotiene.tiendas.infrastructure.web;

import com.quienlotiene.tiendas.application.dto.RegistrarTiendaComando;
import com.quienlotiene.tiendas.application.usecase.RegistrarTienda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Registro público de tiendas con sus documentos (CU-15). */
@Tag(name = "Tiendas", description = "Registro y configuración de las tiendas")
@RestController
@RequestMapping("/api/v1/tiendas")
class RegistroTiendaController {

    /** Recurso de la tienda del usuario actual, una vez que inicie sesión. */
    private static final URI UBICACION_TIENDA_ACTUAL = URI.create("/api/v1/tiendas/me");

    private final RegistrarTienda registrarTienda;

    RegistroTiendaController(RegistrarTienda registrarTienda) {
        this.registrarTienda = registrarTienda;
    }

    @Operation(
            summary = "Registrar tienda",
            description = "HU-002. multipart/form-data con la parte \"datos\" (JSON, Content-Type application/json) "
                    + "y los archivos \"fotoLocal\" y \"rut\" (JPEG, PNG, WebP o PDF de máximo 5 MB). "
                    + "Crea la cuenta del dueño con rol TIENDA y deja la tienda PENDIENTE de revisión.")
    @ApiResponse(responseCode = "201", description = "Tienda registrada y en revisión")
    @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos (GEN-001, TIE-001), documento faltante (TIE-002) o archivo no permitido "
                    + "(TIE-003); reglas de la cuenta (IDE-002 a IDE-007)")
    @ApiResponse(responseCode = "409", description = "El correo ya tiene una cuenta (IDE-001)")
    @ApiResponse(responseCode = "413", description = "Un archivo pesa más de 5 MB (GEN-010)")
    @PostMapping(path = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<TiendaRegistradaResponse> registrar(
            @Valid @RequestPart("datos") RegistrarTiendaRequest datos,
            @RequestPart(name = "fotoLocal", required = false) MultipartFile fotoLocal,
            @RequestPart(name = "rut", required = false) MultipartFile rut) {
        var tiendaRegistrada = registrarTienda.ejecutar(new RegistrarTiendaComando(
                datos.nombreDueno(),
                datos.correo(),
                datos.telefono(),
                datos.fraseContrasena(),
                datos.aceptaPoliticaDatos(),
                datos.versionPoliticaDatos(),
                datos.nombreTienda(),
                datos.categoriaId(),
                datos.ubicacion().latitud(),
                datos.ubicacion().longitud(),
                bytesDe(fotoLocal),
                bytesDe(rut)));
        return ResponseEntity.created(UBICACION_TIENDA_ACTUAL)
                .body(new TiendaRegistradaResponse(
                        tiendaRegistrada.id(),
                        tiendaRegistrada.nombre(),
                        tiendaRegistrada.estado(),
                        tiendaRegistrada.fechaRegistro()));
    }

    /** {@code null} si la parte no llegó: el dominio responde qué documento falta (TIE-002). */
    private static byte[] bytesDe(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            return archivo.getBytes();
        } catch (IOException causa) {
            throw new UncheckedIOException("No se pudo leer el archivo recibido", causa);
        }
    }
}
