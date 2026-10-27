
package sigelab.view;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    public MainView() {
        setTitle("SIGELAB - Menú Principal");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel titulo = new JLabel(
                "SIGELAB - Sistema de Gestión de Laboratorios",
                SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel botones = new JPanel(new GridLayout(4, 2, 15, 15));

        JButton btnUsuarios = new JButton("Usuarios");
        JButton btnLaboratorios = new JButton("Laboratorios");
        JButton btnTipos = new JButton("Tipos de Equipo");
        JButton btnEquipos = new JButton("Equipos");

        JButton btnPrestamos = new JButton("Préstamos");
        JButton btnDevoluciones = new JButton("Devoluciones");
        JButton btnMantenimiento = new JButton("Mantenimiento");
        JButton btnReportes = new JButton("Reportes");

        botones.add(btnUsuarios);
        botones.add(btnLaboratorios);
        botones.add(btnTipos);
        botones.add(btnEquipos);
        botones.add(btnPrestamos);
        botones.add(btnDevoluciones);
        botones.add(btnMantenimiento);
        botones.add(btnReportes);

        // Módulos de la Etapa 2
        btnUsuarios.addActionListener(e ->
                new UsuarioView().setVisible(true));

        btnLaboratorios.addActionListener(e ->
                new LaboratorioView().setVisible(true));

        btnTipos.addActionListener(e ->
                new TipoEquipoView().setVisible(true));

        btnEquipos.addActionListener(e ->
                new EquipoView().setVisible(true));

        // Módulos pendientes de otras etapas
        btnPrestamos.setEnabled(false);
        btnDevoluciones.setEnabled(false);
        btnMantenimiento.setEnabled(false);
        btnReportes.setEnabled(false);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(botones, BorderLayout.CENTER);

        add(principal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MainView().setVisible(true));
    }
}
