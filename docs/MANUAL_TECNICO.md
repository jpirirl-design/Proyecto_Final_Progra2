# Manual técnico – SIGELAB

## Arquitectura
```
View (Swing) → Controller → Service (reglas) → Repository (SQL/JDBC) → SQL Server
                                  │
                                  └→ facial/ClienteFacial ──HTTP──> facial-service (Python)
```
| Paquete | Responsabilidad |
|---|---|
| `view` | Pantallas Swing; no contienen SQL. |
| `controller` | Recibe la acción de la pantalla y llama al Service. |
| `service` | Reglas RN-01 a RN-20, transacciones y auditoría. |
| `repository` | Consultas `PreparedStatement`. |
| `config` | `AppConfig` (lee `config/application.properties`) y `ConexionDB`. |
| `facial` | Cliente HTTP del reconocimiento facial. |
| `qr` | Generación y lectura de QR con ZXing. |
| `seguridad` | Sesión actual y validación de roles (`Autorizacion`). |
| `enums`, `model`, `exception` | Estados, entidades, `BusinessException` y `DataAccessException`. |

## Configuración
`AppConfig` lee `config/application.properties` desde el directorio de trabajo.
Cada clave puede sobrescribirse con una variable `SIGELAB_<CLAVE>` (`db.user` → `SIGELAB_DB_USER`).
`scripts/configurar.py` crea ese archivo, `facial-service/.env` y `.env` con un token y
una clave de cifrado aleatorios.

## Login (RN-01, RN-02, RN-18)
`LoginService.iniciarSesion`:
1. `comprobarCredenciales`: usuario existe, estado ACTIVO, contraseña PBKDF2-SHA256.
2. `ClienteFacial.verificar(idUsuario)` → `POST /face/verify` con cabecera `X-API-Key`.
3. Se exige `success`, `matched`, `livenessPassed` y el mismo `userId`.
4. Se audita `LOGIN_EXITOSO` o `LOGIN_FALLIDO`.

`registrarRostroInicial` valida contraseña y solo registra si el usuario no tenía perfil.
`UsuarioService.registrarRostro` (ADMIN) reemplaza perfiles.
`UsuarioService.crearAdministradorInicial` inserta el primer ADMIN con bloqueo
`UPDLOCK, HOLDLOCK` para que solo funcione con la tabla Usuario vacía.

## Servicio facial
FastAPI en `127.0.0.1:8765`. InsightFace (`buffalo_l`) obtiene un vector de 512 valores;
se guarda promediado y cifrado (AES-GCM) en `PerfilFacial`. La verificación compara por
similitud coseno contra `FACE_MATCH_THRESHOLD` (0.55) y pide un giro de cabeza como prueba
de vida. Cada intento queda en `AuditoriaFacial`. No se guardan fotografías.

## Transacciones
`PrestamoService.confirmarPrestamo`, `DevolucionService.devolver` y `MantenimientoService`
usan `setAutoCommit(false)` y hacen `rollback` si algo falla, incluida la auditoría.
Préstamo y mantenimiento leen el equipo con `buscarPorIdParaActualizar` (bloqueo de fila).

## Estados del equipo
`EquipoService` crea equipos como DISPONIBLE y `EquipoRepository.editar` no modifica el
estado. Los cambios ocurren solo en préstamo (PRESTADO), devolución (DISPONIBLE/DANADO),
mantenimiento (MANTENIMIENTO → DISPONIBLE/BAJA) y baja.

## Base de datos
| Script | Uso |
|---|---|
| `01_sigelab_schema.sql` | Base, tablas, PK/FK/UNIQUE/CHECK y catálogos. Una sola vez. |
| `02_datos_prueba.sql` | Usuarios, laboratorios y equipos para CP-01 a CP-18. |
| `03_facial.sql` | `PerfilFacial` y `AuditoriaFacial` (repetible). |
| `04_comprobar.sql` | Resumen de tablas, equipos y rostros registrados. |

## Preguntas típicas de la defensa
- *¿Dónde se impide prestar un equipo prestado?* `PrestamoService.confirmarPrestamo`, al validar `EstadoEquipo.DISPONIBLE`.
- *¿Cómo se comunica Java con el reconocimiento facial?* `ClienteFacial` usa `java.net.http.HttpClient` contra la API local, con token.
- *¿Cómo se identifica un préstamo vencido?* `ReporteRepository`: ACTIVO, fecha esperada menor a hoy y algún detalle PENDIENTE.
