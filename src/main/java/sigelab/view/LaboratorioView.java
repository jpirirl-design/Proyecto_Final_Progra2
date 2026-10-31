package sigelab.view;

import sigelab.controller.LaboratorioController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class LaboratorioView extends JFrame {

    private final LaboratorioController controller =
            new LaboratorioController();

    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtUbicacion;
    private JSpinner spCapacidad;
    private JComboBox<String> cbEstado;
    private JTable tablaLaboratorios;
    private DefaultTableModel modelo;

    public LaboratorioView() {
        setTitle("SIGELAB - Gestión de Laboratorios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 850, 580);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(new GridLayout(5, 2, 10, 10));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        txtUbicacion = new JTextField();
        spCapacidad = new JSpinner(
                new SpinnerNumberModel(1, 1, 10000, 1));
        cbEstado = new JComboBox<>(
                new String[]{"ACTIVO", "INACTIVO"});

        formulario.add(new JLabel("Código:"));
        formulario.add(txtCodigo);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Ubicación:"));
        formulario.add(txtUbicacion);
        formulario.add(new JLabel("Capacidad:"));
        formulario.add(spCapacidad);
        formulario.add(new JLabel("Estado:"));
        formulario.add(cbEstado);

        String[] columnas = {
                "ID", "Código", "Nombre",
                "Ubicación", "Capacidad", "Estado"
        };

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaLaboratorios = new JTable(modelo);
        tablaLaboratorios.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tablaLaboratorios.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        int fila = tablaLaboratorios.getSelectedRow();
                        if (fila >= 0) {
                            int m = tablaLaboratorios
                                    .convertRowIndexToModel(fila);

                            txtCodigo.setText(
                                    modelo.getValueAt(m, 1).toString());
                            txtNombre.setText(
                                    modelo.getValueAt(m, 2).toString());
                            txtUbicacion.setText(
                                    modelo.getValueAt(m, 3).toString());
                            spCapacidad.setValue(
                                    modelo.getValueAt(m, 4));
                            cbEstado.setSelectedItem(
                                    modelo.getValueAt(m, 5).toString());
                        }
                    }
                });

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> {
            try {
                controller.guardar(
                        txtCodigo.getText(),
                        txtNombre.getText(),
                        txtUbicacion.getText(),
                        (int) spCapacidad.getValue());

                cargarLaboratorios();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Laboratorio guardado correctamente.");

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
                        txtCodigo.getText(),
                        txtNombre.getText(),
                        txtUbicacion.getText(),
                        (int) spCapacidad.getValue(),
                        cbEstado.getSelectedItem().toString());

                cargarLaboratorios();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Laboratorio actualizado correctamente.");

            } catch (SQLException | IllegalArgumentException ex) {
                mostrarError(ex);
            }
        });

        btnDesactivar.addActionListener(e -> {
            int id = obtenerIdSeleccionado();
            if (id == -1) return;

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea desactivar este laboratorio?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);

            if (respuesta != JOptionPane.YES_OPTION) return;

            try {
                controller.desactivar(id);
                cargarLaboratorios();
                limpiar();

                JOptionPane.showMessageDialog(this,
                        "Laboratorio desactivado correctamente.");

            } catch (SQLException ex) {
                mostrarError(ex);
            }
        });

        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = UiUtil.panelBotones(
                btnGuardar, btnEditar, btnDesactivar, btnLimpiar,
                UiUtil.botonCerrar(this));

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(
                new JScrollPane(tablaLaboratorios),
                BorderLayout.CENTER);

        add(principal);

        cargarLaboratorios();
    }

    private void cargarLaboratorios() {
        try {
            tablaLaboratorios.clearSelection();
            modelo.setRowCount(0);

            for (Object[] fila : controller.listar()) {
                modelo.addRow(fila);
            }

        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private int obtenerIdSeleccionado() {
        int fila = tablaLaboratorios.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un laboratorio de la tabla.");
            return -1;
        }

        int filaModelo =
                tablaLaboratorios.convertRowIndexToModel(fila);

        return ((Number) modelo.getValueAt(filaModelo, 0))
                .intValue();
    }

    private void limpiar() {
        tablaLaboratorios.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtUbicacion.setText("");
        spCapacidad.setValue(1);
        cbEstado.setSelectedItem("ACTIVO");
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
                new LaboratorioView().setVisible(true));
    }
}