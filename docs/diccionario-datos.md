# SIGELAB – Diccionario de datos

## Rol
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_rol | INT IDENTITY | No | PK | Identificador |
| nombre | NVARCHAR(20) | No | UNIQUE, CHECK (ADMIN, ENCARGADO, DOCENTE) | Nombre del rol |
| descripcion | NVARCHAR(150) | Sí | | Descripción |

## Usuario
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_usuario | INT IDENTITY | No | PK | Identificador |
| nombre | NVARCHAR(80) | No | | Nombre |
| apellido | NVARCHAR(80) | No | | Apellido |
| nombre_usuario | NVARCHAR(50) | No | UNIQUE | Usuario de login |
| contrasena_hash | NVARCHAR(255) | No | | Contraseña cifrada (hash) |
| correo | NVARCHAR(120) | No | UNIQUE | Correo |
| id_rol | INT | No | FK → Rol | Rol asignado |
| estado | NVARCHAR(10) | No | CHECK (ACTIVO, INACTIVO), DEFAULT ACTIVO | RN-01 |

## Laboratorio
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_laboratorio | INT IDENTITY | No | PK | Identificador |
| codigo | NVARCHAR(20) | No | UNIQUE | Código del laboratorio (RN-03) |
| nombre | NVARCHAR(100) | No | | Nombre |
| ubicacion | NVARCHAR(150) | No | | Ubicación |
| capacidad | INT | No | CHECK > 0 | Capacidad |
| estado | NVARCHAR(10) | No | CHECK (ACTIVO, INACTIVO) | Estado |

## TipoEquipo
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_tipo_equipo | INT IDENTITY | No | PK | Identificador |
| nombre | NVARCHAR(60) | No | UNIQUE | Laptop, Cámara, Proyector… |
| descripcion | NVARCHAR(150) | Sí | | Descripción |

## Equipo
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_equipo | INT IDENTITY | No | PK | Identificador |
| codigo_inventario | NVARCHAR(30) | No | UNIQUE | Código de inventario; valor del QR (RN-04) |
| nombre | NVARCHAR(100) | No | | Nombre |
| id_tipo_equipo | INT | No | FK → TipoEquipo | Tipo |
| marca | NVARCHAR(60) | No | | Marca |
| modelo | NVARCHAR(60) | No | | Modelo |
| numero_serie | NVARCHAR(60) | No | UNIQUE | Número de serie |
| id_laboratorio | INT | No | FK → Laboratorio | Laboratorio |
| estado | NVARCHAR(15) | No | CHECK (DISPONIBLE, PRESTADO, DANADO, MANTENIMIENTO, BAJA) | Estado actual |
| observaciones | NVARCHAR(300) | Sí | | Observaciones |

## Prestamo
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_prestamo | INT IDENTITY | No | PK | Identificador |
| solicitante | NVARCHAR(150) | No | | Quien recibe los equipos |
| fecha_prestamo | DATETIME2 | No | DEFAULT SYSDATETIME() | Fecha/hora del préstamo |
| fecha_esperada_devolucion | DATE | No | CHECK ≥ fecha_prestamo | Fecha límite (RN-17) |
| id_usuario_registra | INT | No | FK → Usuario | Usuario que registra |
| estado | NVARCHAR(10) | No | CHECK (ACTIVO, FINALIZADO) | Vencido se calcula (RN-17) |
| observaciones | NVARCHAR(300) | Sí | | Observaciones |

## DetallePrestamo
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_detalle | INT IDENTITY | No | PK | Identificador |
| id_prestamo | INT | No | FK → Prestamo | Encabezado |
| id_equipo | INT | No | FK → Equipo | Equipo prestado |
| estado | NVARCHAR(10) | No | CHECK (PENDIENTE, DEVUELTO) | Estado del ítem |
| — | | | UNIQUE (id_prestamo, id_equipo) | RN-06 |

## Devolucion
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_devolucion | INT IDENTITY | No | PK | Identificador |
| id_detalle | INT | No | FK → DetallePrestamo, UNIQUE | Un ítem solo se devuelve una vez (RN-11) |
| fecha_devolucion | DATETIME2 | No | DEFAULT SYSDATETIME() | Fecha/hora |
| condicion | NVARCHAR(10) | No | CHECK (BUENO, DANADO) | RN-12 / RN-13 |
| observacion | NVARCHAR(300) | Sí | | Observación |
| id_usuario_registra | INT | No | FK → Usuario | Usuario que registra |

## Mantenimiento
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_mantenimiento | INT IDENTITY | No | PK | Identificador |
| id_equipo | INT | No | FK → Equipo | Equipo |
| fecha_ingreso | DATE | No | | Fecha de ingreso |
| tipo | NVARCHAR(12) | No | CHECK (PREVENTIVO, CORRECTIVO) | Tipo |
| descripcion | NVARCHAR(300) | No | | Descripción |
| responsable | NVARCHAR(150) | No | | Responsable |
| fecha_salida | DATE | Sí | CHECK ≥ fecha_ingreso | Fecha de salida |
| resultado | NVARCHAR(15) | Sí | CHECK (REPARADO, NO_REPARABLE) | RN-15 |
| estado | NVARCHAR(12) | No | CHECK (EN_PROCESO, FINALIZADO) | Al finalizar exige fecha_salida y resultado |
| observaciones | NVARCHAR(300) | Sí | | Observaciones |

## Auditoria
| Campo | Tipo | Nulo | Restricción | Descripción |
|---|---|---|---|---|
| id_auditoria | BIGINT IDENTITY | No | PK | Identificador |
| id_usuario | INT | Sí | FK → Usuario | Nulo en login fallido de usuario inexistente |
| fecha_hora | DATETIME2 | No | DEFAULT SYSDATETIME() | Momento |
| accion | NVARCHAR(40) | No | CHECK (acciones de la guía 6.10) | Acción |
| entidad | NVARCHAR(40) | No | | Tabla afectada |
| id_registro | INT | Sí | | ID del registro afectado |
| descripcion | NVARCHAR(400) | Sí | | Detalle (RN-18) |
