package ar.net.fpetrola.humo;

public class Spinner
{
    private int stepSize, value;

    public int getStepSize()
    {
	return stepSize;
    }
    public void setStepSize(int stepSize)
    {
	this.stepSize= stepSize;
    }
    public Spinner(int value, int minimum, int maximum, int stepSize)
    {
	super();
	this.stepSize= stepSize;
	this.value= value;
	this.minimum= minimum;
	this.maximum= maximum;
    }
    public Comparable getMinimum()
    {
	return minimum;
    }
    public void setMinimum(Comparable minimum)
    {
	this.minimum= minimum;
    }
    public Comparable getMaximum()
    {
	return maximum;
    }
    public void setMaximum(Comparable maximum)
    {
	this.maximum= maximum;
    }

    private Comparable minimum, maximum;

    public void up()
    {
    }
    public void down()
    {
    }
    public int getValue()
    {
	return value;
    }
    public void setValue(int value)
    {
	this.value= value;
    }

}
