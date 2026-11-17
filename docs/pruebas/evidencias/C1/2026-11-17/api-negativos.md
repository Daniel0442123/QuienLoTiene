# Pruebas por API contra el backend real · 17/11/2026

Backend `main` (5e8ab98) en perfil dev con PostGIS 17-3.5, datos creados por API (3 tiendas de Farmacia en Popayán, clientes Ana y Luis). Script: `negativos.py` (copia en `/mnt/project-files/pruebas-sprint2/`). Respuestas recortadas a los campos de ProblemDetail.

### CP-SOL-018-01 · Solicitudes disponibles para otra tienda (tomada a las 00:49 UTC, con las dos solicitudes de Luis abiertas)
`GET /solicitudes/disponibles` como **botica** → HTTP 200
```json
[
 {
  "id": "6c828ea0-940c-475b-9230-87e85a4a67c1",
  "categoriaId": "3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001",
  "producto": "Amoxicilina 500 mg",
  "cantidad": 1,
  "modalidad": "RECOGER",
  "esUrgente": true,
  "distanciaEnMetros": 300,
  "fechaPublicacion": "2026-11-17T00:49:40.272421Z",
  "fechaVencimiento": "2026-11-17T01:19:40.272421Z",
  "precioSugeridoEnPesos": null
 },
 {
  "id": "79714c88-202c-4dff-b3e9-1d1bd310770d",
  "categoriaId": "3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001",
  "producto": "Ibuprofeno 400 mg",
  "cantidad": 1,
  "modalidad": "RECOGER",
  "esUrgente": false,
  "distanciaEnMetros": 300,
  "fechaPublicacion": "2026-11-17T00:49:44.175008Z",
  "fechaVencimiento": "2026-11-17T01:19:44.175008Z",
  "precioSugeridoEnPesos": null
 }
]
```
No trae nombre, teléfono, dirección ni coordenadas del cliente; la distancia viene redondeada a 100 m (300 m; la real es 324 m).

### CP-SOL-019-04 · La 5 responde por segunda vez la misma solicitud
`POST /solicitudes/6c828ea0-940c-475b-9230-87e85a4a67c1/respuestas` como **drogueria5** → HTTP 409
```json
{"status": 409, "codigo": "SOL-005", "title": "Respuesta duplicada", "detail": "Ya respondiste esta solicitud."}
```

### CP-ORD-024-02 · La tienda elegida consulta la orden de Luis (activa)
`GET /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f` como **drogueria5** → HTTP 200
```json
{"id": "74ea87e6-24cc-4bab-99b8-2c37fce88d2f", "solicitudId": "6c828ea0-940c-475b-9230-87e85a4a67c1", "respuestaId": "6a262c9a-7bac-44c8-83d6-d897707b58ce", "producto": "Amoxicilina 500 mg", "cantidad": 1, "precioUnitarioEnPesos": 12000, "subtotalEnPesos": 12000, "costoDomicilioEnPesos": 0, "totalEnPesos": 12000, "modalidad": "RECOGER", "medioPago": "TRANSFERENCIA", "estado": "CONFIRMADO", "esActiva": true, "fechaCreacion": "2026-11-17T00:50:03.132621Z", "tienda": {"id": "3ffb7aaf-433a-4daf-9b99-be6b7a48bee8", "nombre": "Droguería La 5"}, "cliente": {"nombre": "Luis Pérez", "telefono": "320555
```

### CP-ORD-024-03 · Botica San José (no elegida) consulta la orden de Luis
`GET /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f` como **botica** → HTTP 404
```json
{"status": 404, "codigo": "ORD-001", "title": "Orden no encontrada", "detail": "La orden no existe."}
```

### CP-ORD-024-03 · Botica San José pide el enlace de WhatsApp de la orden de Luis
`GET /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/whatsapp` como **botica** → HTTP 404
```json
{"status": 404, "codigo": "ORD-001", "title": "Orden no encontrada", "detail": "La orden no existe."}
```

