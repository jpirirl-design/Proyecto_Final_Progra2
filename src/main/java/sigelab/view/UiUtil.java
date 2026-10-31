package sigelab.view;

import javax.swing.*;
import java.awt.*;

/** Utilidades comunes para que todas las ventanas se vean completas en cualquier pantalla. */
public final class UiUtil {

    private UiUtil() {
    }

    /**
     * Da a la ventana el tamaño pedido, pero nunca más grande que la pantalla
     * (descontando la barra de tareas). Así no quedan botones fuera de la vista.
     */
    public static void ajustarAPantalla(JFrame ventana, int ancho, int alto) {
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int w = Math.min(ancho, area.width - 20);
        int h = Math.min(alto, area.height - 20);
        ventana.setSize(w, h);
        ventana.setMinimumSize(new Dimension(Math.min(640, w), Math.min(420, h)));
        ventana.setResizable(true);
        ventana.setLocationRelativeTo(null);
    }

    /** Panel de botones que pasa a otra fila cuando no caben (en vez de ocultarlos). */
    public static JPanel panelBotones(JButton... botones) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 6));
        panel.setOpaque(false);
        for (JButton b : botones) {
            panel.add(b);
        }
        return panel;
    }

    /** Botón "Cerrar" que cierra solo esta ventana. */
    public static JButton botonCerrar(JFrame ventana) {
        JButton cerrar = Tema.boton("Cerrar", Tema.Estilo.SECUNDARIO);
        cerrar.addActionListener(e -> ventana.dispose());
        return cerrar;
    }
}