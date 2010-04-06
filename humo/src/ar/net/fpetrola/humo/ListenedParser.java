/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import javax.swing.JTextArea;

public class ListenedParser extends HumoParser
{
    protected ParserListener parserListener;
    private final JTextArea textArea;

    public ListenedParser(ParserListener parserListener, JTextArea textArea)
    {
        this.parserListener = parserListener;
        this.textArea = textArea;
        productions = new LoggingMap(parserListener);
    }

    public int parse(StringBuilder sourcecode, int first)
    {
        if (first != 0)
            parserListener.startProductionCreation("");

        int result = super.parse(sourcecode, first);
        textArea.setText(sourcecode.toString());
        return result;
    }

    public LoggingMap getLoggingMap()
    {
        return (LoggingMap) productions;
    }
}
