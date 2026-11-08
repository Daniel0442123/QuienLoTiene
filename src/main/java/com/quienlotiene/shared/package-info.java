/**
 * Núcleo común a todos los módulos: excepción base de reglas de negocio, manejo de errores, correlación de
 * peticiones y configuración transversal. Es un módulo abierto: cualquier módulo puede usar sus clases.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package com.quienlotiene.shared;

import org.springframework.modulith.ApplicationModule;
