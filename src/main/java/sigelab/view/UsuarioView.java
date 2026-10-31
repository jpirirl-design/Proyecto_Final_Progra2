package sigelab.view;

import sigelab.controller.UsuarioController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UsuarioView extends JFrame {

    private final UsuarioController controller =
            new UsuarioController();

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;

    private JComboBox<Opcion> cbRol;
    private JComboBox<String> cbEstado;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    private int idSeleccionado = -1;

    public UsuarioView() {
        setTitle("SIGELAB - Gestión de Usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 1000, 650);

        construirInterfaz();
        cargarRoles();
        cargarUsuarios();
    }

    private void construirInterfaz() {

        JPanel principal = new JPanel(
                new BorderLayout(10, 10)
        );

        principal.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JPanel formulario = new JPanel(
                new GridLayout(4, 4, 10, 10)
        );

        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtUsuario = new JTextField();
        txtCorreo = new JTextField();
        txtPassword = new JPasswordField();

        cbRol = new JComboBox<>();

        cbEstado = new JComboBox<>(
                new String[]{"ACTIVO", "INACTIVO"}
        );

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

        JPanel botones = new JPanel(
                new WrapLayout(FlowLayout.CENTER, 10, 6)
        );

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnRostro = new JButton("Registrar rostro");

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnDesactivar);
        botones.add(btnLimpiar);
        botones.add(btnRostro);
        botones.add(UiUtil.botonCerrar(this));

        btnRostro.addActionListener(e -> registrarRostro());

        modeloTabla = new DefaultTableModel(
                new String[]{
                        "ID",
                        "Nombre",
                        "Apellido",
                        "Usuario",
                        "Correo",
                        "Rol",
                        "Estado"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(
                    int fila,
                    int columna
            ) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setAutoCreateRowSorter(true);

        tablaUsuarios.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tablaUsuarios.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarUsuario();
                    }
                });

        btnGuardar.addActionListener(
                e -> guardarUsuario()
        );

        btnEditar.addActionListener(
                e -> editarUsuario()
        );

        btnDesactivar.addActionListener(
                e -> desactivarUsuario()
        );

        btnLimpiar.addActionListener(
                e -> limpiarFormulario()
        );

        JPanel superior = new JPanel(
                new BorderLayout(10, 10)
        );

        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);

        principal.add(
                new JScrollPane(tablaUsuarios),
                BorderLayout.CENTER
        );

        setContentPane(principal);
    }

    // CARGAR ROLES DESDE SQL SERVER
    private void cargarRoles() {

        try {
            cbRol.removeAllItems();

            cbRol.addItem(
                    new Opcion(-1, "Seleccione rol")
            );

            List<Object[]> roles =
                    controller.listarRoles();

            for (Object[] fila : roles) {

                cbRol.addItem(
                        new Opcion(
                                ((Number) fila[0]).intValue(),
                                String.valueOf(fila[1])
                        )
                );
            }

        } catch (SQLException ex) {

            mostrarError(
                    "Error al cargar roles: "
                            + ex.getMessage()
            );
        }
    }

    // CARGAR USUARIOS DESDE SQL SERVER
    private void cargarUsuarios() {

        try {
            modeloTabla.setRowCount(0);

            for (Object[] fila : controller.listar()) {
                modeloTabla.addRow(fila);
            }

        } catch (SQLException ex) {

            mostrarError(
                    "Error al cargar usuarios: "
                            + ex.getMessage()
            );
        }
    }

    private Opcion obtenerRol() {

        return (Opcion) cbRol.getSelectedItem();
    }

    // GUARDAR USUARIO
    private void guardarUsuario() {

        char[] password = txtPassword.getPassword();

        try {
            Opcion rol = obtenerRol();

            if (rol == null || rol.id <= 0) {
                mostrarError("Debe seleccionar un rol.");
                return;
            }

            controller.guardarUsuario(
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtUsuario.getText(),
                    txtCorreo.getText(),
                    password,
                    rol.id,
                    rol.nombre,
                    String.valueOf(
                            cbEstado.getSelectedItem()
                    )
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario guardado correctamente."
            );

            cargarUsuarios();
            limpiarFormulario();

        } catch (IllegalArgumentException | SQLException ex) {

            mostrarError(
                    "No se pudo guardar el usuario: "
                            + ex.getMessage()
            );

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    // EDITAR USUARIO
    private void editarUsuario() {

        if (idSeleccionado <= 0) {
            mostrarError(
                    "Debe seleccionar un usuario para editar."
            );
            return;
        }

        char[] password = txtPassword.getPassword();

        try {
            Opcion rol = obtenerRol();

            if (rol == null || rol.id <= 0) {
                mostrarError("Debe seleccionar un rol.");
                return;
            }

            controller.editarUsuario(
                    idSeleccionado,
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtUsuario.getText(),
                    txtCorreo.getText(),
                    password,
                    rol.id,
                    rol.nombre,
                    String.valueOf(
                            cbEstado.getSelectedItem()
                    )
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario actualizado correctamente."
            );

            cargarUsuarios();
            limpiarFormulario();

        } catch (IllegalArgumentException | SQLException ex) {

            mostrarError(
                    "No se pudo editar el usuario: "
                            + ex.getMessage()
            );

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    // DESACTIVAR USUARIO
    private void desactivarUsuario() {

        if (idSeleccionado <= 0) {
            mostrarError(
                    "Debe seleccionar un usuario para desactivar."
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea desactivar este usuario?",
                "Confirmar desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.desactivarUsuario(idSeleccionado);

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario desactivado correctamente."
            );

            cargarUsuarios();
            limpiarFormulario();

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudo desactivar el usuario: "
                            + ex.getMessage()
            );

        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // REGISTRAR O REEMPLAZAR EL ROSTRO DEL USUARIO SELECCIONADO
    private void registrarRostro() {

        String nombreUsuario = txtUsuario.getText().trim();
        int idUsuario = idSeleccionado;

        if (idUsuario <= 0 || nombreUsuario.isEmpty()) {
            mostrarError("Seleccione un usuario de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Se abrirá la cámara para registrar el rostro de " + nombreUsuario
                        + ".\nSi ya tenía un rostro registrado, será reemplazado. ¿Continuar?",
                "Registrar rostro",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return controller.registrarRostro(idUsuario);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    JOptionPane.showMessageDialog(UsuarioView.this, get());
                } catch (java.util.concurrent.ExecutionException ex) {
                    mostrarError(ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // SELECCIONAR USUARIO DE LA TABLA
    private void seleccionarUsuario() {

        int filaVista = tablaUsuarios.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int fila = tablaUsuarios.convertRowIndexToModel(
                filaVista
        );

        idSeleccionado = ((Number)
                modeloTabla.getValueAt(fila, 0)
        ).intValue();

        txtNombre.setText(valor(fila, 1));
        txtApellido.setText(valor(fila, 2));
        txtUsuario.setText(valor(fila, 3));
        txtCorreo.setText(valor(fila, 4));

        seleccionarRol(valor(fila, 5));

        cbEstado.setSelectedItem(valor(fila, 6));

        // Nunca recuperar contraseñas desde la tabla.
        // En edición, dejar vacío significa conservarla.
        txtPassword.setText("");
    }

    private String valor(int fila, int columna) {

        Object dato = modeloTabla.getValueAt(
                fila, columna
        );

        return dato == null
                ? ""
                : String.valueOf(dato);
    }

    private void seleccionarRol(String nombreRol) {

        for (int i = 0; i < cbRol.getItemCount(); i++) {

            Opcion opcion = cbRol.getItemAt(i);

            if (opcion.nombre.equals(nombreRol)) {
                cbRol.setSelectedIndex(i);
                return;
            }
        }

        cbRol.setSelectedIndex(0);
    }

    // LIMPIAR FORMULARIO
    private void limpiarFormulario() {

        idSeleccionado = -1;

        txtNombre.setText("");
        txtApellido.setText("");
        txtUsuario.setText("");
        txtCorreo.setText("");
        txtPassword.setText("");

        if (cbRol.getItemCount() > 0) {
            cbRol.setSelectedIndex(0);
        }

        cbEstado.setSelectedItem("ACTIVO");

        tablaUsuarios.clearSelection();
        txtNombre.requestFocus();
    }

    private void mostrarError(String mensaje) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "SIGELAB",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // OPCIÓN DE ROL
    private static class Opcion {

        private final int id;
        private final String nombre;

        public Opcion(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            UsuarioView ventana = new UsuarioView();
            ventana.setVisible(true);
        });
    }
}