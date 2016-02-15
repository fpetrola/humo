import java.util.ArrayList;
import java.util.List;

public class TextHighlighter
{
    private StringBuffer textBuffer= new StringBuffer();
    private List<TextRange> ranges= new ArrayList<>();

    public String getResultingText()
    {
	return textBuffer.toString();
    }

    public void insert(int start, String aText)
    {
	int delta= aText.length();
	int relocationDelta= relocateRanges(start, delta);
	Helper.insertIntoStringBufferSafely(textBuffer, relocationDelta + start, aText);
    }

    protected int relocateRanges(int start, int delta)
    {
	int relocationDelta= 0;
	for (TextRange textRange : ranges)
	{
	    if (textRange.start < start)
		relocationDelta+= textRange.startSpanLength;
	    else
		textRange.start+= delta;

	    if (textRange.end < start)
		relocationDelta+= textRange.closeSpanLength;
	    else
		textRange.end+= delta;
	}
	return relocationDelta;
    }

    public void remove(int start, int end)
    {
	int deletedChars= -(end - start);
	int relocationDelta= relocateRanges(start, deletedChars);
	textBuffer.delete(start + relocationDelta, end + relocationDelta);
    }

    public void highlight(String style, int start, int end)
    {
	String closeSpanText= "</span>";
	String startSpanText= "<span class='" + style + "'>";

	int relocationDelta= 0;

	for (TextRange textRange : ranges)
	{
	    if (textRange.end < start)
		relocationDelta+= textRange.spanTagsLength;
	    else if (textRange.start < start)
		relocationDelta+= textRange.startSpanLength;
	}

	textBuffer.insert(relocationDelta + end, closeSpanText);
	textBuffer.insert(relocationDelta + start, startSpanText);

	TextRange textRange= new TextRange(start, end, closeSpanText.length() + startSpanText.length(), startSpanText.length(), closeSpanText.length());
	ranges.add(textRange);
    }
}
