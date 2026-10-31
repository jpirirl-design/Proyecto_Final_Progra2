package sigelab.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Paleta de colores y componentes redondeados de SIGELAB.
 * Llamar Tema.aplicar() una sola vez, antes de crear cualquier ventana (LoginView.main).
 */
public final class Tema {

    // ----- Paleta (azul petróleo + celeste) -----
    public static final Color SIDEBAR = new Color(0x2E5B70);
    public static final Color PRIMARIO = new Color(0x3F6E85);
    public static final Color PRIMARIO_HOVER = new Color(0x4F8199);
    public static final Color FONDO = new Color(0xDCE9F0);
    public static final Color TARJETA = new Color(0xF7FAFC);
    public static final Color BORDE = new Color(0xC9DCE6);
    public static final Color TEXTO = new Color(0x1F4558);
    public static final Color TEXTO_SUAVE = new Color(0x6B8797);
    public static final Color CELESTE = new Color(0xB9D2DE);
    public static final Color VERDE_SUAVE = new Color(0xD6EFE8);
    public static final Color VERDE = new Color(0x3E8E7E);
    public static final Color NARANJA_SUAVE = new Color(0xFBE3D7);
    public static final Color NARANJA = new Color(0xD9825B);
    public static final Color ROJO_SUAVE = new Color(0xF6D9D9);
    public static final Color ROJO = new Color(0xB85C5C);

    public static final int RADIO = 18;

    private Tema() {
    }

    /** Look & Feel Nimbus (componentes redondeados de Java) con la paleta de SIGELAB. */
    public static void aplicar() {
        UIManager.put("control", FONDO);
        UIManager.put("nimbusBase", SIDEBAR);
        UIManager.put("nimbusBlueGrey", new Color(0xB4CBD7));
        UIManager.put("nimbusFocus", new Color(0x7FA6BA));
        UIManager.put("nimbusSelectionBackground", PRIMARIO);
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("text", TEXTO);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Table.alternateRowColor", new Color(0xEEF4F8));
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("TitledBorder.titleColor", TEXTO);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Si Nimbus no está disponible se usa el aspecto por defecto de Java.
        }
    }

    public static Font fuente(int estilo, float tamano) {
        return new Font("Segoe UI", estilo, Math.round(tamano));
    }

    public static JLabel etiqueta(String texto, int estilo, float tamano, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(estilo, tamano));
        l.setForeground(color);
        return l;
    }

    // ------------------------------------------------------------------ Botones

    public enum Estilo { PRIMARIO, SECUNDARIO, MENU, MENU_ACTIVO, PELIGRO }

    public static JButton boton(String texto, Estilo estilo) {
        return new BotonRedondeado(texto, estilo);
    }

    /** Botón con esquinas redondeadas y color de fondo propio. */
    public static class BotonRedondeado extends JButton {

        private Estilo estilo;

        public BotonRedondeado(String texto, Estilo estilo) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(fuente(Font.BOLD, 13));
            setEstilo(estilo);
        }

        public final void setEstilo(Estilo estilo) {
            this.estilo = estilo;
            boolean menu = estilo == Estilo.MENU || estilo == Estilo.MENU_ACTIVO;
            setHorizontalAlignment(menu ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(menu ? new EmptyBorder(10, 18, 10, 18) : new EmptyBorder(9, 18, 9, 18));
            setForeground(switch (estilo) {
                case PRIMARIO, PELIGRO -> Color.WHITE;
                case SECUNDARIO, MENU_ACTIVO -> TEXTO;
                case MENU -> new Color(0xE3EEF4);
            });
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean encima = getModel().isRollover() && isEnabled();
            Color fondo = switch (estilo) {
                case PRIMARIO -> encima ? PRIMARIO_HOVER : PRIMARIO;
                case PELIGRO -> encima ? NARANJA : ROJO;
                case SECUNDARIO -> encima ? CELESTE : Color.WHITE;
                case MENU_ACTIVO -> FONDO;
                case MENU -> encima ? new Color(255, 255, 255, 35) : null;
            };
            if (!isEnabled() && fondo != null) {
                fondo = new Color(fondo.getRed(), fondo.getGreen(), fondo.getBlue(), 120);
            }
            if (fondo != null) {
                g2.setColor(fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO));
            }
            if (estilo == Estilo.SECUNDARIO) {
                g2.setColor(BORDE);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------ Paneles

    /** Panel con esquinas redondeadas (tarjetas, barra lateral). */
    public static class PanelRedondeado extends JPanel {

        private final Color borde;
        private final int radio;

        public PanelRedondeado(LayoutManager layout, Color fondo, Color borde, int radio) {
            super(layout);
            this.borde = borde;
            this.radio = radio;
            setBackground(fondo);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radio, radio));
            if (borde != null) {
                g2.setColor(borde);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radio, radio));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tarjeta blanca redondeada con margen interno. */
    public static PanelRedondeado tarjeta(LayoutManager layout) {
        PanelRedondeado p = new PanelRedondeado(layout, TARJETA, BORDE, RADIO);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        return p;
    }

    /** Cuadro de color con un símbolo, como los iconos de las tarjetas del resumen. */
    public static JComponent insignia(String simbolo, Color fondo, Color color) {
        PanelRedondeado p = new PanelRedondeado(new GridBagLayout(), fondo, null, 12);
        p.setPreferredSize(new Dimension(42, 42));
        JLabel l = new JLabel(simbolo);
        l.setFont(new Font("Dialog", Font.BOLD, 18));
        l.setForeground(color);
        p.add(l);
        return p;
    }
}