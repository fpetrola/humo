/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.Stack;

public class ParserListenerMultiplexer implements ParserListener
{
    private ParserListener[] parserListeners;
    protected Stack<ProductionFrame> productionFrames= new Stack<ProductionFrame>();

    public Stack<ProductionFrame> getProductionFrames()
    {
	return productionFrames;
    }

    public void setProductionFrames(Stack<ProductionFrame> productionFrames)
    {
	this.productionFrames= productionFrames;
    }

    public void init(String filename, StringBuilder sourcecode, boolean createComponents)
    {
	ProductionFrame rootFrame= new ProductionFrame(filename, sourcecode);
	productionFrames.push(rootFrame);
    }

    public ParserListenerMultiplexer(ParserListener... parserListener)
    {
	this.parserListeners= parserListener;
    }

    private ProductionFrame getCurrentFrame()
    {
	return productionFrames.peek();
    }

    private int updateFramePositions(StringBuilder sourcecode, int first, int current, int last)
    {
	ProductionFrame currentProductionFrame= getCurrentFrame();

	int lastCurrent= currentProductionFrame.getCurrent();
	currentProductionFrame.setFirst(first);
	currentProductionFrame.setCurrent(current);
	currentProductionFrame.setLast(last);
	return lastCurrent;
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterParseProductionBody(sourcecode, first, current, last, currentChar, name, value);
    }

    private void updateListeners(StringBuilder sourcecode, int first, int current, int last)
    {
	updateFramePositions(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.setCurrentFrame(productionFrames.peek());
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder value)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterProductionFound(sourcecode, first, current, last, currentChar, name, value);

	ProductionFrame frame= new ProductionFrame(name.toString(), value, HumoTester.createAndSetupDocument(value));
	productionFrames.push(frame);
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.afterProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition);
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	productionFrames.pop();
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition, name);
    }

    public void beforeProductionSearch(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeProductionSearch(sourcecode, first, current, last, currentChar);
    }

    public void endParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.endParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.endProductionParsing(sourcecode, first, current, last);
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	updateListeners(sourcecode, first, current, last);
	for (ParserListener parserListener : parserListeners)
	    parserListener.startParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	updateListeners(sourcecode, first, 0, 0);
	for (ParserListener parserListener : parserListeners)
	    parserListener.startProductionParsing(sourcecode, first, current, last);
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
    }

    public void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder stringBuilder, StringBuilder value)
    {
	updateListeners(sourcecode, first, 0, 0);
	for (ParserListener parserListener : parserListeners)
	    parserListener.beforeProductionParsing(sourcecode, first, current, last, currentChar, stringBuilder, value);
    }
}
