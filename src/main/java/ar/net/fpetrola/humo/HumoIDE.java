/*
 * Humo Language
 * Copyright (C) 2002-2015, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import org.fpetrola.humo.service.ExamplesProviderService;

import com.dragome.commons.ExecutionHandler;
import com.dragome.forms.bindings.builders.ComponentBuilder;
import com.dragome.guia.GuiaVisualActivity;
import com.dragome.guia.components.VisualPanelImpl;
import com.dragome.guia.components.VisualTextFieldImpl;
import com.dragome.guia.components.interfaces.VisualButton;
import com.dragome.guia.components.interfaces.VisualComponent;
import com.dragome.guia.components.interfaces.VisualPanel;
import com.dragome.guia.components.interfaces.VisualTextField;
import com.dragome.render.html.renderers.HTMLComponentRenderer;
import com.dragome.services.ServiceLocator;
import com.dragome.templates.interfaces.Template;
import com.dragome.web.annotations.PageAlias;

@PageAlias(alias= "humo-ide")
public class HumoIDE extends GuiaVisualActivity
{
    private Controls controls;
    private Spinner spinner;
    private boolean popupEnabled;
    private HighlighterParserListener highlighterParserListener;

    private void createEnvironment(String aFilename)
    {
	HTMLComponentRenderer.addRenderFor(VisualTextPaneImpl.class, HTMLVisualTextPaneRenderer.class);

	final ExecutionHandler executionHandler= ServiceLocator.getInstance().getConfigurator().getExecutionHandler();

	ExamplesProviderService examplesProviderService= serviceFactory.createFixedSyncService(ExamplesProviderService.class);

	VisualTextField<String> filenameTextField= new VisualTextFieldImpl<>("filenameTextField", aFilename);
	spinner= new Spinner(50, 0, 100000, 1000);
	controls= new Controls();

	DebuggerParserListener debugListener= new DebuggerParserListener(controls, executionHandler);
	//	CallStackParserListener callStackParserListener= new CallStackParserListener(debugListener);
	highlighterParserListener= new HighlighterParserListener(debugListener);
	//	ProductionsParserListener productionsParserListener= new ProductionsParserListener(debugListener);
	//	ExecutionParserListener treeParserListener= new ExecutionParserListener(debugListener);
	//	ParserListenerMultiplexer parserListenerMultiplexer= new ParserListenerMultiplexer(productionsParserListener, treeParserListener, highlighterParserListener, callStackParserListener, debugListener);
	ParserListenerMultiplexer parserListenerMultiplexer= new ParserListenerMultiplexer(highlighterParserListener, debugListener);
	debugListener.setProductionFrames(parserListenerMultiplexer.getProductionFrames());
	ListenedParser parser= new ListenedParser(parserListenerMultiplexer);

	//	parser.getLoggingMap().log("begin parsing");

	executionHandler.getExecutor().execute(() -> {

	    //	    debugListener.stepInto();
	    boolean initialized= false;
	    while (true)
	    {
		try
		{
		    String file= filenameTextField.getValue();
		    String example= examplesProviderService.getExample(file);
		    StringBuilder sourcecode= new StringBuilder(example);

		    parser.setDisabled(false);

		    parserListenerMultiplexer.init(file, sourcecode);
		    //		treeParserListener.init(file, sourcecode);
		    //		productionsParserListener.init(file);
		    //		callStackParserListener.init(file, sourcecode);

		    //		    debugListener.stepInto();

		    //				((DefaultTreeModel) treeParserListener.getExecutionTree().getModel()).reload();
		    //				((DefaultTreeModel) callStackParserListener.getUsedProductionsTree().getModel()).reload();
		    //				((DefaultTreeModel) productionsParserListener.getProductionsTree().getModel()).reload();

		    if (!initialized)
		    {
			createToolbar(executionHandler, highlighterParserListener, debugListener, parser, filenameTextField, parserListenerMultiplexer.getCurrentFrame().getDocument());
			initialized= true;
		    }
		    highlighterParserListener.setTextDocument(parserListenerMultiplexer.getCurrentFrame().getDocument());
		    parser.init();
		    parser.parse(sourcecode, 0);
		    //		    parser.getLoggingMap().log("end parsing");
		    debugListener.getStepper().pause();
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}

		System.out.println("new cycle");
	    }
	});
    }

    private void createToolbar(ExecutionHandler executionHandler, final HighlighterParserListener highlighterParserListener, final DebuggerParserListener debugListener, final ListenedParser parser, final VisualTextField<String> textField, final HumoTextDocument textPane)
    {
	ComponentBuilder componentBuilder= new ComponentBuilder(getMainPanel());
	componentBuilder.bindTemplate("nextReplacementButton").as(VisualButton.class).onClick(() -> debugListener.runToNextReplacement()).build();
	componentBuilder.bindTemplate("textPane").as(VisualTextPaneImpl.class).toProperty(highlighterParserListener::getTextDocument, highlighterParserListener::setTextDocument).build();

	componentBuilder.bindTemplate("stepButton").as(VisualButton.class).onClick(() -> debugListener.stepOver()).build();
	componentBuilder.bindTemplate("miniStepButton").as(VisualButton.class).onClick(() -> debugListener.stepInto()).build();
	componentBuilder.bindTemplate("stepoutButton").as(VisualButton.class).onClick(() -> debugListener.stepOut()).build();

	componentBuilder.bindTemplate("continueButton").as(VisualButton.class).onClick(() -> debugListener.continueExecution()).build();

	//	componentBuilder.bindTemplate("runToSelectionButton").as(VisualButton.class).onClick(() -> {
	//	    final int start= textPane.getLength() - textPane.getSelectionStart();
	//	    final int end= textPane.getLength() - textPane.getSelectionEnd();
	//	    debugListener.runToExpression(start, end);
	//	});
	//
	//	componentBuilder.bindTemplate("pauseButton").as(VisualButton.class).onClick(() -> debugListener.stepInto());
	//	componentBuilder.bindTemplate("loadButton").as(VisualButton.class).onClick(() -> {
	//	    String str= (String) JOptionPane.showInputDialog(null, "Enter file name: ", "Open a new source code", JOptionPane.QUESTION_MESSAGE, null, null, textField.getValue());
	//	    if (str != null)
	//	    {
	//		textField.setValue(str);
	//		debugListener.continueExecution();
	//		parser.setDisabled(true);
	//	    }
	//	});

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

	//	VisualComponent spinnerComponent= componentBuilder.bindTemplate("skipSizeSpinner").as(VisualPanel.class).buildChildren(b -> {
	//	    Template mainPanel= null;
	//	    createSpinner(mainPanel, b);
	//	}).build();
	//
	//	componentBuilder.bindTemplate("skipSmall").as(VisualCheckbox.class).disable(spinnerComponent).when(() -> !controls.isSkipSmall());
	//	componentBuilder.bindTemplate("skipAll").as(VisualCheckbox.class).toProperty(controls::isSkipAll, controls::setSkipAll);
    }

    private VisualPanel createSpinner(Template aTemplate, ComponentBuilder componentBuilder)
    {
	VisualPanel spinnerPanel= new VisualPanelImpl(aTemplate);
	componentBuilder.bindTemplate("up").as(VisualButton.class).onClick(() -> spinner.up());
	componentBuilder.bindTemplate("down").as(VisualButton.class).onClick(() -> spinner.down());
	componentBuilder.bindTemplate("value").as(VisualButton.class).toProperty(spinner::getValue, spinner::setValue);
	return spinnerPanel;
    }

    public void addPopupMenu(final HumoTextDocument textPane, final DebuggerParserListener debugDelegator, ComponentBuilder componentBuilder)
    {
	VisualComponent popup= componentBuilder.bindTemplate("popup").as(VisualPanel.class).buildChildren(b -> {
	    b.bindTemplate("run-to").as(VisualButton.class).onClick(() -> {
		final int start= textPane.getLength() - textPane.getSelectionStart();
		final int end= textPane.getLength() - textPane.getSelectionEnd();

		debugDelegator.runToExpression(start, end);
	    });
	}).build();

	componentBuilder.bindTemplate("textPane").as(VisualPanel.class).onDoubleClick(c -> setPopupEnabled(true));
	componentBuilder.show(popup).when(() -> isPopupEnabled());
    }

    public boolean isPopupEnabled()
    {
	return popupEnabled;
    }

    public void setPopupEnabled(boolean popupEnabled)
    {
	this.popupEnabled= popupEnabled;
    }

    public void build()
    {
	String filename= "test1.humo";

	if (ServiceLocator.getInstance().getParametersHandler() != null)
	{
	    String file= ServiceLocator.getInstance().getParametersHandler().getParameter("file");

	    if (file != null && file.trim().length() > 0)
		filename= file;
	}

	createEnvironment(filename);
    }
}