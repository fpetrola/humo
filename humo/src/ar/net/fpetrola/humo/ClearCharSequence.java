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

    public ClearCharSequence(CharSequence aCharSequence)
    {
        charSequence = new StringBuilder(aCharSequence).toString();
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
        return aText.replaceAll("[\\x0D \\x0A \\x20  \\x09]", "");
    }
}
