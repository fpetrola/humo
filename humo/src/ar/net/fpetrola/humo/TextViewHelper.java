package ar.net.fpetrola.humo;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class TextViewHelper
{

    public static final String CURSOR_STYLE= "Cursor";
    public static final String PRODUCTION_FOUND_STYLE= "production-found";
    public static final String DEFAULT_STYLE= "default";
    public static final String FETCH_STYLE= "fetch";
    public static final String CURLY_STYLE= "curly";
    public static final String PRODUCTION_BEFORE_REPLACEMENT_STYLE= "production-before-replacement";

    public static StyledDocument createStyleDocument()
    {
        StyleContext styleContext= new StyleContext();
        Color grey= new Color(0.95f, 0.95f, 0.95f);
    
        Color orange= new Color(Integer.parseInt("008EFF", 16));
        createStyle(styleContext, DEFAULT_STYLE, Color.black, "monospaced", Color.white, 11, null);
        createStyle(styleContext, CURSOR_STYLE, new Color(0.8f, 0, 0), "monospaced", grey, 11, null);
        createStyle(styleContext, CURLY_STYLE, Color.BLACK, "monospaced", Color.WHITE, 11, true);
        createStyle(styleContext, FETCH_STYLE, new Color(0, 0.5f, 0), "monospaced", grey, 11, null);
        createStyle(styleContext, PRODUCTION_FOUND_STYLE, Color.BLUE, "monospaced", grey, 11, true);
        createStyle(styleContext, PRODUCTION_BEFORE_REPLACEMENT_STYLE, orange, "monospaced", grey, 11, true);
        return new DefaultStyledDocument(styleContext);
    }

    public static void createStyle(StyleContext sc, String aName, Color aForegroundColor, String aFontFamily, Color aBackgroundColor, int aFontSize, Boolean isBold)
    {
        Style cursorStyle= sc.addStyle(aName, null);
        StyleConstants.setForeground(cursorStyle, aForegroundColor);
        StyleConstants.setFontFamily(cursorStyle, aFontFamily);
        StyleConstants.setBackground(cursorStyle, aBackgroundColor);
        StyleConstants.setFontSize(cursorStyle, aFontSize);
        if (isBold != null)
            StyleConstants.setBold(cursorStyle, isBold);
    }

    public static StyledDocument createAndSetupDocument(StringBuilder sourceCode)
    {
        try
        {
            StyledDocument styleDocument= createStyleDocument();
    
            Style curly= styleDocument.getStyle(CURLY_STYLE);
            Style defaultStyle= styleDocument.getStyle(DEFAULT_STYLE);
            styleDocument.remove(0, styleDocument.getLength());
            styleDocument.insertString(0, sourceCode.toString(), null);
            for (int i= 0; i < sourceCode.length(); i++)
            {
        	if (sourceCode.charAt(i) == '{' || sourceCode.charAt(i) == '}')
        	    styleDocument.setCharacterAttributes(i, 1, curly, false);
        	else
        	    styleDocument.setCharacterAttributes(i, 1, defaultStyle, false);
            }
    
            return styleDocument;
        }
        catch (BadLocationException e)
        {
            throw new RuntimeException(e);
        }
    }

}
