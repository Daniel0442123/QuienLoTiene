/*
 * Avisos push de ¿Quién lo tiene? (HU-031). El service worker que genera vite-plugin-pwa (generateSW) carga este
 * archivo con importScripts (vite.config.ts, workbox.importScripts). No usa librerías ni guarda nada en caché.
 *
 * El servidor manda solo { tipo, id } (decisión 9 del contrato del Sprint 3, diagrama 28): ningún dato personal viaja
 * en el aviso. Hoy los tres tipos le llegan a la tienda cuando no tiene la app abierta (AvisarPorPush), así que al
 * tocar el aviso se abre la pantalla de la tienda que corresponde. Los textos van aquí porque el service worker no
 * puede importar textos.ts.
 */

const AVISOS_PUSH = {
  'solicitud-nueva': {
    titulo: 'Solicitud nueva',
    cuerpo: 'Un vecino pregunta si tienes un producto. Toca para responder.',
    ruta: (id) => `/tienda/solicitudes/${id}`,
  },
  'orden-creada': {
    titulo: 'Te eligieron: pedido nuevo',
    cuerpo: 'Un cliente eligió tu respuesta. Toca para ver el pedido.',
    ruta: (id) => `/tienda/pedidos/${id}`,
  },
  'estado-orden-cambiado': {
    titulo: 'Un pedido cambió',
    cuerpo: 'Toca para ver cómo va.',
    ruta: (id) => `/tienda/pedidos/${id}`,
  },
}

// Solo ids con forma de UUID: el id va dentro de la ruta y no debe poder cambiarla
const FORMA_DE_UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

function avisoDe(datos) {
  const aviso = datos && AVISOS_PUSH[datos.tipo]
  if (!aviso) return null
  const id = typeof datos.id === 'string' && FORMA_DE_UUID.test(datos.id) ? datos.id : null
  return {
    titulo: aviso.titulo,
    opciones: {
      body: aviso.cuerpo,
      icon: '/icono-192.png',
      badge: '/icono-192.png',
      lang: 'es-CO',
      // Un aviso por recurso: si llegan dos del mismo pedido, el nuevo reemplaza al anterior
      tag: `${datos.tipo}-${id ?? 'general'}`,
      data: { url: id ? aviso.ruta(id) : '/' },
    },
  }
}

self.addEventListener('push', (evento) => {
  let datos = null
  try {
    datos = evento.data ? evento.data.json() : null
  } catch {
    datos = null
  }
  const aviso = avisoDe(datos)
  if (!aviso) return
  evento.waitUntil(self.registration.showNotification(aviso.titulo, aviso.opciones))
})

// Al tocar el aviso: si la app ya está abierta, se enfoca y va a la pantalla; si no, se abre ahí
self.addEventListener('notificationclick', (evento) => {
  evento.notification.close()
  const url = new URL((evento.notification.data && evento.notification.data.url) || '/', self.location.origin)
  if (url.origin !== self.location.origin) return
  evento.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((ventanas) => {
      const abierta = ventanas.find((ventana) => new URL(ventana.url).origin === url.origin)
      if (abierta) return abierta.focus().then((enfocada) => enfocada.navigate(url.href))
      return self.clients.openWindow(url.href)
    }),
  )
})
