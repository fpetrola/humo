package ar.net.fpetrola.humo;

public class Controls
{
	protected boolean skipSmall= true;

	public boolean isSkipSmall()
	{
		return skipSmall;
	}
	public void setSkipSmall(boolean skipSmall)
	{
		this.skipSmall= skipSmall;
	}
	public int getSkipSize()
	{
		return skipSize;
	}
	public void setSkipSize(int skipSize)
	{
		this.skipSize= skipSize;
	}
	public boolean isSkipAll()
	{
		return skipAll;
	}
	public void setSkipAll(boolean skipAll)
	{
		this.skipAll= skipAll;
	}

	protected int skipSize;
	protected boolean skipAll;
}
