package ar.net.fpetrola.humo;

public class ParserListenerDelegator implements ParserListener
{
    protected ParserListener parserListenerDelegate;

    public ParserListener getParserListenerDelegate()
    {
        return parserListenerDelegate;
    }

    public void setParserListenerDelegate(ParserListener parserListenerDelegate)
    {
        this.parserListenerDelegate= parserListenerDelegate;
    }

    public ParserListenerDelegator(ParserListener parserListener)
    {
	this.parserListenerDelegate= parserListener;
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	parserListenerDelegate.startProductionParsing(sourcecode, first, current, last);
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.startParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void endParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.endParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	parserListenerDelegate.afterParseProductionBody(sourcecode, first, current, last, currentChar, name, value);
    }

    public void beforeProductionSearch(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.beforeProductionSearch(sourcecode, first, current, last, currentChar);
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	parserListenerDelegate.afterProductionFound(sourcecode, first, current, last, currentChar, name, value);
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	parserListenerDelegate.beforeProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition, name);
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	parserListenerDelegate.afterProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition);
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	parserListenerDelegate.endProductionParsing(sourcecode, first, current, last);
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	parserListenerDelegate.setCurrentFrame(productionFrame);
    }

    public void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder stringBuilder, StringBuilder value)
    {
	parserListenerDelegate.beforeProductionParsing(sourcecode, first, current, last, currentChar, stringBuilder, value);
    }
}
