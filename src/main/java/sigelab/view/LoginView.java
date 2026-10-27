package sigelab.view;

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
