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
    void startProductionParsing(StringBuilder sourcecode, int first, int current, int last);
    void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar);
    void endParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar);
    void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar);
    void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value);
    void beforeProductionSearch(StringBuilder sourcecode, int first, int current, int last, char currentChar);
    void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value);
    void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name);
    void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition);
    void endProductionParsing(StringBuilder sourcecode, int first, int current, int last);
    void setCurrentFrame(ProductionFrame productionFrame);
    void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder stringBuilder, StringBuilder value);
}
