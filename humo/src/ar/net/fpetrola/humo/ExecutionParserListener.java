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
    protected JTree executionTree;

    public JTree getExecutionTree()
    {
	return executionTree;
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

    public void init(String filename, boolean createComponents, CharSequence sourcecode)
    {
	nodes= new Stack<DefaultMutableTreeNode>();
	root= new DefaultMutableTreeNode("Execution of: " + filename);
	nodes.push(root);
	if (createComponents)
	{
	    executionTree= new JTree();
	}

	executionTree.setModel(new DefaultTreeModel(root));
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	nodes.push(new DefaultMutableTreeNode(name));
	((DefaultTreeModel) executionTree.getModel()).reload();
	nodes.peek().add(new DefaultMutableTreeNode(value));
	DefaultMutableTreeNode child= nodes.pop();
	child.setUserObject(name);
	nodes.peek().add(child);
	((DefaultTreeModel) executionTree.getModel()).reload();
    }
}