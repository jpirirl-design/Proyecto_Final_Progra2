# Solución de problemas – SIGELAB

## Java / IntelliJ

**"Configure db.user en config/application.properties":** no existe el archivo o falta el dato.
Ejecute `py -3.12 scripts/configurar.py` o copie `config/application.example.properties`
como `config/application.properties`. En la configuración de ejecución de IntelliJ,
*Working directory* debe ser la carpeta que contiene `pom.xml`.

**Prefiero variables de entorno:** siguen funcionando y tienen prioridad:
`SIGELAB_DB_USER`, `SIGELAB_DB_PASSWORD`, `SIGELAB_DB_PORT`, `SIGELAB_FACIAL_TOKEN`.

**Maven no descarga dependencias:** revise Internet y recargue Maven (icono de Maven → Reload).

## SQL Server / Docker

**Conexión rechazada:** `docker ps` debe mostrar `sigelab-sql`. El proyecto publica el
puerto 1433. En SSMS el servidor es `localhost,1433`; en `application.properties`
host y puerto van separados (`db.host=localhost`, `db.port=1433`).

**Login failed for user 'sa':** la contraseña de `db.password` debe ser la de
`MSSQL_SA_PASSWORD`. Cambiar `.env` NO cambia la contraseña de un volumen ya creado.

**Las tablas no existen:** ejecute `scripts/inicializar-docker.ps1` o, en SSMS, los scripts
01, 02, 03 y 04 de `database/` en ese orden. `01` solo se ejecuta una vez; `03` se puede repetir.

## Python / cámara

**No encuentra Python 3.12:** `py -0p` debe listarlo. Instálelo desde python.org (64 bits).

**InsightFace falla al instalar:** instale Microsoft C++ Build Tools (herramientas C++ y
Windows SDK) y repita `setup-facial.ps1`.

**No encuentra ODBC Driver 18:** instálelo (64 bits). Python usa ODBC; Java usa JDBC.
Que Java conecte no significa que Python pueda.

**401 / token inválido:** `facial.token` (Java) debe ser idéntico a `FACIAL_API_TOKEN`
(`facial-service/.env`). No lo confunda con `FACIAL_ENCRYPTION_KEY`.

**"El usuario no posee un perfil facial registrado":** use *Registrar mi rostro por
primera vez* en el login, o un ADMIN desde Usuarios.

**Modelo no disponible (degraded):** el primer arranque descarga `buffalo_l`; espere y
revise `python -m scripts.doctor`.

**Cámara ocupada:** cierre Zoom/Teams/Cámara de Windows. Solo se atiende una sesión a la vez.
Puede fijar `CAMERA_INDEX` en `facial-service/.env`.

**No puede descifrar perfiles:** se cambió `FACIAL_ENCRYPTION_KEY`. Recupere la clave
original o vuelva a registrar los rostros.

## Reglas de negocio

**Equipo bloqueado:** revise su estado en Reportes → Historial. Un equipo DANADO debe
pasar por mantenimiento y cerrarse como REPARADO antes de prestarse.

**El estado del equipo no se puede editar:** es intencional. El estado solo cambia por
préstamos, devoluciones, mantenimiento o baja (RN-09, RN-12, RN-13, RN-15).

**Préstamo sigue ACTIVO:** todavía tiene equipos pendientes (RN-16).
