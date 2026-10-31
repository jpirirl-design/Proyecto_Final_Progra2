package sigelab.model;

import java.util.List;

/** Resultado genérico de un reporte: encabezados de columna y filas, listo para un JTable. */
public record ResultadoConsulta(List<String> columnas, List<Object[]> filas) {
}
