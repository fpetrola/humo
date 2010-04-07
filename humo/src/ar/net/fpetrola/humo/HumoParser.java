/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

public class HumoParser
{
    protected Map<CharSequence, CharSequence> productions = new HashMap<CharSequence, CharSequence>();

    public int parse(StringBuilder sourcecode, int first)
    {
        int last = first, current = first;
        int last2= sourcecode.length();

        for (char currentChar; last < sourcecode.length() && (currentChar = sourcecode.charAt(last)) != '}';)
        {
            last++;
            if (last > last2)
                current= last2;
            
            if (currentChar == '{')
            {
                current = parse(sourcecode, last);
                productions.put(sourcecode.subSequence(first, last - 1), sourcecode.subSequence(last, current));
                last = first = ++current;
            }
            else
            {
                CharSequence production = productions.get(sourcecode.subSequence(current, last));
                if (production != null)
                {
                    sourcecode.replace(current, last, production.toString());
                    last= current; 
                    last2= current+production.length();
                }
            }
        }

        return last;
    }
}
