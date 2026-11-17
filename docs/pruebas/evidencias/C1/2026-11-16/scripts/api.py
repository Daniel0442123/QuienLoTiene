import json,urllib.request,base64,hmac,hashlib,struct,time,uuid,os
B='http://localhost:8080/api/v1'
def req(m,p,body=None,tok=None,raw=False,ct='application/json'):
    data=None if body is None else (body if isinstance(body,bytes) else json.dumps(body).encode())
    r=urllib.request.Request(B+p,data=data,method=m)
    if data is not None: r.add_header('Content-Type',ct)
    if tok: r.add_header('Authorization','Bearer '+tok)
    try:
        with urllib.request.urlopen(r) as x: t=x.read(); return x.status,(json.loads(t) if t and not raw else t)
    except urllib.error.HTTPError as e:
        t=e.read(); 
        try: return e.code,json.loads(t)
        except Exception: return e.code,t
def totp(sec):
    k=base64.b32decode(sec); c=struct.pack('>Q',int(time.time())//30)
    h=hmac.new(k,c,hashlib.sha1).digest(); o=h[-1]&15
    return '%06d'%((struct.unpack('>I',h[o:o+4])[0]&0x7fffffff)%1000000)
def login(c,f):
    s,r=req('POST','/auth/login',{'correo':c,'fraseContrasena':f}); assert s==200,(s,r); return r
def multipart(fields,files):
    b='----qa'+uuid.uuid4().hex; out=b''
    for n,(v,ct) in fields.items(): out+=f'--{b}\r\nContent-Disposition: form-data; name="{n}"\r\nContent-Type: {ct}\r\n\r\n'.encode()+v+b'\r\n'
    for n,(fn,v,ct) in files.items(): out+=f'--{b}\r\nContent-Disposition: form-data; name="{n}"; filename="{fn}"\r\nContent-Type: {ct}\r\n\r\n'.encode()+v+b'\r\n'
    return out+f'--{b}--\r\n'.encode(),'multipart/form-data; boundary='+b
