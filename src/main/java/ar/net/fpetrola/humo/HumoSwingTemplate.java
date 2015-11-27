package ar.net.fpetrola.humo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.dragome.forms.bindings.builders.ComponentBuilder;
import com.dragome.guia.components.VisualCheckboxImpl;
import com.dragome.guia.components.interfaces.VisualTextField;

import ar.net.fpetrola.humo.gui.TreeNode;

public class HumoSwingTemplate
{
	private static void createEnvironment(String aFilename, JFrame jframe)
	{
		JTextPane textPane= new JTextPane()
		{
			public boolean getScrollableTracksViewportWidth()
			{
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		
		VisualCheckboxImpl skipSmall= new VisualCheckboxImpl("skipSmall", "skip productions smaller than:", true);
		VisualCheckboxImpl skipAll= new VisualCheckboxImpl("skipAll", "skip all  ", false);

	}
	
	public static void showTree(final HighlighterParserListener highlighterParserListener, final DebuggerParserListener debugListener, final ListenedParser parser, StringBuilder sourceCode, final JTextPane textPane, TreeNode treeNode, JTree executionTree, JTree productionsTree, final JFrame jframe, final VisualTextField<String> textField, final ParserListenerMultiplexer parserListenerMultiplexer)
	{
		jframe.setLocation(100, 100);

		JSplitPane treesSplitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(executionTree), new JScrollPane(productionsTree));
		treesSplitPane.setDividerLocation(300);
		JScrollPane textPanelScrollPane= new JScrollPane(textPane);
		JSplitPane newRightComponent= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, textPanelScrollPane, new JScrollPane(treeNode));
		newRightComponent.setDividerLocation(900);
		JSplitPane verticalSplitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, treesSplitPane, newRightComponent);
		verticalSplitPane.setDividerLocation(200);

		addPopupMenu(textPane, debugListener);

		JPanel mainPanel= new JPanel(new BorderLayout());

		JToolBar toolBar= createToolbar(highlighterParserListener, debugListener, parser, treeNode, textField, textPane);
		mainPanel.add(toolBar, BorderLayout.PAGE_START);
		mainPanel.add(verticalSplitPane, BorderLayout.CENTER);

		jframe.setContentPane(mainPanel);
		jframe.setSize(1200, 1000);
		jframe.setVisible(true);
	}

	private static JToolBar createToolbar(HighlighterParserListener highlighterParserListener, DebuggerParserListener debugListener, ListenedParser parser, TreeNode treeNode, VisualTextField<String> textField, JTextPane textPane)
	{
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
						String str= (String) JOptionPane.showInputDialog(null, "Enter file name: ", "Open a new source code", JOptionPane.QUESTION_MESSAGE, null, null, textField.getValue());
						if (str != null)
						{
							textField.setValue(str);
							debugListener.continueExecution();
							parser.setDisabled(true);
						}
					}
				});
			}
		}));

		addButtonToToolbar(toolBar, loadButton);

		toolBar.add(new JSeparator(SwingConstants.VERTICAL));

		return null;
	}

	private static Component addButtonToToolbar(JToolBar toolBar, JButton aButton)
	{
		Font font= new Font("Verdana", Font.PLAIN, 12);
		aButton.setFont(font);
		return toolBar.add(aButton);
	}

	public static void addPopupMenu(final JTextPane textPane, final DebuggerParserListener debugDelegator, ComponentBuilder componentBuilder)
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
