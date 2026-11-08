package com.quienlotiene.identidad.infrastructure.config;

import com.quienlotiene.identidad.application.dto.RegistrarAdministradorComando;
import com.quienlotiene.identidad.application.usecase.RegistrarAdministrador;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Crea el administrador inicial al arrancar la aplicación, solo si está configurado y todavía no existe. */
@Slf4j
@Component
class CreadorAdministradorInicial implements ApplicationRunner {

    private final PropiedadesAdministradorInicial propiedades;
    private final RegistrarAdministrador registrarAdministrador;

    CreadorAdministradorInicial(
            PropiedadesAdministradorInicial propiedades, RegistrarAdministrador registrarAdministrador) {
        this.propiedades = propiedades;
        this.registrarAdministrador = registrarAdministrador;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (!propiedades.estaConfigurado()) {
            return;
        }
        var fueCreado = registrarAdministrador.ejecutar(new RegistrarAdministradorComando(
                propiedades.nombre(),
                propiedades.correo(),
                propiedades.telefono(),
                propiedades.fraseContrasena(),
                propiedades.secretoTotp()));
        if (fueCreado) {
            log.info("Se creó el administrador inicial");
        }
    }
}
