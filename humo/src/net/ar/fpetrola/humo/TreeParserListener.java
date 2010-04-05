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