/* =========================================================
   SIGELAB - Script inicial (Etapa 1)
   Microsoft SQL Server
   ========================================================= */
IF DB_ID('SIGELAB') IS NULL
    CREATE DATABASE SIGELAB;
GO
USE SIGELAB;
GO

/* ---------- ROL ---------- */
CREATE TABLE Rol (
    id_rol       INT IDENTITY(1,1) CONSTRAINT PK_Rol PRIMARY KEY,
    nombre       NVARCHAR(20)  NOT NULL CONSTRAINT UQ_Rol_nombre UNIQUE,
    descripcion  NVARCHAR(150) NULL,
    CONSTRAINT CK_Rol_nombre CHECK (nombre IN ('ADMIN','ENCARGADO','DOCENTE'))
);

/* ---------- USUARIO ---------- */
CREATE TABLE Usuario (
    id_usuario      INT IDENTITY(1,1) CONSTRAINT PK_Usuario PRIMARY KEY,
    nombre          NVARCHAR(80)  NOT NULL,
    apellido        NVARCHAR(80)  NOT NULL,
    nombre_usuario  NVARCHAR(50)  NOT NULL CONSTRAINT UQ_Usuario_nombre_usuario UNIQUE,
    contrasena_hash NVARCHAR(255) NOT NULL,
    correo          NVARCHAR(120) NOT NULL CONSTRAINT UQ_Usuario_correo UNIQUE,
    id_rol          INT          NOT NULL,
    estado          NVARCHAR(10)  NOT NULL CONSTRAINT DF_Usuario_estado DEFAULT 'ACTIVO',
    CONSTRAINT FK_Usuario_Rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol),
    CONSTRAINT CK_Usuario_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

