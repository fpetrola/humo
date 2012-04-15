package ar.net.fpetrola.humo;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
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

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name= name;
    }

    public ProductionFrame(String aName, StringBuilder production)
    {
	this.name= aName;
	this.production= production;
	document= TextViewHelper.createAndSetupDocument(production);
	document.addDocumentListener(new DocumentListener()
	{
	    public void removeUpdate(DocumentEvent e)
	    {
		if (document.getProperty("auto") == null)
		{
		    try
		    {
			ProductionFrame.this.production.replace(e.getOffset(), e.getOffset() + e.getLength(), "");
		    }
		    catch (Exception e1)
		    {
			e1.printStackTrace();
		    }
		}
	    }

	    public void insertUpdate(DocumentEvent e)
	    {
		if (document.getProperty("auto") == null)
		{
		    try
		    {
			ProductionFrame.this.production.replace(0, ProductionFrame.this.production.length(), e.getDocument().getText(0, e.getDocument().getLength()));
		    }
		    catch (Exception e1)
		    {
			e1.printStackTrace();
		    }
		}
	    }

	    public void changedUpdate(DocumentEvent e)
	    {
	    }
	});
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

    public String toString()
    {
	return name + "= " + production.toString().substring(0, 100) + "....";
    }
}