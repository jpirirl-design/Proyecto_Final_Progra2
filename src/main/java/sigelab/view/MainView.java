package sigelab.view;

import sigelab.controller.ReporteController;
import sigelab.enums.NombreRol;
import sigelab.enums.TipoReporte;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.ResumenDashboard;
import sigelab.model.Usuario;
import sigelab.seguridad.SesionActual;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

/** Menú principal con dashboard (6.11). El menú se adapta al rol, pero los Services validan igual (RN-20). */
public class MainView extends JFrame {

    private final ReporteController reporteController = new ReporteController();
    private final JPanel panelDashboard = new JPanel(new GridLayout(2, 4, 10, 10));

    public MainView() {
        Usuario usuario = SesionActual.getUsuario();
        NombreRol rol = usuario.getRol().getNombre();

        setTitle("SIGELAB - Menú principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 950, 600);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ----- Encabezado -----
        JLabel titulo = new JLabel("SIGELAB - Sistema de Gestión de Laboratorios", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel bienvenida = new JLabel("Bienvenido, " + usuario.getNombre() + " "
                + usuario.getApellido() + "  |  Rol: " + rol, SwingConstants.CENTER);

        JPanel encabezado = new JPanel(new GridLayout(2, 1));
        encabezado.add(titulo);
        encabezado.add(bienvenida);

        // ----- Dashboard -----
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarDashboard());

        JPanel dashboard = new JPanel(new BorderLayout(5, 5));
        dashboard.setBorder(BorderFactory.createTitledBorder("Resumen"));
        dashboard.add(panelDashboard, BorderLayout.CENTER);
        dashboard.add(btnActualizar, BorderLayout.EAST);

        // ----- Menú según rol -----
        JPanel menu = new JPanel(new GridLayout(0, 4, 12, 12));
        menu.setBorder(BorderFactory.createTitledBorder("Módulos"));

        boolean esAdmin = rol == NombreRol.ADMIN;
        boolean esOperador = esAdmin || rol == NombreRol.ENCARGADO;

        if (esOperador) {
            agregar(menu, "Préstamos", PrestamoView::new);
            agregar(menu, "Devoluciones", DevolucionView::new);
            agregar(menu, "Mantenimiento", MantenimientoView::new);
            agregar(menu, "Equipos", EquipoView::new);
            agregar(menu, "Laboratorios", LaboratorioView::new);
            agregar(menu, "Tipos de equipo", TipoEquipoView::new);
        }
        agregar(menu, "Reportes", ReportesView::new);
        if (esAdmin) {
            agregar(menu, "Usuarios", UsuarioView::new);
            agregar(menu, "Auditoría", () -> new ReportesView(TipoReporte.AUDITORIA));
        }

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.addActionListener(e -> {
            SesionActual.cerrar();
            dispose();
            new LoginView().setVisible(true);
        });

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(dashboard, BorderLayout.NORTH);
        centro.add(menu, BorderLayout.CENTER);

        principal.add(encabezado, BorderLayout.NORTH);
        principal.add(centro, BorderLayout.CENTER);
        principal.add(btnSalir, BorderLayout.SOUTH);
        setContentPane(principal);

        // Al volver a esta ventana (después de un préstamo, por ejemplo) se refrescan las cantidades.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarDashboard();
            }
        });
        cargarDashboard();
    }

    private void agregar(JPanel menu, String texto, Supplier<JFrame> pantalla) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> pantalla.get().setVisible(true));
        menu.add(boton);
    }

    private void cargarDashboard() {
        panelDashboard.removeAll();
        try {
            ResumenDashboard r = reporteController.obtenerDashboard();
            tarjeta("Total equipos", r.totalEquipos());
            tarjeta("Disponibles", r.disponibles());
            tarjeta("Prestados", r.prestados());
            tarjeta("Dañados", r.danados());
            tarjeta("En mantenimiento", r.enMantenimiento());
            tarjeta("De baja", r.deBaja());
            tarjeta("Préstamos activos", r.prestamosActivos());
            tarjeta("Préstamos vencidos", r.prestamosVencidos());
        } catch (BusinessException | DataAccessException ex) {
            panelDashboard.add(new JLabel(ex.getMessage()));
        }
        panelDashboard.revalidate();
        panelDashboard.repaint();
    }

    private void tarjeta(String titulo, int valor) {
        JLabel numero = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 22f));

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        tarjeta.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);
        panelDashboard.add(tarjeta);
    }
}