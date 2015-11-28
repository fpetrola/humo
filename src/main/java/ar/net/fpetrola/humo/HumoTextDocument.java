package ar.net.fpetrola.humo;

import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;

public class HumoTextDocument implements HumoStyledDocument
{
    private HumoStyledDocument delegate;
    private StyleContext styleContext;

    public HumoTextDocument()
    {
    }

    public HumoTextDocument(StyleContext styleContext)
    {
	this.styleContext= styleContext;
    }

    public int getSelectionStart()
    {
	return getDelegate().getSelectionStart();
    }

    public int getSelectionEnd()
    {
	return getDelegate().getSelectionEnd();
    }

    public void setCaretPosition(int caretPosition)
    {
	getDelegate().setCaretPosition(caretPosition);
    }

    public int getLength()
    {
	return getDelegate().getLength();
    }

    public void putProperty(String name, Object value)
    {
	getDelegate().putProperty(name, value);
    }

    public void remove(int startPosition, int length2)
    {
	getDelegate().remove(startPosition, length2);
    }

    public void insertString(int startPosition, String string, AttributeSet object)
    {
	getDelegate().insertString(startPosition, string, object);
    }

    public Style getStyle(String string)
    {
	return getDelegate().getStyle(string);
    }

    public void setCharacterAttributes(int current, int length2, Style style, boolean b)
    {
	getDelegate().setCharacterAttributes(current, length2, style, b);
    }

    public void addDocumentListener(DocumentListener documentListener)
    {
	getDelegate().addDocumentListener(documentListener);
    }

    public Object getProperty(String name)
    {
	return getDelegate().getProperty(name);
    }

    public HumoStyledDocument getDelegate()
    {
	return delegate;
    }

    public void setDelegate(HumoStyledDocument delegate)
    {
	this.delegate = delegate;
    }
}
