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

    public ListenedParser(ParserListener parserListener, JTextPane textPane)
    {
        this.parserListener = parserListener;
        this.textPane = textPane;
        productions = new LoggingMap(parserListener);
    }

    public int parse(StringBuilder sourcecode, int first)
    {
        if (first != 0)
            parserListener.startProductionCreation("");

        int result = super.parse(sourcecode, first);

        if (sourcecode.length() > 50)
        {
            HumoTester.configureTextPane(sourcecode, textPane);
            StyledDocument styledDocument = (StyledDocument) textPane.getDocument();
            styledDocument.setCharacterAttributes(first, 2, styledDocument.getStyle("Cursor"), false);
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        return result;
    }

    public LoggingMap getLoggingMap()
    {
        return (LoggingMap) productions;
    }
}
