/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import com.digitprop.tonic.TonicLookAndFeel;

public class HumoTester
{
    public static void main(String[] args) throws Exception
    {
	UIManager.setLookAndFeel(new TonicLookAndFeel());

	if (args.length == 0)
	    args= new String[] { "prueba+de+objetos2.humo" };

	String filename= args[0];

	JFrame jframe= new JFrame();

	createEnvironment(filename, jframe);
    }

    private static void createEnvironment(String aFilename, JFrame jframe)
    {
	JTextPane textPane= new JTextPane()
	{
	    public boolean getScrollableTracksViewportWidth()
	    {
		return getUI().getPreferredSize(this).width <= getParent().getSize().width;
	    }
	};

	JTextField filenameTextField= new JTextField(aFilename);
	JSpinner skipSizeSpinner= new JSpinner(new SpinnerNumberModel(50, 0, 100000, 1000));
	JCheckBox skipSmall= new JCheckBox("skip productions smaller than:");
	JCheckBox skipAll= new JCheckBox("skip all  ");

	DebuggerParserListener debugListener= new DebuggerParserListener(skipSmall.getModel(), skipSizeSpinner.getModel(), skipAll.getModel());
	CallStackParserListener callStackParserListener= new CallStackParserListener(debugListener);
	HighlighterParserListener highlighterParserListener= new HighlighterParserListener(textPane, debugListener);
	ProductionsParserListener productionsParserListener= new ProductionsParserListener(debugListener);
	ExecutionParserListener treeParserListener= new ExecutionParserListener(debugListener);
	ParserListenerMultiplexer parserListenerMultiplexer= new ParserListenerMultiplexer(productionsParserListener, treeParserListener, highlighterParserListener, callStackParserListener, debugListener);
	debugListener.setProductionFrames(parserListenerMultiplexer.getProductionFrames());
	ListenedParser parser= new ListenedParser(parserListenerMultiplexer);
	debugListener.stepInto();

	parser.getLoggingMap().log("begin parsing");
	boolean initialized= false;

	while (true)
	{
	    try
	    {
		parser.setDisabled(false);

		String file= filenameTextField.getText();
		StringBuilder sourcecode= new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream("/" + file)).useDelimiter("\\Z").next());

		parserListenerMultiplexer.init(file, sourcecode, !initialized);
		treeParserListener.init(file, !initialized, sourcecode);
		productionsParserListener.init(file, !initialized);
		callStackParserListener.init(file, sourcecode, !initialized);
		textPane.setDocument(parserListenerMultiplexer.getCurrentFrame().getDocument());
		debugListener.stepInto();

		((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
		((DefaultTreeModel) callStackParserListener.getUsedProductionsTree().getModel()).reload();
		((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

		if (!initialized)
		{
		    showTree(highlighterParserListener, debugListener, parser, sourcecode, textPane, callStackParserListener.getUsedProductionsTree(), treeParserListener.getExecutionTree(), productionsParserListener.getProductionsTree(), jframe, filenameTextField, skipSmall, skipSizeSpinner, parserListenerMultiplexer, skipAll);
		    initialized= true;
		}
		parser.init();
		parser.parse(sourcecode, 0);
		parser.getLoggingMap().log("end parsing");
		debugListener.getStepper().pause();
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    public static void showTree(final HighlighterParserListener highlighterParserListener, final DebuggerParserListener debugListener, final ListenedParser parser, StringBuilder sourceCode, final JTextPane textPane, JTree stacktraceTree, JTree executionTree, JTree productionsTree, final JFrame jframe, final JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner, final ParserListenerMultiplexer parserListenerMultiplexer, JCheckBox skipAll)
    {
	jframe.setLocation(100, 100);

	JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(executionTree), new JScrollPane(productionsTree));
	treesSplitPane.setDividerLocation(300);
	JScrollPane textPanelScrollPane= new JScrollPane(textPane);
	JSplitPane newRightComponent= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, textPanelScrollPane, new JScrollPane(stacktraceTree));
	newRightComponent.setDividerLocation(900);
	JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, newRightComponent);
	verticalSplitPane.setDividerLocation(200);

	addPopupMenu(textPane, debugListener);

	JPanel mainPanel= new JPanel(new BorderLayout());

	JToolBar toolBar= createToolbar(highlighterParserListener, debugListener, parser, stacktraceTree, textField, skipSmall, skipSizeSpinner, skipAll, textPane);
	mainPanel.add(toolBar, BorderLayout.PAGE_START);
	mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

	jframe.setContentPane(mainPanel);
	jframe.setSize(1200, 1000);
	jframe.setVisible(true);
    }

    private static JToolBar createToolbar(final HighlighterParserListener highlighterParserListener, final DebuggerParserListener debugListener, final ListenedParser parser, JTree stacktraceTree, final JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner, JCheckBox skipAll, final JTextPane textPane)
    {
	JToolBar toolBar= new JToolBar("debugger actions");
	toolBar.setPreferredSize(new Dimension(1200, 40));

	toolBar.add(new JSeparator(SwingConstants.VERTICAL));

	JButton nextReplacementButton= new JButton("next replacement", new ImageIcon(HumoTester.class.getResource("/images/runtocomp.gif")));
	nextReplacementButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.runToNextReplacement();
	    }
	});
	addButtonToToolbar(toolBar, nextReplacementButton);

