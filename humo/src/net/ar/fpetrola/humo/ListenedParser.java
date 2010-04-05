/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package net.ar.fpetrola.humo;

public class ListenedParser extends HumoParser
{
    protected ParserListener parserListener;

    public ListenedParser(ParserListener parserListener)
    {
	this.parserListener= parserListener;
	productions= new LoggingMap(parserListener);
    }

    public int parse(StringBuilder sourcecode, int first)
    {
	if (first != 0)
	    parserListener.startProductionCreation("");

	return super.parse(sourcecode, first);
    }

    public LoggingMap getLoggingMap()
    {
	return (LoggingMap) productions;
    }
}
