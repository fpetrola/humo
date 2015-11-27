package ar.net.fpetrola.humo.gui;

import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.List;

public class DefaultTreeNode implements TreeNode
{

    @Override
    public <T extends EventListener> void addListener(Class<T> arg0, T arg1)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public <T extends EventListener> T getListener(Class<T> arg0)
    {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T extends EventListener> boolean hasListener(Class<T> arg0)
    {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public <T extends EventListener> void removeListener(Class<T> arg0, T arg1)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setChildren(List<TreeNode> children)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public List<TreeNode> getChildren()
    {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getName()
    {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setName(String name)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setLastChild(boolean lastChild)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean isLastChild()
    {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setRoot(boolean root)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean isRoot()
    {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setOpen(boolean open)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean isOpen()
    {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setFolder(boolean folder)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean isFolder()
    {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void addPropertyChangeListener(Object owner, PropertyChangeListener propertyChangeListener)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void remove(TreeNode peek)
    {
	// TODO Auto-generated method stub
	
    }

}
