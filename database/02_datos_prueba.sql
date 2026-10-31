/* =========================================================
   SIGELAB - Datos de prueba
   Ejecutar DESPUÉS de 01_sigelab_schema.sql
   Contraseñas:  admin / Admin123*
                 encargado / Encargado123*
                 docente / Docente123*
   ========================================================= */
USE SIGELAB;
GO

/* ---------- Usuarios (hash PBKDF2 compatible con LoginService) ---------- */
INSERT INTO Usuario (nombre, apellido, nombre_usuario, contrasena_hash, correo, id_rol, estado) VALUES
 ('Admin', 'SIGELAB', 'admin',
  'pbkdf2_sha256$600000$GWJf9tM6tHu8usnrbRKRGw==$AyP/eTb3lV+L5PpMRX8oFvqM6G1McquCGouuUtI/pnc=',
  'admin@sigelab.local', (SELECT id_rol FROM Rol WHERE nombre = 'ADMIN'), 'ACTIVO'),
 ('Encargado', 'Laboratorio', 'encargado',
  'pbkdf2_sha256$600000$4WnD5NS8L7f08vw0vTEyiA==$+zgFOlFsv9woRKEN7z6CW11nzgO055CxzAl3dSViB0k=',
  'encargado@sigelab.local', (SELECT id_rol FROM Rol WHERE nombre = 'ENCARGADO'), 'ACTIVO'),
 ('Docente', 'Prueba', 'docente',
  'pbkdf2_sha256$600000$yaJXDpMBsccUKcGrRJRwSQ==$xr0rmty9jCyEuwEgPqG1M5EVSdG6pl2f6L+S3o8lMW4=',
  'docente@sigelab.local', (SELECT id_rol FROM Rol WHERE nombre = 'DOCENTE'), 'ACTIVO');

/* ---------- Laboratorios ---------- */
INSERT INTO Laboratorio (codigo, nombre, ubicacion, capacidad, estado) VALUES
 ('LAB-01', N'Laboratorio de Redes',      N'Edificio A, nivel 2', 30, 'ACTIVO'),
 ('LAB-02', N'Laboratorio de Multimedia', N'Edificio B, nivel 1', 25, 'ACTIVO');

/* ---------- Equipos (uno en cada estado para los casos de prueba) ---------- */
DECLARE @lab1 INT = (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01');
DECLARE @lab2 INT = (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-02');

INSERT INTO Equipo (codigo_inventario, nombre, id_tipo_equipo, marca, modelo, numero_serie, id_laboratorio, estado, observaciones) VALUES
 ('CAM-001', N'Cámara Canon 1',  (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Cámara'),    'Canon',  'EOS R50',   'SN-CAM-001', @lab2, 'DISPONIBLE',    NULL),
 ('CAM-002', N'Cámara Canon 2',  (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Cámara'),    'Canon',  'EOS R50',   'SN-CAM-002', @lab2, 'DISPONIBLE',    NULL),
 ('LAP-001', N'Laptop Dell 1',   (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Laptop'),     'Dell',   'Latitude 5440', 'SN-LAP-001', @lab1, 'DISPONIBLE', NULL),
 ('LAP-002', N'Laptop Dell 2',   (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Laptop'),     'Dell',   'Latitude 5440', 'SN-LAP-002', @lab1, 'DISPONIBLE', NULL),
 ('KIT-001', N'Kit Arduino',     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Kit electrónico'), 'Arduino', 'Uno R4', 'SN-KIT-001', @lab1, 'DISPONIBLE', NULL),
 ('PRY-001', N'Proyector Epson', (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Proyector'),  'Epson',  'X49',       'SN-PRY-001', @lab2, 'DANADO',        N'Lámpara fundida'),
 ('RTR-001', N'Router Cisco',    (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Router'),     'Cisco',  'ISR 1111',  'SN-RTR-001', @lab1, 'MANTENIMIENTO', NULL),
 ('MON-001', N'Monitor LG',      (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Monitor'),    'LG',     '24MK430',   'SN-MON-001', @lab1, 'BAJA',          N'Pantalla rota'),
 ('TAB-001', N'Tablet Samsung',  (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = 'Tablet'),     'Samsung','Tab A9',    'SN-TAB-001', @lab2, 'PRESTADO',      NULL);

/* ---------- Mantenimiento en proceso de RTR-001 ---------- */
INSERT INTO Mantenimiento (id_equipo, fecha_ingreso, tipo, descripcion, responsable, estado)
VALUES ((SELECT id_equipo FROM Equipo WHERE codigo_inventario = 'RTR-001'),
        CAST(DATEADD(DAY, -2, GETDATE()) AS DATE), 'CORRECTIVO',
        N'No enciende el puerto WAN', N'Técnico de laboratorio', 'EN_PROCESO');

/* ---------- Préstamo VENCIDO (CP-18): TAB-001 debía volver hace 3 días ---------- */
DECLARE @admin INT = (SELECT id_usuario FROM Usuario WHERE nombre_usuario = 'admin');

INSERT INTO Prestamo (solicitante, fecha_prestamo, fecha_esperada_devolucion, id_usuario_registra, estado, observaciones)
VALUES (N'Juan Pérez', DATEADD(DAY, -10, SYSDATETIME()), CAST(DATEADD(DAY, -3, GETDATE()) AS DATE),
        @admin, 'ACTIVO', N'Dato de prueba: préstamo vencido');

INSERT INTO DetallePrestamo (id_prestamo, id_equipo, estado)
VALUES (SCOPE_IDENTITY(), (SELECT id_equipo FROM Equipo WHERE codigo_inventario = 'TAB-001'), 'PENDIENTE');
GO
