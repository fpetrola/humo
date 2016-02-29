package ar.net.fpetrola.h;

public class OpenSpanTextRange extends SpanTextRange
{
    private CloseSpanTextRange closeTagTextRange;

    public OpenSpanTextRange(String style, TextRange previousTextRange, StringBuffer textBuffer)
    {
	super("<span class='" + style + "'>", style, previousTextRange, textBuffer);
    }

    public void deleteInRange(int start, int end)
    {
	if (nextRange != null)
	{
	    nextRange.deleteInRange(start, end);
	    if (getStart() == closeTagTextRange.getStart())
	    {
		deleteRange();
		closeTagTextRange.deleteRange();
	    }
	}
    }

    public void setCloseTagRange(CloseSpanTextRange closeTagTextRange)
    {
	this.closeTagTextRange= closeTagTextRange;
    }

    public CloseSpanTextRange getCloseTagTextRange()
    {
	return closeTagTextRange;
    }
}
