package org.fpetrola.humo.service.serverside;

import java.util.List;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import ar.net.fpetrola.humo.HumoTextDocument;
import ar.net.fpetrola.humo.StyledSpan;

final class HumoTextDocumentListener implements HumoTextDocument
{
    private StyledDocument styledDocument;
    private JTextPane jTextPane;

    public HumoTextDocumentListener(StyledDocument styledDocument, JTextPane jTextPane)
    {
	this.styledDocument= styledDocument;
	this.jTextPane= jTextPane;
    }

    public void setSpans(List<StyledSpan> spans)
    {

    }

    public void setSpan(String style, int start, int end)
    {
	styledDocument.setCharacterAttributes(start, end - start, styledDocument.getStyle(style), false);
    }

    public void setCaretPosition(int caretPosition)
    {
	SwingUtilities.invokeLater(new Runnable()
	{
	    public void run()
	    {
		jTextPane.setCaretPosition(caretPosition);
	    }
	});
    }

    public void setAuto(boolean auto)
    {

    }

    public boolean isAuto()
    {
	return false;
    }

    public void insert(int start, String string)
    {
	try
	{
	    styledDocument.insertString(start, string, null);
	}
	catch (BadLocationException e)
	{
	    throw new RuntimeException(e);
	}
    }

    public String getText()
    {
	try
	{
	    return styledDocument.getText(0, styledDocument.getLength());
	}
	catch (BadLocationException e)
	{
	    throw new RuntimeException(e);
	}
    }

    public List<StyledSpan> getSpans()
    {
	return null;
    }

    public int getSelectionStart()
    {
	return 0;
    }

    public int getSelectionEnd()
    {
	return 0;
    }

    public int getLength()
    {
	return 0;
    }

    public int getCaretPosition()
    {
	return 0;
    }

    public void delete(int startPosition, int endPosition)
    {
	try
	{
	    styledDocument.remove(startPosition, endPosition - startPosition);
	}
	catch (BadLocationException e)
	{
	    throw new RuntimeException(e);
	}
    }

    public void clear()
    {
	try
	{
	    styledDocument.remove(0, styledDocument.getLength());
	}
	catch (BadLocationException e)
	{
	    throw new RuntimeException(e);
	}
    }
}