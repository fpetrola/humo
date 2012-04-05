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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultTreeModel;

public class HumoTester
{
    public static final String DEFAULT_STYLE= "default";
    public static final String FETCH_STYLE= "fetch";
    public static final String CURLY_STYLE= "curly";

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
	//	textPane.setFont(new Font("Monospaced", Font.PLAIN, 11));

	JTextField filenameTextField= new JTextField(aFilename);
	JSpinner skipSizeSpinner= new JSpinner(new SpinnerNumberModel(50, 0, 100000, 1000));
	JCheckBox skipSmall= new JCheckBox("skip productions smaller than:");

	ExecutionParserListener treeParserListener= new ExecutionParserListener();
	ProductionsParserListener productionsParserListener= new ProductionsParserListener();
	DebuggingParserListener debuggingParserListener= new DebuggingParserListener(skipSmall.getModel(), textPane, skipSizeSpinner);
	ListenedParser parser= new ListenedParser(new ParserListenerMultiplexer(treeParserListener, productionsParserListener, debuggingParserListener));

	parser.getLoggingMap().log("begin parsing");
	boolean initialized= false;

	while (true)
	{
	    parser.setDisabled(false);

	    debuggingParserListener.getStepper().stop();
	    String file= filenameTextField.getText();
	    StringBuilder sourceCode= new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream("/" + file)).useDelimiter("\\Z").next());
	    StyledDocument doc= createAndSetupDocument(sourceCode);
	    textPane.setDocument(doc);

	    treeParserListener.init(file, !initialized, sourceCode);
	    productionsParserListener.init(file, !initialized);
	    debuggingParserListener.init(file, sourceCode, !initialized);

	    ((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
	    ((DefaultTreeModel) debuggingParserListener.getUsedProductionsTree().getModel()).reload();
	    ((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

	    if (!initialized)
	    {
		showTree(parser, sourceCode, textPane, debuggingParserListener, debuggingParserListener.getUsedProductionsTree(), treeParserListener.getExecutionTree(), productionsParserListener.getProductionsTree(), jframe, filenameTextField, skipSmall, skipSizeSpinner);
		initialized= true;
	    }
	    parser.init();
	    parser.parse(sourceCode, 0);
	    debuggingParserListener.getStepper().performStep("finish");
	    parser.getLoggingMap().log("end parsing");
	}
    }

    public static void showTree(final ListenedParser parser, StringBuilder sourceCode, final JTextPane textPane, final DebuggingParserListener debuggingParserListener, JTree stacktraceTree, JTree executionTree, JTree productionsTree, final JFrame jframe, JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner)
    {
	jframe.setLocation(100, 100);
	//jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	JScrollPane tree1= new JScrollPane(executionTree);
	executionTree.setPreferredSize(new Dimension(300, 300));

	final JScrollPane stacktraceTreePanel= new JScrollPane(stacktraceTree);
	stacktraceTree.setPreferredSize(new Dimension(300, 300));

	JScrollPane tree2= new JScrollPane(productionsTree);
	tree2.setPreferredSize(new Dimension(300, 300));
	JComponent textPanel= new JScrollPane(textPane);
	JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tree1, tree2);
	treesSplitPane.setDividerLocation(300);

	JSplitPane newRightComponent= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, textPanel, stacktraceTreePanel);
	newRightComponent.setDividerLocation(700);

	treesSplitPane.setPreferredSize(new Dimension(1000, 300));
	JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, newRightComponent);
	verticalSplitPane.setDividerLocation(200);

	jframe.setSize(900, 1000);
	jframe.setVisible(true);

	JToolBar toolBar= new JToolBar("Still draggable");
	JButton pauseButton= new JButton("next replacement");
	pauseButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.getStepper().step("afterProductionFound");
	    }
	});

	toolBar.add(pauseButton);
	JButton stepButton= new JButton("step");
	stepButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.getStepper().step("afterProductionFound", "beforeParseProductionBody", "afterProductionReplacement");
	    }
	});
	toolBar.add(stepButton);

	JButton miniStepButton= new JButton("mini-step");
	miniStepButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.getStepper().step("startParsingLoop", "afterProductionFound");
	    }
	});
	toolBar.add(miniStepButton);

	JButton stepoutButton= new JButton("stepout");
	stepoutButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		ProductionFrame productionFrame= debuggingParserListener.getProductionFrames().peek();
		debuggingParserListener.getStepper().step("beforeProductionReplacement:" + productionFrame.getName());
	    }
	});
	toolBar.add(stepoutButton);

	JButton continueButton= new JButton("continue");
	continueButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.getStepper().step("finish");
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
		debuggingParserListener.getStepper().continueExecution();
	    }
	});

	stacktraceTree.addTreeSelectionListener(new TreeSelectionListener()
	{
	    public void valueChanged(TreeSelectionEvent e)
	    {
		if (e.getNewLeadSelectionPath() != null)
		{
		    Object lastPathComponent= e.getNewLeadSelectionPath().getLastPathComponent();
		    StacktraceTreeNode stacktraceTreeNode= (StacktraceTreeNode) lastPathComponent;
		    ProductionFrame frame= stacktraceTreeNode.getFrame();

		    textPane.setDocument(frame.getDocument());
		    DebuggingParserListener.updateCaretPosition(textPane, frame);
		    //debuggingParserListener.updateFrame(stacktraceTreeNode.getFrame());
		}
	    }
	});

	toolBar.add(loadButton);

	skipSmall.setSelected(true);
	skipSmall.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		skipSizeSpinner.setEnabled(skipSmall.isSelected());
	    }
	});

	toolBar.add(skipSmall);
	toolBar.add(skipSizeSpinner);

	JPanel mainPanel= new JPanel(new BorderLayout());
	mainPanel.add(toolBar, BorderLayout.PAGE_START);
	mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

	jframe.setContentPane(mainPanel);
    }

    public static StyledDocument createStyleDocument()
    {
	StyleContext styleContext= new StyleContext();
	createStyle(styleContext, DEFAULT_STYLE, Color.black, "monospaced", Color.white, 11, false);
	createStyle(styleContext, "Cursor", new Color(0.8f, 0, 0), "monospaced", Color.white, 11, true);
	createStyle(styleContext, CURLY_STYLE, Color.BLACK, "monospaced", Color.WHITE, 11, true);
	createStyle(styleContext, FETCH_STYLE, new Color(0, 0.5f, 0), "monospaced", new Color(0.95f, 0.95f, 0.95f), 11, true);
	createStyle(styleContext, "production-matching", Color.BLUE, "monospaced", Color.WHITE, 11, true);
	return new DefaultStyledDocument(styleContext);
    }

    public static void createStyle(StyleContext sc, String aName, Color aForegroundColor, String aFontFamily, Color aBackgroundColor, int aFontSize, boolean isBold)
    {
	Style cursorStyle= sc.addStyle(aName, null);
	StyleConstants.setForeground(cursorStyle, aForegroundColor);
	StyleConstants.setFontFamily(cursorStyle, aFontFamily);
	StyleConstants.setBackground(cursorStyle, aBackgroundColor);
	StyleConstants.setFontSize(cursorStyle, aFontSize);
	StyleConstants.setBold(cursorStyle, isBold);
    }

    public static StyledDocument createAndSetupDocument(StringBuilder sourceCode)
    {
	try
	{
	    StyledDocument styleDocument= createStyleDocument();

	    Style curly= styleDocument.getStyle(CURLY_STYLE);
	    Style defaultStyle= styleDocument.getStyle(DEFAULT_STYLE);
	    styleDocument.remove(0, styleDocument.getLength());
	    styleDocument.insertString(0, sourceCode.toString(), null);
	    for (int i= 0; i < sourceCode.length(); i++)
	    {
		if (sourceCode.charAt(i) == '{' || sourceCode.charAt(i) == '}')
		    styleDocument.setCharacterAttributes(i, 1, curly, false);
		else
		    styleDocument.setCharacterAttributes(i, 1, defaultStyle, false);
	    }

	    return styleDocument;
	}
	catch (BadLocationException e)
	{
	    throw new RuntimeException(e);
	}
    }

}