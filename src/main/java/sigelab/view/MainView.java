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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

/** Menú principal con barra lateral y dashboard (6.11). El menú se adapta al rol; los Services validan igual (RN-20). */
public class MainView extends JFrame {

    private final ReporteController reporteController = new ReporteController();

    // Valores del resumen, en el orden de las tarjetas
    private final JLabel[] valores = new JLabel[8];
    private final GraficoDona dona = new GraficoDona();
    private final GraficoBarras barras = new GraficoBarras();
    private final JLabel lblAviso = Tema.etiqueta(" ", Font.PLAIN, 12, Tema.ROJO);

    public MainView() {
        Usuario usuario = SesionActual.getUsuario();
        NombreRol rol = usuario.getRol().getNombre();

        setTitle("SIGELAB - Menú principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UiUtil.ajustarAPantalla(this, 1150, 700);

        JPanel raiz = new JPanel(new BorderLayout(16, 0));
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(new EmptyBorder(14, 14, 14, 14));
        raiz.add(crearBarraLateral(usuario, rol), BorderLayout.WEST);
        raiz.add(crearContenido(usuario), BorderLayout.CENTER);
        setContentPane(raiz);

        // Al volver a esta ventana (después de un préstamo, por ejemplo) se refrescan las cantidades.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarDashboard();
            }
        });
        cargarDashboard();
    }

    // ------------------------------------------------------------------ Barra lateral

    private JPanel crearBarraLateral(Usuario usuario, NombreRol rol) {
        Tema.PanelRedondeado lateral = new Tema.PanelRedondeado(
                new BorderLayout(), Tema.SIDEBAR, null, 26);
        lateral.setBorder(new EmptyBorder(22, 12, 18, 12));
        lateral.setPreferredSize(new Dimension(220, 0));

        // Perfil
        JPanel perfil = new JPanel();
        perfil.setOpaque(false);
        perfil.setLayout(new BoxLayout(perfil, BoxLayout.Y_AXIS));

        Avatar avatar = new Avatar(iniciales(usuario));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel nombre = Tema.etiqueta(usuario.getNombre() + " " + usuario.getApellido(),
                Font.BOLD, 14, Color.WHITE);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblRol = Tema.etiqueta(rol.name(), Font.PLAIN, 11, new Color(0xB9D2DE));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

        perfil.add(avatar);
        perfil.add(Box.createVerticalStrut(10));
        perfil.add(nombre);
        perfil.add(Box.createVerticalStrut(2));
        perfil.add(lblRol);
        perfil.add(Box.createVerticalStrut(24));

        // Menú según rol
        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 6));
        menu.setOpaque(false);

        JButton inicio = Tema.boton("  Inicio", Tema.Estilo.MENU_ACTIVO);
        inicio.addActionListener(e -> cargarDashboard());
        menu.add(inicio);

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

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(perfil, BorderLayout.NORTH);
        arriba.add(menu, BorderLayout.CENTER);

        JButton salir = Tema.boton("  Cerrar sesión", Tema.Estilo.MENU);
        salir.addActionListener(e -> {
            SesionActual.cerrar();
            dispose();
            new LoginView().setVisible(true);
        });

        JScrollPane desplazable = new JScrollPane(arriba,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazable.setBorder(BorderFactory.createEmptyBorder());
        desplazable.setViewportBorder(BorderFactory.createEmptyBorder());
        desplazable.setOpaque(false);
        desplazable.getViewport().setOpaque(false);
        lateral.add(desplazable, BorderLayout.CENTER);
        lateral.add(salir, BorderLayout.SOUTH);
        return lateral;
    }

    private void agregar(JPanel menu, String texto, Supplier<JFrame> pantalla) {
        JButton boton = Tema.boton("  " + texto, Tema.Estilo.MENU);
        boton.addActionListener(e -> pantalla.get().setVisible(true));
        menu.add(boton);
    }

    private String iniciales(Usuario u) {
        String n = u.getNombre() == null || u.getNombre().isBlank() ? "?" : u.getNombre().substring(0, 1);
        String a = u.getApellido() == null || u.getApellido().isBlank() ? "" : u.getApellido().substring(0, 1);
        return (n + a).toUpperCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------ Contenido

    private JPanel crearContenido(Usuario usuario) {
        // Encabezado
        Tema.PanelRedondeado encabezado = Tema.tarjeta(new BorderLayout());
        JLabel bienvenida = Tema.etiqueta("Bienvenido, " + usuario.getNombre() + " !",
                Font.BOLD, 24, Tema.TEXTO);
        String fecha = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, yyyy", Locale.of("es", "GT")));
        JLabel lblFecha = Tema.etiqueta(fecha, Font.PLAIN, 13, Tema.TEXTO_SUAVE);

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(bienvenida);
        textos.add(lblFecha);

        JButton actualizar = Tema.boton("Actualizar", Tema.Estilo.SECUNDARIO);
        actualizar.addActionListener(e -> cargarDashboard());
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        derecha.setOpaque(false);
        derecha.add(actualizar);

        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(derecha, BorderLayout.EAST);

        // Resumen (8 tarjetas)
        Tema.PanelRedondeado resumen = Tema.tarjeta(new BorderLayout(0, 10));
        resumen.add(titulo("Resumen"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);
        grid.add(tarjetaDato(0, "Σ", "Total equipos", new Color(0xDCE8F0), Tema.PRIMARIO, Tema.BORDE));
        grid.add(tarjetaDato(1, "✓", "Disponibles", Tema.VERDE_SUAVE, Tema.VERDE, Tema.BORDE));
        grid.add(tarjetaDato(2, "↗", "Prestados", new Color(0xDCE8F0), Tema.PRIMARIO, Tema.BORDE));
        grid.add(tarjetaDato(6, "≡", "Préstamos activos", Tema.VERDE_SUAVE, Tema.VERDE, Tema.BORDE));
        grid.add(tarjetaDato(3, "!", "Dañados", Tema.ROJO_SUAVE, Tema.ROJO, Tema.BORDE));
        grid.add(tarjetaDato(4, "⚙", "En mantenimiento", Tema.NARANJA_SUAVE, Tema.NARANJA, Tema.BORDE));
        grid.add(tarjetaDato(5, "×", "De baja", new Color(0xE6E6E6), new Color(0x707070), Tema.BORDE));
        grid.add(tarjetaDato(7, "◷", "Préstamos vencidos", Tema.NARANJA_SUAVE, Tema.NARANJA, Tema.NARANJA));
        resumen.add(grid, BorderLayout.CENTER);
        resumen.add(lblAviso, BorderLayout.SOUTH);

        // Gráficos
        Tema.PanelRedondeado cajaDona = Tema.tarjeta(new BorderLayout(0, 8));
        cajaDona.add(titulo("Disponibilidad de equipos"), BorderLayout.NORTH);
        cajaDona.add(dona, BorderLayout.CENTER);

        Tema.PanelRedondeado cajaBarras = Tema.tarjeta(new BorderLayout(0, 8));
        cajaBarras.add(titulo("Equipos por estado"), BorderLayout.NORTH);
        cajaBarras.add(barras, BorderLayout.CENTER);

        JPanel graficos = new JPanel(new GridLayout(1, 2, 14, 0));
        graficos.setOpaque(false);
        graficos.add(cajaDona);
        graficos.add(cajaBarras);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.add(resumen, BorderLayout.NORTH);
        cuerpo.add(graficos, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(cuerpo, BorderLayout.CENTER);
        return contenido;
    }

    private JLabel titulo(String texto) {
        return Tema.etiqueta(texto, Font.BOLD, 15, Tema.TEXTO);
    }

    private JPanel tarjetaDato(int indice, String simbolo, String texto,
                               Color fondoIcono, Color colorIcono, Color borde) {
        Tema.PanelRedondeado t = new Tema.PanelRedondeado(
                new BorderLayout(12, 0), Color.WHITE, borde, 16);
        t.setBorder(new EmptyBorder(10, 12, 10, 12));

        valores[indice] = Tema.etiqueta("–", Font.BOLD, 22, Tema.TEXTO);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(valores[indice]);
        textos.add(Tema.etiqueta(texto, Font.PLAIN, 12, Tema.TEXTO_SUAVE));

        t.add(Tema.insignia(simbolo, fondoIcono, colorIcono), BorderLayout.WEST);
        t.add(textos, BorderLayout.CENTER);
        return t;
    }

    private void cargarDashboard() {
        try {
            ResumenDashboard r = reporteController.obtenerDashboard();
            int[] datos = {r.totalEquipos(), r.disponibles(), r.prestados(), r.danados(),
                    r.enMantenimiento(), r.deBaja(), r.prestamosActivos(), r.prestamosVencidos()};
            for (int i = 0; i < datos.length; i++) {
                valores[i].setText(String.valueOf(datos[i]));
            }
            dona.setDatos(r.disponibles(), r.totalEquipos());
            barras.setDatos(
                    new String[]{"Disponibles", "Prestados", "Dañados", "Mantenimiento", "Baja"},
                    new int[]{r.disponibles(), r.prestados(), r.danados(), r.enMantenimiento(), r.deBaja()},
                    new Color[]{Tema.VERDE, Tema.PRIMARIO, Tema.ROJO, Tema.NARANJA, new Color(0x8A8A8A)});
            lblAviso.setText(" ");
        } catch (BusinessException | DataAccessException ex) {
            lblAviso.setText(ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ Componentes dibujados

    /** Círculo con las iniciales del usuario. */
    private static class Avatar extends JComponent {
        private final String texto;

        Avatar(String texto) {
            this.texto = texto;
            Dimension d = new Dimension(72, 72);
            setPreferredSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = (getWidth() - 72) / 2;
            g2.setColor(new Color(0xB9D2DE));
            g2.fill(new Ellipse2D.Float(x, 0, 72, 72));
            g2.setColor(Tema.FONDO);
            g2.fill(new Ellipse2D.Float(x + 4, 4, 64, 64));
            g2.setColor(Tema.SIDEBAR);
            g2.setFont(Tema.fuente(Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, x + (72 - fm.stringWidth(texto)) / 2,
                    (72 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Dona: porcentaje de equipos disponibles frente al resto. */
    private static class GraficoDona extends JComponent {
        private int disponibles;
        private int total;

        GraficoDona() {
            setPreferredSize(new Dimension(300, 190));
        }

        void setDatos(int disponibles, int total) {
            this.disponibles = disponibles;
            this.total = total;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int d = Math.max(60, Math.min(getHeight() - 10, getWidth() / 2 - 20));
            int x = 10;
            int y = (getHeight() - d) / 2;
            double pct = total == 0 ? 0 : disponibles * 100.0 / total;

            g2.setStroke(new BasicStroke(d / 5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            int m = d / 10;
            g2.setColor(Tema.CELESTE);
            g2.draw(new Arc2D.Double(x + m, y + m, d - 2 * m, d - 2 * m, 0, 360, Arc2D.OPEN));
            g2.setColor(Tema.PRIMARIO);
            g2.draw(new Arc2D.Double(x + m, y + m, d - 2 * m, d - 2 * m, 90, -3.6 * pct, Arc2D.OPEN));

            String texto = Math.round(pct) + "%";
            g2.setFont(Tema.fuente(Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Tema.TEXTO);
            g2.drawString(texto, x + (d - fm.stringWidth(texto)) / 2, y + (d - fm.getHeight()) / 2 + fm.getAscent());

            // Leyenda
            int lx = x + d + 24;
            int ly = getHeight() / 2 - 22;
            leyenda(g2, lx, ly, Tema.PRIMARIO, "Disponibles (" + disponibles + ")");
            leyenda(g2, lx, ly + 30, Tema.CELESTE, "Otros estados (" + (total - disponibles) + ")");
            g2.dispose();
        }

        private void leyenda(Graphics2D g2, int x, int y, Color color, String texto) {
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(x, y, 16, 16, 5, 5));
            g2.setColor(Tema.TEXTO);
            g2.setFont(Tema.fuente(Font.PLAIN, 12));
            g2.drawString(texto, x + 24, y + 13);
        }
    }

    /** Barras horizontales: cantidad de equipos en cada estado. */
    private static class GraficoBarras extends JComponent {
        private String[] etiquetas = new String[0];
        private int[] cantidades = new int[0];
        private Color[] colores = new Color[0];

        GraficoBarras() {
            setPreferredSize(new Dimension(300, 190));
        }

        void setDatos(String[] etiquetas, int[] cantidades, Color[] colores) {
            this.etiquetas = etiquetas;
            this.cantidades = cantidades;
            this.colores = colores;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (etiquetas.length == 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(Tema.fuente(Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();

            int max = 1;
            for (int c : cantidades) {
                max = Math.max(max, c);
            }
            int anchoEtiqueta = 105;
            int anchoNumero = 36;
            int disponible = Math.max(20, getWidth() - anchoEtiqueta - anchoNumero - 10);
            int fila = Math.max(22, getHeight() / etiquetas.length);
            int alto = Math.min(14, fila - 10);

            for (int i = 0; i < etiquetas.length; i++) {
                int y = i * fila + (fila - alto) / 2;
                g2.setColor(Tema.TEXTO);
                g2.drawString(etiquetas[i], 0, y + alto - 2);

                g2.setColor(new Color(0xE6EEF3));
                g2.fill(new RoundRectangle2D.Float(anchoEtiqueta, y, disponible, alto, alto, alto));

                int largo = (int) Math.round(disponible * (cantidades[i] / (double) max));
                if (cantidades[i] > 0) {
                    g2.setColor(colores[i]);
                    g2.fill(new RoundRectangle2D.Float(anchoEtiqueta, y, Math.max(alto, largo), alto, alto, alto));
                }
                g2.setColor(Tema.TEXTO_SUAVE);
                String n = String.valueOf(cantidades[i]);
                g2.drawString(n, getWidth() - fm.stringWidth(n) - 2, y + alto - 2);
            }
            g2.dispose();
        }
    }
}