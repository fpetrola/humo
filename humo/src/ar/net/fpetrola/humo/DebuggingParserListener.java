package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.ButtonModel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DebuggingParserListener extends DefaultParserListener implements ParserListener
{
    protected volatile boolean pause;

    public void continueExecution()
    {
	pause= false;
    }

    protected void pause()
    {
	pause= true;
	while (pause == true)
	    ;
    }
    protected ButtonModel skipSmall;
    protected JSpinner skipSizeSpinner;
    protected JTree stacktraceTree;

    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected DefaultMutableTreeNode usedProductionsStackRoot;

    public DebuggingParserListener(ButtonModel skipSmall, JTextPane textPane, JSpinner skipSizeSpinner)
    {
	this.skipSmall= skipSmall;
	this.skipSizeSpinner= skipSizeSpinner;
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	boolean visibleFrame= value != null && (value.length() > (Integer) skipSizeSpinner.getValue() || !skipSmall.isSelected());
	if (visibleFrame)
	{
	    DefaultMutableTreeNode node= new StacktraceTreeNode(name, currentFrame);
	    usedProductionsStack.push(node);
	    usedProductionsStackRoot.add(usedProductionsStack.peek());
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
	}
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
    }
    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	if (frameIsVisible())
	{
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	if (usedProductionsStack.size() > 1)
	{
	    usedProductionsStackRoot.remove(usedProductionsStack.peek());
	    usedProductionsStack.pop();
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
	}

    }
    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
    }

    private boolean frameIsVisible()
    {
	return true;//((StacktraceTreeNode) usedProductionsStack.peek()).getFrame() == currentFrame && !forceSkip;
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
    }

    public void setUsedProductionsStackRoot(DefaultMutableTreeNode usedProductionsStackRoot)
    {
	this.usedProductionsStackRoot= usedProductionsStackRoot;
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	pause();
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
    }

    public void runTo(int start, int end)
    {
    }
}
