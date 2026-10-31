package sigelab.view;

import sigelab.controller.MantenimientoController;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.ResultadoMantenimiento;
import sigelab.enums.TipoMantenimiento;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.Equipo;
import sigelab.seguridad.SesionActual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MantenimientoView extends JFrame {

    private static final int COL_ID = 0;
    private static final int COL_CODIGO = 1;

    private final MantenimientoController controller = new MantenimientoController();

    private JTextField txtCodigo;
    private JTextField txtDescripcion;
    private JTextField txtResponsable;
    private JTextField txtObservaciones;
    private JComboBox<TipoMantenimiento> cbTipo;
    private JLabel lblEquipo;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JComboBox<ResultadoMantenimiento> cbResultado;
    private JTextField txtObservacionesCierre;

    public MantenimientoView() {
        setTitle("SIGELAB - Mantenimiento");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 1000, 650);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarEnProceso();
    }

    private void construirInterfaz() {

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ----- Enviar equipo a mantenimiento -----
        txtCodigo = new JTextField();
        txtDescripcion = new JTextField();
        txtResponsable = new JTextField();
        txtObservaciones = new JTextField();
        cbTipo = new JComboBox<>(TipoMantenimiento.values());
        lblEquipo = new JLabel("Equipo: (busque un código para ver su estado)");
        JButton btnBuscar = new JButton("Buscar equipo");
        JButton btnEnviar = new JButton("Enviar a mantenimiento");

        JPanel formulario = new JPanel(new GridLayout(3, 4, 10, 8));
        formulario.add(new JLabel("Código del equipo:"));
        formulario.add(txtCodigo);
        formulario.add(new JLabel("Tipo de mantenimiento:"));
        formulario.add(cbTipo);
        formulario.add(new JLabel("Descripción del problema o trabajo:"));
        formulario.add(txtDescripcion);
        formulario.add(new JLabel("Responsable:"));
        formulario.add(txtResponsable);
        formulario.add(new JLabel("Observaciones:"));
        formulario.add(txtObservaciones);
        formulario.add(btnBuscar);
        formulario.add(btnEnviar);

        JPanel envio = new JPanel(new BorderLayout(0, 8));
        envio.setBorder(BorderFactory.createTitledBorder("Enviar equipo a mantenimiento"));
        envio.add(formulario, BorderLayout.CENTER);
        envio.add(lblEquipo, BorderLayout.SOUTH);

        // ----- Mantenimientos en proceso -----
        modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Código", "Equipo", "Tipo", "Descripción", "Responsable", "Fecha de ingreso"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(COL_ID).setMaxWidth(60);

        cbResultado = new JComboBox<>(ResultadoMantenimiento.values());
        txtObservacionesCierre = new JTextField(22);
        JButton btnFinalizar = new JButton("Finalizar seleccionado");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel cierre = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 6));
        cierre.add(new JLabel("Resultado:"));
        cierre.add(cbResultado);
        cierre.add(new JLabel("Observaciones del cierre:"));
        cierre.add(txtObservacionesCierre);
        cierre.add(btnFinalizar);
        cierre.add(btnActualizar);
        cierre.add(btnCerrar);

        JPanel enProceso = new JPanel(new BorderLayout());
        enProceso.setBorder(BorderFactory.createTitledBorder("Mantenimientos en proceso"));
        enProceso.add(new JScrollPane(tabla), BorderLayout.CENTER);
        enProceso.add(cierre, BorderLayout.SOUTH);

        principal.add(envio, BorderLayout.NORTH);
        principal.add(enProceso, BorderLayout.CENTER);
        setContentPane(principal);

        btnBuscar.addActionListener(e -> buscarEquipo());
        txtCodigo.addActionListener(e -> buscarEquipo());
        btnEnviar.addActionListener(e -> enviar());
        btnFinalizar.addActionListener(e -> finalizar());
        btnActualizar.addActionListener(e -> cargarEnProceso());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void buscarEquipo() {
        try {
            Equipo equipo = controller.identificar(txtCodigo.getText());
            lblEquipo.setText("Equipo: " + equipo.getCodigoInventario() + " - " + equipo.getNombre()
                    + "   |   Estado actual: " + equipo.getEstado());
        } catch (BusinessException | DataAccessException ex) {
            lblEquipo.setText("Equipo: (no identificado)");
            mostrarError(ex.getMessage());
        }
    }

    private void enviar() {
        try {
            int idMantenimiento = controller.enviar(
                    txtCodigo.getText(),
                    (TipoMantenimiento) cbTipo.getSelectedItem(),
                    txtDescripcion.getText(),
                    txtResponsable.getText(),
                    txtObservaciones.getText());

            mostrarInfo("Equipo " + txtCodigo.getText().trim() + " enviado a mantenimiento (registro #"
                    + idMantenimiento + ").");
            limpiarEnvio();
            cargarEnProceso();

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void finalizar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Seleccione un mantenimiento de la tabla.");
            return;
        }
        int id = ((Number) modeloTabla.getValueAt(fila, COL_ID)).intValue();
        String codigo = String.valueOf(modeloTabla.getValueAt(fila, COL_CODIGO));
        ResultadoMantenimiento resultado = (ResultadoMantenimiento) cbResultado.getSelectedItem();

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Finalizar el mantenimiento de " + codigo + " como " + resultado + "?",
                "Confirmar cierre", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            EstadoEquipo nuevoEstado = controller.finalizar(id, resultado, txtObservacionesCierre.getText());
            mostrarInfo("Mantenimiento finalizado. El equipo " + codigo + " pasó a " + nuevoEstado + ".");
            txtObservacionesCierre.setText("");
            cargarEnProceso();

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarEnProceso() {
        try {
            modeloTabla.setRowCount(0);
            for (Object[] fila : controller.listarEnProceso()) {
                modeloTabla.addRow(fila);
            }
        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void limpiarEnvio() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        txtResponsable.setText("");
        txtObservaciones.setText("");
        cbTipo.setSelectedIndex(0);
        lblEquipo.setText("Equipo: (busque un código para ver su estado)");
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", JOptionPane.ERROR_MESSAGE);
    }

    /** SOLO PARA PRUEBAS LOCALES: simula el login. En la aplicación real lo hace LoginView. */
    public static void main(String[] args) {
        SesionActual.iniciarDesdeLogin(new Object[]{1, "Prueba", "Local", "prueba", "ADMIN"}); // 1 = un id_usuario existente
        SwingUtilities.invokeLater(() -> new MantenimientoView().setVisible(true));
    }
}