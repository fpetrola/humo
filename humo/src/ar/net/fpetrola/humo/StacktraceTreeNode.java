package ar.net.fpetrola.humo;

import javax.swing.tree.DefaultMutableTreeNode;

import ar.net.fpetrola.humo.DebuggingParserListener.ProductionFrame;

public class StacktraceTreeNode extends DefaultMutableTreeNode
{
    private ProductionFrame frame;

    public ProductionFrame getFrame()
    {
        return frame;
    }

    public void setFrame(ProductionFrame frame)
    {
        this.frame= frame;
    }


    public StacktraceTreeNode(CharSequence key, ProductionFrame frame)
    {
	super(key);
	this.frame= frame;
    }
}
