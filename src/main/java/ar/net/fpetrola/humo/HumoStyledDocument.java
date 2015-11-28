package ar.net.fpetrola.humo;

import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Style;

public interface HumoStyledDocument
{
    int getSelectionStart();

    int getSelectionEnd();

    void setCaretPosition(int caretPosition);

    int getLength();

    void putProperty(String name, Object value);

    void remove(int startPosition, int length2);

    void insertString(int startPosition, String string, AttributeSet object);

    Style getStyle(String string);

    void setCharacterAttributes(int current, int length2, Style style, boolean b);

    void addDocumentListener(DocumentListener documentListener);

    Object getProperty(String name);
}