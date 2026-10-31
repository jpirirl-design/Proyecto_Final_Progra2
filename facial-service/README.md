# Reconocimiento facial de SIGELAB

Servicio local en Python (FastAPI + InsightFace + OpenCV) que Java usa como segundo
factor del login (RN-02). Incluye captura guiada, prueba de vida (giro de cabeza),
cifrado AES-GCM del vector facial y auditoría facial en SQL Server.
Adaptado al esquema de este proyecto: `Usuario(id_usuario, nombre_usuario, id_rol, estado)`.

**Instalación completa:** seguir `../README.md`. Ejecutar Python en Windows para
usar la webcam; SQL Server puede estar en Docker.

## API local

Todas las rutas requieren cabecera `X-API-Key` igual a `FACIAL_API_TOKEN` del `.env`.
El mismo valor se configura en `facial.token` para Java. El servicio se limita a
localhost, utiliza un único worker y rechaza capturas simultáneas.

| Método | Ruta | Entrada |
|---|---|---|
| GET | /health | Sin cuerpo |
| GET | /face/status/{userId} | `Usuario.id_usuario` |
| POST | /face/enroll | {"userId":1,"replace":false} |
| POST | /face/verify | {"userId":1} |
| DELETE | /face/profile/{userId} | {"adminUserId":1} |

El token autoriza al cliente local. Las contraseñas y permisos de los usuarios se
validan en los servicios Java; no se envían contraseñas a Python. Este protocolo no
es un servicio público multiusuario. Quien posee el token y las credenciales SQL
es un operador privilegiado del equipo local.

`success` significa que la operación pudo procesarse; no equivale a rostro correcto.
Java exige además `matched=true`, `livenessPassed=true` y el mismo `userId`.
Sin perfil, sin cámara, sin modelo o con rostro incorrecto, el acceso se rechaza.

## Persistencia

- `PerfilFacial.id_usuario` referencia `Usuario.id_usuario` de SIGELAB.
- `get_user` obtiene el rol con JOIN `Rol.id_rol = Usuario.id_rol` y convierte `estado` a activo.
- Se guarda el vector facial normalizado y cifrado, no las fotografías capturadas.
- `FACIAL_ENCRYPTION_KEY` debe conservarse junto con la copia de seguridad local.
- AuditoriaFacial registra resultados y errores; Auditoria registra acciones Java.

## Diagnóstico y pruebas

Desde esta carpeta:

```powershell
.\.venv\Scripts\python.exe -m scripts.doctor
.\.venv\Scripts\python.exe -m scripts.health_check
.\.venv\Scripts\python.exe -m pytest -q
```

El doctor puede abrir la cámara. Las pruebas unitarias no abren cámara ni consultan
SQL Server. En la entrega se ejecutaron 18 pruebas, sin cargar el modelo real.

El registro guiado usa frente y perfiles; el reto de vida utiliza giro de cabeza.
El umbral inicial de similitud es 0.55 y debe comprobarse con personas distintas,
la cámara real y su iluminación. No se afirma resistencia comercial contra suplantación.
La calidad de captura y la calibración quedan pendientes de ensayo físico.

## Cliente Java

`src/main/java/sigelab/facial/ClienteFacial.java` llama a estas rutas con `HttpClient`.
`LoginService` exige `success`, `matched`, `livenessPassed` y el mismo `userId`;
cualquier otro resultado bloquea el acceso y queda en `Auditoria` como `LOGIN_FALLIDO`.

## Registrar rostros

- Desde el login: **Registrar mi rostro por primera vez** (pide usuario y contraseña).
- Desde Usuarios (ADMIN): **Registrar rostro** (reemplaza el anterior).
- Desde consola: `powershell -ExecutionPolicy Bypass -File enroll-user.ps1 admin`
