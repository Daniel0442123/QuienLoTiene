# Documento de evidencias de pruebas · Ciclo <N> (C<N>)

**ASEGURAMIENTO DE LA CALIDAD** · Quién lo tiene — Liberación 1.0

> Guardar en `docs/pruebas/informes/<ciclo>/EVIDENCIAS-<ciclo>.md`. Las imágenes viven en `docs/pruebas/evidencias/<ciclo>/<sesión AAAA-MM-DD>/<ID del caso>/` y se enlazan con ruta relativa.

Responsables de QA: Yanimar Burbano Piamba · Francisco José Castro Muñoz
Sesiones de ejecución: <fechas>

## 1. Introducción

Qué contiene, cuántos casos, cómo se organiza.

**Ambiente y versiones**

| Momento | Versión / commit | Uso |
|---|---|---|
| Sesión 1 — AAAA-MM-DD | | |

**Resumen**

| Resultado | Casos | Prioridad Alta |
|---|---|---|
| Aprobado | | |
| Fallido | | |
| Bloqueado | | |
| No ejecutado | | |

## 2. Evidencia de la suite automatizada

| Evidencia | Archivo |
|---|---|
| Salida de `./mvnw verify` (resumen Surefire/Failsafe) | `evidencias/<ciclo>/<sesión>/mvn-verify.txt` |
| Resumen de cobertura JaCoCo | `evidencias/<ciclo>/<sesión>/jacoco.csv` y `jacoco-resumen.png` |
| Ejecución en GitHub Actions | enlace |
| Escaneos (Trivy, Dependency-Check) | `evidencias/<ciclo>/<sesión>/trivy.txt` |

## 3. Módulo <nombre> (<responsable>) — sesión AAAA-MM-DD

### 3.1 CP-XXX-nnn-nn — <Título>

| Campo | Valor |
|---|---|
| Requisito / criterio | RF-nnn · CP-nnn del SRS |
| Tipo / nivel / prioridad | Funcional · Unitaria · Alta |
| Prueba automatizada | `Clase#metodo` |
| Precondición y datos | |
| Pasos ejecutados | |
| Resultado esperado | |
| Resultado obtenido | |
| Resultado | Aprobado · Fallido · Bloqueado |
| Defecto | DEF-nnn / — |
| Evidencia | ![01-descripcion](../../evidencias/<ciclo>/<sesión>/<caso>/01-descripcion.png) |

(Repetir la ficha por cada caso. Para las pruebas unitarias en verde basta una ficha por clase de prueba con la lista de métodos y la captura del resultado.)
