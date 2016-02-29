package ar.net.fpetrola.h;

import java.util.Arrays;

public class Helper
{
    public static void insertIntoStringBufferSafely(StringBuffer textBuffer, int start, String string)
    {
	if (textBuffer.length() < start)
	    appendCharacters(start - textBuffer.length(), ' ', textBuffer);

	textBuffer.insert(start, string);
    }

    private static void appendCharacters(final int numberOfChars, char aChar, StringBuffer textBuffer)
    {
	final char[] spaceArray= new char[numberOfChars];
	Arrays.fill(spaceArray, aChar);
	textBuffer.append(spaceArray);
    }

    public static String escapeHTML(String text1)
    {
	return text1.replace("<", "&lt;").replace("<", "&gt;").replaceAll("\n", "<br>");
    }

    public static void verifySpans(TextRange firstRange, StringBuffer textBuffer)
    {
//        TextRange currentRange= firstRange;
//        int spans= 0;
//    
//        while (currentRange != null)
//        {
//            if (currentRange instanceof SpanTextRange)
//            {
//        	SpanTextRange spanTextRange= (SpanTextRange) currentRange;
//        	String actualString= textBuffer.substring(currentRange.mapPosition(currentRange.getStart()), currentRange.mapPosition(currentRange.getStart() + spanTextRange.overflow));
//        	if (!actualString.equals(currentRange.text))
//        	    throw new RuntimeException("bug!");
//    
//        	if (currentRange instanceof OpenSpanTextRange)
//        	    spans++;
//    
//        	if (currentRange instanceof CloseSpanTextRange)
//        	    spans--;
//            }
//    
//            currentRange= currentRange.getNextRange();
//        }
//    
//        if (spans != 0)
//            throw new RuntimeException("bug: open/close tags");
    }
}