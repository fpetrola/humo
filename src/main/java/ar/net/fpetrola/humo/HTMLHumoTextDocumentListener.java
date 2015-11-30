package ar.net.fpetrola.humo;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import com.dragome.render.html.renderers.AbstractHTMLComponentRenderer;

public class HTMLHumoTextDocumentListener implements HumoTextDocument
{
    private HumoTextDocument humoTextDocument;
    private Element textFieldElement;

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
    }

    public void clear()
    {
	updateWholeText();
    }

    public void insert(int start, String string)
    {
	updateWholeText();
    }

    private void updateWholeText()
    {
	String text1= humoTextDocument.getText();
	String text2= text1.replace("<", "&lt;").replace("<", "&gt;").replaceAll("\n", "<br>");
	StringBuilder text= new StringBuilder(text2);
	List<StyledSpan> spans= humoTextDocument.getSpans();
	
	StringBuilder result= new StringBuilder();
	int lastPosition= 0;
	
	System.out.println("spans:"+spans.size());
	for (StyledSpan styledSpan : spans)
	{
	    String toReplace= text.substring(styledSpan.getStart(), styledSpan.getEnd());
	    result.append(text.substring(lastPosition, styledSpan.getStart()));
	    result.append("<span style='color:red'>" + toReplace + "</span>");
	    result.append(text.substring(styledSpan.getEnd()));
	    lastPosition= styledSpan.getEnd();
	}
	AbstractHTMLComponentRenderer.setElementInnerHTML(textFieldElement, text1.toString());
    }

    public int getLength()
    {
	return 0;
    }

    public void delete(int startPosition, int endPosition)
    {
	updateWholeText();
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