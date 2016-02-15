import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTextDocument
{
    public class UpdateSpansFindRangeAction implements FindRangeAction
    {
	public Range performAction(int start, int end, int requiredSpace, StyledSpan storedStyledSpan, boolean isStartingInsideStoredSpan, boolean isEndingInsideStoredSpan, boolean startsAfterStored, boolean endsAfterStored, Range range)
	{
	    int openTagLength= storedStyledSpan.getSpanOpenTag().length();
	    int closeTagLength= storedStyledSpan.getSpanCloseTag().length();

	    if (startsAfterStored)
	    {
		range.addStart(openTagLength);

		if (endsAfterStored)
		    range.addStart(closeTagLength);
	    }

	    if (startsAfterStored || endsAfterStored)
		range.addEnd(openTagLength);

	    if (endsAfterStored)
		range.addEnd(closeTagLength);

	    if (isStartingInsideStoredSpan ^ isEndingInsideStoredSpan)
	    {
		removeStoredSpan(storedStyledSpan);
		if (isStartingInsideStoredSpan)
		    setSpan(storedStyledSpan.getStyle(), storedStyledSpan.getStart(), start);
		else if (isEndingInsideStoredSpan)
		    setSpan(storedStyledSpan.getStyle(), end, storedStyledSpan.getEnd());
	    }

	    if (!startsAfterStored && !endsAfterStored)
	    {
		storedStyledSpan.setReplacementStart(storedStyledSpan.getReplacementStart() + requiredSpace);
		storedStyledSpan.setReplacementEnd(storedStyledSpan.getReplacementEnd() + requiredSpace);
	    }
	    else if (start > storedStyledSpan.getStart() && end < storedStyledSpan.getEnd())
	    {
		storedStyledSpan.setReplacementEnd(storedStyledSpan.getReplacementEnd() + requiredSpace);
	    }

	    return range;
	}
    }

    private StringBuilder textBuffer= new StringBuilder();
    private List<StyledSpan> spans= new ArrayList<StyledSpan>();

    public void insert(int start, final String string)
    {
	Range range= findRange(start, start, string.length(), new FindRangeAction()
	{
	    public Range performAction(int start, int end, int requiredSpace, StyledSpan storedStyledSpan, boolean isStartingInsideStoredSpan, boolean isEndingInsideStoredSpan, boolean startsAfterStored, boolean endsAfterStored, Range range)
	    {
		if (startsAfterStored)
		    range.addStart(storedStyledSpan.getSpanOpenTag().length());

		if (endsAfterStored)
		    range.addStart(storedStyledSpan.getSpanCloseTag().length());

		if (endsAfterStored || startsAfterStored)
		    range.addEnd(storedStyledSpan.calculateTotalTagSpace());

		if (isStartingInsideStoredSpan && isEndingInsideStoredSpan)
		{
		    storedStyledSpan.setReplacementEnd(storedStyledSpan.getReplacementEnd() + requiredSpace);
		    storedStyledSpan.setEnd(storedStyledSpan.getEnd() + requiredSpace);
		    String head= storedStyledSpan.getReplacementText().substring(0, start - storedStyledSpan.getStart());
		    String tail= storedStyledSpan.getReplacementText().substring(start - storedStyledSpan.getStart());
		    storedStyledSpan.setReplacementText(head + string + tail);
		}

		return range;
	    }
	});

	insertIntoStringBufferSafety(range.getStart(), string);
    }

    private void insertIntoStringBufferSafety(int start, String string)
    {
	if (textBuffer.length() < start)
	    appendCharacters(start - textBuffer.length(), ' ');

	textBuffer.insert(start, string);
    }

    public void delete(int start, int end)
    {
	StyledSpan styledSpan= setSpan("to_be_deleted", start, end);

	textBuffer.delete(styledSpan.getReplacementStart(), styledSpan.getReplacementEnd());
	spans.remove(styledSpan);
    }

    private void removeStoredSpan(StyledSpan storedStyledSpan)
    {
	removeSpansBetween(storedStyledSpan.getStart(), storedStyledSpan.getEnd());
    }

    private void appendCharacters(final int numberOfChars, char aChar)
    {
	final char[] spaceArray= new char[numberOfChars];
	Arrays.fill(spaceArray, aChar);
	textBuffer.append(spaceArray);
    }

    public StyledSpan setSpan(String style, int start, int end)
    {
	StyledSpan styledSpan= new StyledSpan(style, start, end);

	int requiredSpace= styledSpan.calculateTotalTagSpace();

	Range range= findRange(start, end, requiredSpace, new UpdateSpansFindRangeAction());

	int length= end - start;
	createReplacement(length, styledSpan, range, range.getEnd() + requiredSpace);

	spans.add(styledSpan);
	return styledSpan;
    }

    private Range findRange(int start, int end, int requiredSpace, FindRangeAction findRangeAction)
    {
	Range resultingRange= new Range(start, end);

	List<StyledSpan> currentSpans= new ArrayList<StyledSpan>(spans);
	for (StyledSpan storedStyledSpan : currentSpans)
	{
	    boolean startsAfterStored= storedStyledSpan.getStart() < start;
	    boolean endsAfterStored= storedStyledSpan.getEnd() < end;
	    boolean isStartingInsideStoredSpan= storedStyledSpan.getEnd() > start && startsAfterStored;
	    boolean isEndingInsideStoredSpan= storedStyledSpan.getStart() < end && !endsAfterStored;

	    Range initialRange= new Range(0, 0);
	    Range partialRange= findRangeAction.performAction(start, end, requiredSpace, storedStyledSpan, isStartingInsideStoredSpan, isEndingInsideStoredSpan, startsAfterStored, endsAfterStored, initialRange);

	    resultingRange.add(partialRange);
	}

	return resultingRange;
    }

    private void createReplacement(int length, StyledSpan styledSpan, Range range, int replacementEnd)
    {
	styledSpan.setReplacementStart(range.getStart());
	styledSpan.setReplacementEnd(replacementEnd);
	styledSpan.setReplacementText(textBuffer.substring(range.getStart(), range.getStart() + length));

	insertIntoStringBufferSafety(range.getEnd(), styledSpan.getSpanCloseTag());
	insertIntoStringBufferSafety(range.getStart(), styledSpan.getSpanOpenTag());
    }

    public String getRenderedText()
    {
	return textBuffer.toString();
    }

    public void removeSpansBetween(int start, int end)
    {
	List<StyledSpan> currentSpans= new ArrayList<StyledSpan>(spans);
	for (StyledSpan storedStyledSpan : currentSpans)
	{
	    if (start <= storedStyledSpan.getStart() && end >= storedStyledSpan.getEnd())
	    {
		textBuffer.replace(storedStyledSpan.getReplacementStart(), storedStyledSpan.getReplacementEnd(), storedStyledSpan.getReplacementText());
		spans.remove(storedStyledSpan);
	    }
	}
    }
}
