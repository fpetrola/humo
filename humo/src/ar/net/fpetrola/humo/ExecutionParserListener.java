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

public class ExecutionParserListener implements ParserListener
{
    protected Stack<DefaultMutableTreeNode> nodes = new Stack<DefaultMutableTreeNode>();
    protected DefaultMutableTreeNode root;

    public DefaultMutableTreeNode getRoot()
    {
        return root;
    }
    public void setRoot(DefaultMutableTreeNode root)
    {
        this.root = root;
    }
    public ExecutionParserListener(String filename)
    {
        root = new DefaultMutableTreeNode("Execution of: " + filename);
        nodes.push(root);
    }
    public void startProductionCreation(CharSequence name)
    {
        nodes.push(new DefaultMutableTreeNode(name));
    }
    public void endProductionCreation(CharSequence name, CharSequence value)
    {
        nodes.peek().add(new DefaultMutableTreeNode(value));
        DefaultMutableTreeNode child = nodes.pop();
        child.setUserObject(name);
        nodes.peek().add(child);
    }
}