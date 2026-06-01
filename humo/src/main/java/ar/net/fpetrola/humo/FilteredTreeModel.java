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
        
        // Solo filtrar si es el root (primer nivel)
        if (node == root) {
            int count = 0;
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                if (shouldShowFirstLevelNode(child)) {
                    count++;
                }
            }
            return count;
        }
        
        return super.getChildCount(parent);
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (!hideFiltered) {
            return super.getChild(parent, index);
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        
        // Solo filtrar si es el root
        if (node == root) {
            int visibleIndex = 0;
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                if (shouldShowFirstLevelNode(child)) {
                    if (visibleIndex == index) {
                        return child;
                    }
                    visibleIndex++;
                }
            }
            return null;
        }
        
        return super.getChild(parent, index);
    }

    private boolean shouldShowFirstLevelNode(DefaultMutableTreeNode node) {
        if (filterPrefix == null || !hideFiltered) {
            return true;
        }
        
        String nodeText = node.getUserObject().toString().trim();
        for (String prefix : filterPrefix) {
            if (nodeText.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
}
