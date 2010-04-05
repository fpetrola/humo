/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package net.ar.fpetrola.humo;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeParserListener implements ParserListener
{
    protected Stack<DefaultMutableTreeNode> nodes = new Stack<DefaultMutableTreeNode>();

    public TreeParserListener(DefaultMutableTreeNode aRoot)
    {
        nodes.push(aRoot);
    }
    public void startProductionCreation(String name)
    {
        nodes.push(new DefaultMutableTreeNode(name));
    }
    public void endProductionCreation(String name, String value)
    {
        nodes.peek().add(new DefaultMutableTreeNode(value));
        DefaultMutableTreeNode child = nodes.pop();
        child.setUserObject(name);
        nodes.peek().add(child);
    }
}