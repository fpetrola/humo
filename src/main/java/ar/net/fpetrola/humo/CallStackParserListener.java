package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

public class CallStackParserListener extends DefaultParserListener implements ParserListener
{
    protected HumoTreeModel stacktraceTreeModel;
    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected StacktraceTreeNode usedProductionsStackRoot;
    private final DebuggerParserListener debugDelegator;

    public CallStackParserListener(DebuggerParserListener debugDelegator)
    {
	this.debugDelegator= debugDelegator;
	debugDelegator.setVisibilityListener(new VisibilityListener()
	{
	    public void invisibleChanged(boolean invisible)
	    {
		stacktraceTreeModel.reload();
	    }
	});
    }

    public void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	DefaultMutableTreeNode node= new StacktraceTreeNode(name, currentFrame);
	usedProductionsStack.push(node);
	usedProductionsStackRoot.add(usedProductionsStack.peek());
	if (!debugDelegator.isTotallyInvisible())
	    stacktraceTreeModel.reload();
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	if (usedProductionsStack.size() > 1)
	{
	    usedProductionsStackRoot.remove(usedProductionsStack.peek());
	    usedProductionsStack.pop();
	    if (!debugDelegator.isTotallyInvisible())
		stacktraceTreeModel.reload();
	}
    }

    public DefaultMutableTreeNode getUsedProductionsStackRoot()
    {
	return usedProductionsStackRoot;
    }

    public void init(String filename, StringBuilder sourcecode)
    {
	usedProductionsStack= new Stack<DefaultMutableTreeNode>();
	usedProductionsStackRoot= new StacktraceTreeNode("Call stack of: " + filename, currentFrame);
	usedProductionsStack.push(usedProductionsStackRoot);
	stacktraceTreeModel= new HumoTreeModel(usedProductionsStackRoot);
	currentFrame= null;
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	if (currentFrame == null)
	    usedProductionsStackRoot.setFrame(productionFrame);

	super.setCurrentFrame(productionFrame);
    }

    public void setUsedProductionsStackRoot(StacktraceTreeNode usedProductionsStackRoot)
    {
	this.usedProductionsStackRoot= usedProductionsStackRoot;
    }
}
