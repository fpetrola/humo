package ar.net.fpetrola.h;

public class CloseSpanTextRange extends SpanTextRange
{
    public CloseSpanTextRange(String style, TextRange previousTextRange, StringBuffer textBuffer)
    {
	super("</span>", style, previousTextRange, textBuffer);
    }

    public void deleteInRange(int start, int end)
    {
	if (nextRange != null)
	    nextRange.deleteInRange(start, end);
    }

    public int moveRangeTo(int end)
    {
	int delta= 0;
	if (end != getEnd())
	{
	    stash();
	    int length= end - getPreviousRange().getStart();
	    delta= getPreviousRange().getLength() - length;
	    getPreviousRange().setLength(length);
	    stashPop();
	    if (getNextRange() != null)
		if (getNextRange().getLength() > 0)
		    getNextRange().incLength(delta);
		else
		    getNextRange().deleteRange();
	}

	return delta;
    }
}
