package ar.net.fpetrola.humo;

import ar.net.fpetrola.humo.gui.DefaultTreeNode;

public class StacktraceTreeNode extends DefaultTreeNode
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
		this.frame= frame;
	}
}
