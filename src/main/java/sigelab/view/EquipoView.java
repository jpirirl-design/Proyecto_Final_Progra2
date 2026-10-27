
package sigelab.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EquipoView extends JFrame {

    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtSerie;
    private JTextArea txtObservaciones;

    private JComboBox<String> cbTipo;
    private JComboBox<String> cbLaboratorio;
    private JComboBox<String> cbEstado;

    private JTable tablaEquipos;

    public EquipoView() {
        setTitle("SIGELAB - Gestión de Equipos");
        setSize(1050, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(new GridLayout(5, 4, 10, 10));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        txtMarca = new JTextField();
        txtModelo = new JTextField();
        txtSerie = new JTextField();

        txtObservaciones = new JTextArea(2, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);

        // Se cargarán desde SQL Server posteriormente.
        cbTipo = new JComboBox<>(new String[]{"Seleccione tipo"});
        cbLaboratorio = new JComboBox<>(new String[]{"Seleccione laboratorio"});

        cbEstado = new JComboBox<>(new String[]{
                "DISPONIBLE",
                "PRESTADO",
                "DAÑADO",
                "MANTENIMIENTO",
                "BAJA"
        });

        formulario.add(new JLabel("Código de inventario:"));
        formulario.add(txtCodigo);

        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);

        formulario.add(new JLabel("Tipo de equipo:"));
        formulario.add(cbTipo);

        formulario.add(new JLabel("Laboratorio:"));
        formulario.add(cbLaboratorio);

        formulario.add(new JLabel("Marca:"));
        formulario.add(txtMarca);

        formulario.add(new JLabel("Modelo:"));
        formulario.add(txtModelo);

        formulario.add(new JLabel("Número de serie:"));
        formulario.add(txtSerie);

        formulario.add(new JLabel("Estado:"));
        formulario.add(cbEstado);

        formulario.add(new JLabel("Observaciones:"));
        formulario.add(new JScrollPane(txtObservaciones));

        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));

        JPanel botones = new JPanel(new FlowLayout());

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDesactivar = new JButton("Dar de baja");
        JButton btnLimpiar = new JButton("Limpiar");

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnDesactivar);
        botones.add(btnLimpiar);

        String[] columnas = {
                "ID", "Código", "Nombre", "Tipo", "Marca",
                "Modelo", "Serie", "Laboratorio", "Estado"
        };

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaEquipos = new JTable(modelo);
        tablaEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaEquipos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tablaEquipos), BorderLayout.CENTER);

        add(principal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EquipoView ventana = new EquipoView();
            ventana.setVisible(true);
        });
    }
}
