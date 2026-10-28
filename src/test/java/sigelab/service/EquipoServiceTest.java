
package sigelab.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EquipoServiceTest {

    private final EquipoService service = new EquipoService();

    @Test
    void codigoVacioDebeMostrarError() {
        String resultado = service.validarEquipo(
                "", "Laptop Dell", "Laptop",
                "Laboratorio A", "Dell", "Latitude 5420", "SN123"
        );

        assertEquals("Debe ingresar el código de inventario.", resultado);
    }

    @Test
    void nombreVacioDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "", "Laptop",
                "Laboratorio A", "Dell", "Latitude 5420", "SN123"
        );

        assertEquals("Debe ingresar el nombre del equipo.", resultado);
    }

    @Test
    void tipoNoSeleccionadoDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Seleccione tipo",
                "Laboratorio A", "Dell", "Latitude 5420", "SN123"
        );

        assertEquals("Debe seleccionar un tipo de equipo.", resultado);
    }

    @Test
    void laboratorioNoSeleccionadoDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Laptop",
                "Seleccione laboratorio", "Dell", "Latitude 5420", "SN123"
        );

        assertEquals("Debe seleccionar un laboratorio.", resultado);
    }

    @Test
    void marcaVaciaDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Laptop",
                "Laboratorio A", "", "Latitude 5420", "SN123"
        );

        assertEquals("Debe ingresar la marca del equipo.", resultado);
    }

    @Test
    void modeloVacioDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Laptop",
                "Laboratorio A", "Dell", "", "SN123"
        );

        assertEquals("Debe ingresar el modelo del equipo.", resultado);
    }

    @Test
    void serieVaciaDebeMostrarError() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Laptop",
                "Laboratorio A", "Dell", "Latitude 5420", ""
        );

        assertEquals("Debe ingresar el número de serie.", resultado);
    }

    @Test
    void datosCorrectosDebenSerValidos() {
        String resultado = service.validarEquipo(
                "EQ-001", "Laptop Dell", "Laptop",
                "Laboratorio A", "Dell", "Latitude 5420", "SN123"
        );

        assertNull(resultado);
    }
}
