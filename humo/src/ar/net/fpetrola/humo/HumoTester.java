/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultTreeModel;

public class HumoTester
{
    public static void main(String[] args) throws Exception
    {
	if (args.length == 0)
	    args= new String[] { "prueba+de+objetos2.humo" };

	String filename= args[0];

	JFrame jframe= new JFrame();
	createEnvironment(filename, jframe);
    }

    private static void createEnvironment(String aFilename, JFrame jframe)
    {
	JTextPane textPane= new JTextPane();
	JTextField textField= new JTextField(aFilename);

	ExecutionParserListener treeParserListener= new ExecutionParserListener();
	ProductionsParserListener productionsParserListener= new ProductionsParserListener();
	DebuggingParserListener debuggingParserListener= new DebuggingParserListener();
	ListenedParser parser= new ListenedParser(new ParserListenerMultiplexer(treeParserListener, productionsParserListener, debuggingParserListener), textPane);

	parser.getLoggingMap().log("begin parsing");
	boolean initialized= false;

	while (true)
	{
	    parser.setDisabled(false);

	    debuggingParserListener.stop();
	    String file= textField.getText();
	    StringBuilder sourceCode= new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream("/" + file)).useDelimiter("\\Z").next());

	    treeParserListener.init(file, !initialized);
	    productionsParserListener.init(file, !initialized);

	    ((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
	    ((DefaultTreeModel) treeParserListener.getUsedProductionsTree().getModel()).reload();
	    ((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

	    createTextPane(sourceCode, textPane);
	    if (!initialized)
	    {
		showTree(parser, sourceCode, textPane, debuggingParserListener, treeParserListener.getUsedProductionsTree(), treeParserListener.getExecutionTree(), productionsParserListener.getProductionsTree(), jframe, textField);
		initialized= true;
	    }
	    parser.init();
	    parser.parse(sourceCode, 0);
	    parser.getLoggingMap().log("end parsing");
	}
    }

    public static JTextPane createTextPane(StringBuilder sourceCode, JTextPane textPane)
    {
	StyleContext sc= new StyleContext();
	Style defaultStyle= sc.getStyle(StyleContext.DEFAULT_STYLE);
	StyleConstants.setFontFamily(defaultStyle, "monospaced");
	StyleConstants.setFontSize(defaultStyle, 11);
	StyleConstants.setBold(defaultStyle, true);

	Style cursorStyle= sc.addStyle("Cursor", null);
	StyleConstants.setForeground(cursorStyle, Color.RED);
	StyleConstants.setFontFamily(cursorStyle, "monospaced");
	StyleConstants.setFontSize(cursorStyle, 11);
	StyleConstants.setBold(cursorStyle, true);

	Style heading2Style= sc.addStyle("Heading2", null);
	StyleConstants.setForeground(heading2Style, Color.BLACK);
	StyleConstants.setFontFamily(heading2Style, "monospaced");
	StyleConstants.setFontSize(heading2Style, 11);
	StyleConstants.setBold(heading2Style, true);

	DefaultStyledDocument doc= new DefaultStyledDocument(sc);
	textPane.setDocument(doc);

	configureTextPane(sourceCode, textPane);
	textPane.setFont(new Font("Monospaced", Font.PLAIN, 11));

	return textPane;
    }

    public static void configureTextPane(StringBuilder sourceCode, JTextPane textPane)
    {
	try
	{
	    StyledDocument doc= (StyledDocument) textPane.getDocument();
	    Style heading2Style= doc.getStyle("Heading2");
	    doc.remove(0, doc.getLength());
	    doc.insertString(0, sourceCode.toString(), null);
	    for (int i= 0; i < sourceCode.length(); i++)
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

    public static void showTree(final ListenedParser parser, StringBuilder sourceCode, JTextPane textComponent, final DebuggingParserListener debuggingParserListener, JTree stacktraceTree, JTree executionTree, JTree productionsTree, final JFrame jframe, JTextField textField)
    {
	jframe.setLocation(100, 100);
	//jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	JScrollPane tree1= new JScrollPane(executionTree);
	executionTree.setPreferredSize(new Dimension(300, 300));

	final JScrollPane stacktraceTreePanel= new JScrollPane(stacktraceTree);
	stacktraceTree.setPreferredSize(new Dimension(300, 300));

	JScrollPane tree2= new JScrollPane(productionsTree);
	tree2.setPreferredSize(new Dimension(300, 300));
	JComponent textPanel= new JScrollPane(textComponent);
	JSplitPane newRightComponent= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree2, stacktraceTreePanel);
	newRightComponent.setDividerLocation(300);
	JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree1, newRightComponent);
	treesSplitPane.setDividerLocation(300);

	treesSplitPane.setPreferredSize(new Dimension(1000, 300));
	JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, textPanel);
	verticalSplitPane.setDividerLocation(200);

	jframe.setSize(900, 1000);
	jframe.setVisible(true);

	JToolBar toolBar= new JToolBar("Still draggable");
	JButton pauseButton= new JButton("pause");
	pauseButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.stop();
	    }
	});

	toolBar.add(pauseButton);
	JButton stepButton= new JButton("step");
	stepButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.step();
	    }
	});
	toolBar.add(stepButton);
	JButton continueButton= new JButton("continue");
	continueButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.continueExecution();
	    }
	});
	toolBar.add(continueButton);

	toolBar.add(textField);

	JButton loadButton= new JButton("load source file");
	loadButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		parser.setDisabled(true);
		debuggingParserListener.continueExecution();
	    }
	});
	toolBar.add(loadButton);

	JPanel mainPanel= new JPanel(new BorderLayout());
	mainPanel.add(toolBar, BorderLayout.PAGE_START);
	mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

	jframe.setContentPane(mainPanel);
    }
}