
package sigelab.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LaboratorioServiceTest {

    private final LaboratorioService service =
            new LaboratorioService();

    @Test
    void codigoVacioDebeMostrarError() {
        String resultado = service.validarLaboratorio(
                "", "Laboratorio A", "Edificio 1", 30
        );

        assertEquals(
                "Debe ingresar el código del laboratorio.",
                resultado
        );
    }

    @Test
    void nombreVacioDebeMostrarError() {
        String resultado = service.validarLaboratorio(
                "LAB-001", "", "Edificio 1", 30
        );

        assertEquals(
                "Debe ingresar el nombre del laboratorio.",
                resultado
        );
    }

    @Test
    void capacidadInvalidaDebeMostrarError() {
        String resultado = service.validarLaboratorio(
                "LAB-001", "Laboratorio A", "Edificio 1", 0
        );

        assertEquals(
                "La capacidad debe ser mayor que cero.",
                resultado
        );
    }

    @Test
    void datosCorrectosDebenSerValidos() {
        String resultado = service.validarLaboratorio(
                "LAB-001", "Laboratorio A", "Edificio 1", 30
        );

        assertNull(resultado);
    }
}
