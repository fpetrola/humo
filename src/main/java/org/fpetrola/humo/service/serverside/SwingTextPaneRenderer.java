package org.fpetrola.humo.service.serverside;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.dragome.model.interfaces.ValueChangeEvent;
import com.dragome.model.interfaces.ValueChangeHandler;
import com.dragome.render.canvas.CanvasImpl;
import com.dragome.render.canvas.interfaces.Canvas;
import com.dragome.render.html.renderers.Mergeable;
import com.dragome.render.interfaces.ComponentRenderer;
import com.dragome.render.serverside.swing.SwingUtils;

import ar.net.fpetrola.humo.HumoTextDocument;
import ar.net.fpetrola.humo.HumoTextDocumentImpl;
import ar.net.fpetrola.humo.StyledSpan;
import ar.net.fpetrola.humo.TextViewHelper;
import ar.net.fpetrola.humo.VisualTextPaneImpl;
import ar.net.fpetrola.humo.delegates.DelegateMultiplexer;

public class SwingTextPaneRenderer implements ComponentRenderer<Object, VisualTextPaneImpl<HumoTextDocumentImpl>>
{
    private Map<HumoTextDocument, StyledDocument> documents= new HashMap<>();

    public Canvas<Object> render(final VisualTextPaneImpl<HumoTextDocumentImpl> visualTextField)
    {
	StyleContext styleContext= createStyleDocument();

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

		visualTextField.addValueChangeHandler(new ValueChangeHandler<HumoTextDocumentImpl>()
		{

		    public void onValueChange(ValueChangeEvent<HumoTextDocumentImpl> event)
		    {
			try
			{
			    HumoTextDocument humoTextDocument= event.getValue();

			    SwingUtilities.invokeLater(new Runnable()
			    {
				public void run()
				{
				    StyledDocument foundStyledDocument= documents.get(humoTextDocument);
				    
				    if (foundStyledDocument == null)
				    {
					foundStyledDocument= createStyledDocumentFromHumoTextDocument(humoTextDocument);
					DelegateMultiplexer<HumoTextDocument> delegateMultiplexer= (DelegateMultiplexer<HumoTextDocument>) humoTextDocument;
					HumoTextDocumentListener humoTextDocumentListener= new HumoTextDocumentListener(foundStyledDocument, jTextPane);
					delegateMultiplexer.addDelegate(humoTextDocumentListener);
					documents.put(humoTextDocument, foundStyledDocument);
				    }
				    jTextPane.setDocument(foundStyledDocument);
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

		    private StyledDocument createStyledDocumentFromHumoTextDocument(HumoTextDocument humoTextDocument)
		    {
			try
			{
			    DefaultStyledDocument result= new DefaultStyledDocument(createStyleDocument());
			    result.insertString(0, humoTextDocument.getText(), null);

			    for (StyledSpan styledSpan : humoTextDocument.getSpans())
			    {
				int length= styledSpan.getEnd() - styledSpan.getStart();
				result.setCharacterAttributes(styledSpan.getStart(), length, getStyle(styledSpan.getStyle()), false);
			    }

			    return result;
			}
			catch (BadLocationException e)
			{
			    throw new RuntimeException(e);
			}
		    }

		    private AttributeSet getStyle(String style)
		    {
			return styleContext.getStyle(style);
		    }
		});

	    }
	});
	return canvasImpl;
    }

    public StyleContext createStyleDocument()
    {
	StyleContext styleContext= new StyleContext();
	Color grey= new Color(0.95f, 0.95f, 0.95f);

	Color orange= new Color(Integer.parseInt("008EFF", 16));
	createStyle(styleContext, TextViewHelper.DEFAULT_STYLE, Color.black, "monospaced", Color.white, 11, null);
	createStyle(styleContext, TextViewHelper.CURSOR_STYLE, new Color(0.8f, 0, 0), "monospaced", grey, 11, null);
	createStyle(styleContext, TextViewHelper.CURLY_STYLE, Color.BLACK, "monospaced", Color.WHITE, 11, true);
	createStyle(styleContext, TextViewHelper.FETCH_STYLE, new Color(0, 0.5f, 0), "monospaced", grey, 11, null);
	createStyle(styleContext, TextViewHelper.PRODUCTION_FOUND_STYLE, Color.BLUE, "monospaced", grey, 11, true);
	createStyle(styleContext, TextViewHelper.PRODUCTION_BEFORE_REPLACEMENT_STYLE, orange, "monospaced", grey, 11, true);
	return styleContext;
    }

    public void createStyle(StyleContext sc, String aName, Color aForegroundColor, String aFontFamily, Color aBackgroundColor, int aFontSize, Boolean isBold)
    {
	Style cursorStyle= sc.addStyle(aName, null);
	StyleConstants.setForeground(cursorStyle, aForegroundColor);
	StyleConstants.setFontFamily(cursorStyle, aFontFamily);
	StyleConstants.setBackground(cursorStyle, aBackgroundColor);
	StyleConstants.setFontSize(cursorStyle, aFontSize);
	if (isBold != null)
	    StyleConstants.setBold(cursorStyle, isBold);
    }
}