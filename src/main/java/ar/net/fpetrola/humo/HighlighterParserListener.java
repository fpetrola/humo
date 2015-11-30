package ar.net.fpetrola.humo;

public class HighlighterParserListener extends DefaultParserListener implements ParserListener
{
    public void updateCaretPosition(final ProductionFrame aFrame)
    {
	HumoTextDocument styledDocument= (HumoTextDocument) aFrame.getDocument();
	int length= styledDocument.getLength();
	int caretPosition= aFrame.getFirst();
	if (length > caretPosition + 300)
	    caretPosition+= 300;
	else
	    caretPosition= length - 1;

	if (caretPosition >= 0 && caretPosition < textDocument.getLength())
	    textDocument.setCaretPosition(caretPosition);
    }

    private HumoTextDocument textDocument;
    private final DebuggerParserListener debugDelegator;

    public HighlighterParserListener(DebuggerParserListener debugDelegator)
    {
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
	showProductionMatch(sourcecode, current, last, TextViewHelper.PRODUCTION_FOUND_STYLE);
	updateFrame(currentFrame);
    }

    public void afterProductionReplacement(final StringBuilder sourcecode, int first, final int current, int last, char currentChar, final StringBuilder value, final int startPosition, final int endPosition)
    {
	if (debugDelegator.isVisible())
	{
	    HumoTextDocument doc= (HumoTextDocument) currentFrame.getDocument();
	    doc.setAuto(true);
	    doc.delete(startPosition, endPosition);
	    doc.insert(startPosition, value.toString());

	    doc.setSpan(TextViewHelper.CURSOR_STYLE, current, current + value.length());
	    highlightCurlys(sourcecode, startPosition, doc, value.length());
	    doc.setAuto(false);
	}
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	updateFrame(currentFrame);
	if (debugDelegator.isVisible())
	    showProductionMatch(currentFrame.getProduction(), currentFrame.getCurrent(), currentFrame.getLast(), TextViewHelper.PRODUCTION_BEFORE_REPLACEMENT_STYLE);
    }

    private void highlightCurlys(StringBuilder sourcecode, int startPosition, HumoTextDocument doc, int length)
    {
	if (debugDelegator.isVisible())
	{
	    String defaultstyle= TextViewHelper.DEFAULT_STYLE;
	    String curlyStyle= TextViewHelper.CURLY_STYLE;
	    for (int i= startPosition; i < startPosition + length; i++)
	    {
		String usingStyle;
		if (sourcecode.charAt(i) == '{' || sourcecode.charAt(i) == '}')
		{
		    usingStyle= curlyStyle;
		    doc.setSpan(usingStyle, i, i + 1);
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
	    HumoTextDocument doc= (HumoTextDocument) currentFrame.getDocument();
	    highlightCurlys(sourcecode, current, doc, last - current);
	    doc.setSpan(styleId, current, last);
	}
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	try
	{
	    if (debugDelegator.isVisible())
	    {
		HumoTextDocument doc= (HumoTextDocument) currentFrame.getDocument();
		// highlightCurlys(sourcecode, lastCurrent, doc, current -
		// lastCurrent);
		doc.setSpan(TextViewHelper.FETCH_STYLE, current, last);
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
		if (getTextDocument() != productionFrame.getDocument())
		{
		    setTextDocument(productionFrame.getDocument());
		}
		updateCaretPosition(productionFrame);
	    }
	    else
	    {
		if (debugDelegator.isInvisible())
		    setTextDocument(HumoTextDocumentFactory.createTextDocument());
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

    public HumoTextDocument getTextDocument()
    {
	return textDocument;
    }

    public void setTextDocument(HumoTextDocument textDocument)
    {
	this.textDocument= textDocument;
    }

}
