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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProductionsParserListener extends DefaultParserListener implements ParserListener
{
    private DefaultMutableTreeNode root;
    private Map<CharSequence, DefaultMutableTreeNode> nodes;
    private int productionsCount= 0;
    protected JTree productionsTree;

    public ProductionsParserListener()
    {
    }

    public void init(String filename, boolean createComponents)
    {
	nodes= new HashMap<CharSequence, DefaultMutableTreeNode>();
	this.root= new DefaultMutableTreeNode("Productions of: " + filename);
	if (createComponents)
	    productionsTree= new JTree(root);

	productionsTree.setModel(new DefaultTreeModel(root));
    }

    public JTree getProductionsTree()
    {
	return productionsTree;
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	productionsCount++;

	DefaultMutableTreeNode node= nodes.get(name);
	DefaultMutableTreeNode child= new DefaultMutableTreeNode(value + " (count:" + productionsCount + ")");
	if (node != null)
	    node.add(child);
	else
	{
	    DefaultMutableTreeNode parent= new DefaultMutableTreeNode(name);
	    root.add(parent);
	    parent.add(child);
	    nodes.put(name, parent);
	}

	((DefaultTreeModel) productionsTree.getModel()).reload();
    }

    public DefaultMutableTreeNode getRoot()
    {
	return root;
    }

    public void setRoot(DefaultMutableTreeNode productionsRoot)
    {
	this.root= productionsRoot;
    }
}
