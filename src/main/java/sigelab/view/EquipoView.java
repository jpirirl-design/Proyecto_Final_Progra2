package sigelab.view;

import sigelab.controller.EquipoController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class EquipoView extends JFrame {

    private final EquipoController controller = new EquipoController();

    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtSerie;
    private JTextField txtObservaciones;

    private JComboBox<Opcion> cbTipo;
    private JComboBox<Opcion> cbLaboratorio;
    private JComboBox<String> cbEstado;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private int idSeleccionado = -1;

    public EquipoView() {
        setTitle("SIGELAB - Gestión de Equipos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 1050, 650);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarOpciones();
        cargarEquipos();
    }

    private void construirInterfaz() {

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formulario = new JPanel(new GridLayout(5, 4, 10, 8));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        txtMarca = new JTextField();
        txtModelo = new JTextField();
        txtSerie = new JTextField();
        txtObservaciones = new JTextField();

        cbTipo = new JComboBox<>();
        cbLaboratorio = new JComboBox<>();
        cbEstado = new JComboBox<>(new String[]{
                "DISPONIBLE",
                "PRESTADO",
                "DANADO",
                "MANTENIMIENTO",
                "BAJA"
        });
        // Solo informativo: el estado cambia por préstamos, devoluciones y mantenimiento.
        cbEstado.setEnabled(false);
        cbEstado.setToolTipText("El estado cambia automáticamente por los procesos del sistema.");

        formulario.add(new JLabel("Código inventario:"));
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
        formulario.add(txtObservaciones);

        JPanel botones = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 6));

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Editar");
        JButton btnBaja = new JButton("Dar de baja");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnQr = new JButton("Ver / guardar QR");

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnBaja);
        botones.add(btnLimpiar);
        botones.add(btnQr);
        botones.add(UiUtil.botonCerrar(this));

        btnQr.addActionListener(e -> mostrarQr());

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        modeloTabla = new DefaultTableModel(
                new String[]{
                        "ID",
                        "Código",
                        "Nombre",
                        "Tipo ID",
                        "Marca",
                        "Modelo",
                        "Serie",
                        "Laboratorio ID",
                        "Estado",
                        "Observaciones"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tabla);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(scroll, BorderLayout.CENTER);

        setContentPane(principal);

        btnGuardar.addActionListener(e -> guardarEquipo());
        btnEditar.addActionListener(e -> editarEquipo());
        btnBaja.addActionListener(e -> darDeBaja());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarEquipo();
            }
        });
    }

    private void cargarOpciones() {
        try {
            cbTipo.removeAllItems();
            cbLaboratorio.removeAllItems();

            cbTipo.addItem(new Opcion(-1, "Seleccione tipo"));
            cbLaboratorio.addItem(
                    new Opcion(-1, "Seleccione laboratorio")
            );

            List<Object[]> tipos = controller.listarOpciones("TipoEquipo");

            for (Object[] fila : tipos) {
                cbTipo.addItem(new Opcion(
                        ((Number) fila[0]).intValue(),
                        String.valueOf(fila[1])
                ));
            }

            List<Object[]> laboratorios =
                    controller.listarOpciones("Laboratorio");

            for (Object[] fila : laboratorios) {
                cbLaboratorio.addItem(new Opcion(
                        ((Number) fila[0]).intValue(),
                        String.valueOf(fila[1])
                ));
            }

        } catch (SQLException ex) {
            mostrarError("Error al cargar tipos y laboratorios: "
                    + ex.getMessage());
        }
    }

    private void cargarEquipos() {
        try {
            modeloTabla.setRowCount(0);

            for (Object[] fila : controller.listar()) {
                modeloTabla.addRow(fila);
            }

        } catch (SQLException ex) {
            mostrarError("Error al consultar equipos: "
                    + ex.getMessage());
        }
    }

    private int obtenerId(JComboBox<Opcion> combo) {
        Opcion opcion = (Opcion) combo.getSelectedItem();
        return opcion == null ? -1 : opcion.id;
    }

    private void guardarEquipo() {
        try {
            int tipo = obtenerId(cbTipo);
            int laboratorio = obtenerId(cbLaboratorio);

            if (tipo <= 0 || laboratorio <= 0) {
                mostrarError("Seleccione un tipo de equipo y un laboratorio.");
                return;
            }

            controller.guardarEquipo(
                    txtCodigo.getText(),
                    txtNombre.getText(),
                    tipo,
                    laboratorio,
                    txtMarca.getText(),
                    txtModelo.getText(),
                    txtSerie.getText(),
                    String.valueOf(cbEstado.getSelectedItem()),
                    txtObservaciones.getText()
            );

            JOptionPane.showMessageDialog(this,
                    "Equipo guardado correctamente.");

            cargarEquipos();
            limpiarFormulario();

        } catch (IllegalArgumentException | SQLException ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    private void editarEquipo() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un equipo de la tabla.");
            return;
        }

        try {
            int tipo = obtenerId(cbTipo);
            int laboratorio = obtenerId(cbLaboratorio);

            if (tipo <= 0 || laboratorio <= 0) {
                mostrarError("Seleccione un tipo de equipo y un laboratorio.");
                return;
            }

            controller.editarEquipo(
                    idSeleccionado,
                    txtCodigo.getText(),
                    txtNombre.getText(),
                    tipo,
                    laboratorio,
                    txtMarca.getText(),
                    txtModelo.getText(),
                    txtSerie.getText(),
                    String.valueOf(cbEstado.getSelectedItem()),
                    txtObservaciones.getText()
            );

            JOptionPane.showMessageDialog(this,
                    "Equipo actualizado correctamente.");

            cargarEquipos();
            limpiarFormulario();

        } catch (IllegalArgumentException | SQLException ex) {
            mostrarError("No se pudo editar: " + ex.getMessage());
        }
    }

    private void darDeBaja() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un equipo de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea dar de baja el equipo seleccionado?",
                "Confirmar baja",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.darDeBaja(idSeleccionado);

            JOptionPane.showMessageDialog(this,
                    "Solicitud de baja procesada.");

            cargarEquipos();
            limpiarFormulario();

        } catch (SQLException ex) {
            mostrarError("No se pudo dar de baja: " + ex.getMessage());
        }
    }

    private void seleccionarEquipo() {
        int filaVista = tabla.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int fila = tabla.convertRowIndexToModel(filaVista);

        idSeleccionado =
                ((Number) modeloTabla.getValueAt(fila, 0)).intValue();

        txtCodigo.setText(valor(fila, 1));
        txtNombre.setText(valor(fila, 2));

        seleccionarOpcion(
                cbTipo,
                ((Number) modeloTabla.getValueAt(fila, 3)).intValue()
        );

        txtMarca.setText(valor(fila, 4));
        txtModelo.setText(valor(fila, 5));
        txtSerie.setText(valor(fila, 6));

        seleccionarOpcion(
                cbLaboratorio,
                ((Number) modeloTabla.getValueAt(fila, 7)).intValue()
        );

        cbEstado.setSelectedItem(valor(fila, 8));
        txtObservaciones.setText(valor(fila, 9));
    }

    private String valor(int fila, int columna) {
        Object dato = modeloTabla.getValueAt(fila, columna);
        return dato == null ? "" : String.valueOf(dato);
    }

    private void seleccionarOpcion(
            JComboBox<Opcion> combo,
            int id
    ) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Opcion opcion = combo.getItemAt(i);

            if (opcion.id == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        combo.setSelectedIndex(0);
    }

    // MOSTRAR Y GUARDAR EL QR DEL EQUIPO SELECCIONADO
    private void mostrarQr() {
        String codigo = txtCodigo.getText().trim();

        if (idSeleccionado <= 0 || codigo.isEmpty()) {
            mostrarError("Seleccione un equipo de la tabla.");
            return;
        }

        try {
            ImageIcon imagen = new ImageIcon(controller.generarQr(codigo));
            Object[] opciones = {"Guardar PNG", "Cerrar"};

            int opcion = JOptionPane.showOptionDialog(
                    this,
                    new JLabel(codigo, imagen, SwingConstants.CENTER),
                    "QR del equipo " + codigo,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            if (opcion == 0) {
                JFileChooser selector = new JFileChooser();
                selector.setSelectedFile(new java.io.File("qr-equipos", codigo + ".png"));
                if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File archivo = controller.guardarQr(codigo, selector.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "QR guardado en:\n" + archivo.getAbsolutePath());
                }
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;

        txtCodigo.setText("");
        txtNombre.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtSerie.setText("");
        txtObservaciones.setText("");

        cbTipo.setSelectedIndex(0);
        cbLaboratorio.setSelectedIndex(0);
        cbEstado.setSelectedItem("DISPONIBLE");

        tabla.clearSelection();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "SIGELAB",
                JOptionPane.ERROR_MESSAGE
        );
    }

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
            EquipoView ventana = new EquipoView();
            ventana.setVisible(true);
        });
    }
}