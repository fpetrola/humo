package ar.net.fpetrola.humo;

import javax.swing.tree.DefaultMutableTreeNode;

public class StacktraceTreeNode extends DefaultMutableTreeNode
{
    private CharSequence value;

    public CharSequence getValue()
    {
	return value;
    }

    public StacktraceTreeNode(CharSequence key, CharSequence value)
    {
	super(key);
	this.value= value;
    }
}
