/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

public class ClearCharSequence implements CharSequence, Comparable<CharSequence>
{
    private String charSequence;
    protected static String regex= "[\\x0D\\x0A\\x20\\x09]";

    public ClearCharSequence(CharSequence aCharSequence)
    {
	charSequence= new StringBuilder(aCharSequence).toString();
    }

    public char charAt(int index)
    {
	return charSequence.charAt(index);
    }

    public int length()
    {
	return charSequence.length();
    }

    public CharSequence subSequence(int start, int end)
    {
	return charSequence.subSequence(start, end);
    }

    public String toString()
    {
	return charSequence;
    }

    public int compareTo(CharSequence o)
    {
	return clearText(o.toString()).compareTo(clearText(toString()));
    }

    public static String clearText(String aText)
    {
	return aText.replaceAll(regex, "");
    }

    public static int[] findEdges(StringBuilder sourcecode, int current, int last)
    {
	//	return new int[] { current, last };
	int[] result= new int[] { current, last };
	boolean firstFound= false;
	boolean lastFound= false;

	for (int i= 0; i < sourcecode.length() && result[0] < result[1] && !(firstFound && lastFound); i++)
	{
	    if (!firstFound && (sourcecode.charAt(current + i) + "").matches(regex))
	    {
		result[0]++;
	    }
	    else
		firstFound= true;

	    if (!lastFound && (sourcecode.charAt(last - 1 - i) + "").matches(regex))
	    {
		result[1]--;
	    }
	    else
		lastFound= true;
	}
	return result;
    }
}
