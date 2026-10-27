
package sigelab.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UsuarioView extends JFrame {

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRol;
    private JComboBox<String> cbEstado;
    private JTable tablaUsuarios;

    public UsuarioView() {
        setTitle("SIGELAB - Gestión de Usuarios");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Formulario de usuarios
        JPanel formulario = new JPanel(new GridLayout(4, 4, 10, 10));

        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtUsuario = new JTextField();
        txtCorreo = new JTextField();
        txtPassword = new JPasswordField();

        cbRol = new JComboBox<>(new String[]{
                "Seleccione rol", "ADMIN", "ENCARGADO"
        });

        cbEstado = new JComboBox<>(new String[]{
                "ACTIVO", "INACTIVO"
        });

        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);

        formulario.add(new JLabel("Apellido:"));
        formulario.add(txtApellido);

        formulario.add(new JLabel("Usuario:"));
        formulario.add(txtUsuario);

        formulario.add(new JLabel("Correo:"));
        formulario.add(txtCorreo);

        formulario.add(new JLabel("Contraseña:"));
        formulario.add(txtPassword);

        formulario.add(new JLabel("Rol:"));
        formulario.add(cbRol);

        formulario.add(new JLabel("Estado:"));
        formulario.add(cbEstado);

        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));

        // Botones
        JPanel botones = new JPanel(new FlowLayout());

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar = new JButton("Limpiar");

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnDesactivar);
        botones.add(btnLimpiar);

        // Tabla
        String[] columnas = {
                "ID", "Nombre", "Apellido", "Usuario",
                "Correo", "Rol", "Estado"
        };

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modelo);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        add(principal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UsuarioView ventana = new UsuarioView();
            ventana.setVisible(true);
        });
    }
}
