# Matriz de casos de prueba – SIGELAB

Preparación: ejecutar los scripts 01 a 03, iniciar `facial-service/start-facial.ps1`
y tener registrado el rostro de `admin`. Completar la columna **Resultado obtenido** al probar.

| Caso | Pasos | Resultado esperado | Dónde se valida | Resultado obtenido |
|---|---|---|---|---|
| CP-01 | Login `admin` / `Admin123*` mirando a la cámara | Acceso permitido, auditoría LOGIN_EXITOSO | `LoginService.iniciarSesion` | |
| CP-02 | Login `admin` / `Admin123*` con otra persona frente a la cámara | Acceso denegado, LOGIN_FALLIDO | `LoginService` + `facial-service` (similitud < 0.55) | |
| CP-03 | Equipos → guardar otro equipo con código `CAM-001` | Rechazado: código duplicado | `EquipoService` + `UQ_Equipo_codigo` | |
| CP-04 | Préstamos → agregar `CAM-001` → confirmar | Préstamo creado, CAM-001 PRESTADO | `PrestamoService.confirmarPrestamo` | |
| CP-05 | Préstamos → agregar `CAM-001` otra vez | Rechazado (PRESTADO) | `PrestamoService` (RN-07/08) | |
| CP-06 | Préstamos → agregar `PRY-001` | Rechazado (DAÑADO) | `PrestamoService` | |
| CP-07 | Préstamos → agregar `RTR-001` | Rechazado (MANTENIMIENTO) | `PrestamoService` (RN-14) | |
| CP-08 | Préstamos → agregar `MON-001` | Rechazado (BAJA) | `PrestamoService` | |
| CP-09 | Préstamos → *Leer QR* con el PNG de `CAM-002` | Equipo identificado y agregado | `QrEquipoService` | |
| CP-10 | Préstamos → *Leer QR* con el PNG de `TAB-001` | Identificado pero bloqueado | `QrEquipoService` (RN-19) | |
| CP-11 | Devoluciones → devolver `CAM-001` como BUENO | CAM-001 DISPONIBLE | `DevolucionService` (RN-12) | |
| CP-12 | Prestar y devolver `CAM-001` como DAÑADO | CAM-001 DAÑADO | `DevolucionService` (RN-13) | |
| CP-13 | Intentar devolver de nuevo `CAM-001` | Rechazado | `DevolucionService` + `UQ_Devolucion_detalle` (RN-11) | |
| CP-14 | Prestar LAP-001, LAP-002, KIT-001 → devolver 2 | Préstamo sigue ACTIVO con 1 pendiente | `DevolucionService` (RN-16) | |
| CP-15 | Mantenimiento → enviar `PRY-001` | PRY-001 MANTENIMIENTO | `MantenimientoService` | |
| CP-16 | Finalizar mantenimiento como REPARADO | Equipo DISPONIBLE | `MantenimientoService` (RN-15) | |
| CP-17 | Finalizar mantenimiento de `RTR-001` como NO_REPARABLE | RTR-001 BAJA | `MantenimientoService` (RN-15) | |
| CP-18 | Reportes → *Préstamos vencidos* | Aparece el préstamo de TAB-001 | `ReporteRepository` (RN-17) | |
