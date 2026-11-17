from api import *
import json,sys
F='frase segura de pruebas qa sprint dos'
O=json.load(open('flujo2-salida.json')); ana_orden='8adeee69-129b-4e24-b0ec-0f125c573145'; luis_orden=O['ordenLuis']
tok={c:login(c+'@quienlotiene.test',F)['tokenAcceso'] for c in ['drogueria5','botica','popular','ana','luis']}
def show(caso,desc,m,p,body=None,quien=None):
    s,r=req(m,p,body,tok=tok[quien]); 
    r2={k:r.get(k) for k in ('status','code','codigo','title','detail') if isinstance(r,dict) and k in r} if isinstance(r,dict) else r
    print(f'### {caso} · {desc}\n`{m} {p}` como **{quien}** → HTTP {s}\n```json\n{json.dumps(r2 if isinstance(r,dict) and r2 else r,ensure_ascii=False)[:600]}\n```\n')
    return s,r
show('CP-SOL-018-01','Solicitudes disponibles para otra tienda: solo producto, cantidad, modalidad, urgencia, distancia redondeada y vencimiento','GET','/solicitudes/disponibles',quien='botica')
show('CP-SOL-019-04','La 5 responde por segunda vez la misma solicitud','POST',f"/solicitudes/{O['solUrgente']}/respuestas",{'tipo':'SI','precioEnPesos':11000,'cantidadDisponible':1},quien='drogueria5')
show('CP-ORD-024-02','La tienda elegida consulta la orden de Luis (activa)','GET',f'/ordenes/{luis_orden}',quien='drogueria5')
show('CP-ORD-024-03','Botica San José (no elegida) consulta la orden de Luis','GET',f'/ordenes/{luis_orden}',quien='botica')
show('CP-ORD-024-03','Botica San José pide el enlace de WhatsApp de la orden de Luis','GET',f'/ordenes/{luis_orden}/whatsapp',quien='botica')
show('CP-ORD-027-02','La 5 intenta pasar la orden de Luis (CONFIRMADO, recoger) directamente a ENTREGADO','PATCH',f'/ordenes/{luis_orden}/estado',{'estado':'ENTREGADO'},quien='drogueria5')
show('CP-ORD-027-02','La 5 intenta pasar a EN_CAMINO una orden para recoger','PATCH',f'/ordenes/{luis_orden}/estado',{'estado':'EN_CAMINO'},quien='drogueria5')
show('CP-ORD-028-02','Ana intenta confirmar la recepción de la orden de Luis','POST',f'/ordenes/{luis_orden}/recepcion',quien='ana')
show('CP-ORD-028-02','Luis intenta confirmar la recepción antes de que la tienda la entregue','POST',f'/ordenes/{luis_orden}/recepcion',quien='luis')
show('CP-MEN-032-01','Luis pide el enlace de WhatsApp de su orden activa','GET',f'/ordenes/{luis_orden}/whatsapp',quien='luis')
show('CP-MEN-032-02','Ana pide el enlace de WhatsApp de su orden ya recibida (RESUELTA)','GET',f'/ordenes/{ana_orden}/whatsapp',quien='ana')
show('CP-ORD-024-02','Con la orden de Ana cerrada, La 5 ya no recibe sus datos','GET',f'/ordenes/{ana_orden}',quien='drogueria5')
show('CP-ORD-027-02','La 5 intenta mover la orden de Ana ya RESUELTA a PREPARANDO','PATCH',f'/ordenes/{ana_orden}/estado',{'estado':'PREPARANDO'},quien='drogueria5')
