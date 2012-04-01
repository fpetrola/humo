/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;

public class HumoTester
{
    public static void main(String[] args) throws Exception
    {
        if (args.length == 0)
            args = new String[] { "/prueba+de+objetos2.humo" };

        String filename = args[0];


        StringBuilder sourceCode = new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream(filename)).useDelimiter("\\Z").next());

        JTextPane textPane = new JTextPane();
        createTextPane(textPane, sourceCode);

        ExecutionParserListener treeParserListener = new ExecutionParserListener(filename);
        ProductionsParserListener productionsParserListener = new ProductionsParserListener(filename);
        ListenedParser parser = new ListenedParser(new ParserListenerMultiplexer(treeParserListener, productionsParserListener), textPane);

        parser.getLoggingMap().log("begin parsing");
        showTree(sourceCode, treeParserListener.getRoot(), productionsParserListener.getRoot(), textPane);
        parser.parse(sourceCode, 0);
        parser.getLoggingMap().log("end parsing");
    }

    public static JTextPane createTextPane(JTextPane textPane, StringBuilder sourceCode)
    {
        StyleContext sc = new StyleContext();
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "monospaced");

        Style cursorStyle = sc.addStyle("Cursor", null);
        StyleConstants.setForeground(cursorStyle, Color.RED);
        StyleConstants.setFontFamily(cursorStyle, "monospaced");
        StyleConstants.setBold(cursorStyle, true);

        Style heading2Style = sc.addStyle("Heading2", null);
        StyleConstants.setForeground(heading2Style, Color.BLACK);
        StyleConstants.setFontFamily(heading2Style, "monospaced");
        StyleConstants.setBold(heading2Style, true);

        DefaultStyledDocument doc = new DefaultStyledDocument(sc);
        textPane.setDocument(doc);

        configureTextPane(sourceCode, textPane);
        return textPane;
    }

    public static void configureTextPane(StringBuilder sourceCode, JTextPane textPane)
    {
        try
        {
            StyledDocument doc = (StyledDocument) textPane.getDocument();
            Style heading2Style = doc.getStyle("Heading2");
            doc.remove(0, doc.getLength());
            doc.insertString(0, sourceCode.toString(), null);
            for (int i = 0; i < sourceCode.length(); i++)
            {
                if (sourceCode.charAt(i) == '{' || sourceCode.charAt(i) == '}')
                    doc.setCharacterAttributes(i, 1, heading2Style, false);
            }
        }
        catch (BadLocationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void showTree(StringBuilder sourceCode, DefaultMutableTreeNode executionRoot, DefaultMutableTreeNode productionsRoot, JComponent textComponent)
    {
        JFrame jframe = new JFrame();
//        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane tree1 = new JScrollPane(new JTree(executionRoot));
        JScrollPane tree2 = new JScrollPane(new JTree(productionsRoot));
        JComponent textPanel = new JScrollPane(textComponent);
        JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree1, tree2);
        treesSplitPane.setSize(new Dimension(100, 300));
	jframe.setContentPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, textPanel));

        jframe.setSize(800, 1000);
        jframe.setVisible(true);
    }
}