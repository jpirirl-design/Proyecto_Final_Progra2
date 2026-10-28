# SIGELAB – Modelo Entidad-Relación

```mermaid
erDiagram
    Rol ||--o{ Usuario : "clasifica"
    Laboratorio ||--o{ Equipo : "contiene"
    TipoEquipo ||--o{ Equipo : "categoriza"
    Usuario ||--o{ Prestamo : "registra"
    Prestamo ||--|{ DetallePrestamo : "incluye"
    Equipo ||--o{ DetallePrestamo : "aparece en"
    DetallePrestamo ||--o| Devolucion : "se devuelve en"
    Usuario ||--o{ Devolucion : "registra"
    Equipo ||--o{ Mantenimiento : "recibe"
    Usuario |o--o{ Auditoria : "genera"

    Rol { int id_rol PK
          nvarchar nombre UK }
    Usuario { int id_usuario PK
              nvarchar nombre_usuario UK
              nvarchar correo UK
              int id_rol FK
              nvarchar estado }
    Laboratorio { int id_laboratorio PK
                  nvarchar codigo UK
                  int capacidad
                  nvarchar estado }
    TipoEquipo { int id_tipo_equipo PK
                 nvarchar nombre UK }
    Equipo { int id_equipo PK
             nvarchar codigo_inventario UK
             nvarchar numero_serie UK
             int id_tipo_equipo FK
             int id_laboratorio FK
             nvarchar estado }
    Prestamo { int id_prestamo PK
               nvarchar solicitante
               datetime2 fecha_prestamo
               date fecha_esperada_devolucion
               int id_usuario_registra FK
               nvarchar estado }
    DetallePrestamo { int id_detalle PK
                      int id_prestamo FK
                      int id_equipo FK
                      nvarchar estado }
    Devolucion { int id_devolucion PK
                 int id_detalle FK
                 nvarchar condicion
                 int id_usuario_registra FK }
    Mantenimiento { int id_mantenimiento PK
                    int id_equipo FK
                    nvarchar tipo
                    nvarchar resultado
                    nvarchar estado }
    Auditoria { bigint id_auditoria PK
                int id_usuario FK
                datetime2 fecha_hora
                nvarchar accion }
```

**Cardinalidades clave**
- Laboratorio 1 : N Equipo · TipoEquipo 1 : N Equipo
- Prestamo 1 : N DetallePrestamo (mínimo 1, RN-05) · Equipo 1 : N DetallePrestamo
- DetallePrestamo 1 : 0..1 Devolucion (UNIQUE en `id_detalle`, RN-11)
- Equipo 1 : N Mantenimiento
