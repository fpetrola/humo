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

    public void endProductionCreation(CharSequence name, CharSequence value)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.endProductionCreation(name, value);
    }

    public void startProductionCreation(CharSequence name)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.startProductionCreation(name);
    }

    public void getProduction(CharSequence key, CharSequence value)
    {
	for (ParserListener parserListener : parserListeners)
	    parserListener.getProduction(key, value);
    }
}
