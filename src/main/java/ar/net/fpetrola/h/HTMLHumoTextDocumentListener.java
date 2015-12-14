package ar.net.fpetrola.h;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.dragome.render.html.renderers.AbstractHTMLComponentRenderer;

import ar.net.fpetrola.humo.HumoTextDocument;
import ar.net.fpetrola.humo.StyledSpan;

public class HTMLHumoTextDocumentListener implements HumoTextDocument
{
    private HumoTextDocument humoTextDocument;
    private Element textFieldElement;
    private Map<StyledSpan, Integer> spanElementsIds= new HashMap<>();

    public HTMLHumoTextDocumentListener(HumoTextDocument humoTextDocument, Element textFieldElement)
    {
	this.humoTextDocument= humoTextDocument;
	this.textFieldElement= textFieldElement;
    }

    public int getCaretPosition()
    {
	return 0;
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

    }

    public void setSpan(String style, int start, int end)
    {
	//	updateWholeText();
    }

    public void clear()
    {
	updateWholeText();
    }

    public void insert(int start, String string)
    {
	List<StyledSpan> spans= humoTextDocument.getSpans();

	if (string.length() > 0)
	    for (StyledSpan styledSpan : spans)
	    {
		if (start <= styledSpan.getStart())
		{
		    int len= styledSpan.getEnd() - styledSpan.getStart();
		    styledSpan.setStart(styledSpan.getStart() + string.length());
		    styledSpan.setEnd(styledSpan.getEnd() + string.length());
		}
		else if (start + string.length() <= styledSpan.getEnd())
		{
		    styledSpan.setEnd(styledSpan.getEnd() + string.length());
		}
		else if (start >= styledSpan.getStart() && start < styledSpan.getEnd())
		{
		    styledSpan.setEnd(start);
		}

		//		else if (start <= styledSpan.getStart())
		//		{
		//		    styledSpan.setStart(styledSpan.getStart() + string.length());
		//		    styledSpan.setEnd(styledSpan.getEnd() + string.length());
		//		}

	    }

	updateWholeText();
    }

    public void delete(int startPosition, int endPosition)
    {
	List<StyledSpan> spans= humoTextDocument.getSpans();

	for (StyledSpan styledSpan : spans)
	{
	    int delta= endPosition - startPosition;

	    if (endPosition == styledSpan.getEnd() && startPosition == styledSpan.getStart() + 1)
	    {
		styledSpan.setStyle("");
	    }
	    else if (startPosition <= styledSpan.getStart())
	    {
		if (endPosition >= styledSpan.getEnd())
		{
		    styledSpan.setStyle("");
		}
		else
		{
		    styledSpan.setEnd(styledSpan.getEnd() - delta);
		    if (endPosition <= styledSpan.getStart())
		    {
			styledSpan.setStart(styledSpan.getStart() - delta);
		    }
		    else
		    {
			styledSpan.setStart(startPosition);
		    }
		}
	    }
	    else if (startPosition > styledSpan.getStart())
	    {
		if (endPosition < styledSpan.getEnd())
		{
		    styledSpan.setEnd(styledSpan.getEnd() - delta);
		}
		else
		{
		    //		    styledSpan.setStyle("");
		    //		    styledSpan.setEnd(startPosition + 2);

		    //		    System.out.println("fer1");
		}
	    }

	}

	updateWholeText();
    }

    //    private void updateWholeText()
    //    {
    //	String text1= humoTextDocument.getText();
    //	StringBuilder text= new StringBuilder(text1);
    //	List<StyledSpan> spans= humoTextDocument.getSpans();
    //
    //	StringBuilder result= new StringBuilder();
    //	result.insert(0, text.toString());
    //
    //	System.out.println("spans:" + spans.size());
    //
    //	int delta= 0;
    //
    //	for (StyledSpan styledSpan : spans)
    //	{
    //	    String toReplace= text.substring(styledSpan.getStart(), styledSpan.getEnd());
    //	    String str= "<span class='" + styledSpan.getStyle() + "'>" + escapeHTML(toReplace) + "</span>";
    //	    result.replace(styledSpan.getStart() + delta, styledSpan.getEnd() + delta, str);
    //	    delta+= str.length() - toReplace.length();
    //	}
    //
    //	AbstractHTMLComponentRenderer.setElementInnerHTML(textFieldElement, result.toString());
    //    }

    private void updateWholeText()
    {
	String text1= humoTextDocument.getText();
	StringBuilder text= new StringBuilder(text1);

	StringBuilder result= new StringBuilder();

	String[] classes= findClasses(text1);

	String lastStyle= "";
	int lastStart= 0;
	int id= 0;
	for (int i= 0; i < classes.length; i++)
	{
	    String currentStyle= classes[i];
	    if (currentStyle == null)
		currentStyle= "";

	    if (!lastStyle.equals(currentStyle))
	    {
		String toReplace= text.substring(lastStart, i);
		String escapeHTML= escapeHTML(toReplace);
		if (currentStyle.equals(""))
		{
		    result.append(escapeHTML);
		}
		else
		{
		    String str= "<span id='styled-span-" + id + "' class='" + lastStyle + "'>" + escapeHTML + "</span>";
		    result.append(str);
		    id++;
		}
		lastStart= i;
	    }

	    lastStyle= currentStyle;
	}

	if (lastStart < text1.length())
	    result.append(escapeHTML(text1.substring(lastStart)));

	AbstractHTMLComponentRenderer.setElementInnerHTML(textFieldElement, result.toString());
    }

    private String[] findClasses(String text1)
    {
	String[] classes= new String[text1.length()];

	List<StyledSpan> spans= humoTextDocument.getSpans();
	for (StyledSpan styledSpan : spans)
	{
	    for (int i= styledSpan.getStart(); i < styledSpan.getEnd(); i++)
	    {
		if (classes[i] == null)
		    classes[i]= "";

		if (!classes[i].contains(styledSpan.getStyle()))
		    classes[i]+= styledSpan.getStyle() + " ";
	    }
	}
	return classes;
    }

    private String escapeHTML(String text1)
    {
	return text1.replace("<", "&lt;").replace("<", "&gt;").replaceAll("\n", "<br>");
    }

    public int getLength()
    {
	return 0;
    }

    public void setAuto(boolean auto)
    {

    }

    public boolean isAuto()
    {
	return false;
    }

    public String getText()
    {
	return null;
    }

    public List<StyledSpan> getSpans()
    {
	return null;
    }

    public void setSpans(List<StyledSpan> spans)
    {

    }

}