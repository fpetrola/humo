package ar.net.fpetrola.h;

public class NullTextRange extends TextRange
{
    public NullTextRange()
    {
	super(0, null);
    }

    public int getOverflowStart()
    {
	return 0;
    }

    public int getStart()
    {
	return 0;
    }

    public void incLength(int delta)
    {
    }
}
