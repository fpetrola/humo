/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.TreeMap;

@SuppressWarnings("serial")
public class LoggingMap extends TreeMap<CharSequence, CharSequence>
{
    private ParserListener parserListener;

    public LoggingMap(ParserListener parserListener)
    {
        this.parserListener = parserListener;
    }

    public synchronized CharSequence get(Object key)
    {
        CharSequence o = super.get(new ClearCharSequence((CharSequence) key));
        log("uso: " +  key + " = " + o + "");
        return o;
    }

    public synchronized CharSequence remove(Object key)
    {
        CharSequence o = super.remove(key);
        log((String) key + "{}");
        return o;
    }
    
    public synchronized CharSequence put(CharSequence key, CharSequence value)
    {
        parserListener.endProductionCreation(key, value);
        
        log((String) key);
        log("{");
        log("\t" + (String) value);
        log("}\n");
        CharSequence o = super.put(new ClearCharSequence(key), new ClearCharSequence(value));
        return o;
    }

    public void log(String t)
    {
//        StringBuffer sb = new StringBuffer();
        //        for (int primero = 0; primero < parser.getDepth(); primero++)
        //            sb.append("\t");

//        System.out.println(sb.toString() + t);
    }

}