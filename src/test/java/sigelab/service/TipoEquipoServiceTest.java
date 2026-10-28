
package sigelab.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TipoEquipoServiceTest {

    private final TipoEquipoService service =
            new TipoEquipoService();

    @Test
    void nombreVacioDebeMostrarError() {
        String resultado = service.validarTipoEquipo(
                "", "Computadora portátil"
        );

        assertEquals(
                "Debe ingresar el nombre del tipo de equipo.",
                resultado
        );
    }

    @Test
    void descripcionVaciaDebeMostrarError() {
        String resultado = service.validarTipoEquipo(
                "Laptop", ""
        );

        assertEquals(
                "Debe ingresar una descripción.",
                resultado
        );
    }

    @Test
    void datosCorrectosDebenSerValidos() {
        String resultado = service.validarTipoEquipo(
                "Laptop", "Computadora portátil"
        );

        assertNull(resultado);
    }
}
