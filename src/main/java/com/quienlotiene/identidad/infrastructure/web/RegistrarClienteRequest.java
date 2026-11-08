package com.quienlotiene.identidad.infrastructure.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/v1/auth/registro}. Aquí solo se revisa la forma; la longitud mínima de la frase y la
 * lista de frases comunes son reglas del dominio (IDE-002 e IDE-003).
 */
public record RegistrarClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre puede tener máximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 254, message = "El correo puede tener máximo 254 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio")
        String telefono,

        @NotBlank(message = "La frase de contraseña es obligatoria")
        String fraseContrasena,

        // CP-IDE-005-01: el error sale en la lista "errores" con este campo para que la pantalla resalte la casilla
        @NotNull(message = "Debes aceptar la política de tratamiento de datos")
        @AssertTrue(message = "Debes aceptar la política de tratamiento de datos")
        Boolean aceptaPoliticaDatos,

        @NotBlank(message = "La versión de la política aceptada es obligatoria")
        @Size(max = 10, message = "La versión de la política puede tener máximo 10 caracteres")
        String versionPoliticaDatos) {

    @Override
    public String toString() {
        return "RegistrarClienteRequest[***]";
    }
}
