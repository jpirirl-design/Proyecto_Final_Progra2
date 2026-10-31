package sigelab.repository;

import sigelab.config.ConexionDB;
import sigelab.enums.EstadoEquipo;
import sigelab.exception.DataAccessException;
import sigelab.model.ResultadoConsulta;
import sigelab.model.ResumenDashboard;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Consultas de solo lectura para el dashboard y los reportes. */
public class ReporteRepository {

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** RN-17: vencido = ACTIVO, fecha esperada ya pasó y al menos un detalle PENDIENTE. */
    private static final String CONDICION_VENCIDO = """
            p.estado = 'ACTIVO'
            AND p.fecha_esperada_devolucion < CAST(GETDATE() AS DATE)
            AND EXISTS (SELECT 1 FROM DetallePrestamo dp
                        WHERE dp.id_prestamo = p.id_prestamo AND dp.estado = 'PENDIENTE')
            """;

    private static final String SELECT_PRESTAMOS = """
            SELECT p.id_prestamo               AS [No.],
                   p.solicitante               AS [Solicitante],
                   p.fecha_prestamo            AS [Fecha préstamo],
                   p.fecha_esperada_devolucion AS [Fecha esperada],
                   u.nombre_usuario            AS [Registró],
                   p.estado                    AS [Estado],
                   COUNT(d.id_detalle)         AS [Equipos],
                   SUM(CASE WHEN d.estado = 'PENDIENTE' THEN 1 ELSE 0 END) AS [Pendientes]
            FROM Prestamo p
            INNER JOIN Usuario u ON u.id_usuario = p.id_usuario_registra
            LEFT JOIN DetallePrestamo d ON d.id_prestamo = p.id_prestamo
            """;

    private static final String GROUP_PRESTAMOS = """
            GROUP BY p.id_prestamo, p.solicitante, p.fecha_prestamo,
                     p.fecha_esperada_devolucion, u.nombre_usuario, p.estado
            ORDER BY p.fecha_prestamo DESC
            """;

    // ------------------------------------------------------------ Dashboard

