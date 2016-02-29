package ar.net.fpetrola.h;

public class TextHighlighter
{
    StringBuffer textBuffer= new StringBuffer();
    private TextRange firstRange= new NullTextRange();

    public void insert(int position, String text)
    {
	TextRange foundRange= firstRange.findRangeForPosition(position);
	
	if (foundRange == null)
	{
	    TextRange lastRange= firstRange.findLastRange();
	    foundRange= new TextRange(text.length(), lastRange, textBuffer);
	}

	if (foundRange instanceof NullTextRange)
	    foundRange= new TextRange(text.length(), foundRange, textBuffer);

	foundRange.insert(position, text);
	Helper.verifySpans(firstRange, textBuffer);
    }

    public String getResultingText()
    {
	Helper.verifySpans(firstRange, textBuffer);
	return textBuffer.toString();
    }

    public void highlight(String style, int start, int end)
    {
	TextRange startRange= firstRange.findRangeForPosition(start);
	TextRange endRange= firstRange.findRangeForPosition(end);

	if (startRange != endRange)
	    wrapHighlightedRange(style, start, end, startRange, endRange);
	else if (startRange != null)
	    highlightFromPlainTextRange(style, start, end, startRange);

	Helper.verifySpans(firstRange, textBuffer);
    }

    private void highlightFromPlainTextRange(String style, int start, int end, TextRange startRange)
    {
	TextRange previousRange= startRange.getPreviousRange();
	if (previousRange != null && style.equals(previousRange.style) && previousRange instanceof CloseSpanTextRange && start == previousRange.getStart())
	{
	    ((CloseSpanTextRange) previousRange).moveRangeTo(end);
	}
	else
	{
	    int nextRangeStart= 0;
	    TextRange nextRange= startRange.getNextRange();
	    if (nextRange != null)
		nextRangeStart= nextRange.getStart();

	    startRange.setLength(start - startRange.getStart());

	    OpenSpanTextRange openTagTextRange= new OpenSpanTextRange(style, startRange, textBuffer);
	    TextRange middleTagTextRange= new TextRange(end - start, openTagTextRange, textBuffer);
	    CloseSpanTextRange closeTagTextRange= new CloseSpanTextRange(style, middleTagTextRange, textBuffer);
	    openTagTextRange.setCloseTagRange(closeTagTextRange);

	    int length= textBuffer.length() - closeTagTextRange.getMappedStart();
	    if (nextRange != null)
		length= nextRangeStart - closeTagTextRange.getStart();

	    if (length > 0)
	    {
		TextRange afterTagTextRange= new TextRange(length, closeTagTextRange, textBuffer);
		afterTagTextRange.setNextRange(nextRange);
	    }
	    else
		closeTagTextRange.setNextRange(nextRange);
	}
    }

    private void wrapHighlightedRange(String style, int start, int end, TextRange startRange, TextRange endRange)
    {
	if (startRange.getPreviousRange() != null && style.equals(startRange.getPreviousRange().style) && startRange.getPreviousRange() instanceof OpenSpanTextRange)
	{
	    ((CloseSpanTextRange) startRange.getNextRange()).moveRangeTo(end);
	}
	else
	{
	    TextRange splitEnd= endRange.splitAt(end);
	    TextRange splitStart= startRange.splitAt(start);
	    if (checkOpenClose(startRange, endRange, new EndsBeforeCloseChecker()))
	    {
		SpanTextRange openSpanTextRange= splitStart.findFirst(OpenSpanTextRange.class);
		openSpanTextRange.moveBetween(endRange, splitEnd);
		splitEnd= openSpanTextRange;
	    }
	    if (checkOpenClose(startRange, endRange, new EndsWithoutOpen()))
	    {
		CloseSpanTextRange closeSpanTextRange= splitStart.findFirst(CloseSpanTextRange.class);
		closeSpanTextRange.moveBetween(startRange, splitStart);
		CloseSpanTextRange closeTagTextRange= new CloseSpanTextRange(style, endRange, textBuffer);
		OpenSpanTextRange openTagTextRange= new OpenSpanTextRange(style, closeSpanTextRange, textBuffer);
		openTagTextRange.setNextRange(splitStart);
		closeTagTextRange.setNextRange(splitEnd);
		openTagTextRange.setCloseTagRange(closeTagTextRange);
		return;
	    }

	    OpenSpanTextRange openTagTextRange= new OpenSpanTextRange(style, startRange, textBuffer);
	    openTagTextRange.setNextRange(splitStart);
	    CloseSpanTextRange closeTagTextRange= new CloseSpanTextRange(style, endRange, textBuffer);
	    closeTagTextRange.setNextRange(splitEnd);
	    openTagTextRange.setCloseTagRange(closeTagTextRange);
	}
    }

    private boolean checkOpenClose(TextRange startInternalRange, TextRange endInternalRange, OpenCloseChecker openCloseChecker)
    {
	TextRange currentRange= startInternalRange;
	boolean openFound= false;
	boolean closeFound= false;
	while (currentRange != null && currentRange != endInternalRange)
	{
	    if (currentRange instanceof OpenSpanTextRange)
		openFound= true;
	    if (currentRange instanceof CloseSpanTextRange)
		closeFound= true;

	    currentRange= currentRange.getNextRange();
	}

	return openCloseChecker.checkOpenClose(openFound, closeFound);
    }

    public void delete(int start, int end)
    {
	TextRange startRange= firstRange.findRangeForPosition(start);

	startRange.deleteInRange(start, end);

	Helper.verifySpans(firstRange, textBuffer);
    }

    public void removeStyle(int start, int end)
    {
	start--;
	TextRange startRange= firstRange.findRangeForPosition(start);
	TextRange endRange= firstRange.findRangeForPosition(end);

	TextRange currentRange= startRange;

	while (currentRange != null)
	{
	    if (currentRange instanceof OpenSpanTextRange)
	    {
		OpenSpanTextRange openSpanTextRange= (OpenSpanTextRange) currentRange;
		CloseSpanTextRange closeTagTextRange= openSpanTextRange.getCloseTagTextRange();
		closeTagTextRange.deleteRange();
		openSpanTextRange.deleteRange();
		//				if (closeTagTextRange.getEnd() < end)
		//				{
		//				}
		//				else
		//				{
		//					openSpanTextRange.stash();
		//					openSpanTextRange.set
		//				}
	    }

	    currentRange= currentRange.getNextRange();
	}

	Helper.verifySpans(firstRange, textBuffer);
    }

    public int getLength()
    {
	TextRange foundRange= firstRange.findRangeForPosition(textBuffer.length());
	return foundRange.getEnd();
    }
}
