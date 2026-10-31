package sigelab.view;

import sigelab.controller.ReporteController;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.NombreRol;
import sigelab.enums.TipoReporte;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.FiltroReporte;
import sigelab.model.ResultadoConsulta;
import sigelab.seguridad.SesionActual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class ReportesView extends JFrame {

    private static final String TODOS = "TODOS";

    private final ReporteController controller = new ReporteController();

    private JComboBox<TipoReporte> cbReporte;
    private JComboBox<Object> cbEstado;
    private JSpinner spDesde;
    private JSpinner spHasta;
    private JTextField txtCodigo;
    private JLabel lblTotal;
    private DefaultTableModel modeloTabla;

    public ReportesView() {
        this(TipoReporte.RESUMEN_EQUIPOS_POR_ESTADO);
    }

    public ReportesView(TipoReporte inicial) {
        setTitle("SIGELAB - Reportes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 1050, 620);

        construirInterfaz();
        cbReporte.setSelectedItem(inicial);
        actualizarFiltros();
        consultar();
    }

    private void construirInterfaz() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cbReporte = new JComboBox<>();
        boolean esAdmin = SesionActual.haySesion()
                && SesionActual.getUsuario().getRol().getNombre() == NombreRol.ADMIN;
        for (TipoReporte tipo : TipoReporte.values()) {
            if (tipo != TipoReporte.AUDITORIA || esAdmin) {
                cbReporte.addItem(tipo);
            }
        }

        cbEstado = new JComboBox<>();
        cbEstado.addItem(TODOS);
        for (EstadoEquipo estado : EstadoEquipo.values()) {
            cbEstado.addItem(estado);
        }

        spDesde = crearSpinnerFecha(LocalDate.now().minusDays(30));
        spHasta = crearSpinnerFecha(LocalDate.now());
        txtCodigo = new JTextField(10);

        JButton btnConsultar = new JButton("Consultar");

        JPanel filtros = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 5));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        filtros.add(new JLabel("Reporte:"));
        filtros.add(cbReporte);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cbEstado);
        filtros.add(new JLabel("Desde:"));
        filtros.add(spDesde);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(spHasta);
        filtros.add(new JLabel("Código equipo:"));
        filtros.add(txtCodigo);
        filtros.add(btnConsultar);

        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setAutoCreateRowSorter(true);

        lblTotal = new JLabel(" ");

        principal.add(filtros, BorderLayout.NORTH);
        principal.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(lblTotal, BorderLayout.WEST);
        inferior.add(UiUtil.botonCerrar(this), BorderLayout.EAST);
        principal.add(inferior, BorderLayout.SOUTH);
        setContentPane(principal);

        cbReporte.addActionListener(e -> actualizarFiltros());
        btnConsultar.addActionListener(e -> consultar());
        txtCodigo.addActionListener(e -> consultar());
    }

    private JSpinner crearSpinnerFecha(LocalDate fecha) {
        Date valor = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(valor, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    /** Habilita solo los filtros que usa el reporte seleccionado. */
    private void actualizarFiltros() {
        TipoReporte tipo = (TipoReporte) cbReporte.getSelectedItem();
        if (tipo == null) {
            return;
        }
        cbEstado.setEnabled(tipo.usaEstado());
        spDesde.setEnabled(tipo.usaFechas());
        spHasta.setEnabled(tipo.usaFechas());
        txtCodigo.setEnabled(tipo.usaCodigo());
    }

    private void consultar() {
        TipoReporte tipo = (TipoReporte) cbReporte.getSelectedItem();
        Object estado = cbEstado.getSelectedItem();

        FiltroReporte filtro = new FiltroReporte(
                estado instanceof EstadoEquipo e ? e : null,
                aLocalDate(spDesde),
                aLocalDate(spHasta),
                txtCodigo.getText());

        try {
            ResultadoConsulta resultado = controller.generar(tipo, filtro);
            modeloTabla.setDataVector(
                    resultado.filas().toArray(new Object[0][]),
                    resultado.columnas().toArray());
            lblTotal.setText("Registros: " + resultado.filas().size());
        } catch (BusinessException | DataAccessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "SIGELAB", JOptionPane.WARNING_MESSAGE);
        }
    }

    private LocalDate aLocalDate(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}