/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ExecutionParserListener extends DefaultParserListener implements ParserListener
{
    protected Stack<DefaultMutableTreeNode> nodes;
    protected DefaultMutableTreeNode root;

    protected Stack<DefaultMutableTreeNode> usedProductionsStack;
    protected DefaultMutableTreeNode usedProductionsStackRoot;

    protected JTree executionTree;

    public JTree getExecutionTree()
    {
	return executionTree;
    }

    protected JTree stacktraceTree;

    public DefaultMutableTreeNode getUsedProductionsStackRoot()
    {
	return usedProductionsStackRoot;
    }
    public void setUsedProductionsStackRoot(DefaultMutableTreeNode usedProductionsStackRoot)
    {
	this.usedProductionsStackRoot= usedProductionsStackRoot;
    }
    public DefaultMutableTreeNode getRoot()
    {
	return root;
    }
    public void setRoot(DefaultMutableTreeNode root)
    {
	this.root= root;
    }
    public ExecutionParserListener()
    {
    }

    public void init(String filename, boolean createComponents)
    {
	nodes= new Stack<DefaultMutableTreeNode>();
	usedProductionsStack= new Stack<DefaultMutableTreeNode>();
	root= new DefaultMutableTreeNode("Execution of: " + filename);
	usedProductionsStackRoot= new DefaultMutableTreeNode("Call stack of: " + filename);
	nodes.push(root);
	usedProductionsStack.push(usedProductionsStackRoot);
	if (createComponents)
	{
	    stacktraceTree= new JTree();
	    executionTree= new JTree();
	}

	stacktraceTree.setModel(new DefaultTreeModel(usedProductionsStackRoot));
	executionTree.setModel(new DefaultTreeModel(root));
    }
    public void startProductionCreation(CharSequence name)
    {
	nodes.push(new DefaultMutableTreeNode(name));
	((DefaultTreeModel) executionTree.getModel()).reload();
    }
    public void endProductionCreation(CharSequence name, CharSequence value)
    {
	nodes.peek().add(new DefaultMutableTreeNode(value));
	DefaultMutableTreeNode child= nodes.pop();
	child.setUserObject(name);
	nodes.peek().add(child);
	((DefaultTreeModel) executionTree.getModel()).reload();
    }

    public void getProduction(CharSequence key, CharSequence value)
    {
	if (value != null)
	{
	    usedProductionsStack.push(new DefaultMutableTreeNode(key));
	    usedProductionsStackRoot.add(usedProductionsStack.peek());
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
	}
    }

    public void parseEnded()
    {
	if (usedProductionsStack.size() > 1)
	{
	    usedProductionsStackRoot.remove(usedProductionsStack.peek());
	    usedProductionsStack.pop();
	    ((DefaultTreeModel) stacktraceTree.getModel()).reload();
	}
    }
    public JTree getUsedProductionsTree()
    {
	return stacktraceTree;
    }
}