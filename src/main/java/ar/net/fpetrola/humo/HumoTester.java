/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.dragome.forms.bindings.builders.ComponentBuilder;
import com.dragome.guia.components.VisualCheckboxImpl;
import com.dragome.guia.components.VisualPanelImpl;
import com.dragome.guia.components.VisualTextFieldImpl;
import com.dragome.guia.components.interfaces.VisualButton;
import com.dragome.guia.components.interfaces.VisualCheckbox;
import com.dragome.guia.components.interfaces.VisualComponent;
import com.dragome.guia.components.interfaces.VisualPanel;
import com.dragome.guia.components.interfaces.VisualTextField;
import com.dragome.templates.interfaces.Template;

import ar.net.fpetrola.humo.gui.TreeNode;

public class HumoTester
{
	private static Controls controls;
	private static Spinner spinner;

	private static boolean popupEnabled;
	private static TextPanel textPane;

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
		VisualTextField<String> filenameTextField= new VisualTextFieldImpl<>("filenameTextField", aFilename);
		spinner= new Spinner(50, 0, 100000, 1000); 

		DebuggerParserListener debugListener= new DebuggerParserListener(controls);
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

				String file= filenameTextField.getValue();
				StringBuilder sourcecode= new StringBuilder(new Scanner(HumoTester.class.getResourceAsStream("/" + file)).useDelimiter("\\Z").next());

				parserListenerMultiplexer.init(file, sourcecode, !initialized);
				treeParserListener.init(file, !initialized, sourcecode);
				productionsParserListener.init(file, !initialized);
				callStackParserListener.init(file, sourcecode, !initialized);
				textPane.setDocument(parserListenerMultiplexer.getCurrentFrame().getDocument());
				debugListener.stepInto();

				//				((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
				//				((DefaultTreeModel) callStackParserListener.getUsedProductionsTree().getModel()).reload();
				//				((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

				if (!initialized)
				{
					showTree(highlighterParserListener, debugListener, parser, sourcecode, textPane, callStackParserListener.getUsedProductionsTree(), treeParserListener.getExecutionTree(), productionsParserListener.getProductionsTree(), jframe, filenameTextField, parserListenerMultiplexer);
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

	private static JToolBar createToolbar(final HighlighterParserListener highlighterParserListener, final DebuggerParserListener debugListener, final ListenedParser parser, TreeNode treeNode, final VisualTextField<String> textField, final JTextPane textPane)
	{
		JToolBar toolBar= new JToolBar("debugger actions");

		toolBar.add(new JSeparator(SwingConstants.VERTICAL));

		ComponentBuilder componentBuilder= null;
		componentBuilder.bindTemplate("nextReplacementButton").as(VisualButton.class).onClick(() -> debugListener.runToNextReplacement());
		componentBuilder.bindTemplate("stepButton").as(VisualButton.class).onClick(() -> debugListener.stepOver());
		componentBuilder.bindTemplate("miniStepButton").as(VisualButton.class).onClick(() -> debugListener.stepInto());
		componentBuilder.bindTemplate("stepoutButton").as(VisualButton.class).onClick(() -> debugListener.stepOut());

		componentBuilder.bindTemplate("continueButton").as(VisualButton.class).onClick(() -> debugListener.continueExecution());
		componentBuilder.bindTemplate("runToSelectionButton").as(VisualButton.class).onClick(() -> {
			final int start= textPane.getDocument().getLength() - textPane.getSelectionStart();
			final int end= textPane.getDocument().getLength() - textPane.getSelectionEnd();
			debugListener.runToExpression(start, end);
		});

		componentBuilder.bindTemplate("pauseButton").as(VisualButton.class).onClick(() -> debugListener.stepInto());
		componentBuilder.bindTemplate("loadButton").as(VisualButton.class).onClick(() -> {
			String str= (String) JOptionPane.showInputDialog(null, "Enter file name: ", "Open a new source code", JOptionPane.QUESTION_MESSAGE, null, null, textField.getValue());
			if (str != null)
			{
				textField.setValue(str);
				debugListener.continueExecution();
				parser.setDisabled(true);
			}
		});

		//		treeNode.addTreeSelectionListener(new TreeSelectionListener()
		//		{
		//			public void valueChanged(TreeSelectionEvent e)
		//			{
		//				if (e.getNewLeadSelectionPath() != null)
		//				{
		//					Object lastPathComponent= e.getNewLeadSelectionPath().getLastPathComponent();
		//					StacktraceTreeNode stacktraceTreeNode= (StacktraceTreeNode) lastPathComponent;
		//					ProductionFrame frame= stacktraceTreeNode.getFrame();
		//					if (frame != null)
		//						highlighterParserListener.updateFrame(frame);
		//				}
		//			}
		//		});

		VisualComponent spinnerComponent= componentBuilder.bindTemplate("skipSizeSpinner").as(VisualPanel.class).buildChildren(b -> {
			Template mainPanel= null;
			createSpinner(mainPanel, b);
		}).build();

		componentBuilder.bindTemplate("skipSmall").as(VisualCheckbox.class).disable(spinnerComponent).when(() -> !controls.isSkipSmall());
		componentBuilder.bindTemplate("skipAll").as(VisualCheckbox.class).toProperty(controls::isSkipAll, controls::setSkipAll);

		return toolBar;
	}

	private static VisualPanel createSpinner(Template aTemplate, ComponentBuilder componentBuilder)
	{
		VisualPanel spinnerPanel= new VisualPanelImpl(aTemplate);
		componentBuilder.bindTemplate("up").as(VisualButton.class).onClick(() -> spinner.up());
		componentBuilder.bindTemplate("down").as(VisualButton.class).onClick(() -> spinner.down());
		componentBuilder.bindTemplate("value").as(VisualButton.class).toProperty(spinner::getValue, spinner::setValue);
		return spinnerPanel;
	}

	public static void addPopupMenu(final JTextPane textPane, final DebuggerParserListener debugDelegator, ComponentBuilder componentBuilder)
	{
		VisualComponent popup= componentBuilder.bindTemplate("popup").as(VisualPanel.class).buildChildren(b -> {
			b.bindTemplate("run-to").as(VisualButton.class).onClick(() -> {
				final int start= textPane.getDocument().getLength() - textPane.getSelectionStart();
				final int end= textPane.getDocument().getLength() - textPane.getSelectionEnd();

				debugDelegator.runToExpression(start, end);
			});
		}).build();

		componentBuilder.bindTemplate("textPane").as(VisualPanel.class).onDoubleClick(c -> setPopupEnabled(true));
		componentBuilder.show(popup).when(() -> isPopupEnabled());
	}

	public static boolean isPopupEnabled()
	{
		return popupEnabled;
	}

	public static void setPopupEnabled(boolean popupEnabled)
	{
		HumoTester.popupEnabled= popupEnabled;
	}

}