package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProductionsParserListener implements ParserListener
{
    private DefaultMutableTreeNode productionsRoot;
    private Map<String, DefaultMutableTreeNode> nodes = new HashMap<String, DefaultMutableTreeNode>();
    private int productionsCount = 0;

    public ProductionsParserListener(DefaultMutableTreeNode productionsRoot)
    {
        this.productionsRoot = productionsRoot;
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
            productionsRoot.add(parent);
            parent.add(child);
            nodes.put(name, parent);
        }
    }

    public void startProductionCreation(String name)
    {
    }
}
