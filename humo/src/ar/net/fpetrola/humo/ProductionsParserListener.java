/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProductionsParserListener implements ParserListener
{
    private DefaultMutableTreeNode root;
    private Map<String, DefaultMutableTreeNode> nodes = new HashMap<String, DefaultMutableTreeNode>();
    private int productionsCount = 0;

    public ProductionsParserListener(String filename)
    {
        this.root = new DefaultMutableTreeNode("Productions of: " + filename);
    }
    
    public void endProductionCreation(String name, String value)
    {
        productionsCount++;

        DefaultMutableTreeNode node = nodes.get(name);
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(value + " (count:" + productionsCount + ")");
        if (node != null)
            node.add(child);
        else
        {
            DefaultMutableTreeNode parent = new DefaultMutableTreeNode(name);
            root.add(parent);
            parent.add(child);
            nodes.put(name, parent);
        }
    }

    public DefaultMutableTreeNode getRoot()
    {
        return root;
    }

    public void setRoot(DefaultMutableTreeNode productionsRoot)
    {
        this.root = productionsRoot;
    }

    public void startProductionCreation(String name)
    {
    }
}
