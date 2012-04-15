package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DebuggingParserListener extends DefaultParserListener implements ParserListener
{
    protected JTree stacktraceTree;
    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected StacktraceTreeNode usedProductionsStackRoot;
    private final ParserListenerDelegator debugDelegator;

    public DebuggingParserListener(ParserListenerDelegator debugDelegator)
    {
	this.debugDelegator= debugDelegator;
	debugDelegator.setVisibilityListener(new VisibilityListener()
	{
	    public void invisibleChanged(boolean invisible)
	    {
		((DefaultTreeModel) stacktraceTree.getModel()).reload();
	    }
	});
    }

    public void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	DefaultMutableTreeNode node= new StacktraceTreeNode(name, currentFrame);
	usedProductionsStack.push(node);
	usedProductionsStackRoot.add(usedProductionsStack.peek());
	if (!debugDelegator.isTotallyInvisible())
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	if (usedProductionsStack.size() > 1)
	{
	    usedProductionsStackRoot.remove(usedProductionsStack.peek());
	    usedProductionsStack.pop();
	    if (!debugDelegator.isTotallyInvisible())
		((DefaultTreeModel) stacktraceTree.getModel()).reload();
	}
    }

    public DefaultMutableTreeNode getUsedProductionsStackRoot()
    {
	return usedProductionsStackRoot;
    }

    public JTree getUsedProductionsTree()
    {
	return stacktraceTree;
    }

    public void init(String filename, StringBuilder sourcecode, boolean createComponents)
    {
	usedProductionsStack= new Stack<DefaultMutableTreeNode>();
	usedProductionsStackRoot= new StacktraceTreeNode("Call stack of: " + filename, currentFrame);
	usedProductionsStack.push(usedProductionsStackRoot);
	if (createComponents)
	    stacktraceTree= new JTree();

	stacktraceTree.setModel(new DefaultTreeModel(usedProductionsStackRoot));
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
