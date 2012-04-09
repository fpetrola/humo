package ar.net.fpetrola.humo;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class HighlighterParserListener extends DefaultParserListener implements ParserListener
{
    public static void updateCaretPosition(JTextPane textPane, ProductionFrame aFrame)
    {
	StyledDocument styledDocument= (StyledDocument) aFrame.getDocument();
	int caretPosition= aFrame.getFirst();
	if (styledDocument.getLength() > caretPosition + 300)
	    caretPosition+= 300;
	else
	    caretPosition= styledDocument.getLength() - 1;

	try
	{
	    textPane.setCaretPosition(caretPosition);
	    //	    Thread.sleep(1);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    protected JTextPane textPane;

    public HighlighterParserListener(JTextPane textPane)
    {
	this.textPane= textPane;
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	try
	{
	    showProductionMatch(sourcecode, current, last);

	    updateFrame(currentFrame, textPane);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	try
	{
	    StyledDocument doc= (StyledDocument) currentFrame.getDocument();
	    int length= endPosition - startPosition;
	    doc.remove(startPosition, length);
	    doc.insertString(startPosition, value.toString(), null);

	    Style style= doc.getStyle("Cursor");
	    doc.setCharacterAttributes(current, value.length(), style, false);
	    highlightCurlys(sourcecode, startPosition, doc, value.length());
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	textPane.setDocument(currentFrame.getDocument());
	updateFrame(currentFrame, textPane);
	showProductionMatch(currentFrame.getProduction(), currentFrame.getCurrent(), currentFrame.getLast());
    }

    private void highlightCurlys(StringBuilder sourcecode, int startPosition, StyledDocument doc, int length)
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
    public void init(String filename, StringBuilder sourcecode, boolean createComponents)
    {
    }

    private void showProductionMatch(StringBuilder sourcecode, int current, int last)
    {
	StyledDocument doc= (StyledDocument) currentFrame.getDocument();
	highlightCurlys(sourcecode, current, doc, last - current);
	Style style= doc.getStyle("production-matching");
	doc.setCharacterAttributes(current, last - current, style, false);
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	try
	{
	    StyledDocument doc= (StyledDocument) currentFrame.getDocument();
//	    highlightCurlys(sourcecode, lastCurrent, doc, current - lastCurrent);
	    Style style= doc.getStyle(HumoTester.FETCH_STYLE);
	    doc.setCharacterAttributes(current, last - current, style, false);
	}
	catch (Exception e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	try
	{
	    textPane.setDocument(currentFrame.getDocument());
	    //		Thread.sleep(10);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public static void updateFrame(ProductionFrame productionFrame, JTextPane textPane)
    {
	textPane.setDocument(productionFrame.getDocument());
	updateCaretPosition(textPane, productionFrame);
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	this.currentFrame= productionFrame;
    }
}
