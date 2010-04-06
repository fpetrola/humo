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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class HumoTester
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        if (args.length == 0)
            args = new String[] { "tests/prueba+de+objetos2.humo" };

        String filename = args[0];

        StringBuilder sourceCode = new StringBuilder(new Scanner(new File(filename)).useDelimiter("\\Z").next());

        JTextArea textArea = new JTextArea(sourceCode.toString());
        ExecutionParserListener treeParserListener = new ExecutionParserListener(filename);
        ProductionsParserListener productionsParserListener = new ProductionsParserListener(filename);
        ListenedParser parser = new ListenedParser(new ParserListenerMultiplexer(treeParserListener, productionsParserListener), textArea);

        parser.getLoggingMap().log("begin parsing");
        showTree(sourceCode, treeParserListener.getRoot(), productionsParserListener.getRoot(), textArea);
        parser.parse(sourceCode, 0);
        parser.getLoggingMap().log("end parsing");
    }

    public static void showTree(StringBuilder sourceCode, DefaultMutableTreeNode executionRoot, DefaultMutableTreeNode productionsRoot, JTextArea textArea)
    {
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane tree1 = new JScrollPane(new JTree(executionRoot));
        JScrollPane tree2 = new JScrollPane(new JTree(productionsRoot));
        JComponent textPanel = new JScrollPane(textArea);
        jframe.setContentPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree1, tree2), textPanel));

        jframe.setSize(800, 1000);
        jframe.setVisible(true);
    }
}