package sigelab.view;

import javax.swing.*;
import java.awt.*;

/**
 * FlowLayout que SÍ pasa los componentes a la siguiente fila cuando no caben.
 * Con FlowLayout normal, los botones que no caben en el ancho quedan ocultos.
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout() {
        super();
    }

    public WrapLayout(int alineacion, int hgap, int vgap) {
        super(alineacion, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container destino) {
        return calcularTamano(destino, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container destino) {
        Dimension minimo = calcularTamano(destino, false);
        minimo.width -= (getHgap() + 1);
        return minimo;
    }

    private Dimension calcularTamano(Container destino, boolean preferido) {
        synchronized (destino.getTreeLock()) {
            int anchoDestino = destino.getSize().width;
            Container contenedor = destino;
            while (contenedor.getSize().width == 0 && contenedor.getParent() != null) {
                contenedor = contenedor.getParent();
            }
            anchoDestino = contenedor.getSize().width;
            if (anchoDestino == 0) {
                anchoDestino = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets bordes = destino.getInsets();
            int margen = bordes.left + bordes.right + (hgap * 2);
            int anchoMaximo = anchoDestino - margen;

            Dimension total = new Dimension(0, 0);
            int anchoFila = 0;
            int altoFila = 0;

            for (Component c : destino.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = preferido ? c.getPreferredSize() : c.getMinimumSize();
                if (anchoFila + d.width > anchoMaximo) {
                    agregarFila(total, anchoFila, altoFila);
                    anchoFila = 0;
                    altoFila = 0;
                }
                if (anchoFila != 0) {
                    anchoFila += hgap;
                }
                anchoFila += d.width;
                altoFila = Math.max(altoFila, d.height);
            }
            agregarFila(total, anchoFila, altoFila);

            total.width += margen;
            total.height += bordes.top + bordes.bottom + vgap * 2;

            // Dentro de un JScrollPane, no forzar un ancho mayor al visible.
            Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, destino);
            if (scroll != null && destino.isValid()) {
                total.width -= (hgap + 1);
            }
            return total;
        }
    }

    private void agregarFila(Dimension total, int anchoFila, int altoFila) {
        total.width = Math.max(total.width, anchoFila);
        if (total.height > 0) {
            total.height += getVgap();
        }
        total.height += altoFila;
    }
}