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
    protected Map<String, String> productions = new HashMap<String, String>();

    public int parse(StringBuilder sourcecode, int first)
    {
        int last = first, current = first;

        for (char currentChar; last < sourcecode.length() && (currentChar = sourcecode.charAt(last)) != '}';)
        {
            last++;
            if (currentChar == '{')
            {
                current = parse(sourcecode, last);
                productions.put(sourcecode.substring(first, last - 1), sourcecode.substring(last, current));
                last = first = ++current;
            }
            else
            {
                String production = productions.get(sourcecode.substring(current, last));
                if (production != null)
                {
                    StringBuilder value = new StringBuilder(production);
                    parse(value, 0);
                    sourcecode.replace(current, last, value.toString());
                    last = current += value.length();
                }
            }
        }

        return last;
    }
}
