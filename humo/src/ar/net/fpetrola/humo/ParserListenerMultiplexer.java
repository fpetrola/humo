/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

public class ParserListenerMultiplexer implements ParserListener
{
    private ParserListener[] parserListeners;

    public ParserListenerMultiplexer(ParserListener... parserListener)
    {
	this.parserListeners= parserListener;
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterParseProductionBody(sourcecode, first, current, last, currentChar, name, value);
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterProductionFound(sourcecode, first, current, last, currentChar, name, production);
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition);
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition);
    }

    public void beforeProductionSearch(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeProductionSearch(sourcecode, first, current, last, currentChar);
    }

    public void endParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.endParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.endProductionParsing(sourcecode, first, current, last);
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.startParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void startProductionParsing(StringBuilder sourcecode, int first)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.startProductionParsing(sourcecode, first);
    }
}
