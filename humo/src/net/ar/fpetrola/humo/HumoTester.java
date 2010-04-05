package net.ar.fpetrola.humo;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class HumoTester
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        args = new String[] { "tests/prueba+de+objetos2.humo" };

        StringBuilder sourceCode = new StringBuilder(new Scanner(new File(args[0])).useDelimiter("\\Z").next().replaceAll("[\\x0D \\x0A \\x20  \\x09]", ""));

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("humo source file: " + args[0]);
        ListenedParser parser = new ListenedParser(new TreeParserListener(raiz));

        parser.getLoggingMap().log("begin parsing");
        showTree(raiz);
        parser.parse(sourceCode, 0);
        parser.getLoggingMap().log("end parsing");
    }

    public static void showTree(DefaultMutableTreeNode aRoot)
    {
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.getContentPane().add(new JScrollPane(new JTree(aRoot)), BorderLayout.CENTER);
        jframe.setSize(800, 1000);
        jframe.setVisible(true);
    }
}