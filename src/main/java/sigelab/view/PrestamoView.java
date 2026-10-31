package sigelab.view;

import sigelab.controller.PrestamoController;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.Equipo;
import sigelab.model.Prestamo;
import sigelab.seguridad.SesionActual;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class PrestamoView extends JFrame {

    private final PrestamoController controller = new PrestamoController();

    private JTextField txtSolicitante;
    private JTextField txtObservaciones;
    private JTextField txtCodigo;
    private JSpinner spFecha;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    /** Equipos agregados al préstamo, en el mismo orden que las filas de la tabla. */
    private final List<Equipo> equiposAgregados = new ArrayList<>();

    public PrestamoView() {
        setTitle("SIGELAB - Registrar préstamo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 950, 620);
        setLocationRelativeTo(null);

        construirInterfaz();
    }

    private void construirInterfaz() {

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ----- Datos del préstamo -----
        txtSolicitante = new JTextField();
        txtObservaciones = new JTextField();

        Date porDefecto = Date.from(LocalDate.now().plusDays(3)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        spFecha = new JSpinner(new SpinnerDateModel(porDefecto, null, null, Calendar.DAY_OF_MONTH));
        spFecha.setEditor(new JSpinner.DateEditor(spFecha, "yyyy-MM-dd"));

        JPanel formulario = new JPanel(new GridLayout(2, 4, 10, 8));
        formulario.add(new JLabel("Solicitante:"));
        formulario.add(txtSolicitante);
        formulario.add(new JLabel("Fecha esperada de devolución:"));
        formulario.add(spFecha);
        formulario.add(new JLabel("Observaciones:"));
        formulario.add(txtObservaciones);
        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));

        // ----- Agregar equipos -----
        txtCodigo = new JTextField(14);
        JButton btnAgregar = new JButton("Agregar equipo");
        JButton btnQr = new JButton("Leer QR desde imagen...");
        JButton btnQuitar = new JButton("Quitar seleccionado");

        JPanel agregar = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        agregar.setBorder(BorderFactory.createTitledBorder("Equipos del préstamo"));
        agregar.add(new JLabel("Código del equipo:"));
        agregar.add(txtCodigo);
        agregar.add(btnAgregar);
        agregar.add(btnQr);
        agregar.add(btnQuitar);

        JPanel superior = new JPanel(new BorderLayout(0, 10));
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(agregar, BorderLayout.SOUTH);

        // ----- Tabla -----
        modeloTabla = new DefaultTableModel(new String[]{"Código", "Nombre", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ----- Botones inferiores -----
        JButton btnConfirmar = new JButton("Confirmar préstamo");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel botones = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 6));
        botones.add(btnConfirmar);
        botones.add(btnLimpiar);
        botones.add(btnCerrar);

        principal.add(superior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tabla), BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);
        setContentPane(principal);

        // ----- Eventos -----
        btnAgregar.addActionListener(e -> agregarPorCodigo());
        txtCodigo.addActionListener(e -> agregarPorCodigo());
        btnQr.addActionListener(e -> leerQr());
        btnQuitar.addActionListener(e -> quitarSeleccionado());
        btnConfirmar.addActionListener(e -> confirmar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void agregarPorCodigo() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            mostrarAviso("Escriba o escanee el código del equipo.");
            return;
        }
        agregarEquipo(() -> controller.identificarPorCodigo(codigo));
    }

    private void leerQr() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccione la imagen del código QR");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg, jpeg, gif, bmp)", "png", "jpg", "jpeg", "gif", "bmp"));

        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = selector.getSelectedFile();
            agregarEquipo(() -> controller.identificarPorImagen(archivo));
        }
    }

    /** El QR identifica el equipo; si no está DISPONIBLE se rechaza. La decisión final la toma el Service al confirmar. */
    private void agregarEquipo(Supplier<Equipo> origen) {
        try {
            Equipo equipo = origen.get();

            if (yaAgregado(equipo)) {
                mostrarAviso("El equipo " + equipo.getCodigoInventario() + " ya está en el préstamo.");
                return;
            }
            if (!controller.puedeAgregarse(equipo)) {
                mostrarAviso("El equipo " + equipo.getCodigoInventario() + " está "
                        + equipo.getEstado() + " y no se puede prestar.");
                return;
            }

            equiposAgregados.add(equipo);
            modeloTabla.addRow(new Object[]{
                    equipo.getCodigoInventario(), equipo.getNombre(), equipo.getEstado()});
            txtCodigo.setText("");
            txtCodigo.requestFocusInWindow();

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private boolean yaAgregado(Equipo equipo) {
        return equiposAgregados.stream()
                .anyMatch(e -> e.getIdEquipo().equals(equipo.getIdEquipo()));
    }

    private void quitarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Seleccione un equipo de la tabla.");
            return;
        }
        equiposAgregados.remove(fila);
        modeloTabla.removeRow(fila);
    }

    private void confirmar() {
        try {
            LocalDate fecha = ((Date) spFecha.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            List<Integer> ids = equiposAgregados.stream().map(Equipo::getIdEquipo).toList();

            Prestamo prestamo = controller.confirmar(
                    txtSolicitante.getText(), fecha, ids, txtObservaciones.getText());

            JOptionPane.showMessageDialog(this,
                    "Préstamo #" + prestamo.getIdPrestamo() + " registrado con "
                            + prestamo.getDetalles().size() + " equipo(s).",
                    "SIGELAB", JOptionPane.INFORMATION_MESSAGE);
            limpiar();

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void limpiar() {
        txtSolicitante.setText("");
        txtObservaciones.setText("");
        txtCodigo.setText("");
        equiposAgregados.clear();
        modeloTabla.setRowCount(0);
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "SIGELAB", JOptionPane.ERROR_MESSAGE);
    }

    /** SOLO PARA PRUEBAS LOCALES: simula el login. En la aplicación real lo hace LoginView. */
    public static void main(String[] args) {
        SesionActual.iniciarDesdeLogin(new Object[]{1, "Prueba", "Local", "prueba", "ADMIN"}); // 1 = un id_usuario existente
        SwingUtilities.invokeLater(() -> new PrestamoView().setVisible(true));
    }
}