
package sigelab.view;

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
        tablaLaboratorios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel botones = new JPanel(new FlowLayout());

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar = new JButton("Limpiar");

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
