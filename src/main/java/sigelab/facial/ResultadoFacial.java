package sigelab.facial;

/** Respuesta de /face/verify. score puede ser null si no se llegó a comparar. */
public record ResultadoFacial(boolean coincide, boolean livenessAprobado, Double score, String mensaje) {
}
