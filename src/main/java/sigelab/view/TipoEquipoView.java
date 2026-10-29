
package sigelab.view;

import sigelab.controller.TipoEquipoController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class TipoEquipoView extends JFrame {

    private final TipoEquipoController controller =
            new TipoEquipoController();

    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTable tablaTipos;
    private DefaultTableModel modelo;

    public TipoEquipoView() {

        setTitle("SIGELAB - Gestión de Tipos de Equipo");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(
                new GridLayout(2, 2, 10, 10));

        txtNombre = new JTextField();
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        formulario.add(new JLabel("Nombre del tipo:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Descripción:"));
        formulario.add(new JScrollPane(txtDescripcion));

        String[] columnas = {"ID", "Nombre", "Descripción"};

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaTipos = new JTable(modelo);
        tablaTipos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tablaTipos.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (e.getValueIsAdjusting()) return;

                    int fila = tablaTipos.getSelectedRow();
                    if (fila < 0) return;

                    int m = tablaTipos.convertRowIndexToModel(fila);

                    txtNombre.setText(
                            String.valueOf(modelo.getValueAt(m, 1)));

                    Object descripcion = modelo.getValueAt(m, 2);
                    txtDescripcion.setText(
                            descripcion == null ? "" :
                                    descripcion.toString());
                });

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> {
            try {
                controller.guardar(
                        txtNombre.getText(),
                        txtDescripcion.getText());

                cargarTipos();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Tipo de equipo guardado correctamente.");

            } catch (SQLException | IllegalArgumentException ex) {
                mostrarError(ex);
            }
        });

        btnEditar.addActionListener(e -> {

            int id = obtenerIdSeleccionado();
            if (id == -1) return;

            try {
                controller.editar(
                        id,
                        txtNombre.getText(),
                        txtDescripcion.getText());

                cargarTipos();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Tipo de equipo actualizado correctamente.");

            } catch (SQLException | IllegalArgumentException ex) {
                mostrarError(ex);
            }
        });

        btnEliminar.addActionListener(e -> {

            int id = obtenerIdSeleccionado();
            if (id == -1) return;

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar este tipo de equipo?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (respuesta != JOptionPane.YES_OPTION) return;

            try {
                controller.eliminar(id);

                cargarTipos();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Tipo de equipo eliminado correctamente.");

            } catch (SQLException ex) {
                mostrarError(ex);
            }
        });

        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(
                new JScrollPane(tablaTipos),
                BorderLayout.CENTER);

        add(principal);

        cargarTipos();
    }

    private void cargarTipos() {

        try {
            tablaTipos.clearSelection();
            modelo.setRowCount(0);

            for (Object[] fila : controller.listar()) {
                modelo.addRow(fila);
            }

        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private int obtenerIdSeleccionado() {

        int fila = tablaTipos.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un tipo de equipo de la tabla.");
            return -1;
        }

        int m = tablaTipos.convertRowIndexToModel(fila);

        return ((Number) modelo.getValueAt(m, 0))
                .intValue();
    }

    private void limpiar() {

        tablaTipos.clearSelection();
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtNombre.requestFocus();
    }

    private void mostrarError(Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new TipoEquipoView().setVisible(true));
    }
}
