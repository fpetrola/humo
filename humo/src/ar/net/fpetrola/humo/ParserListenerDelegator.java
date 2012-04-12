package ar.net.fpetrola.humo;

import java.util.Stack;

import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.SpinnerModel;

public class ParserListenerDelegator extends DefaultParserListener implements ParserListener
{
    protected ParserListener parserListenerDelegate;
    protected ButtonModel skipSmall;
    protected SpinnerModel spinnerModel;
    protected ProductionFrame nextVisibleFrame;
    protected Stepper stepper= new Stepper();
    protected Stack<ProductionFrame> productionFrames;
    protected ButtonModel skipAll;

    public Stack<ProductionFrame> getProductionFrames()
    {
	return productionFrames;
    }

    public void setProductionFrames(Stack<ProductionFrame> productionFrames)
    {
	this.productionFrames= productionFrames;
    }

    public ProductionFrame getNextVisibleFrame()
    {
	return nextVisibleFrame;
    }

    public void setNextVisibleFrame(ProductionFrame nextVisibleFrame)
    {
	this.nextVisibleFrame= nextVisibleFrame;
    }

    public ParserListenerDelegator(DebuggingParserListener debuggingParserListener, ButtonModel skipSmall, SpinnerModel spinnerModel, ButtonModel skipAll)
    {
	this.skipSmall= skipSmall;
	this.spinnerModel= spinnerModel;
	this.skipAll= skipAll;
    }

    public ParserListener getParserListenerDelegate()
    {
	return parserListenerDelegate;
    }

    public void setParserListenerDelegate(ParserListener parserListenerDelegate)
    {
	this.parserListenerDelegate= parserListenerDelegate;
    }

    public void startProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	parserListenerDelegate.startProductionParsing(sourcecode, first, current, last);
    }

    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.startParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void endParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.endParsingLoop(sourcecode, first, current, last, currentChar);
    }

    public void beforeParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.beforeParseProductionBody(sourcecode, first, current, last, currentChar);
    }

    public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
    {
	parserListenerDelegate.afterParseProductionBody(sourcecode, first, current, last, currentChar, name, value);
    }

    public void beforeProductionSearch(StringBuilder sourcecode, int first, int current, int last, char currentChar)
    {
	parserListenerDelegate.beforeProductionSearch(sourcecode, first, current, last, currentChar);
    }

    public void afterProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition)
    {
	parserListenerDelegate.afterProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition);
    }

    public void endProductionParsing(StringBuilder sourcecode, int first, int current, int last)
    {
	parserListenerDelegate.endProductionParsing(sourcecode, first, current, last);
    }

    public void setCurrentFrame(ProductionFrame productionFrame)
    {
	super.setCurrentFrame(productionFrame);
	parserListenerDelegate.setCurrentFrame(productionFrame);
    }

    protected boolean isVisible()
    {
	if (nextVisibleFrame == currentFrame)
	    nextVisibleFrame= null;

	return nextVisibleFrame == null;
    }
    private boolean isProductionVisible(StringBuilder value)
    {
	boolean result= false;

	if (nextVisibleFrame == null)
	{
	    result= value != null && (value.length() > (Integer) spinnerModel.getValue() || !skipSmall.isSelected()) && !skipAll.isSelected();
	    if (!result)
		nextVisibleFrame= currentFrame;
	}

	return result;
    }

    public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
    {
	if (nextVisibleFrame == currentFrame)
	    nextVisibleFrame= null;

	parserListenerDelegate.beforeProductionReplacement(sourcecode, first, current, last, currentChar, value, startPosition, endPosition, name);
    }

    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
    {
	parserListenerDelegate.afterProductionFound(sourcecode, first, current, last, currentChar, name, production);
	isProductionVisible(production);
    }

    public void beforeProductionParsing(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder stringBuilder, StringBuilder value)
    {
	parserListenerDelegate.beforeProductionParsing(sourcecode, first, current, last, currentChar, stringBuilder, value);
    }

    public void stepOver()
    {
	this.setNextVisibleFrame(productionFrames.peek());
	this.setParserListenerDelegate(new DefaultParserListener()
	{
	    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
	    {
		if (isVisible())
		    stepper.pause();
	    }
	});
	stepper.continueExecution();
    }

    public void stepInto()
    {
	this.setNextVisibleFrame(null);
	this.setParserListenerDelegate(new DefaultParserListener()
	{
	    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
	    {
		if (isVisible())
		    stepper.pause();
	    }

	    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
	    {
		if (isVisible())
		    stepper.pause();
	    }
	});
	stepper.continueExecution();
    }

    public void stepOut()
    {
	if (productionFrames.size() > 1)
	{
	    final ProductionFrame productionFrame= productionFrames.elementAt(productionFrames.size() - 2);
	    this.setNextVisibleFrame(productionFrame);

	    this.setParserListenerDelegate(new DefaultParserListener()
	    {
		public void beforeProductionReplacement(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder value, int startPosition, int endPosition, StringBuilder name)
		{
		    if (isVisible())
		    {
			stepper.pause();
		    }
		}

		public void afterParseProductionBody(StringBuilder sourcecode, int first, int current, int last, char currentChar, CharSequence name, CharSequence value)
		{
		    if (isVisible())
		    {
			stepper.pause();
		    }
		}
	    });

	    stepper.continueExecution();
	}
    }

    public void runToExpression(final int start, final int end)
    {
	final ProductionFrame breakpointFrame= productionFrames.peek();
	this.setNextVisibleFrame(null);

	this.setNextVisibleFrame(breakpointFrame);

	this.setParserListenerDelegate(new DefaultParserListener()
	{
	    public void startParsingLoop(StringBuilder sourcecode, int first, int current, int last, char currentChar)
	    {
		if (isVisible() && currentFrame == breakpointFrame)
		{
		    int length= currentFrame.getProduction().length();
		    if ((length - last) <= start && (length - last) >= end)
			stepper.pause();
		}
	    }
	});
	stepper.continueExecution();
    }

    public void runToNextReplacement()
    {
	this.setParserListenerDelegate(new DefaultParserListener()
	{
	    public void afterProductionFound(StringBuilder sourcecode, int first, int current, int last, char currentChar, StringBuilder name, StringBuilder production)
	    {
		if (isVisible())
		    stepper.pause();
	    }
	});
	stepper.continueExecution();
    }

    public void continueExecution()
    {
	this.setParserListenerDelegate(new DefaultParserListener()
	{
	});
	stepper.continueExecution();
    }

}
