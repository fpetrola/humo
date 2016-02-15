
public interface FindRangeAction
{
    public Range performAction(int start, int end, int requiredSpace, StyledSpan storedStyledSpan, boolean isStartingInsideStoredSpan, boolean isEndingInsideStoredSpan, boolean startsAfterStored, boolean endsAfterStored, Range range);
}