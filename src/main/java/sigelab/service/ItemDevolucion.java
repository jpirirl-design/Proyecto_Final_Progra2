package sigelab.service;

import sigelab.enums.CondicionDevolucion;

/** Un equipo que se devuelve: su detalle, la condición en que regresa y una observación opcional. */
public record ItemDevolucion(int idDetalle, CondicionDevolucion condicion, String observacion) {
}
