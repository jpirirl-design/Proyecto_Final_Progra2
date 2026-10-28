
package sigelab.view;
import sigelab.controller.TipoEquipoController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TipoEquipoView extends JFrame {

    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTable tablaTipos;

    public TipoEquipoView() {
        setTitle("SIGELAB - Gestión de Tipos de Equipo");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Formulario
        JPanel formulario = new JPanel(new GridLayout(2, 2, 10, 10));

        txtNombre = new JTextField();
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        formulario.add(new JLabel("Nombre del tipo:"));
        formulario.add(txtNombre);

        formulario.add(new JLabel("Descripción:"));
        formulario.add(new JScrollPane(txtDescripcion));

        // Botones
        JPanel botones = new JPanel(new FlowLayout());

        JButton btnGuardar = new JButton("Guardar");


        btnGuardar.addActionListener(e -> {

            TipoEquipoController controller = new TipoEquipoController();

            String mensaje = controller.validarTipoEquipo(
                    txtNombre.getText(),
                    txtDescripcion.getText()
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

            int fila = tablaTipos.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un tipo de equipo para editar.");
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Tipo de equipo seleccionado.\n"
                            + "La edición estará disponible al conectar SQL Server.");
        });




        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> {

            int fila = tablaTipos.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un tipo de equipo para eliminar.");
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea eliminar este tipo de equipo?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this,
                        "Confirmación recibida.\n"
                                + "La eliminación estará disponible al conectar SQL Server.");
            }
        });



        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            txtNombre.setText("");
            txtDescripcion.setText("");
            tablaTipos.clearSelection();
            txtNombre.requestFocus();
        });

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        // Tabla
        String[] columnas = {"ID", "Nombre", "Descripción"};

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaTipos = new JTable(modelo);

        tablaTipos.getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int fila = tablaTipos.getSelectedRow();

            if (fila == -1) {
                return;
            }

            int filaModelo = tablaTipos.convertRowIndexToModel(fila);

            txtNombre.setText(
                    modelo.getValueAt(filaModelo, 1).toString()
            );

            txtDescripcion.setText(
                    modelo.getValueAt(filaModelo, 2).toString()
            );
        });

        tablaTipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tablaTipos), BorderLayout.CENTER);

        add(principal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TipoEquipoView ventana = new TipoEquipoView();
            ventana.setVisible(true);
        });
    }
}
