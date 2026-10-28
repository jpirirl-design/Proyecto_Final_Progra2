
package sigelab.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    private final LoginService service = new LoginService();

    @Test
    void usuarioVacioDebeMostrarError() {
        String resultado = service.validarCredenciales(
                "", "12345678".toCharArray()
        );

        assertEquals(
                "Debe ingresar su usuario.",
                resultado
        );
    }

    @Test
    void passwordVaciaDebeMostrarError() {
        String resultado = service.validarCredenciales(
                "admin", new char[0]
        );

        assertEquals(
                "Debe ingresar su contraseña.",
                resultado
        );
    }

    @Test
    void usuarioYPasswordVaciosDebeMostrarError() {
        String resultado = service.validarCredenciales(
                "", new char[0]
        );

        assertEquals(
                "Debe ingresar su usuario.",
                resultado
        );
    }

    @Test
    void camposCompletosDebenSerValidos() {
        String resultado = service.validarCredenciales(
                "admin", "12345678".toCharArray()
        );

        assertNull(resultado);
    }
}
