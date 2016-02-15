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
}