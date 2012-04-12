package ar.net.fpetrola.humo;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class HighlighterParserListener extends DefaultParserListener implements ParserListener
{
    public void updateCaretPosition(ProductionFrame aFrame)
    {
	StyledDocument styledDocument= (StyledDocument) aFrame.getDocument();
	int caretPosition= aFrame.getFirst();
	if (styledDocument.getLength() > caretPosition + 300)
	    caretPosition+= 300;
	else
	    caretPosition= styledDocument.getLength() - 1;

	try
	{
	    if (caretPosition >= 0)
		textPane.setCaretPosition(caretPosition);
	    //Thread.sleep(1);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    protected JTextPane textPane;
    private final ParserListenerDelegator debugDelegator;

    public HighlighterParserListener(JTextPane textPane, ParserListenerDelegator debugDelegator)
    {
	this.textPane= textPane;
	this.debugDelegator= debugDelegator;
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	showProductionMatch(sourcecode, current, last);
	updateFrame(currentFrame);
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	try
	{
	    if (debugDelegator.isVisible())
	    {
		StyledDocument doc= (StyledDocument) currentFrame.getDocument();
		int length= endPosition - startPosition;
		doc.remove(startPosition, length);
		doc.insertString(startPosition, value.toString(), null);

		Style style= doc.getStyle("Cursor");
		doc.setCharacterAttributes(current, value.length(), style, false);
		highlightCurlys(sourcecode, startPosition, doc, value.length());
	    }
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	updateFrame(currentFrame);
	if (debugDelegator.isVisible())
	    showProductionMatch(currentFrame.getProduction(), currentFrame.getCurrent(), currentFrame.getLast());
    }

    private void highlightCurlys(StringBuilder sourcecode, int startPosition, StyledDocument doc, int length)
    {
	if (debugDelegator.isVisible())
	{
	    Style defaultstyle= doc.getStyle("default");
	    Style curlyStyle= doc.getStyle(HumoTester.CURLY_STYLE);
	    for (int i= startPosition; i < startPosition + length; i++)
	    {
		Style usingStyle;
		if (sourcecode.charAt(i) == '{' || sourcecode.charAt(i) == '}')
		{
		    usingStyle= curlyStyle;
		    doc.setCharacterAttributes(i, 1, usingStyle, false);
		}
		else
		    usingStyle= defaultstyle;
	    }
	}
    }
    public void init(String filename, StringBuilder sourcecode, boolean createComponents)
    {
    }

    private void showProductionMatch(StringBuilder sourcecode, int current, int last)
    {
	if (debugDelegator.isVisible())
	{
	    StyledDocument doc= (StyledDocument) currentFrame.getDocument();
	    highlightCurlys(sourcecode, current, doc, last - current);
	    Style style= doc.getStyle("production-matching");
	    doc.setCharacterAttributes(current, last - current, style, false);
	}
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	try
	{
	    if (debugDelegator.isVisible())
	    {
		StyledDocument doc= (StyledDocument) currentFrame.getDocument();
		//	    highlightCurlys(sourcecode, lastCurrent, doc, current - lastCurrent);
		Style style= doc.getStyle(HumoTester.FETCH_STYLE);
		doc.setCharacterAttributes(current, last - current, style, false);
	    }
	}
	catch (Exception e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	startParsingLoop(sourcecode, first, current, last, '}');
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	updateFrame(currentFrame);
    }

    public void updateFrame(ProductionFrame productionFrame)
    {
	if (debugDelegator.isVisible())
	{
	    if (textPane.getDocument() != productionFrame.getDocument())
		textPane.setDocument(productionFrame.getDocument());

	    updateCaretPosition(productionFrame);
	}
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	this.currentFrame= productionFrame;
    }
}
