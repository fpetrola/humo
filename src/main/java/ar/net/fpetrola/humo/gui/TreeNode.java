package ar.net.fpetrola.humo.gui;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.dragome.model.interfaces.EventProducer;

public interface TreeNode extends EventProducer {
	public void setChildren(List<TreeNode> children);

	public List<TreeNode> getChildren();

	public String getName();

	public abstract void setName(String name);

	public abstract void setLastChild(boolean lastChild);

	public abstract boolean isLastChild();

	public abstract void setRoot(boolean root);

	public abstract boolean isRoot();

	public abstract void setOpen(boolean open);

	public abstract boolean isOpen();

	public abstract void setFolder(boolean folder);

	public abstract boolean isFolder();

	public void addPropertyChangeListener(Object owner, PropertyChangeListener propertyChangeListener);

	void remove(TreeNode peek);
}
