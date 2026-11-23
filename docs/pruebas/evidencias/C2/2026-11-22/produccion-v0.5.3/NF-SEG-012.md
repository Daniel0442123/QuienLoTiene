# NF-SEG-012 · Re-prueba en producción sobre v0.5.3

- Fecha y hora: 22/11/2026 00:05 (hora de Bogotá).
- Versión: `v0.5.3` (`ff9ac6d`).
- Cómo:
  1. Daniel Montenegro registró «Tienda Prueba EXIF 2» en https://quienlotiene.tech/registrar-tienda. Como foto del local usó `foto-con-exif-NF-SEG-012.jpg`, un JPEG de 800×600 y 15.946 bytes con EXIF: Make «Samsung», Model «SM-A546 PRUEBA-EXIF» y GPS 2°26'41" N, 76°36'53" W.
  2. Como administrador, se descargó la foto desde `/admin/tiendas`. Se leyeron los segmentos del JPEG en el navegador.

| Archivo guardado | Tamaño | Segmentos | EXIF (APP1) | Modelo o marca | XMP |
|---|---|---|---|---|---|
| Foto del local de Tienda Prueba EXIF 2 | 5.517 bytes | APP0 JFIF, APP2 ICC, DQT, SOF0, DHT, SOS | No | No | No |

Resultado: **Aprobado**. La foto guardada no conserva metadatos.

Nota: el formulario reduce la foto en el navegador (`frontend/src/services/archivos.ts`, `reducirFoto`) y la pasa por un canvas, lo que ya descarta el EXIF antes de enviarla. Esta re-prueba verifica el recorrido de punta a punta, no el limpiador del servidor (`LimpiadorMetadatos`). El limpiador queda cubierto por `LimpiadorMetadatosTest` y `AdministracionTiendasIT#guardaLaFotoSinMetadatos` sobre el mismo código de v0.5.3. Cubre el caso en que el navegador conserva la foto original porque la versión reducida no pesa menos.

Las tiendas de prueba «Tienda Prueba EXIF» y «Tienda Prueba EXIF 2» se rechazaron el 22/11 a las 9:55 a. m. con el motivo «Tienda de prueba de QA (re-prueba de NF-SEG-012 sobre v0.5.3)».
