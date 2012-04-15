package ar.net.fpetrola.humo;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class HighlighterParserListener extends DefaultParserListener implements ParserListener
{
    public void updateCaretPosition(final ProductionFrame aFrame)
    {
	try
	{
	    SwingUtilities.invokeLater(new Runnable()
	    {
		public void run()
		{
		    StyledDocument styledDocument= (StyledDocument) aFrame.getDocument();
		    int length= styledDocument.getLength();
		    int caretPosition= aFrame.getFirst();
		    if (length > caretPosition + 300)
			caretPosition+= 300;
		    else
			caretPosition= length - 1;

		    if (caretPosition >= 0 && caretPosition < textPane.getDocument().getLength())
			textPane.setCaretPosition(caretPosition);
		}
	    });
	    Thread.sleep(1);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    protected JTextPane textPane;
    private final DebuggerParserListener debugDelegator;

    public HighlighterParserListener(JTextPane textPane, DebuggerParserListener debugDelegator)
    {
	this.textPane= textPane;
	this.debugDelegator= debugDelegator;
	debugDelegator.setVisibilityListener(new VisibilityListener()
	{
	    public void invisibleChanged(boolean invisible)
	    {
		updateFrame(currentFrame);
	    }
	});
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	showProductionMatch(sourcecode, current, last, HumoTester.PRODUCTION_FOUND_STYLE);
	updateFrame(currentFrame);
    }

    public void afterProductionReplacement(final StringBuilder sourcecode, int first, final int current, int last, char currentChar, final StringBuilder value, final int startPosition, final int endPosition)
    {
	if (debugDelegator.isVisible())
	{
	    try
	    {
		SwingUtilities.invokeAndWait(new Runnable()
		{
		    public void run()
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
		});
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	updateFrame(currentFrame);
	if (debugDelegator.isVisible())
	    showProductionMatch(currentFrame.getProduction(), currentFrame.getCurrent(), currentFrame.getLast(), HumoTester.PRODUCTION_BEFORE_REPLACEMENT_STYLE);
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

    private void showProductionMatch(StringBuilder sourcecode, int current, int last, String styleId)
    {
	if (debugDelegator.isVisible())
	{
	    StyledDocument doc= (StyledDocument) currentFrame.getDocument();
	    highlightCurlys(sourcecode, current, doc, last - current);
	    Style style= doc.getStyle(styleId);
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

    public void updateFrame(final ProductionFrame productionFrame)
    {
	try
	{
	    if (!debugDelegator.isTotallyInvisible())
	    {
		if (textPane.getDocument() != productionFrame.getDocument())
		{
		    SwingUtilities.invokeLater(new Runnable()
		    {
			public void run()
			{
			    textPane.setDocument(productionFrame.getDocument());
			}
		    });
		}
		SwingUtilities.invokeLater(new Runnable()
		{
		    public void run()
		    {
			updateCaretPosition(productionFrame);
		    }
		});
	    }
	    else
	    {
		if (debugDelegator.isInvisible())
		    SwingUtilities.invokeLater(new Runnable()
		    {
			public void run()
			{
			    textPane.setDocument(new DefaultStyledDocument());
			}
		    });
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	this.currentFrame= productionFrame;
    }
}
