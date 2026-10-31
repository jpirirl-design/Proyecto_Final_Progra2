package sigelab.view;

import sigelab.controller.DevolucionController;
import sigelab.enums.CondicionDevolucion;
import sigelab.enums.EstadoPrestamo;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.DetallePrestamo;
import sigelab.model.Prestamo;
import sigelab.seguridad.SesionActual;
import sigelab.service.ItemDevolucion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DevolucionView extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Columnas de la tabla de pendientes
    private static final int COL_DEVOLVER = 0;
    private static final int COL_ID_DETALLE = 1;
    private static final int COL_CONDICION = 4;
    private static final int COL_OBSERVACION = 5;

    private final DevolucionController controller = new DevolucionController();

    private DefaultTableModel modeloPrestamos;
    private DefaultTableModel modeloPendientes;
    private JTable tablaPrestamos;
    private JTable tablaPendientes;

    /** Mismo orden que las filas de tablaPrestamos (la tabla no se ordena, así que los índices coinciden). */
    private final List<Prestamo> prestamos = new ArrayList<>();
    private int idPrestamoSeleccionado = -1;

    public DevolucionView() {
        setTitle("SIGELAB - Devoluciones");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 950, 650);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarPrestamos();
    }

    private void construirInterfaz() {

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ----- Préstamos activos -----
        modeloPrestamos = new DefaultTableModel(
                new String[]{"ID", "Solicitante", "Fecha del préstamo", "Fecha esperada", "Situación"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaPrestamos = new JTable(modeloPrestamos);
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarPendientes();
            }
        });

        JPanel panelPrestamos = new JPanel(new BorderLayout());
        panelPrestamos.setBorder(BorderFactory.createTitledBorder("Préstamos activos"));
        panelPrestamos.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        // ----- Equipos pendientes del préstamo seleccionado -----
        modeloPendientes = new DefaultTableModel(
                new String[]{"Devolver", "ID detalle", "Código", "Nombre", "Condición", "Observación"}, 0) {
            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == COL_DEVOLVER ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return columna == COL_DEVOLVER || columna == COL_CONDICION || columna == COL_OBSERVACION;
            }
        };
        tablaPendientes = new JTable(modeloPendientes);
        tablaPendientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPendientes.getColumnModel().getColumn(COL_CONDICION).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(new String[]{
                        CondicionDevolucion.BUENO.name(), CondicionDevolucion.DANADO.name()})));
        tablaPendientes.getColumnModel().getColumn(COL_DEVOLVER).setMaxWidth(80);
        tablaPendientes.getColumnModel().getColumn(COL_ID_DETALLE).setMaxWidth(90);

        JPanel panelPendientes = new JPanel(new BorderLayout());
        panelPendientes.setBorder(BorderFactory.createTitledBorder(
                "Equipos pendientes (marque los que se devuelven y elija su condición)"));
        panelPendientes.add(new JScrollPane(tablaPendientes), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPrestamos, panelPendientes);
        division.setResizeWeight(0.4);

        // ----- Botones -----
        JButton btnTodos = new JButton("Marcar todos");
        JButton btnRegistrar = new JButton("Registrar devolución");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel botones = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 6));
        botones.add(btnTodos);
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnCerrar);

        principal.add(division, BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);
        setContentPane(principal);

        btnTodos.addActionListener(e -> marcarTodos());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> cargarPrestamos());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void cargarPrestamos() {
        try {
            prestamos.clear();
            modeloPrestamos.setRowCount(0);

            prestamos.addAll(controller.listarPrestamosActivos());
            LocalDate hoy = LocalDate.now();

            for (Prestamo p : prestamos) {
                // RN-17: un préstamo activo con fecha esperada vencida tiene equipos pendientes, por lo tanto está vencido.
                boolean vencido = p.getFechaEsperadaDevolucion().isBefore(hoy);
                modeloPrestamos.addRow(new Object[]{
                        p.getIdPrestamo(),
                        p.getSolicitante(),
                        p.getFechaPrestamo().format(FORMATO_FECHA_HORA),
                        p.getFechaEsperadaDevolucion(),
                        vencido ? "VENCIDO" : "ACTIVO"});
            }
            modeloPendientes.setRowCount(0);
            idPrestamoSeleccionado = -1;

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarPendientes() {
        modeloPendientes.setRowCount(0);

        int fila = tablaPrestamos.getSelectedRow();
        if (fila < 0 || fila >= prestamos.size()) {
            idPrestamoSeleccionado = -1;
            return;
        }
        idPrestamoSeleccionado = prestamos.get(fila).getIdPrestamo();

        try {
            for (DetallePrestamo d : controller.listarPendientes(idPrestamoSeleccionado)) {
                modeloPendientes.addRow(new Object[]{
                        false,
                        d.getIdDetalle(),
                        d.getEquipo().getCodigoInventario(),
                        d.getEquipo().getNombre(),
                        CondicionDevolucion.BUENO.name(),
                        ""});
            }
        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void marcarTodos() {
        for (int i = 0; i < modeloPendientes.getRowCount(); i++) {
            modeloPendientes.setValueAt(true, i, COL_DEVOLVER);
        }
    }

    private void registrar() {
        if (idPrestamoSeleccionado <= 0) {
            mostrarAviso("Seleccione un préstamo de la tabla.");
            return;
        }
        if (tablaPendientes.isEditing()) {
            tablaPendientes.getCellEditor().stopCellEditing();
        }

        List<ItemDevolucion> items = new ArrayList<>();
        for (int i = 0; i < modeloPendientes.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modeloPendientes.getValueAt(i, COL_DEVOLVER))) {
                int idDetalle = ((Number) modeloPendientes.getValueAt(i, COL_ID_DETALLE)).intValue();
                CondicionDevolucion condicion =
                        CondicionDevolucion.valueOf(String.valueOf(modeloPendientes.getValueAt(i, COL_CONDICION)));
                Object obs = modeloPendientes.getValueAt(i, COL_OBSERVACION);
                items.add(new ItemDevolucion(idDetalle, condicion, obs == null ? "" : obs.toString()));
            }
        }
        if (items.isEmpty()) {
            mostrarAviso("Marque al menos un equipo para devolver.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Registrar la devolución de " + items.size() + " equipo(s)?",
                "Confirmar devolución", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int idPrestamo = idPrestamoSeleccionado;
            EstadoPrestamo resultado = controller.devolver(idPrestamo, items);

            cargarPrestamos();

            if (resultado == EstadoPrestamo.FINALIZADO) {
                mostrarInfo("Devolución registrada. El préstamo #" + idPrestamo
                        + " quedó FINALIZADO: se devolvieron todos sus equipos.");
            } else {
                seleccionarPrestamo(idPrestamo);
                mostrarInfo("Devolución registrada. El préstamo #" + idPrestamo + " sigue activo con "
                        + modeloPendientes.getRowCount() + " equipo(s) pendiente(s).");
            }

        } catch (BusinessException | DataAccessException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void seleccionarPrestamo(int idPrestamo) {
        for (int i = 0; i < prestamos.size(); i++) {
            if (prestamos.get(i).getIdPrestamo() == idPrestamo) {
                tablaPrestamos.setRowSelectionInterval(i, i);
                return;
            }
        }
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
        SwingUtilities.invokeLater(() -> new DevolucionView().setVisible(true));
    }
}