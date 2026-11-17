from api import *
FARM='3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001'
FR='frase segura de pruebas qa sprint dos'
CENTRO=(2.4448,-76.6147)  # Popayán
admin=login('admin@quienlotiene.test','frase segura del administrador de pruebas'); print('admin',list(admin))
s,a=req('POST','/auth/totp',{'tokenSegundoFactor':admin['tokenSegundoFactor'],'codigo':totp(open('/tmp/claude-0/s2/app/totp.txt').read().strip())}); print('totp',s); AT=a['tokenAcceso']
tiendas=[('Droguería La 5','drogueria5@quienlotiene.test',(2.4462,-76.6131),['EFECTIVO','TRANSFERENCIA'],True),
         ('Botica San José','botica@quienlotiene.test',(2.4430,-76.6170),['EFECTIVO','DATAFONO'],True),
         ('Farmacia Popular','popular@quienlotiene.test',(2.4475,-76.6105),['EFECTIVO'],False)]
for i,(n,c,(la,lo),mp,dom) in enumerate(tiendas):
    datos={'nombreDueno':'Dueño '+n,'correo':c,'telefono':'31000000%02d'%(i+1),'fraseContrasena':FR,'aceptaPoliticaDatos':True,'versionPoliticaDatos':'1.0','nombreTienda':n,'categoriaId':FARM,'ubicacion':{'latitud':la,'longitud':lo}}
    body,ct=multipart({'datos':(json.dumps(datos).encode(),'application/json')},{'fotoLocal':('foto.png',open('foto.png','rb').read(),'image/png'),'rut':('rut.pdf',open('rut.pdf','rb').read(),'application/pdf')})
    s,r=req('POST','/tiendas/registro',body,ct=ct); print('registro',n,s); tid=r['tiendaId'] if 'tiendaId' in r else r.get('id')
    s,r=req('POST',f'/admin/tiendas/{tid}/aprobacion',tok=AT); print(' aprobada',s)
    t=login(c,FR)['tokenAcceso']
    s,_=req('PUT','/tiendas/me/perfil',{'nombre':n,'telefono':'31000000%02d'%(i+1),'horario':[{'diaSemana':d,'abre':'00:00','cierra':'23:59'} for d in ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']]},tok=t); print(' horario',s)
    s,_=req('PUT','/tiendas/me/domicilio',{'haceDomicilio':dom,'radioCoberturaEnMetros':3000 if dom else None,'tarifaFijaEnPesos':5000 if dom else None},tok=t); print(' domicilio',s,_ if s>=400 else '')
    s,_=req('PUT','/tiendas/me/medios-pago',{'mediosPago':mp},tok=t); print(' medios',s)
    s,_=req('PATCH','/tiendas/me/apertura',{'abierta':True},tok=t); print(' abierta',s,_ if s>=400 else '')
for n,c,tel in [('Ana Gómez','ana@quienlotiene.test','3205550001'),('Luis Pérez','luis@quienlotiene.test','3205550002')]:
    s,_=req('POST','/auth/registro',{'nombre':n,'correo':c,'telefono':tel,'fraseContrasena':FR,'aceptaPoliticaDatos':True,'versionPoliticaDatos':'1.0'}); print('cliente',n,s)
    t=login(c,FR)['tokenAcceso']
    s,_=req('POST','/clientes/me/direcciones',{'nombre':'Casa','ubicacion':{'latitud':CENTRO[0],'longitud':CENTRO[1]},'direccionEscrita':'Calle 5 # 10-20, Centro','indicaciones':'Portón verde','telefono':tel},tok=t); print(' direccion',s)