	toolBar.add(new JSeparator(SwingConstants.VERTICAL));

	JButton stepButton= new JButton("step over", new ImageIcon(HumoTester.class.getResource("/images/stepover.gif")));
	stepButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.stepOver();
	    }
	}));

	addButtonToToolbar(toolBar, stepButton);

	JButton miniStepButton= new JButton("step into", new ImageIcon(HumoTester.class.getResource("/images/stepinto.gif")));
	miniStepButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.stepInto();
	    }
	}));
	addButtonToToolbar(toolBar, miniStepButton);

	JButton stepoutButton= new JButton("step out", new ImageIcon(HumoTester.class.getResource("/images/stepreturn.gif")));
	stepoutButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.stepOut();
	    }
	}));
	addButtonToToolbar(toolBar, stepoutButton);

	JButton runToSelectionButton= new JButton("run to selection", new ImageIcon(HumoTester.class.getResource("/images/stepintosp.gif")));
	runToSelectionButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		final int start= textPane.getDocument().getLength() - textPane.getSelectionStart();
		final int end= textPane.getDocument().getLength() - textPane.getSelectionEnd();
		debugListener.runToExpression(start, end);
	    }
	}));
	addButtonToToolbar(toolBar, runToSelectionButton);

	JButton continueButton= new JButton("continue", new ImageIcon(HumoTester.class.getResource("/images/resume.gif")));
	continueButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.continueExecution();
	    }
	}));
	addButtonToToolbar(toolBar, continueButton);

	JButton pauseButton= new JButton("pause", new ImageIcon(HumoTester.class.getResource("/images/threads_obj.gif")));
	pauseButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		debugListener.stepInto();
	    }
	}));
	addButtonToToolbar(toolBar, pauseButton);

	toolBar.add(new JSeparator(SwingConstants.VERTICAL));

	JButton loadButton= new JButton("load source file", new ImageIcon(HumoTester.class.getResource("/images/schema_variable.gif")));
	loadButton.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		SwingUtilities.invokeLater(new Runnable()
		{
		    public void run()
		    {
			String str= (String) JOptionPane.showInputDialog(null, "Enter file name: ", "Open a new source code", JOptionPane.QUESTION_MESSAGE, null, null, textField.getText());
			if (str != null)
			{
			    textField.setText(str);
			    debugListener.continueExecution();
			}

			parser.setDisabled(true);
		    }
		});
	    }
	}));

	stacktraceTree.addTreeSelectionListener(new TreeSelectionListener()
	{
	    public void valueChanged(TreeSelectionEvent e)
	    {
		if (e.getNewLeadSelectionPath() != null)
		{
		    Object lastPathComponent= e.getNewLeadSelectionPath().getLastPathComponent();
		    StacktraceTreeNode stacktraceTreeNode= (StacktraceTreeNode) lastPathComponent;
		    ProductionFrame frame= stacktraceTreeNode.getFrame();
		    if (frame != null)
			highlighterParserListener.updateFrame(frame);
		}
	    }
	});

	addButtonToToolbar(toolBar, loadButton);

	toolBar.add(new JSeparator(SwingConstants.VERTICAL));

	skipSmall.setSelected(true);
	skipSmall.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		skipSizeSpinner.setEnabled(skipSmall.isSelected());
	    }
	}));

	toolBar.add(skipAll);
	toolBar.add(skipSmall);
	toolBar.add(skipSizeSpinner);
	return toolBar;
    }

    private static Component addButtonToToolbar(JToolBar toolBar, JButton aButton)
    {
	Font font= new Font("Verdana", Font.PLAIN, 12);
	aButton.setFont(font);
	return toolBar.add(aButton);
    }
    public static void addPopupMenu(final JTextPane textPane, final DebuggerParserListener debugDelegator)
    {
	final JPopupMenu menu= new JPopupMenu();
	JMenuItem menuItem= new JMenuItem("Run to this expression");
	menuItem.addActionListener(new ThreadSafeActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		final int start= textPane.getDocument().getLength() - textPane.getSelectionStart();
		final int end= textPane.getDocument().getLength() - textPane.getSelectionEnd();

		debugDelegator.runToExpression(start, end);
	    }
	}));

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

}