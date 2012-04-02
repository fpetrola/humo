package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.ButtonModel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DebuggingParserListener extends DefaultParserListener implements ParserListener
{
    private int skipThreshold= 50;

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

    public DefaultMutableTreeNode getUsedProductionsStackRoot()
    {
	return usedProductionsStackRoot;
    }
    public void setUsedProductionsStackRoot(DefaultMutableTreeNode usedProductionsStackRoot)
    {
	this.usedProductionsStackRoot= usedProductionsStackRoot;
    }
    public DebuggingParserListener(ButtonModel skipSmall, JTextPane textPane)
    {
	this.skipSmall= skipSmall;
	this.textPane= textPane;
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
	performStep();
	if (sourcecode != null && sourcecode != null && (sourcecode.length() > skipThreshold || !skipSmall.isSelected()))
	{
	    updateFrame(sourcecode, first);
	}
	ProductionFrame frame= new ProductionFrame(value);
	productionFrames.push(frame);

	DefaultMutableTreeNode node= new StacktraceTreeNode(name, frame);
	usedProductionsStack.push(node);
	usedProductionsStackRoot.add(usedProductionsStack.peek());
	((DefaultTreeModel) stacktraceTree.getModel()).reload();
    }

    public void updateFrame(StringBuilder sourcecode, int first)
    {
	if (sourcecode.length() > skipThreshold || !skipSmall.isSelected())
	{
	    HumoTester.configureTextPane(sourcecode, textPane);
	    StyledDocument styledDocument= (StyledDocument) textPane.getDocument();
	    int openCurly= sourcecode.substring(first).indexOf('{');
	    int closeCurly= sourcecode.substring(first).indexOf('}');
	    int nextCurly= Math.min(openCurly > 0 ? openCurly : Integer.MAX_VALUE, closeCurly> 0 ? closeCurly : Integer.MAX_VALUE);
	    styledDocument.setCharacterAttributes(first, nextCurly, styledDocument.getStyle("Cursor"), false);

	    int caretPosition= first;
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
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	if (sourcecode != null && sourcecode != null && (sourcecode.length() > skipThreshold || !skipSmall.isSelected()))
	{
	    updateFrame(sourcecode, first);
	    performStep();
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	if (sourcecode != null && sourcecode != null && (sourcecode.length() > skipThreshold || !skipSmall.isSelected()))
	{
	    updateFrame(sourcecode, first);
	}
	productionFrames.pop();
	if (usedProductionsStack.size() > 1)
	{
	    usedProductionsStackRoot.remove(usedProductionsStack.peek());
	    usedProductionsStack.pop();
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
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
	updateFrame(sourcecode, first);
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
}
