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

        // ----- Panel izquierdo (marca) -----
        Tema.PanelRedondeado marca = new Tema.PanelRedondeado(
                new GridBagLayout(), Tema.SIDEBAR, null, 28);
        marca.setPreferredSize(new Dimension(320, 0));
        JPanel textosMarca = new JPanel();
        textosMarca.setOpaque(false);
        textosMarca.setLayout(new BoxLayout(textosMarca, BoxLayout.Y_AXIS));
        JLabel logo = Tema.etiqueta("SIGELAB", Font.BOLD, 34, Color.WHITE);
        JLabel subtitulo = Tema.etiqueta("<html><div style='text-align:center'>Sistema de Gestión y Control<br>"
                + "de Laboratorios Universitarios</div></html>", Font.PLAIN, 14, new Color(0xDCE9F0));
        JLabel umg = Tema.etiqueta("Universidad Mariano Gálvez", Font.PLAIN, 12, new Color(0xB9D2DE));
        for (JLabel l : new JLabel[]{logo, subtitulo, umg}) {
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        textosMarca.add(logo);
        textosMarca.add(Box.createVerticalStrut(10));
        textosMarca.add(subtitulo);
        textosMarca.add(Box.createVerticalStrut(28));
        textosMarca.add(umg);
        marca.add(textosMarca);

        // ----- Tarjeta del formulario -----
        txtUsuario = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnIngresar = Tema.boton("Ingresar con contraseña y rostro", Tema.Estilo.PRIMARIO);
        btnRegistrarRostro = Tema.boton("Registrar mi rostro por primera vez", Tema.Estilo.SECUNDARIO);
        btnAdminInicial = Tema.boton("Crear administrador inicial", Tema.Estilo.SECUNDARIO);
        lblEstado = Tema.etiqueta("El ingreso requiere un rostro previamente registrado.",
                Font.PLAIN, 12, Tema.TEXTO_SUAVE);

        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 8));
        formulario.setOpaque(false);
        formulario.add(Tema.etiqueta("Bienvenido", Font.BOLD, 26, Tema.TEXTO));
        formulario.add(Tema.etiqueta("Ingrese sus credenciales para continuar", Font.PLAIN, 13, Tema.TEXTO_SUAVE));
        formulario.add(Tema.etiqueta("Usuario", Font.BOLD, 12, Tema.TEXTO));
        formulario.add(txtUsuario);
        formulario.add(Tema.etiqueta("Contraseña", Font.BOLD, 12, Tema.TEXTO));
        formulario.add(txtPassword);
        formulario.add(btnIngresar);
        formulario.add(btnRegistrarRostro);
        formulario.add(btnAdminInicial);
        formulario.add(lblEstado);

        Tema.PanelRedondeado tarjeta = Tema.tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel contenedor = new JPanel(new BorderLayout(18, 0));
        contenedor.setBackground(Tema.FONDO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contenedor.add(marca, BorderLayout.WEST);
        contenedor.add(tarjeta, BorderLayout.CENTER);

        btnIngresar.addActionListener(e -> iniciarSesion());
        txtPassword.addActionListener(e -> iniciarSesion());
        btnRegistrarRostro.addActionListener(e -> registrarRostroInicial());
        btnAdminInicial.addActionListener(e -> crearAdministradorInicial());

        setContentPane(contenedor);
        pack();
        setMinimumSize(getSize());
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
        Tema.aplicar();   // paleta y componentes redondeados, antes de crear ventanas
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}