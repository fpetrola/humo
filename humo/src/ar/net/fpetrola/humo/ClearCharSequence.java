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

    public boolean equals(Object obj)
    {
        return LoggingMap.clearText(obj.toString()).equals(LoggingMap.clearText(toString()));
    }

    public int compareTo(CharSequence o)
    {
        return LoggingMap.clearText(o.toString()).compareTo(LoggingMap.clearText(toString()));
    }
}
