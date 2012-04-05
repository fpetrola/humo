package ar.net.fpetrola.humo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.swing.ButtonModel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DebuggingParserListener extends DefaultParserListener implements ParserListener
{
    public static class Stepper
    {
	protected volatile boolean pause;
	protected volatile List<String> validSteps= new ArrayList<String>();

	public void continueExecution()
	{
	    validSteps.clear();
	    pause= false;
	}

	public void step(String... validSteps)
	{
	    this.validSteps= new ArrayList<String>(Arrays.asList(validSteps));
	    pause= false;
	}

	public void stop()
	{
	    pause= true;
	    validSteps.clear();
	}

	public void stepOut()
	{
	}

	protected void performStep(String stepType)
	{
	    if (validSteps.contains(stepType))
		pause= true;

	    while (pause == true)
		;

	}
    }

    protected Stepper stepper= new Stepper();

    public Stepper getStepper()
    {
	return stepper;
    }
    public void setStepper(Stepper stepper)
    {
	this.stepper= stepper;
    }

    protected volatile boolean stepout;

    protected final ButtonModel skipSmall;
    protected Stack<ProductionFrame> productionFrames= new Stack<ProductionFrame>();
    protected JTextPane textPane;
    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected DefaultMutableTreeNode usedProductionsStackRoot;
    protected JTree stacktraceTree;
    protected final JSpinner skipSizeSpinner;

    public DefaultMutableTreeNode getUsedProductionsStackRoot()
    {
	return usedProductionsStackRoot;
    }
    public void setUsedProductionsStackRoot(DefaultMutableTreeNode usedProductionsStackRoot)
    {
	this.usedProductionsStackRoot= usedProductionsStackRoot;
    }
    public DebuggingParserListener(ButtonModel skipSmall, JTextPane textPane, JSpinner skipSizeSpinner)
    {
	this.skipSmall= skipSmall;
	this.textPane= textPane;
	this.skipSizeSpinner= skipSizeSpinner;
    }

    public void init(String filename, StringBuilder sourcecode, boolean createComponents)
    {
	usedProductionsStack= new Stack<DefaultMutableTreeNode>();
	ProductionFrame rootFrame= new ProductionFrame(filename, sourcecode, (StyledDocument) textPane.getDocument());
	usedProductionsStackRoot= new StacktraceTreeNode("Call stack of: " + filename, rootFrame);
	productionFrames.push(rootFrame);
	usedProductionsStack.push(usedProductionsStackRoot);
	if (createComponents)
	    stacktraceTree= new JTree();

	stacktraceTree.setModel(new DefaultTreeModel(usedProductionsStackRoot));
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	showProductionMatch(sourcecode, current, last);

	if (frameIsVisible())
	    stepper.performStep("afterProductionFound");

	ProductionFrame frame= new ProductionFrame(name.toString(), value, HumoTester.createAndSetupDocument(value));
	productionFrames.push(frame);

	boolean visibleFrame= value != null && (value.length() > (Integer) skipSizeSpinner.getValue() || !skipSmall.isSelected());
	if (visibleFrame)
	{
	    DefaultMutableTreeNode node= new StacktraceTreeNode(name, frame);
	    usedProductionsStack.push(node);
	    usedProductionsStackRoot.add(usedProductionsStack.peek());
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
	    ProductionFrame currentFrame= getCurrentFrame();
	    updateFrame(currentFrame);
	}
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	if (frameIsVisible())
	{
	    int lastCurrent= updateFrameValues(first, current, last);
	    stepper.performStep("beforeParseProductionBody");
	}
    }

    public void updateFrame(ProductionFrame productionFrame)
    {
	textPane.setDocument(productionFrame.getDocument());
	updateCaretPosition(textPane, productionFrame);

	//	HumoTester.setupDocument(productionValue, styledDocument);
	//	int openCurly= productionValue.substring(productionFrame.getFirst()).indexOf('{');
	//	int closeCurly= productionValue.substring(productionFrame.getFirst()).indexOf('}');
	//	int nextCurly= Math.min(openCurly > 0 ? openCurly : Integer.MAX_VALUE, closeCurly > 0 ? closeCurly : Integer.MAX_VALUE);
	//	int delta= openCurly == nextCurly ? 1 : 0;
	//	styledDocument.setCharacterAttributes(productionFrame.getFirst() + delta, nextCurly - delta, styledDocument.getStyle("Cursor"), false);

    }
    public static void updateCaretPosition(JTextPane textPane, ProductionFrame aFrame)
    {
	StyledDocument styledDocument= (StyledDocument) aFrame.getDocument();
	int caretPosition= aFrame.getFirst();
	if (styledDocument.getLength() > caretPosition + 300)
	    caretPosition+= 300;
	else
	    caretPosition= styledDocument.getLength()-1;

	try
	{
	    textPane.setCaretPosition(caretPosition);
	    //	    Thread.sleep(1);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	if (frameIsVisible())
	{
	    int lastCurrent= updateFrameValues(first, current, last);

	    try
	    {
		StyledDocument doc= (StyledDocument) getCurrentFrame().getDocument();
		int length= endPosition - startPosition;
		doc.remove(startPosition, length);
		doc.insertString(startPosition, value.toString(), null);

		Style style= doc.getStyle("Cursor");
		doc.setCharacterAttributes(current, value.length(), style, false);
		highlightCurlys(sourcecode, startPosition, doc, value.length());
	    }
	    catch (BadLocationException e)
	    {
		e.printStackTrace();
	    }

	    stepper.performStep("afterProductionReplacement");
	}
    }

    private boolean frameIsVisible()
    {
	return ((StacktraceTreeNode) usedProductionsStack.peek()).getFrame() == productionFrames.peek();
    }

    private void highlightCurlys(StringBuilder sourcecode, int startPosition, StyledDocument doc, int length)
    {
	Style defaultstyle= doc.getStyle("default");
	Style curlyStyle= doc.getStyle(HumoTester.CURLY_STYLE);
	for (int i= startPosition; i < startPosition + length; i++)
	{
	    Style usingStyle;
	    if (sourcecode.charAt(i) == '{' || sourcecode.charAt(i) == '}')
	    {
		usingStyle= curlyStyle;
		doc.setCharacterAttributes(i, 1, usingStyle, false);
	    }
	    else
		usingStyle= defaultstyle;
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	if (frameIsVisible())
	    if (usedProductionsStack.size() > 1)
	    {
		usedProductionsStackRoot.remove(usedProductionsStack.peek());
		usedProductionsStack.pop();
		((DefaultTreeModel) stacktraceTree.getModel()).reload();
	    }
	productionFrames.pop();

	if (frameIsVisible())
	{
	    try
	    {
		int lastCurrent= updateFrameValues(first, current, last);

		ProductionFrame currentFrame= getCurrentFrame();
		textPane.setDocument(currentFrame.getDocument());
		updateFrame(currentFrame);
		showProductionMatch(currentFrame.getProduction(), currentFrame.getCurrent(), currentFrame.getLast());
		stepper.performStep("beforeProductionReplacement");
		stepper.performStep("beforeProductionReplacement:" + name);

		//	    stepper.performStep("beforeProductionReplacement");
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }
    private ProductionFrame getCurrentFrame()
    {
	return productionFrames.peek();
    }
    private void showProductionMatch(StringBuilder sourcecode, int current, int last)
    {
	StyledDocument doc= (StyledDocument) getCurrentFrame().getDocument();
	highlightCurlys(sourcecode, current, doc, last - current);
	Style style= doc.getStyle("production-matching");
	doc.setCharacterAttributes(current, last - current, style, false);
    }

    public Stack<ProductionFrame> getProductionFrames()
    {
	return productionFrames;
    }

    public void setProductionFrames(Stack<ProductionFrame> productionFrames)
    {
	this.productionFrames= productionFrames;
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	if (frameIsVisible())
	{
	    try
	    {
		int lastCurrent= updateFrameValues(first, current, last);

		StyledDocument doc= (StyledDocument) getCurrentFrame().getDocument();
		highlightCurlys(sourcecode, lastCurrent, doc, current - lastCurrent);
		Style style= doc.getStyle(HumoTester.FETCH_STYLE);
		doc.setCharacterAttributes(current, last - current, style, false);
		stepper.performStep("startParsingLoop");
	    }
	    catch (Exception e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private int updateFrameValues(int first, int current, int last)
    {
	ProductionFrame currentProductionFrame= getCurrentFrame();

	int lastCurrent= currentProductionFrame.getCurrent();
	currentProductionFrame.setFirst(first);
	currentProductionFrame.setCurrent(current);
	currentProductionFrame.setLast(last);
	return lastCurrent;
    }

    public void startProductionParsing(StringBuilder sourcecode, int first)
    {
	if (frameIsVisible())
	{
	    stepper.performStep("startProductionParsing");
	    textPane.setDocument(getCurrentFrame().getDocument());
	}
    }

    public JTree getUsedProductionsTree()
    {
	return stacktraceTree;
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	if (frameIsVisible())
	{

	    int lastCurrent= updateFrameValues(first, current, last);

	    stepper.performStep("endProductionParsing");
	    //	    updateFrame(productionFrames.peek());
	}
    }
}
