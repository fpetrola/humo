/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class FilteredTreeModel extends DefaultTreeModel {
    private String[] filterPrefix;
    private boolean hideFiltered;

    public FilteredTreeModel(DefaultMutableTreeNode root) {
        super(root);
        this.filterPrefix = null;
        this.hideFiltered = false;
    }

    public void setFilter(boolean hideFiltered, String... filterPrefix) {
        this.filterPrefix = filterPrefix;
        this.hideFiltered = hideFiltered;
        reload();
    }

    @Override
    public int getChildCount(Object parent) {
        if (!hideFiltered) {
            return super.getChildCount(parent);
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        int count = 0;
        
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (shouldShowNode(child)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (!hideFiltered) {
            return super.getChild(parent, index);
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        int visibleIndex = 0;
        
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (shouldShowNode(child)) {
                if (visibleIndex == index) {
                    return child;
                }
                visibleIndex++;
            }
        }
        return null;
    }

    private boolean shouldShowNode(DefaultMutableTreeNode node) {
        if (filterPrefix == null || !hideFiltered) {
            return true;
        }
        
        String nodeText = node.getUserObject().toString().trim();
        for (String filterPrefix : filterPrefix) {
            if (nodeText.startsWith(filterPrefix)) {
                return false;
            }
        }
        return true;
    }
}
