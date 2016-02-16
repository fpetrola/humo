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
}