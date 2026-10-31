package sigelab.model;

import sigelab.enums.EstadoEquipo;

import java.time.LocalDate;

/** Parámetros opcionales de los reportes. Cada reporte usa solo los que necesita. */
public record FiltroReporte(EstadoEquipo estado, LocalDate desde, LocalDate hasta, String codigoEquipo) {
}
