package ar.net.fpetrola.humo;

import java.util.ArrayList;
import java.util.List;

public class HumoTextDocumentImpl implements HumoTextDocument
{
    private int caretPosition;
    private boolean auto;
    private StringBuilder text= new StringBuilder();
    private List<StyledSpan> spans= new ArrayList<>();

    public int getCaretPosition()
    {
	return caretPosition;
    }

    public HumoTextDocumentImpl()
    {
    }

    public int getSelectionStart()
    {
	return 0;
    }

    public int getSelectionEnd()
    {
	return 0;
    }

    public void setCaretPosition(int caretPosition)
    {
	this.caretPosition= caretPosition;
    }

    public void setSpan(String style, int start, int end)
    {
	boolean spanAdded= false;
	for (StyledSpan styledSpan : spans)
	{
	    if (!spanAdded)
		if (styledSpan.getStyle().equals(style))
		{
		    if (start >= styledSpan.getStart() && start <= styledSpan.getEnd())
		    {
			if (end >= styledSpan.getEnd())
			{
			    styledSpan.setEnd(end);
			    spanAdded= true;
			}
		    }
		}
		else if (styledSpan.getStart() == start && styledSpan.getEnd() == end)
		{
		    styledSpan.setStyle(style);
		    spanAdded= true;
		}
	}

	if (!spanAdded)
	    getSpans().add(new StyledSpan(style, start, end));

	//	System.out.println("spans1:" + spans.size());
    }

    public void clear()
    {
	spans.clear();
	text.delete(0, text.length());
    }

    public void insert(int start, String string)
    {
	text.insert(start, string);
    }

    public int getLength()
    {
	return text.length();
    }

    public void delete(int startPosition, int endPosition)
    {
	text.delete(startPosition, endPosition);
    }

    public void setAuto(boolean auto)
    {
	this.auto= auto;
    }

    public boolean isAuto()
    {
	return auto;
    }

    public String getText()
    {
	return text.toString();
    }

    public List<StyledSpan> getSpans()
    {
	return spans;
    }

    public void setSpans(List<StyledSpan> spans)
    {
	this.spans= spans;
    }

    public int hashCode()
    {
        return text.hashCode();
    }
    
    public boolean equals(Object obj)
    {
        return getText().equals(((HumoTextDocument)obj).getText());
    }
}
