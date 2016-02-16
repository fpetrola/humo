package ar.net.fpetrola.h;

import java.util.List;

import org.w3c.dom.Element;

import com.dragome.render.html.renderers.AbstractHTMLComponentRenderer;

import ar.net.fpetrola.humo.HumoTextDocument;
import ar.net.fpetrola.humo.StyledSpan;

public class HTMLHumoTextDocumentListener implements HumoTextDocument
{
    private HumoTextDocument humoTextDocument;
    private Element textFieldElement;
    private TextHighlighter textHighlighter= new TextHighlighter();

    public HTMLHumoTextDocumentListener(HumoTextDocument humoTextDocument, Element textFieldElement)
    {
	this.humoTextDocument= humoTextDocument;
	this.textFieldElement= textFieldElement;

	textHighlighter.insert(0, humoTextDocument.getText());
    }

    public HTMLHumoTextDocumentListener()
    {
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
	textHighlighter.highlight(style, start, end);
	setResult(textHighlighter.getResultingText());
    }

    public void clear()
    {
    }

    public void insert(int start, String string)
    {
	textHighlighter.insert(start, string);
	setResult(textHighlighter.getResultingText());
    }

    public void delete(int startPosition, int endPosition)
    {
	textHighlighter.delete(startPosition, endPosition);
	setResult(textHighlighter.getResultingText());
    }

    protected void setResult(String result)
    {
	AbstractHTMLComponentRenderer.setElementInnerHTML(textFieldElement, escapeSpans(result));
    }

    private String escapeHTML(String text1)
    {
	return text1.replace("<", "&lt;").replace("<", "&gt;").replaceAll("\n", "<br>");
    }

    private String escapeSpans(String text1)
    {
	text1= text1.replace("<span", "[span");
	text1= text1.replace("'>", "'¿");
	text1= text1.replace("</span>", "[/span]");

	String escapedHTML= escapeHTML(text1);

	escapedHTML= escapedHTML.replace("[/span]", "</span");
	escapedHTML= escapedHTML.replace("[span", "<span");
	escapedHTML= escapedHTML.replace("'¿", "'>");

	return escapedHTML;
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