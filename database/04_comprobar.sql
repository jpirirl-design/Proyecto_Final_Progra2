/* SIGELAB - Comprobación rápida después de ejecutar los scripts 01 a 03 */
USE SIGELAB;
GO
SELECT t.name AS Tabla FROM sys.tables t WHERE t.schema_id = SCHEMA_ID('dbo') ORDER BY t.name;

SELECT e.codigo_inventario, e.nombre, t.nombre AS tipo, l.codigo AS laboratorio, e.estado
FROM dbo.Equipo e
INNER JOIN dbo.TipoEquipo  t ON t.id_tipo_equipo = e.id_tipo_equipo
INNER JOIN dbo.Laboratorio l ON l.id_laboratorio = e.id_laboratorio
ORDER BY e.codigo_inventario;

SELECT estado, COUNT(*) AS cantidad FROM dbo.Equipo GROUP BY estado;

SELECT u.nombre_usuario, r.nombre AS rol, u.estado,
       CASE WHEN p.id_perfil_facial IS NULL THEN 'NO' ELSE 'SI' END AS rostro_registrado
FROM dbo.Usuario u
INNER JOIN dbo.Rol r ON r.id_rol = u.id_rol
LEFT JOIN dbo.PerfilFacial p ON p.id_usuario = u.id_usuario AND p.activo = 1
ORDER BY u.id_usuario;
GO
