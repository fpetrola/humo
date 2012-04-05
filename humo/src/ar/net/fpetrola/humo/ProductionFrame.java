package ar.net.fpetrola.humo;

import javax.swing.text.StyledDocument;

public class ProductionFrame
{
    protected StringBuilder production;
    protected int first;
    protected int current;
    protected int last;
    protected String name;
    protected StyledDocument document;

    public StyledDocument getDocument()
    {
        return document;
    }

    public void setDocument(StyledDocument styleDocument)
    {
        this.document= styleDocument;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name= name;
    }

    public ProductionFrame(String aName, StringBuilder production, StyledDocument styleDocument)
    {
	this.name= aName;
	this.production= production;
	this.document= styleDocument;
    }

    public int getCurrent()
    {
	return current;
    }

    public int getFirst()
    {
	return first;
    }

    public int getLast()
    {
	return last;
    }

    public StringBuilder getProduction()
    {
	return production;
    }

    public void setCurrent(int current)
    {
	this.current= current;
    }
    public void setFirst(int first)
    {
	this.first= first;
    }

    public void setLast(int last)
    {
	this.last= last;
    }

    public void setProduction(StringBuilder production)
    {
	this.production= production;
    }
}