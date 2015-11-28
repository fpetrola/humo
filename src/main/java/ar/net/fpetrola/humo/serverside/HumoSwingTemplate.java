package ar.net.fpetrola.humo.serverside;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
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
import javax.swing.tree.DefaultTreeCellRenderer;

import ar.net.fpetrola.humo.HumoIDE;

public class HumoSwingTemplate
{
    public static void main(String[] args) throws Exception
    {
	JFrame jframe= new JFrame();
	createEnvironment(jframe);
    }

    public static JPanel createEnvironment(JFrame jframe)
    {
	JTextPane textPane= new JTextPane()
	{
	    public boolean getScrollableTracksViewportWidth()
	    {
		return getUI().getPreferredSize(this).width <= getParent().getSize().width;
	    }
	};
	textPane.setName("textPane");

	JTextField filenameTextField= new JTextField("");
	JSpinner skipSizeSpinner= new JSpinner(new SpinnerNumberModel(50, 0, 100000, 1000));
	JCheckBox skipSmall= new JCheckBox("skip productions smaller than:");
	JCheckBox skipAll= new JCheckBox("skip all  ");

	JTree usedProductionsTree= getUsedProductionsTree();
	JTree executionTree= getExecutionTree();
	JTree productionsTree= getProductionsTree();

	return showTree(textPane, usedProductionsTree, executionTree, productionsTree, jframe, filenameTextField, skipSmall, skipSizeSpinner, skipAll);
    }

    protected static JTree getUsedProductionsTree()
    {
	DefaultTreeCellRenderer renderer= new DefaultTreeCellRenderer();
	Icon customOpenIcon= new ImageIcon(HumoIDE.class.getResource("/images/stckframe.gif"));
	Icon customClosedIcon= new ImageIcon(HumoIDE.class.getResource("/images/stckframe.gif"));
	renderer.setOpenIcon(customOpenIcon);
	renderer.setClosedIcon(customClosedIcon);
	JTree jTree= new JTree();
	jTree.setCellRenderer(renderer);
	return jTree;
    }

    protected static JTree getExecutionTree()
    {
	JTree executionTree= new JTree();
	DefaultTreeCellRenderer renderer= new DefaultTreeCellRenderer();
	Icon customOpenIcon= new ImageIcon(HumoIDE.class.getResource("/images/ebrkpnt_green.gif"));
	Icon customClosedIcon= new ImageIcon(HumoIDE.class.getResource("/images/ebrkpnt.gif"));
	renderer.setOpenIcon(customOpenIcon);
	renderer.setClosedIcon(customClosedIcon);
	executionTree.setCellRenderer(renderer);
	return executionTree;
    }

    protected static JTree getProductionsTree()
    {
	JTree productionsTree= new JTree();
	DefaultTreeCellRenderer renderer= new DefaultTreeCellRenderer();
	Icon customOpenIcon= new ImageIcon(HumoIDE.class.getResource("/images/scalarvar.gif"));
	Icon customClosedIcon= new ImageIcon(HumoIDE.class.getResource("/images/genericvariable.gif"));
	renderer.setOpenIcon(customOpenIcon);
	renderer.setClosedIcon(customClosedIcon);
	productionsTree.setCellRenderer(renderer);
	return productionsTree;
    }

    public static JPanel showTree(final JTextPane textPane, JTree stacktraceTree, JTree executionTree, JTree productionsTree, final JFrame jframe, final JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner, JCheckBox skipAll)
    {
	jframe.setLocation(100, 100);

	JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(executionTree), new JScrollPane(productionsTree));
	treesSplitPane.setDividerLocation(300);
	JScrollPane textPanelScrollPane= new JScrollPane(textPane);
	JSplitPane newRightComponent= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, textPanelScrollPane, new JScrollPane(stacktraceTree));
	newRightComponent.setDividerLocation(900);
	JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, newRightComponent);
	verticalSplitPane.setDividerLocation(200);

	addPopupMenu(textPane);

	JPanel mainPanel= new JPanel(new BorderLayout());

	JToolBar toolBar= createToolbar(stacktraceTree, textField, skipSmall, skipSizeSpinner, skipAll, textPane);
	mainPanel.add(toolBar, BorderLayout.PAGE_START);
	mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

	jframe.setContentPane(mainPanel);
	jframe.setSize(1200, 1000);
	jframe.setVisible(true);

	return mainPanel;
    }

    private static JToolBar createToolbar(JTree stacktraceTree, final JTextField textField, final JCheckBox skipSmall, final JSpinner skipSizeSpinner, JCheckBox skipAll, final JTextPane textPane)
    {
	JToolBar toolBar= new JToolBar();

	JButton nextReplacementButton= new JButton("next replacement", new ImageIcon(HumoIDE.class.getResource("/images/runtocomp.gif")));
	JButton stepButton= new JButton("step over", new ImageIcon(HumoIDE.class.getResource("/images/stepover.gif")));
	JButton miniStepButton= new JButton("step into", new ImageIcon(HumoIDE.class.getResource("/images/stepinto.gif")));
	JButton stepoutButton= new JButton("step out", new ImageIcon(HumoIDE.class.getResource("/images/stepreturn.gif")));
	JButton runToSelectionButton= new JButton("run to selection", new ImageIcon(HumoIDE.class.getResource("/images/stepintosp.gif")));
	JButton continueButton= new JButton("continue", new ImageIcon(HumoIDE.class.getResource("/images/resume.gif")));
	JButton pauseButton= new JButton("pause", new ImageIcon(HumoIDE.class.getResource("/images/threads_obj.gif")));
	JButton loadButton= new JButton("load source file", new ImageIcon(HumoIDE.class.getResource("/images/schema_variable.gif")));


	nextReplacementButton.setName("nextReplacementButton");
	stepButton.setName("stepButton");
	miniStepButton.setName("miniStepButton");
	stepoutButton.setName("stepoutButton");
	continueButton.setName("continueButton");

	toolBar.add(new JSeparator(SwingConstants.VERTICAL));
	addButtonToToolbar(toolBar, nextReplacementButton);
	toolBar.add(new JSeparator(SwingConstants.VERTICAL));
	addButtonToToolbar(toolBar, stepButton);
	addButtonToToolbar(toolBar, miniStepButton);
	addButtonToToolbar(toolBar, stepoutButton);
	addButtonToToolbar(toolBar, runToSelectionButton);
	addButtonToToolbar(toolBar, continueButton);
	addButtonToToolbar(toolBar, pauseButton);
	toolBar.add(new JSeparator(SwingConstants.VERTICAL));
	addButtonToToolbar(toolBar, loadButton);
	toolBar.add(new JSeparator(SwingConstants.VERTICAL));
	toolBar.add(skipAll);
	toolBar.add(skipSmall);
	toolBar.add(skipSizeSpinner);

	skipSmall.setSelected(true);

	return toolBar;
    }

    private static Component addButtonToToolbar(JToolBar toolBar, JButton aButton)
    {
	Font font= new Font("Verdana", Font.PLAIN, 12);
	aButton.setFont(font);
	return toolBar.add(aButton);
    }

    public static void addPopupMenu(final JTextPane textPane)
    {
	final JPopupMenu menu= new JPopupMenu();
	JMenuItem menuItem= new JMenuItem("Run to this expression");
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
