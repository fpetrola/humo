package ar.net.fpetrola.humo;

public class StyledSpan
{
    private String style;

    public String getStyle()
    {
	return style;
    }

    public void setStyle(String style)
    {
	this.style= style;
    }

    public int getStart()
    {
	return start;
    }

    public void setStart(int start)
    {
	this.start= start;
    }

    public int getEnd()
    {
	return end;
    }

    public void setEnd(int end)
    {
	this.end= end;
    }

    private int start;
    private int end;

    public StyledSpan(String style, int start, int end)
    {
	this.style= style;
	this.start= start;
	this.end= end;
    }
}