    public ResumenDashboard obtenerResumen() {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM Equipo) AS total,
                  (SELECT COUNT(*) FROM Equipo WHERE estado = 'DISPONIBLE')    AS disponibles,
                  (SELECT COUNT(*) FROM Equipo WHERE estado = 'PRESTADO')      AS prestados,
                  (SELECT COUNT(*) FROM Equipo WHERE estado = 'DANADO')        AS danados,
                  (SELECT COUNT(*) FROM Equipo WHERE estado = 'MANTENIMIENTO') AS mantenimiento,
                  (SELECT COUNT(*) FROM Equipo WHERE estado = 'BAJA')          AS baja,
                  (SELECT COUNT(*) FROM Prestamo WHERE estado = 'ACTIVO')      AS activos,
                  (SELECT COUNT(*) FROM Prestamo p WHERE\s""" + CONDICION_VENCIDO + ") AS vencidos";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new ResumenDashboard(
                    rs.getInt("total"), rs.getInt("disponibles"), rs.getInt("prestados"),
                    rs.getInt("danados"), rs.getInt("mantenimiento"), rs.getInt("baja"),
                    rs.getInt("activos"), rs.getInt("vencidos"));
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo cargar el resumen del dashboard.", e);
        }
    }

    // ------------------------------------------------------------ Equipos

    public ResultadoConsulta resumenEquiposPorEstado() {
        return consultar("""
                SELECT estado AS [Estado], COUNT(*) AS [Cantidad]
                FROM Equipo
                GROUP BY estado
                ORDER BY COUNT(*) DESC
                """);
    }

    /** estado null = todos. */
    public ResultadoConsulta equiposPorEstado(EstadoEquipo estado) {
        String sql = """
                SELECT e.codigo_inventario AS [Código], e.nombre AS [Nombre],
                       t.nombre AS [Tipo], l.nombre AS [Laboratorio],
                       e.marca AS [Marca], e.modelo AS [Modelo], e.estado AS [Estado]
                FROM Equipo e
                INNER JOIN TipoEquipo  t ON t.id_tipo_equipo = e.id_tipo_equipo
                INNER JOIN Laboratorio l ON l.id_laboratorio = e.id_laboratorio
                """ + (estado == null ? "" : "WHERE e.estado = ?\n")
                + "ORDER BY e.estado, e.codigo_inventario";
        return estado == null ? consultar(sql) : consultar(sql, estado.name());
    }

    // ------------------------------------------------------------ Préstamos

    public ResultadoConsulta prestamosPorEstado(String estadoPrestamo) {
        return consultar(SELECT_PRESTAMOS + "WHERE p.estado = ?\n" + GROUP_PRESTAMOS, estadoPrestamo);
    }

    public ResultadoConsulta prestamosVencidos() {
        return consultar(SELECT_PRESTAMOS + "WHERE " + CONDICION_VENCIDO + GROUP_PRESTAMOS);
    }

    public ResultadoConsulta prestamosEntreFechas(LocalDate desde, LocalDate hasta) {
        return consultar(SELECT_PRESTAMOS
                        + "WHERE CAST(p.fecha_prestamo AS DATE) BETWEEN ? AND ?\n" + GROUP_PRESTAMOS,
                Date.valueOf(desde), Date.valueOf(hasta));
    }

    public ResultadoConsulta historialEquipo(String codigo) {
        return consultar("""
                SELECT p.id_prestamo                AS [Préstamo],
                       p.solicitante                AS [Solicitante],
                       p.fecha_prestamo             AS [Fecha préstamo],
                       p.fecha_esperada_devolucion  AS [Fecha esperada],
                       d.estado                     AS [Detalle],
                       dv.fecha_devolucion          AS [Fecha devolución],
                       dv.condicion                 AS [Condición],
                       dv.observacion               AS [Observación]
                FROM Equipo e
                INNER JOIN DetallePrestamo d ON d.id_equipo = e.id_equipo
                INNER JOIN Prestamo p        ON p.id_prestamo = d.id_prestamo
                LEFT JOIN Devolucion dv      ON dv.id_detalle = d.id_detalle
                WHERE e.codigo_inventario = ?
                ORDER BY p.fecha_prestamo DESC
                """, codigo);
    }

    // ------------------------------------------------------------ Mantenimiento

    /** codigo null = todos los equipos. */
    public ResultadoConsulta historialMantenimiento(String codigo) {
        String sql = """
                SELECT m.id_mantenimiento   AS [No.],
                       e.codigo_inventario  AS [Código],
                       e.nombre             AS [Equipo],
                       m.tipo               AS [Tipo],
                       m.fecha_ingreso      AS [Ingreso],
                       m.fecha_salida       AS [Salida],
                       m.responsable        AS [Responsable],
                       m.estado             AS [Estado],
                       m.resultado          AS [Resultado],
                       m.descripcion        AS [Descripción]
                FROM Mantenimiento m
                INNER JOIN Equipo e ON e.id_equipo = m.id_equipo
                """ + (codigo == null ? "" : "WHERE e.codigo_inventario = ?\n")
                + "ORDER BY m.fecha_ingreso DESC, m.id_mantenimiento DESC";
        return codigo == null ? consultar(sql) : consultar(sql, codigo);
    }

    // ------------------------------------------------------------ Auditoría

    public ResultadoConsulta auditoria(LocalDate desde, LocalDate hasta) {
        return consultar("""
                SELECT a.fecha_hora  AS [Fecha/hora],
                       ISNULL(u.nombre_usuario, '(desconocido)') AS [Usuario],
                       a.accion      AS [Acción],
                       a.entidad     AS [Entidad],
                       a.id_registro AS [ID registro],
                       a.descripcion AS [Descripción]
                FROM Auditoria a
                LEFT JOIN Usuario u ON u.id_usuario = a.id_usuario
                WHERE CAST(a.fecha_hora AS DATE) BETWEEN ? AND ?
                ORDER BY a.fecha_hora DESC
                """, Date.valueOf(desde), Date.valueOf(hasta));
    }

    // ------------------------------------------------------------ Ejecutor genérico

    private ResultadoConsulta consultar(String sql, Object... parametros) {
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                List<String> encabezados = new ArrayList<>();
                for (int c = 1; c <= columnas; c++) {
                    encabezados.add(meta.getColumnLabel(c));
                }

                List<Object[]> filas = new ArrayList<>();
                while (rs.next()) {
                    Object[] fila = new Object[columnas];
                    for (int c = 1; c <= columnas; c++) {
                        Object valor = rs.getObject(c);
                        if (valor instanceof Timestamp ts) {
                            valor = ts.toLocalDateTime().format(FORMATO_FECHA_HORA);
                        }
                        fila[c - 1] = valor;
                    }
                    filas.add(fila);
                }
                return new ResultadoConsulta(encabezados, filas);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo generar el reporte.", e);
        }
    }
}
