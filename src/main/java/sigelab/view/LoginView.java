package sigelab.view;
import sigelab.controller.LoginController;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

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
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        panel.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField();
        panel.add(txtUsuario);

        panel.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        panel.add(new JLabel(""));
        btnIngresar = new JButton("Ingresar");

        btnIngresar.addActionListener(e -> {

            LoginController controller = new LoginController();
            char[] password = txtPassword.getPassword();

            try {
                String mensaje = controller.validarCredenciales(
                        txtUsuario.getText(),
                        password
                );

                if (mensaje != null) {
                    JOptionPane.showMessageDialog(this, mensaje);
                    return;
                }

                JOptionPane.showMessageDialog(this,
                        "Campos completos. Autenticación pendiente de implementar.");

            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        });

        panel.add(btnIngresar);

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView ventana = new LoginView();
            ventana.setVisible(true);
        });
    }
}
