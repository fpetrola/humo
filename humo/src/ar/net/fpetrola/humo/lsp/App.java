package ar.net.fpetrola.humo.lsp;

import javax.swing.*;
import java.awt.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        JFrame frame = new JFrame("Mi App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 800);

        // Panel 3D como cualquier componente Swing
        Spectrum3DPanel view3d = new Spectrum3DPanel(new Spectrum3DScreen());
        frame.add(view3d, BorderLayout.CENTER);

        // Podés agregar otros paneles Swing al lado
        JPanel sidebar = new JPanel();
        sidebar.add(new JButton("Controles"));
        frame.add(sidebar, BorderLayout.EAST);

        frame.setVisible(true);                                    }
}
