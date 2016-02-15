
public class Range
{
    private int start;
    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    private int end;

    public Range(int start, int end)
    {
	this.start= start;
	this.end= end;
    }

    public void add(Range aRange)
    {
	start+= aRange.start;
	end+= aRange.end;
    }

    public void addStart(int delta)
    {
	start+= delta;
    }

    public void addEnd(int delta)
    {
	end+= delta;
    }
}