### CP-ORD-027-02 · La 5 intenta pasar la orden de Luis (CONFIRMADO, recoger) directamente a ENTREGADO
`PATCH /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/estado` como **drogueria5** → HTTP 409
```json
{"status": 409, "codigo": "ORD-002", "title": "Cambio de estado no permitido", "detail": "La orden está confirmada y no puede pasar a entregada."}
```

### CP-ORD-027-02 · La 5 intenta pasar a EN_CAMINO una orden para recoger
`PATCH /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/estado` como **drogueria5** → HTTP 409
```json
{"status": 409, "codigo": "ORD-002", "title": "Cambio de estado no permitido", "detail": "La orden está confirmada y no puede pasar a en camino."}
```

### CP-ORD-028-02 · Ana intenta confirmar la recepción de la orden de Luis
`POST /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/recepcion` como **ana** → HTTP 404
```json
{"status": 404, "codigo": "ORD-001", "title": "Orden no encontrada", "detail": "La orden no existe."}
```

### CP-ORD-028-02 · Luis intenta confirmar la recepción antes de que la tienda la entregue
`POST /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/recepcion` como **luis** → HTTP 409
```json
{"status": 409, "codigo": "ORD-002", "title": "Cambio de estado no permitido", "detail": "La orden está confirmada y no puede pasar a resuelta."}
```

### CP-MEN-032-01 · Luis pide el enlace de WhatsApp de su orden activa
`GET /ordenes/74ea87e6-24cc-4bab-99b8-2c37fce88d2f/whatsapp` como **luis** → HTTP 200
```json
{"nombre": "Droguería La 5", "telefono": "573100000001", "enlace": "https://wa.me/573100000001?text=Hola%2C%20te%20escribo%20por%20la%20orden%2074EA87E6%20de%20Amoxicilina%20500%20mg%20en%20%C2%BFQui%C3%A9n%20lo%20tiene%3F"}
```

### CP-MEN-032-02 · Ana pide el enlace de WhatsApp de su orden ya recibida (RESUELTA)
`GET /ordenes/8adeee69-129b-4e24-b0ec-0f125c573145/whatsapp` como **ana** → HTTP 409
```json
{"status": 409, "codigo": "MEN-001", "title": "Contacto no disponible", "detail": "La orden ya terminó; el contacto por WhatsApp solo está disponible con la orden activa."}
```

### CP-ORD-024-02 · Con la orden de Ana cerrada, La 5 ya no recibe sus datos
`GET /ordenes/8adeee69-129b-4e24-b0ec-0f125c573145` como **drogueria5** → HTTP 200
```json
{"id": "8adeee69-129b-4e24-b0ec-0f125c573145", "solicitudId": "548fd0fb-9a70-4751-83e2-44d958285e5b", "respuestaId": "1c1149e3-c864-4fc0-952a-b76e1eadc034", "producto": "Loratadina 10 mg", "cantidad": 2, "precioUnitarioEnPesos": 9000, "subtotalEnPesos": 18000, "costoDomicilioEnPesos": 5000, "totalEnPesos": 23000, "modalidad": "DOMICILIO", "medioPago": "EFECTIVO", "estado": "RESUELTA", "esActiva": false, "fechaCreacion": "2026-11-17T00:48:02.893819Z", "tienda": {"id": "3ffb7aaf-433a-4daf-9b99-be6b7a48bee8", "nombre": "Droguería La 5"}, "cliente": null, "historial": [{"estado": "CONFIRMADO", "fe
```

### CP-ORD-027-02 · La 5 intenta mover la orden de Ana ya RESUELTA a PREPARANDO
`PATCH /ordenes/8adeee69-129b-4e24-b0ec-0f125c573145/estado` como **drogueria5** → HTTP 409
```json
{"status": 409, "codigo": "ORD-002", "title": "Cambio de estado no permitido", "detail": "La orden está resuelta y no puede pasar a en preparación."}
```

