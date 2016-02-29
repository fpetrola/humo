package ar.net.fpetrola.h;

public class TextRange
{
    protected TextRange previousRange;
    protected TextRange nextRange;
    protected StringBuffer textBuffer;
    private int length;
    protected String style;
    protected String text;
    private int start;

    public TextRange(int length, StringBuffer textBuffer)
    {
	this(length, null, textBuffer);
    }
    public TextRange(int length, TextRange previousRange, StringBuffer textBuffer)
    {
	this.start= previousRange != null ? previousRange.getEnd() : 0;
	this.length= length;
	this.previousRange= previousRange;
	this.textBuffer= textBuffer;
	if (previousRange != null)
	    previousRange.setNextRange(this);
    }

    public TextRange(String text, String style, TextRange previousRange, StringBuffer textBuffer)
    {
	this(0, previousRange, textBuffer);
	this.text= text;
	this.style= style;
	Helper.insertIntoStringBufferSafely(textBuffer, mapPosition(getStart()), text);
    }

    public void deleteInRange(int start, int end)
    {
	if (end < getEnd())
	{
	    int i= end - start;
	    setLength(getLength() - i);
	    textBuffer.delete(mapPosition(start), mapPosition(end));
	}
	else
	{
	    int s= mapPosition(start);
	    int e= mapPosition(end > getEnd() ? getEnd() : end);

	    int i= e - s;
	    textBuffer.delete(s, e);
	    setLength(getLength() - i);
	    if (nextRange != null)
		nextRange.deleteInRange(start, end - i);
	}

    }

    public TextRange findRangeForPosition(int position)
    {
	if (nextRange == null || (position >= getStart() && position < getEnd() && getLength() > 0))
	    return this;
	else
	    return nextRange.findRangeForPosition(position);
    }

    public int getEnd()
    {
	return getStart() + getLength();
    }

    public int getMappedStart()
    {
	return getOverflowStart() + getStart();
    }

    public TextRange getNextRange()
    {
	return nextRange;
    }

    public int getOverflowStart()
    {
	return mapPosition(0);
    }

    public TextRange getPreviousRange()
    {
	return previousRange;
    }

    public int getStart()
    {
	return start;
    }

    public void incLength(int delta)
    {
	this.setLength(this.getLength() + delta);
    }

    public void insert(int position, String text)
    {
	Helper.insertIntoStringBufferSafely(textBuffer, mapPosition(position), text);
	incLength(text.length());
    }

    public int mapPosition(int position)
    {
	return previousRange.getOverflowStart() + position;
    }

    public void setLength(int length)
    {
	if (length == 0)
	    deleteRange();
	else if (length < 0)
	    throw new RuntimeException("length cannot be lower than 0");

	this.length= length;

	if (nextRange != null)
	    nextRange.setStart(getEnd());
    }

    public void setStart(int start)
    {
	this.start= start;
	if (nextRange != null)
	    nextRange.setStart(getEnd());
    }

    public void setNextRange(TextRange textRange)
    {
	if (textRange != null)
	{
	    this.nextRange= textRange;
	    textRange.setPreviousRange(this);
	}
    }

    public void setPreviousRange(TextRange textRange)
    {
	previousRange= textRange;
	start= previousRange.getEnd();
	if (previousRange.nextRange != this)
	    previousRange.setNextRange(this);
    }

    public void stash()
    {
	int mappedStart= mapPosition(getStart());
	textBuffer.delete(mappedStart, mappedStart + text.length());
    }

    public void stashPop()
    {
	Helper.insertIntoStringBufferSafely(textBuffer, mapPosition(getStart()), text);
    }

    public String toString()
    {
	return "start:" + getStart() + ", length:" + getLength();
    }
    public void deleteRange()
    {
    }
    public TextRange splitAt(int position)
    {
	TextRange lastNextRange= nextRange;
	int i= position - getStart();
	int delta= getLength() - i;
	TextRange newTextRange= new TextRange(delta, this, textBuffer);
	setLength(getLength() - delta);
	newTextRange.setNextRange(lastNextRange);
	return newTextRange;
    }
    public <T extends TextRange> T findFirst(Class<T> type)
    {
	if (type.isAssignableFrom(this.getClass()))
	    return (T) this;
	else
	    return nextRange.findFirst(type);
    }
    public int getLength()
    {
	return length;
    }
    public TextRange findLastRange()
    {
	if (nextRange != null)
	    return nextRange.findLastRange();
	else
	    return this;
    }
}
