/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class ExecutionParserListener extends DefaultParserListener implements ParserListener
{
    private Stack<DefaultMutableTreeNode> nodes;
    private DefaultMutableTreeNode root;
    private HumoTreeModel treeModel;

    public TreeNode getRoot()
    {
	return root;
    }

    public void setRoot(DefaultMutableTreeNode root)
    {
	this.root= root;
    }

    public ExecutionParserListener(DebuggerParserListener debugDelegator)
    {
	debugDelegator.setVisibilityListener(new VisibilityListener()
	{
	    public void invisibleChanged(boolean invisible)
	    {
		updateTreeUI();
	    }
	});
    }

    public void init(String filename, CharSequence sourcecode)
    {
	nodes= new Stack<DefaultMutableTreeNode>();
	root= new DefaultMutableTreeNode("Execution of: " + filename);
	nodes.push(root);
	setTreeModel(new HumoTreeModel(root));
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	DefaultMutableTreeNode peek= nodes.peek();
	DefaultMutableTreeNode item= new DefaultMutableTreeNode(sourcecode.subSequence(first, last - 1));
	nodes.push(item);
	peek.add(item);
	updateTreeUI();
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	nodes.peek().add(new DefaultMutableTreeNode(value));
	DefaultMutableTreeNode child= nodes.pop();
	child.setUserObject(name);
	nodes.peek().add(child);
	updateTreeUI();
    }

    private void updateTreeUI()
    {
	//	if (!debugDelegator.isTotallyInvisible())
	//	    SwingUtilities.invokeLater(new Runnable()
	//	    {
	//		public void run()
	//		{
	//		    executionTree.updateUI();
	//		}
	//	    });
    }

    public HumoTreeModel getTreeModel()
    {
	return treeModel;
    }

    public void setTreeModel(HumoTreeModel treeModel)
    {
	this.treeModel= treeModel;
    }
}