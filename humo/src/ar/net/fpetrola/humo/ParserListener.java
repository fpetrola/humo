/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

public interface ParserListener
{
    void startProductionCreation(CharSequence aName);
    void endProductionCreation(CharSequence aName, CharSequence aValue);
    void getProduction(CharSequence key, CharSequence value);
}
