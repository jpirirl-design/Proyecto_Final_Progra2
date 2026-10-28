
package sigelab.view;
import sigelab.controller.LaboratorioController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LaboratorioView extends JFrame {

    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtUbicacion;
    private JSpinner spCapacidad;
    private JComboBox<String> cbEstado;
    private JTable tablaLaboratorios;

    public LaboratorioView() {
        setTitle("SIGELAB - Gestión de Laboratorios");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(new GridLayout(5, 2, 10, 10));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        txtUbicacion = new JTextField();
        spCapacidad = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        cbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

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
                "ID", "Código", "Nombre", "Ubicación", "Capacidad", "Estado"
        };

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaLaboratorios = new JTable(modelo);

        tablaLaboratorios.getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int fila = tablaLaboratorios.getSelectedRow();

            if (fila == -1) {
                return;
            }

            int filaModelo = tablaLaboratorios.convertRowIndexToModel(fila);

            txtCodigo.setText(
                    modelo.getValueAt(filaModelo, 1).toString()
            );

            txtNombre.setText(
                    modelo.getValueAt(filaModelo, 2).toString()
            );

            txtUbicacion.setText(
                    modelo.getValueAt(filaModelo, 3).toString()
            );

            spCapacidad.setValue(
                    Integer.parseInt(
                            modelo.getValueAt(filaModelo, 4).toString()
                    )
            );

            cbEstado.setSelectedItem(
                    modelo.getValueAt(filaModelo, 5).toString()
            );
        });

        tablaLaboratorios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel botones = new JPanel(new FlowLayout());

        JButton btnGuardar = new JButton("Guardar");


        btnGuardar.addActionListener(e -> {

            LaboratorioController controller = new LaboratorioController();

            String mensaje = controller.validarLaboratorio(
                    txtCodigo.getText(),
                    txtNombre.getText(),
                    txtUbicacion.getText(),
                    (int) spCapacidad.getValue()
            );

            if (mensaje != null) {
                JOptionPane.showMessageDialog(this, mensaje);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Datos validados correctamente.");
        });




        JButton btnEditar = new JButton("Editar");

        btnEditar.addActionListener(e -> {

            int fila = tablaLaboratorios.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un laboratorio de la tabla.");
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Laboratorio seleccionado correctamente.\n"
                            + "La edición estará disponible al conectar SQL Server.");
        });




        JButton btnDesactivar = new JButton("Desactivar");
        btnDesactivar.addActionListener(e -> {

            int fila = tablaLaboratorios.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un laboratorio para desactivarlo.");
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea desactivar este laboratorio?",
                    "Confirmar desactivación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this,
                        "Confirmación recibida.\n"
                                + "La desactivación estará disponible al conectar SQL Server.");
            }
        });




        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            txtCodigo.setText("");
            txtNombre.setText("");
            txtUbicacion.setText("");
            spCapacidad.setValue(1);
            cbEstado.setSelectedIndex(0);
            tablaLaboratorios.clearSelection();
        });

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnDesactivar);
        botones.add(btnLimpiar);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tablaLaboratorios), BorderLayout.CENTER);

        add(principal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LaboratorioView ventana = new LaboratorioView();
            ventana.setVisible(true);
        });
    }
}
