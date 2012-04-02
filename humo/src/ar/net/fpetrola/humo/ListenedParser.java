/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;


public class ListenedParser extends HumoParser
{
    protected ParserListener parserListener;
    protected boolean disabled;

    public boolean isDisabled()
    {
	return disabled;
    }

    public void setDisabled(boolean disabled)
    {
	this.disabled= disabled;
    }

    public ListenedParser(ParserListener parserListener)
    {
	super(parserListener);
	this.parserListener= parserListener;
	productions= new LoggingMap();
    }

    public int parse(StringBuilder sourcecode, int first)
    {
	if (disabled)
	    return 0;

	int result;
	try
	{
	    result= super.parse(sourcecode, first);
	}
	catch (Exception e)
	{
	    return 0;
	}

	return result;
    }
    public LoggingMap getLoggingMap()
    {
	return (LoggingMap) productions;
    }

    public void init()
    {
	productions.clear();
    }
}
