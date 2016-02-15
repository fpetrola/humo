
public class TextRange
{
    public int start;
    public int end;
    public int spanTagsLength;
    public int startSpanLength;
    public int closeSpanLength;

    public TextRange(int start, int end, int spanTagsLength, int startSpanLength, int closeSpanLength)
    {
	this.start= start;
	this.end= end;
	this.spanTagsLength= spanTagsLength;
	this.startSpanLength= startSpanLength;
	this.closeSpanLength= closeSpanLength;
    }
}
