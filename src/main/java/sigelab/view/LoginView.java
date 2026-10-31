
package sigelab.view;

import sigelab.controller.LoginController;
import sigelab.seguridad.SesionActual;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginView extends JFrame {

    private final LoginController controller = new LoginController();

    private final JTextField txtUsuario;
    private final JPasswordField txtPassword;
    private final JButton btnIngresar;

    public LoginView() {
        setTitle("SIGELAB - Inicio de sesión");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 15));
        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        panel.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField();
        panel.add(txtUsuario);

        panel.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        panel.add(new JLabel(""));
        btnIngresar = new JButton("Ingresar");
        panel.add(btnIngresar);

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        btnIngresar.addActionListener(e -> iniciarSesion());
        txtPassword.addActionListener(e -> iniciarSesion());

        setContentPane(panel);
    }

    private void iniciarSesion() {
        char[] password = txtPassword.getPassword();
        btnIngresar.setEnabled(false);

        try {
            Object[] usuario = controller.iniciarSesion(
                    txtUsuario.getText(), password
            );
            SesionActual.iniciarDesdeLogin(usuario);

            String nombre = String.valueOf(usuario[1]);
            String apellido = String.valueOf(usuario[2]);
            String rol = String.valueOf(usuario[4]);

            abrirMenuPrincipal(nombre, apellido, rol);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error de conexión:\n" + ex.getMessage(),
                    "SIGELAB",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {
            Arrays.fill(password, '\0');
            txtPassword.setText("");
            btnIngresar.setEnabled(true);
        }
    }

    private void abrirMenuPrincipal(
            String nombre,
            String apellido,
            String rol
    ) {
        JFrame menu = new JFrame("SIGELAB - Menú principal");
        menu.setSize(600, 450);
        menu.setLocationRelativeTo(null);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel bienvenida = new JLabel(
                "Bienvenido, " + nombre + " " + apellido
                        + " | Rol: " + rol,
                SwingConstants.CENTER
        );
        principal.add(bienvenida, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(0, 2, 12, 12));

        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        boolean esEncargado = "ENCARGADO".equalsIgnoreCase(rol);
        boolean esDocente = "DOCENTE".equalsIgnoreCase(rol);

        if (esAdmin || esEncargado) {
            JButton btnLaboratorios = new JButton("Laboratorios");
            JButton btnTipos = new JButton("Tipos de equipo");
            JButton btnEquipos = new JButton("Equipos");
            JButton btnPrestamos = new JButton("Préstamos");
            JButton btnDevoluciones = new JButton("Devoluciones");
            JButton btnMantenimiento = new JButton("Mantenimiento");

            btnPrestamos.addActionListener(
                    e -> new PrestamoView().setVisible(true)
            );
            btnDevoluciones.addActionListener(
                    e -> new DevolucionView().setVisible(true)
            );
            btnMantenimiento.addActionListener(
                    e -> new MantenimientoView().setVisible(true)
            );

            botones.add(btnPrestamos);
            botones.add(btnDevoluciones);
            botones.add(btnMantenimiento);

            btnLaboratorios.addActionListener(
                    e -> new LaboratorioView().setVisible(true)
            );
            btnTipos.addActionListener(
                    e -> new TipoEquipoView().setVisible(true)
            );
            btnEquipos.addActionListener(
                    e -> new EquipoView().setVisible(true)
            );

            botones.add(btnLaboratorios);
            botones.add(btnTipos);
            botones.add(btnEquipos);
        }

        if (esAdmin) {
            JButton btnUsuarios = new JButton("Usuarios");
            btnUsuarios.addActionListener(
                    e -> new UsuarioView().setVisible(true)
            );
            botones.add(btnUsuarios);
        }

        if (esDocente) {
            principal.add(
                    new JLabel(
                            "Acceso docente habilitado.",
                            SwingConstants.CENTER
                    ),
                    BorderLayout.CENTER
            );
        } else {
            principal.add(botones, BorderLayout.CENTER);
        }

        JButton btnSalir = new JButton("Cerrar sesión");

        btnSalir.addActionListener(e -> {
            SesionActual.cerrar();
            menu.dispose();
            txtUsuario.setText("");
            txtPassword.setText("");
            setLocationRelativeTo(null);
            setVisible(true);
        });

        principal.add(btnSalir, BorderLayout.SOUTH);
        menu.setContentPane(principal);

        // Ocultar el Login sin destruirlo.
        setVisible(false);
        menu.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}