/* ---------- LABORATORIO ---------- */
CREATE TABLE Laboratorio (
    id_laboratorio INT IDENTITY(1,1) CONSTRAINT PK_Laboratorio PRIMARY KEY,
    codigo         NVARCHAR(20)  NOT NULL CONSTRAINT UQ_Laboratorio_codigo UNIQUE,   -- RN-03
    nombre         NVARCHAR(100) NOT NULL,
    ubicacion      NVARCHAR(150) NOT NULL,
    capacidad      INT          NOT NULL,
    estado         NVARCHAR(10)  NOT NULL CONSTRAINT DF_Laboratorio_estado DEFAULT 'ACTIVO',
    CONSTRAINT CK_Laboratorio_capacidad CHECK (capacidad > 0),
    CONSTRAINT CK_Laboratorio_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

/* ---------- TIPO_EQUIPO ---------- */
CREATE TABLE TipoEquipo (
    id_tipo_equipo INT IDENTITY(1,1) CONSTRAINT PK_TipoEquipo PRIMARY KEY,
    nombre         NVARCHAR(60)  NOT NULL CONSTRAINT UQ_TipoEquipo_nombre UNIQUE,
    descripcion    NVARCHAR(150) NULL
);

/* ---------- EQUIPO ---------- */
CREATE TABLE Equipo (
    id_equipo         INT IDENTITY(1,1) CONSTRAINT PK_Equipo PRIMARY KEY,
    codigo_inventario NVARCHAR(30)  NOT NULL CONSTRAINT UQ_Equipo_codigo UNIQUE,    -- RN-04 / valor del QR
    nombre            NVARCHAR(100) NOT NULL,
    id_tipo_equipo    INT          NOT NULL,
    marca             NVARCHAR(60)  NOT NULL,
    modelo            NVARCHAR(60)  NOT NULL,
    numero_serie      NVARCHAR(60)  NOT NULL CONSTRAINT UQ_Equipo_serie UNIQUE,
    id_laboratorio    INT          NOT NULL,
    estado            NVARCHAR(15)  NOT NULL CONSTRAINT DF_Equipo_estado DEFAULT 'DISPONIBLE',
    observaciones     NVARCHAR(300) NULL,
    CONSTRAINT FK_Equipo_TipoEquipo  FOREIGN KEY (id_tipo_equipo) REFERENCES TipoEquipo(id_tipo_equipo),
    CONSTRAINT FK_Equipo_Laboratorio FOREIGN KEY (id_laboratorio) REFERENCES Laboratorio(id_laboratorio),
    CONSTRAINT CK_Equipo_estado CHECK (estado IN ('DISPONIBLE','PRESTADO','DANADO','MANTENIMIENTO','BAJA'))
);

/* ---------- PRESTAMO (encabezado) ---------- */
CREATE TABLE Prestamo (
    id_prestamo               INT IDENTITY(1,1) CONSTRAINT PK_Prestamo PRIMARY KEY,
    solicitante               NVARCHAR(150) NOT NULL,
    fecha_prestamo            DATETIME2    NOT NULL CONSTRAINT DF_Prestamo_fecha DEFAULT SYSDATETIME(),
    fecha_esperada_devolucion DATE         NOT NULL,
    id_usuario_registra       INT          NOT NULL,
    estado                    NVARCHAR(10)  NOT NULL CONSTRAINT DF_Prestamo_estado DEFAULT 'ACTIVO',
    observaciones             NVARCHAR(300) NULL,
    CONSTRAINT FK_Prestamo_Usuario FOREIGN KEY (id_usuario_registra) REFERENCES Usuario(id_usuario),
    CONSTRAINT CK_Prestamo_estado CHECK (estado IN ('ACTIVO','FINALIZADO')),
    CONSTRAINT CK_Prestamo_fechas CHECK (fecha_esperada_devolucion >= CAST(fecha_prestamo AS DATE))
);

/* ---------- DETALLE_PRESTAMO ---------- */
CREATE TABLE DetallePrestamo (
    id_detalle  INT IDENTITY(1,1) CONSTRAINT PK_DetallePrestamo PRIMARY KEY,
    id_prestamo INT         NOT NULL,
    id_equipo   INT         NOT NULL,
    estado      NVARCHAR(10) NOT NULL CONSTRAINT DF_Detalle_estado DEFAULT 'PENDIENTE',
    CONSTRAINT FK_Detalle_Prestamo FOREIGN KEY (id_prestamo) REFERENCES Prestamo(id_prestamo),
    CONSTRAINT FK_Detalle_Equipo   FOREIGN KEY (id_equipo)   REFERENCES Equipo(id_equipo),
    CONSTRAINT UQ_Detalle_prestamo_equipo UNIQUE (id_prestamo, id_equipo),                -- RN-06
    CONSTRAINT CK_Detalle_estado CHECK (estado IN ('PENDIENTE','DEVUELTO'))
);

/* ---------- DEVOLUCION ---------- */
CREATE TABLE Devolucion (
    id_devolucion       INT IDENTITY(1,1) CONSTRAINT PK_Devolucion PRIMARY KEY,
    id_detalle          INT          NOT NULL CONSTRAINT UQ_Devolucion_detalle UNIQUE,    -- RN-11
    fecha_devolucion    DATETIME2    NOT NULL CONSTRAINT DF_Devolucion_fecha DEFAULT SYSDATETIME(),
    condicion           NVARCHAR(10)  NOT NULL,
    observacion         NVARCHAR(300) NULL,
    id_usuario_registra INT          NOT NULL,
    CONSTRAINT FK_Devolucion_Detalle FOREIGN KEY (id_detalle) REFERENCES DetallePrestamo(id_detalle),
    CONSTRAINT FK_Devolucion_Usuario FOREIGN KEY (id_usuario_registra) REFERENCES Usuario(id_usuario),
    CONSTRAINT CK_Devolucion_condicion CHECK (condicion IN ('BUENO','DANADO'))
);

/* ---------- MANTENIMIENTO ---------- */
CREATE TABLE Mantenimiento (
    id_mantenimiento INT IDENTITY(1,1) CONSTRAINT PK_Mantenimiento PRIMARY KEY,
    id_equipo        INT          NOT NULL,
    fecha_ingreso    DATE         NOT NULL CONSTRAINT DF_Mant_fecha DEFAULT CAST(GETDATE() AS DATE),
    tipo             NVARCHAR(12)  NOT NULL,
    descripcion      NVARCHAR(300) NOT NULL,
    responsable      NVARCHAR(150) NOT NULL,
    fecha_salida     DATE         NULL,
    resultado        NVARCHAR(15)  NULL,
    estado           NVARCHAR(12)  NOT NULL CONSTRAINT DF_Mant_estado DEFAULT 'EN_PROCESO',
    observaciones    NVARCHAR(300) NULL,
    CONSTRAINT FK_Mantenimiento_Equipo FOREIGN KEY (id_equipo) REFERENCES Equipo(id_equipo),
    CONSTRAINT CK_Mant_tipo CHECK (tipo IN ('PREVENTIVO','CORRECTIVO')),
    CONSTRAINT CK_Mant_estado CHECK (estado IN ('EN_PROCESO','FINALIZADO')),
    CONSTRAINT CK_Mant_resultado CHECK (resultado IS NULL OR resultado IN ('REPARADO','NO_REPARABLE')),
    CONSTRAINT CK_Mant_fechas CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso),
    CONSTRAINT CK_Mant_cierre CHECK (
        (estado = 'EN_PROCESO' AND fecha_salida IS NULL AND resultado IS NULL) OR
        (estado = 'FINALIZADO' AND fecha_salida IS NOT NULL AND resultado IS NOT NULL))
);

/* ---------- AUDITORIA ---------- */
CREATE TABLE Auditoria (
    id_auditoria BIGINT IDENTITY(1,1) CONSTRAINT PK_Auditoria PRIMARY KEY,
    id_usuario   INT          NULL,   -- NULL permitido para LOGIN_FALLIDO con usuario inexistente
    fecha_hora   DATETIME2    NOT NULL CONSTRAINT DF_Auditoria_fecha DEFAULT SYSDATETIME(),
    accion       NVARCHAR(40)  NOT NULL,
    entidad      NVARCHAR(40)  NOT NULL,
    id_registro  INT          NULL,
    descripcion  NVARCHAR(400) NULL,
    CONSTRAINT FK_Auditoria_Usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    CONSTRAINT CK_Auditoria_accion CHECK (accion IN (
        'LOGIN_EXITOSO','LOGIN_FALLIDO','PRESTAMO_CREADO','EQUIPO_DEVUELTO',
        'EQUIPO_ENVIADO_MANTENIMIENTO','MANTENIMIENTO_FINALIZADO','USUARIO_DESACTIVADO'))
);
GO

/* ---------- Catálogos iniciales ---------- */
INSERT INTO Rol (nombre, descripcion) VALUES
 ('ADMIN','Acceso completo'),
 ('ENCARGADO',N'Operación cotidiana'),
 ('DOCENTE','Consultas / solicitante');

INSERT INTO TipoEquipo (nombre) VALUES
 ('Laptop'),(N'Cámara'),('Proyector'),('Router'),('Switch'),('Tablet'),('Monitor'),(N'Kit electrónico');
GO
