package org.fpetrola.humo.service.serverside;

import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;

import com.dragome.guia.components.interfaces.VisualTextField;
import com.dragome.model.interfaces.ValueChangeEvent;
import com.dragome.model.interfaces.ValueChangeHandler;
import com.dragome.render.canvas.CanvasImpl;
import com.dragome.render.canvas.interfaces.Canvas;
import com.dragome.render.html.renderers.Mergeable;
import com.dragome.render.interfaces.ComponentRenderer;
import com.dragome.render.serverside.swing.SwingUtils;

import ar.net.fpetrola.humo.HumoStyledDocument;
import ar.net.fpetrola.humo.HumoTextDocument;
import ar.net.fpetrola.humo.VisualTextPaneImpl;

public class SwingTextPaneRenderer implements ComponentRenderer<Object, VisualTextPaneImpl<HumoTextDocument>>
{
    public Canvas<Object> render(final VisualTextPaneImpl<HumoTextDocument> visualTextField)
    {
	CanvasImpl<Object> canvasImpl= new CanvasImpl<Object>();

	canvasImpl.setContent(new Mergeable<JTextPane>()
	{
	    public void mergeWith(JTextPane jTextPane)
	    {
		SwingUtils.addChangeListener(jTextPane, new ChangeListener()
		{
		    public void stateChanged(ChangeEvent e)
		    {
			//			  String text= event.getValue().getText(0, event.getValue().getLength());
			//			if (!jTextPane.getText().equals(text))
			//			    jTextPane.setText(text);
			//			visualTextField.setValue(new HumoTextDocument(jTextPane.getText()));
		    }
		});

		jTextPane.setText(visualTextField.getValue() + "");

		visualTextField.addValueChangeHandler(new ValueChangeHandler<HumoTextDocument>()
		{
		    public void onValueChange(ValueChangeEvent<HumoTextDocument> event)
		    {
			try
			{
			    DefaultStyledDocument document= new DefaultStyledDocument();
			    jTextPane.setDocument(document);
			    
			    event.getValue().setDelegate(new HumoStyledDocument()
			    {
				
				public void setCharacterAttributes(int current, int length2, Style style, boolean b)
				{
				    document.setCharacterAttributes(current, length2, style, b);
				}

				public void setCaretPosition(int caretPosition)
				{
				    jTextPane.setCaretPosition(caretPosition);
				}

				public void remove(int startPosition, int length2)
				{
				    try
				    {
					document.remove(startPosition, length2);
				    }
				    catch (BadLocationException e)
				    {
					e.printStackTrace();
				    }
				}

				public void putProperty(String name, Object value)
				{
				    document.putProperty(name, value);
				}

				public void insertString(int startPosition, String string, AttributeSet object)
				{
				    try
				    {
					document.insertString(startPosition, string, object);
				    }
				    catch (BadLocationException e)
				    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
				}

				public Style getStyle(String string)
				{
				    return document.getStyle(string);
				}

				public int getSelectionStart()
				{
				    return jTextPane.getSelectionStart();
				}

				public int getSelectionEnd()
				{
				    return jTextPane.getSelectionEnd();
				}

				public Object getProperty(String name)
				{
				    return document.getProperty(name);
				}

				public int getLength()
				{
				    return document.getLength();
				}

				public void addDocumentListener(DocumentListener documentListener)
				{
				    document.addDocumentListener(documentListener);
				}
			    });
			    //			    if (!jTextPane.getText().equals(text))
			    //				jTextPane.setText(text);
			}
			catch (Exception e)
			{
			    throw new RuntimeException(e);
			}
		    }
		});

	    }
	});
	return canvasImpl;
    }
}