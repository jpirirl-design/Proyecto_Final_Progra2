
package sigelab.view;

import sigelab.controller.LoginController;
import sigelab.seguridad.SesionActual;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LoginView extends JFrame {

    private final LoginController controller = new LoginController();

    private final JTextField txtUsuario;
    private final JPasswordField txtPassword;
    private final JButton btnIngresar;
    private final JButton btnRegistrarRostro;
    private final JButton btnAdminInicial;
    private final JLabel lblEstado;

    public LoginView() {
        setTitle("SIGELAB - Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel campos = new JPanel(new GridLayout(2, 2, 10, 15));
        campos.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField(18);
        campos.add(txtUsuario);
        campos.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField(18);
        campos.add(txtPassword);

        btnIngresar = new JButton("Ingresar con contraseña y rostro");
        btnRegistrarRostro = new JButton("Registrar mi rostro por primera vez");
        btnAdminInicial = new JButton("Crear administrador inicial");

        JPanel botones = new JPanel(new GridLayout(0, 1, 8, 8));
        botones.add(btnIngresar);
        botones.add(btnRegistrarRostro);
        botones.add(btnAdminInicial);

        lblEstado = new JLabel("El ingreso requiere un rostro previamente registrado.");

        JPanel contenedor = new JPanel(new BorderLayout(10, 15));
        contenedor.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));
        contenedor.add(campos, BorderLayout.NORTH);
        contenedor.add(botones, BorderLayout.CENTER);
        contenedor.add(lblEstado, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> iniciarSesion());
        txtPassword.addActionListener(e -> iniciarSesion());
        btnRegistrarRostro.addActionListener(e -> registrarRostroInicial());
        btnAdminInicial.addActionListener(e -> crearAdministradorInicial());

        setContentPane(contenedor);
        pack();
        setLocationRelativeTo(null);
    }

    // ----- Ingresar: contraseña + rostro -----
    private void iniciarSesion() {
        String usuario = txtUsuario.getText();
        char[] password = txtPassword.getPassword();

        ejecutar("Verificando credenciales y rostro... siga las indicaciones de la cámara.",
                () -> controller.iniciarSesion(usuario, password),
                password,
                datos -> {
                    SesionActual.iniciarDesdeLogin(datos);
                    dispose();
                    new MainView().setVisible(true);
                });
    }

    // ----- Primer registro de rostro (con usuario y contraseña) -----
    private void registrarRostroInicial() {
        String usuario = txtUsuario.getText();
        char[] password = txtPassword.getPassword();

        ejecutar("Registrando rostro... siga las indicaciones de la cámara.",
                () -> controller.registrarRostroInicial(usuario, password),
                password,
                mensaje -> JOptionPane.showMessageDialog(this,
                        mensaje + "\nAhora pulse \"Ingresar con contraseña y rostro\".",
                        "SIGELAB", JOptionPane.INFORMATION_MESSAGE));
    }

    // ----- Administrador inicial (solo si no hay usuarios) -----
    private void crearAdministradorInicial() {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField usuario = new JTextField();
        JTextField correo = new JTextField();
        JPasswordField password = new JPasswordField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido:"));
        formulario.add(apellido);
        formulario.add(new JLabel("Usuario:"));
        formulario.add(usuario);
        formulario.add(new JLabel("Correo:"));
        formulario.add(correo);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(password);

        int opcion = JOptionPane.showConfirmDialog(this, formulario,
                "Administrador inicial (solo si no existen usuarios)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        char[] clave = password.getPassword();
        ejecutar("Creando administrador...",
                () -> {
                    controller.crearAdministradorInicial(nombre.getText(), apellido.getText(),
                            usuario.getText(), correo.getText(), clave);
                    return usuario.getText().trim();
                },
                clave,
                creado -> {
                    txtUsuario.setText(creado);
                    JOptionPane.showMessageDialog(this,
                            "Administrador creado.\nEscriba su contraseña y pulse "
                                    + "\"Registrar mi rostro por primera vez\" antes de ingresar.",
                            "SIGELAB", JOptionPane.INFORMATION_MESSAGE);
                });
    }

    /**
     * La cámara tarda varios segundos, por eso cada acción se ejecuta en segundo plano
     * (SwingWorker) para no congelar la ventana.
     */
    private <T> void ejecutar(String aviso, Callable<T> tarea, char[] password, Consumer<T> alTerminar) {
        habilitar(false);
        lblEstado.setText(aviso);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                try {
                    return tarea.call();
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                habilitar(true);
                lblEstado.setText("El ingreso requiere un rostro previamente registrado.");
                txtPassword.setText("");

                try {
                    alTerminar.accept(get());

                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    if (causa instanceof SQLException) {
                        mostrar("No se pudo conectar con la base de datos.\n" + causa.getMessage(),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        mostrar(causa.getMessage(), JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException ex) {
                    mostrar(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }

    private void habilitar(boolean activo) {
        btnIngresar.setEnabled(activo);
        btnRegistrarRostro.setEnabled(activo);
        btnAdminInicial.setEnabled(activo);
    }

    private void mostrar(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", tipo);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}
