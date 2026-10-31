# Manual de usuario – SIGELAB

## 1. Primer acceso
1. Inicie SQL Server, el servicio facial (`facial-service/start-facial.ps1`) y la aplicación.
2. Si la base no tiene usuarios, pulse **Crear administrador inicial** y complete los datos.
   (Si cargó `02_datos_prueba.sql` ya existen `admin`, `encargado` y `docente`.)
3. Escriba usuario y contraseña y pulse **Registrar mi rostro por primera vez**.
   Mire de frente y siga las indicaciones de la ventana de cámara (frente, perfiles y giro).
4. Pulse **Ingresar con contraseña y rostro**. Se verifica de nuevo el rostro.

Si el rostro no coincide, el servicio está apagado o la cámara falla, no se abre el menú.

## 2. Menú principal
Muestra el resumen: total de equipos, disponibles, prestados, dañados, en mantenimiento,
de baja, préstamos activos y vencidos. Se actualiza al volver a la ventana o con **Actualizar**.
Los botones dependen del rol: ADMIN ve todo; ENCARGADO no ve Usuarios ni Auditoría;
DOCENTE solo ve Reportes.

## 3. Catálogos
- **Laboratorios:** crear, editar y desactivar. **Tipos de equipo:** crear, editar y eliminar.
- **Equipos:** el código de inventario es único. Un equipo nuevo queda DISPONIBLE; el estado
  no se edita a mano. **Ver / guardar QR** genera el PNG con el código del equipo.
- **Usuarios (ADMIN):** crear, editar, desactivar y **Registrar rostro** (reemplaza el anterior).

## 4. Préstamos
1. Escriba solicitante, fecha esperada y observaciones.
2. Agregue equipos por código o con **Leer QR desde imagen**. Solo se aceptan DISPONIBLES.
3. **Confirmar préstamo:** todos los equipos pasan a PRESTADO en una sola transacción.

## 5. Devoluciones
Seleccione el préstamo activo, marque los equipos que regresan, elija BUENO o DAÑADO y confirme.
Se puede devolver parcialmente; el préstamo se finaliza cuando no quedan pendientes.

## 6. Mantenimiento
- Enviar equipo DISPONIBLE (preventivo) o DAÑADO (correctivo).
- Finalizar como REPARADO → DISPONIBLE, o NO_REPARABLE → BAJA.

## 7. Reportes
Equipos por estado, préstamos activos/finalizados/vencidos/entre fechas, historial de un
equipo, historial de mantenimiento y auditoría (solo ADMIN).

## 8. Escenario de defensa
CAM-001 DISPONIBLE → prestar → intentar prestar otra vez (rechazado) → devolver DAÑADA →
intentar prestar (rechazado) → mantenimiento → finalizar REPARADO → DISPONIBLE → prestar de nuevo.
