/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package net.ar.fpetrola.humo;

public interface ParserListener
{
    void startProductionCreation(String aName);
    void endProductionCreation(String aName, String aValue);
}
