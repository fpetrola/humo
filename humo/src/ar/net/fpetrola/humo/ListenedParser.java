/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

public class ListenedParser extends HumoParser
{
    protected ParserListener parserListener;
    private JTextPane textPane;
    protected boolean disabled;

    public boolean isDisabled()
    {
	return disabled;
    }

    public void setDisabled(boolean disabled)
    {
	this.disabled= disabled;
    }

    public ListenedParser(ParserListener parserListener, JTextPane textPane)
    {
	this.parserListener= parserListener;
	this.textPane= textPane;
	productions= new LoggingMap(parserListener);
    }

    public int parse(StringBuilder sourcecode, int first)
    {
	if (disabled)
	    return 0;

	if (first != 0)
	    parserListener.startProductionCreation("");

	if (sourcecode.length() > 50)
	{
	    HumoTester.configureTextPane(sourcecode, textPane);
	    StyledDocument styledDocument= (StyledDocument) textPane.getDocument();
	    int nextCurly= Math.min(sourcecode.substring(first).indexOf('{'), sourcecode.substring(first).indexOf('}'));
	    styledDocument.setCharacterAttributes(first, nextCurly, styledDocument.getStyle("Cursor"), false);

	    int caretPosition= first;
	    if (textPane.getDocument().getLength() > caretPosition + 300)
		caretPosition+= 300;

	    textPane.setCaretPosition(caretPosition);
	    try
	    {
		Thread.sleep(100);
	    }
	    catch (InterruptedException e)
	    {
	    }
	}

	int result;
	try
	{
	    result= super.parse(sourcecode, first);
	}
	catch (Exception e)
	{
	    return 0;
	}

	if (first == 0)
	    parserListener.parseEnded();

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
