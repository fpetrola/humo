/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class HumoTester
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        args = new String[] { "tests/prueba+de+objetos2.humo" };

        StringBuilder sourceCode = new StringBuilder(new Scanner(new File(args[0])).useDelimiter("\\Z").next().replaceAll("[\\x0D \\x0A \\x20  \\x09]", ""));

        DefaultMutableTreeNode executionRoot = new DefaultMutableTreeNode("Humo source file: " + args[0]);
        DefaultMutableTreeNode productionsRoot = new DefaultMutableTreeNode("Productions of: " + args[0]);
        ListenedParser parser = new ListenedParser(new ParserListenerMultiplexer(new TreeParserListener(executionRoot), new ProductionsParserListener(productionsRoot)));

        parser.getLoggingMap().log("begin parsing");
        showTree(executionRoot, productionsRoot);
        parser.parse(sourceCode, 0);
        parser.getLoggingMap().log("end parsing");
    }

    public static void showTree(DefaultMutableTreeNode executionRoot, DefaultMutableTreeNode productionsRoot)
    {
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane tree1 = new JScrollPane(new JTree(executionRoot));
        JScrollPane tree2 = new JScrollPane(new JTree(productionsRoot));
        jframe.setContentPane(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree1, tree2));

        jframe.setSize(800, 1000);
        jframe.setVisible(true);
    }
}