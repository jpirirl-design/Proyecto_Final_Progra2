
package sigelab.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServiceTest {

    private final UsuarioService service = new UsuarioService();

    @Test
    void nombreVacioDebeMostrarError() {
        String resultado = service.validarUsuario(
                "", "Perez", "jperez",
                "jperez@correo.com", "12345678".toCharArray(), "ADMIN"
        );

        assertEquals("Debe ingresar el nombre.", resultado);
    }

    @Test
    void apellidoVacioDebeMostrarError() {
        String resultado = service.validarUsuario(
                "Juan", "", "jperez",
                "jperez@correo.com", "12345678".toCharArray(), "ADMIN"
        );

        assertEquals("Debe ingresar el apellido.", resultado);
    }

    @Test
    void usuarioVacioDebeMostrarError() {
        String resultado = service.validarUsuario(
                "Juan", "Perez", "",
                "jperez@correo.com", "12345678".toCharArray(), "ADMIN"
        );

        assertEquals("Debe ingresar el nombre de usuario.", resultado);
    }

    @Test
    void correoInvalidoDebeMostrarError() {
        String resultado = service.validarUsuario(
                "Juan", "Perez", "jperez",
                "correo-invalido", "12345678".toCharArray(), "ADMIN"
        );

        assertEquals("Debe ingresar un correo electrónico válido.", resultado);
    }

    @Test
    void passwordCortaDebeMostrarError() {
        String resultado = service.validarUsuario(
                "Juan", "Perez", "jperez",
                "jperez@correo.com", "123".toCharArray(), "ADMIN"
        );

        assertEquals(
                "La contraseña debe tener al menos 8 caracteres.",
                resultado
        );
    }

    @Test
    void rolNoSeleccionadoDebeMostrarError() {
        String resultado = service.validarUsuario(
                "Juan", "Perez", "jperez",
                "jperez@correo.com",
                "12345678".toCharArray(),
                "Seleccione rol"
        );

        assertEquals("Debe seleccionar un rol.", resultado);
    }

    @Test
    void datosCorrectosDebenSerValidos() {
        String resultado = service.validarUsuario(
                "Juan", "Perez", "jperez",
                "jperez@correo.com",
                "12345678".toCharArray(),
                "ADMIN"
        );

        assertNull(resultado);
    }
}
