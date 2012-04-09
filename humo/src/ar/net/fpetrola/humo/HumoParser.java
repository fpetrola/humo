/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

public class HumoParser
{
    protected Map<CharSequence, CharSequence> productions= new HashMap<CharSequence, CharSequence>();
    protected ParserListener parserListener;

    public HumoParser(ParserListener parserListener)
    {
	this.parserListener= parserListener;
    }

    public int parse(StringBuilder sourcecode, int first)
    {
	int last= first, current= first;
	parserListener.startProductionParsing(sourcecode, first, current, last);

	for (char currentChar; last < sourcecode.length() && (currentChar= sourcecode.charAt(last++)) != '}';)
	{
	    parserListener.startParsingLoop(sourcecode, first, current, last, currentChar);
	    if (currentChar == '{')
	    {
		parserListener.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
		current= parse(sourcecode, last);
		parserListener.afterParseProductionBody(sourcecode, first, current, last, currentChar, sourcecode.subSequence(first, last - 1), sourcecode.subSequence(last, current));
		productions.put(sourcecode.subSequence(first, last - 1), sourcecode.subSequence(last, current));
		last= first= ++current;
	    }
	    else
	    {
		parserListener.beforeProductionSearch(sourcecode, first, current, last, currentChar);
		CharSequence production= productions.get(sourcecode.subSequence(current, last));
		if (production != null)
		{
		    StringBuilder value= new StringBuilder(production);
		    parserListener.afterProductionFound(sourcecode, first, current, last, currentChar, new StringBuilder(sourcecode.subSequence(current, last)), value);
		    parserListener.beforeProductionParsing(sourcecode, first, current, last, currentChar, new StringBuilder(sourcecode.subSequence(current, last)), value);
		    parse(value, 0);
		    int[] edges= ClearCharSequence.findEdges(sourcecode, current, last); // Just to beautify debugging process
		    parserListener.beforeProductionReplacement(sourcecode, first, current, last, currentChar, value, edges[0], edges[1], new StringBuilder(sourcecode.subSequence(current, last)));
		    sourcecode.replace(current= edges[0], edges[1], value.toString());
		    parserListener.afterProductionReplacement(sourcecode, first, current, last, currentChar, value, edges[0], edges[1]);
		    last= current+= value.length();
		}
	    }
	    parserListener.endParsingLoop(sourcecode, first, current, last, currentChar);
	}

	parserListener.endProductionParsing(sourcecode, first, current, last);
	return last - 1;
    }
}
