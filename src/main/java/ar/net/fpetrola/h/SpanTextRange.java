package ar.net.fpetrola.h;

public class SpanTextRange extends TextRange
{
    public int overflow;

    public SpanTextRange(String text, String style, TextRange previousRange, StringBuffer textBuffer)
    {
	super(text, style, previousRange, textBuffer);

	overflow= text.length();
    }

    public SpanTextRange(int length, TextRange previousRange, StringBuffer textBuffer)
    {
	super(length, previousRange, textBuffer);
    }

    public SpanTextRange(int length, StringBuffer textBuffer)
    {
	super(length, textBuffer);
    }

    public TextRange findRangeForPosition(int position)
    {
	if (nextRange != null)
	    return nextRange.findRangeForPosition(position);
	else
	    return null;
    }

    public int getOverflowStart()
    {
	return previousRange.getOverflowStart() + overflow;
    }

    public void deleteRange()
    {
	stash();
	previousRange.setNextRange(nextRange);
    }

    public void detach()
    {
	TextRange lastPreviousRange= previousRange;

	if (previousRange != null)
	    previousRange.nextRange= nextRange;

	if (nextRange != null)
	    nextRange.previousRange= lastPreviousRange;
    }

    public void moveBetween(TextRange previousRange, TextRange nextRange)
    {
	stash();
	detach();
	setPreviousRange(previousRange);
	setNextRange(nextRange);
	stashPop();
    }
    
    public void insert(int position, String text)
    {
	throw new RuntimeException("cannot insert text inside a span");
    }
    
    public void setLength(int length)
    {
	throw new RuntimeException("cannot change span length");
    }
}