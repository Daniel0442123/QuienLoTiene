package com.quienlotiene.shared.infrastructure.web;

/** Un campo del {@code Request} que no pasó la validación, dentro de la lista {@code errores} del ProblemDetail. */
public record ErrorDeCampo(String campo, String mensaje) {}
