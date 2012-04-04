package ar.net.fpetrola.humo;

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
    public class ProductionFrame
    {
	protected StringBuilder production;
	protected int first;
	protected int current;

	protected int last;

	public ProductionFrame(StringBuilder production)
	{
	    this.production= production;
	}

	public int getCurrent()
	{
	    return current;
	}

	public int getFirst()
	{
	    return first;
	}

	public int getLast()
	{
	    return last;
	}

	public StringBuilder getProduction()
	{
	    return production;
	}

	public void setCurrent(int current)
	{
	    this.current= current;
	}
	public void setFirst(int first)
	{
	    this.first= first;
	}

	public void setLast(int last)
	{
	    this.last= last;
	}

	public void setProduction(StringBuilder production)
	{
	    this.production= production;
	}
    }

    protected volatile boolean pause;
    protected volatile boolean step;
    protected final ButtonModel skipSmall;
    protected Stack<ProductionFrame> productionFrames= new Stack<DebuggingParserListener.ProductionFrame>();
    private JTextPane textPane;
    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected DefaultMutableTreeNode usedProductionsStackRoot;
    protected JTree stacktraceTree;
    private final JSpinner skipSizeSpinner;

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
	ProductionFrame rootFrame= new ProductionFrame(sourcecode);
	usedProductionsStackRoot= new StacktraceTreeNode("Call stack of: " + filename, rootFrame);
	productionFrames.push(rootFrame);
	usedProductionsStack.push(usedProductionsStackRoot);
	if (createComponents)
	    stacktraceTree= new JTree();

	stacktraceTree.setModel(new DefaultTreeModel(usedProductionsStackRoot));
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	ProductionFrame frame= new ProductionFrame(value);
	productionFrames.push(frame);

	DefaultMutableTreeNode node= new StacktraceTreeNode(name, frame);
	usedProductionsStack.push(node);
	usedProductionsStackRoot.add(usedProductionsStack.peek());
	((DefaultTreeModel) stacktraceTree.getModel()).reload();

	if (showFrame(value))
	{
	    performStep();
	    updateFrame(productionFrames.peek());
	}
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	// TODO Auto-generated method stub
	super.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
    }

    public void updateFrame(ProductionFrame productionFrame)
    {
	StringBuilder productionValue= productionFrame.getProduction();

	HumoTester.configureTextPane(productionValue, textPane);
	StyledDocument styledDocument= (StyledDocument) textPane.getDocument();
	int openCurly= productionValue.substring(productionFrame.getFirst()).indexOf('{');
	int closeCurly= productionValue.substring(productionFrame.getFirst()).indexOf('}');
	int nextCurly= Math.min(openCurly > 0 ? openCurly : Integer.MAX_VALUE, closeCurly > 0 ? closeCurly : Integer.MAX_VALUE);
	int delta= openCurly == nextCurly ? 1 : 0;
	styledDocument.setCharacterAttributes(productionFrame.getFirst() + delta, nextCurly - delta, styledDocument.getStyle("Cursor"), false);

	int caretPosition= productionFrame.getFirst();
	if (styledDocument.getLength() > caretPosition + 300)
	    caretPosition+= 300;

	textPane.setCaretPosition(caretPosition);
	try
	{
	    Thread.sleep(10);
	}
	catch (InterruptedException e)
	{
	}
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	if (showFrame(sourcecode))
	{
	    performStep();
	    //	    updateFrame(sourcecode, first);
	}

	try
	{
	    StyledDocument doc= (StyledDocument) textPane.getDocument();
	    int length= endPosition - startPosition;
	    doc.remove(startPosition, length);
	    doc.insertString(startPosition, value.toString(), null);

	    Style heading2Style= doc.getStyle("Heading2");
	    for (int i= startPosition; i < startPosition + value.length(); i++)
	    {
		if (sourcecode.charAt(i) == '{' || sourcecode.charAt(i) == '}')
		    doc.setCharacterAttributes(i, 1, heading2Style, false);
	    }
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	productionFrames.pop();
	if (usedProductionsStack.size() > 1)
	{
	    try
	    {
		usedProductionsStackRoot.remove(usedProductionsStack.peek());
		usedProductionsStack.pop();
		((DefaultTreeModel) stacktraceTree.getModel()).reload();
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
	if (showFrame(sourcecode))
	{
	    performStep();
	    updateFrame(productionFrames.peek());
	}
    }

    public void continueExecution()
    {
	step= false;
	pause= false;
    }

    public Stack<ProductionFrame> getProductionFrames()
    {
	return productionFrames;
    }

    protected void performStep()
    {
	while (pause)
	    ;

	if (step)
	    pause= true;
    }

    public void setProductionFrames(Stack<ProductionFrame> productionFrames)
    {
	this.productionFrames= productionFrames;
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	if (!productionFrames.isEmpty())
	{
	    ProductionFrame currentProductionFrame= productionFrames.peek();
	    currentProductionFrame.setFirst(first);
	    currentProductionFrame.setCurrent(first);
	    currentProductionFrame.setLast(first);
	}
    }

    public void startProductionParsing(StringBuilder sourcecode, int first)
    {
	super.startProductionParsing(sourcecode, first);
	if (showFrame(sourcecode))
	{
	    updateFrame(productionFrames.peek());
	}
    }

    public void step()
    {
	step= true;
	pause= false;
    }

    public void stop()
    {
	pause= true;
	step= false;
    }

    public JTree getUsedProductionsTree()
    {
	return stacktraceTree;
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	if (showFrame(sourcecode))
	{
	    updateFrame(productionFrames.peek());
	    performStep();
	}
    }
    private boolean showFrame(StringBuilder sourcecode)
    {
	return sourcecode != null && (sourcecode.length() > (Integer) skipSizeSpinner.getValue() || !skipSmall.isSelected());
    }
}
