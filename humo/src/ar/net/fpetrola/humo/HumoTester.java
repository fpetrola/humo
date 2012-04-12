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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
	DebuggingParserListener debuggingParserListener= new DebuggingParserListener();
	ParserListenerDelegator debugDelegator= new ParserListenerDelegator(debuggingParserListener, skipSmall.getModel(), skipSizeSpinner.getModel());
	HighlighterParserListener highlighterParserListener= new HighlighterParserListener(textPane, debugDelegator);
	ParserListenerMultiplexer parserListenerMultiplexer= new ParserListenerMultiplexer(highlighterParserListener, debugDelegator);
	ListenedParser parser= new ListenedParser(parserListenerMultiplexer);

	parser.getLoggingMap().log("begin parsing");
	boolean initialized= false;

	while (true)
	{
	    try
	    {
		parser.setDisabled(false);

		String file= filenameTextField.getText();
		StringBuilder sourcecode= new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream("/" + file)).useDelimiter("\\Z").next());
		StyledDocument doc= createAndSetupDocument(sourcecode);
		textPane.setDocument(doc);

		parserListenerMultiplexer.init(file, sourcecode, !initialized);
		treeParserListener.init(file, !initialized, sourcecode);
		productionsParserListener.init(file, !initialized);
		debuggingParserListener.init(file, sourcecode, !initialized);
		//		debuggingParserListener.pause();

		((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
		((DefaultTreeModel) debuggingParserListener.getUsedProductionsTree().getModel()).reload();
		((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

		if (!initialized)
		{
		    showTree(highlighterParserListener, debugDelegator, parser, sourcecode, textPane, debuggingParserListener, debuggingParserListener.getUsedProductionsTree(), treeParserListener.getExecutionTree(), productionsParserListener.getProductionsTree(), jframe, filenameTextField, skipSmall, skipSizeSpinner, parserListenerMultiplexer);
		    initialized= true;
		}
		parser.init();
		parser.parse(sourcecode, 0);
		parser.getLoggingMap().log("end parsing");
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    public static void showTree(final HighlighterParserListener highlighterParserListener, final ParserListenerDelegator debugDelegator, final ListenedParser parser, StringBuilder sourceCode, final JTextPane textPane, final DebuggingParserListener debuggingParserListener, JTree stacktraceTree, JTree executionTree, JTree productionsTree, final JFrame jframe, final JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner, final ParserListenerMultiplexer parserListenerMultiplexer)
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
	newRightComponent.setDividerLocation(900);

	treesSplitPane.setPreferredSize(new Dimension(1000, 300));
	JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, newRightComponent);
	verticalSplitPane.setDividerLocation(200);

	jframe.setSize(1200, 1000);
	jframe.setVisible(true);

	JToolBar toolBar= new JToolBar("debugger actions");
	JButton pauseButton= new JButton("next replacement");
	pauseButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugDelegator.setParserListenerDelegate(new DefaultParserListener()
		{
		    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
		    {
			if (debugDelegator.isVisible())
			    debuggingParserListener.pause();
		    }
		});
		debuggingParserListener.continueExecution();
	    }
	});

	toolBar.add(pauseButton);
	JButton stepButton= new JButton("step over");
	stepButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugDelegator.setNextVisibleFrame(null);

		final ParserListener parserListenerDelegate= debugDelegator.getParserListenerDelegate();
		debugDelegator.setNextVisibleFrame(parserListenerMultiplexer.getProductionFrames().peek());

		debugDelegator.setParserListenerDelegate(new DefaultParserListener()
		{
		    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
		    {
			if (debugDelegator.isVisible())
			    debuggingParserListener.pause();
		    }
		});
		debuggingParserListener.continueExecution();
	    }
	});
	toolBar.add(stepButton);

	JButton miniStepButton= new JButton("step into");
	miniStepButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugDelegator.setNextVisibleFrame(null);
		debugDelegator.setParserListenerDelegate(new DefaultParserListener()
		{
		    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
		    {
			if (debugDelegator.isVisible())
			    debuggingParserListener.pause();
		    }

		    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
		    {
			if (debugDelegator.isVisible())
			    debuggingParserListener.pause();
		    }
		});
		debuggingParserListener.continueExecution();
	    }
	});
	toolBar.add(miniStepButton);

	JButton stepoutButton= new JButton("stepout");
	stepoutButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		Stack<ProductionFrame> productionFrames= parserListenerMultiplexer.getProductionFrames();
		if (productionFrames.size() > 1)
		{
		    final ProductionFrame productionFrame= productionFrames.elementAt(productionFrames.size() - 2);
		    debugDelegator.setNextVisibleFrame(productionFrame);

		    debugDelegator.setParserListenerDelegate(new DefaultParserListener()
		    {
			public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
			{
			    if (debugDelegator.isVisible())
			    {
				debuggingParserListener.pause();
			    }
			}

			public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
			{
			    if (debugDelegator.isVisible())
			    {
				debuggingParserListener.pause();
			    }
			}
		    });

		    debuggingParserListener.continueExecution();
		}
	    }
	});
	toolBar.add(stepoutButton);

	JButton continueButton= new JButton("continue");
	continueButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debuggingParserListener.pause();
	    }
	});
	toolBar.add(continueButton);

	JButton loadButton= new JButton("load source file");
	loadButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		final JDialog openFileDialog= new JDialog();
		openFileDialog.setSize(600, 100);
		openFileDialog.setModal(true);
		openFileDialog.getContentPane().setLayout(new GridBagLayout());
		openFileDialog.add(textField);
		JButton load= new JButton("load");
		openFileDialog.add(load);
		load.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
			debuggingParserListener.pause();
			openFileDialog.setVisible(false);
		    }
		});

		openFileDialog.setVisible(true);
		parser.setDisabled(true);
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
		    highlighterParserListener.updateFrame(frame);
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

	addPopupMenu(textPane, debuggingParserListener, debugDelegator, parserListenerMultiplexer);

	JPanel mainPanel= new JPanel(new BorderLayout());
	mainPanel.add(toolBar, BorderLayout.PAGE_START);
	mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

	jframe.setContentPane(mainPanel);
    }
    private static void addPopupMenu(final JTextPane textPane, final DebuggingParserListener debuggingParserListener, final ParserListenerDelegator debugDelegator, final ParserListenerMultiplexer parserListenerMultiplexer)
    {
	final JPopupMenu menu= new JPopupMenu();
	JMenuItem menuItem= new JMenuItem("Run to this expression");
	menuItem.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugDelegator.setNextVisibleFrame(null);

		final ProductionFrame breakpointFrame= parserListenerMultiplexer.getProductionFrames().peek();
		debugDelegator.setNextVisibleFrame(breakpointFrame);
		final int start= textPane.getDocument().getLength() - textPane.getSelectionStart();
		final int end= textPane.getDocument().getLength() - textPane.getSelectionEnd();

		debugDelegator.setParserListenerDelegate(new DefaultParserListener()
		{
		    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
		    {
			if (debugDelegator.isVisible() && currentFrame == breakpointFrame)
			{
			    int length= currentFrame.getProduction().length();
			    if ((length - last) <= start && (length - last) >= end)
				debuggingParserListener.pause();
			}
		    }
		});
		debuggingParserListener.continueExecution();
	    }
	});

	menu.add(menuItem);

	textPane.addMouseListener(new MouseAdapter()
	{
	    public void mousePressed(MouseEvent evt)
	    {
		if (evt.isPopupTrigger())
		{
		    menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	    }
	    public void mouseReleased(MouseEvent evt)
	    {
		if (evt.isPopupTrigger())
		{
		    menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	    }
	});
    }

    public static StyledDocument createStyleDocument()
    {
	StyleContext styleContext= new StyleContext();
	createStyle(styleContext, DEFAULT_STYLE, Color.black, "monospaced", Color.white, 11, null);
	createStyle(styleContext, "Cursor", new Color(0.8f, 0, 0), "monospaced", Color.white, 11, null);
	createStyle(styleContext, CURLY_STYLE, Color.BLACK, "monospaced", Color.WHITE, 11, true);
	createStyle(styleContext, FETCH_STYLE, new Color(0, 0.5f, 0), "monospaced", new Color(0.95f, 0.95f, 0.95f), 11, null);
	createStyle(styleContext, "production-matching", Color.BLUE, "monospaced", Color.WHITE, 11, true);
	return new DefaultStyledDocument(styleContext);
    }

    public static void createStyle(StyleContext sc, String aName, Color aForegroundColor, String aFontFamily, Color aBackgroundColor, int aFontSize, Boolean isBold)
    {
	Style cursorStyle= sc.addStyle(aName, null);
	StyleConstants.setForeground(cursorStyle, aForegroundColor);
	StyleConstants.setFontFamily(cursorStyle, aFontFamily);
	StyleConstants.setBackground(cursorStyle, aBackgroundColor);
	StyleConstants.setFontSize(cursorStyle, aFontSize);
	if (isBold != null)
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