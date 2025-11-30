/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

public class ProductionTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public ProductionTreeCellRenderer() {
        // Configurar iconos
        try {
            Icon customOpenIcon = new ImageIcon(HumoTester.class.getResource("/images/scalarvar.gif"));
            Icon customClosedIcon = new ImageIcon(HumoTester.class.getResource("/images/genericvariable.gif"));
            setOpenIcon(customOpenIcon);
            setClosedIcon(customClosedIcon);
        } catch (Exception e) {
            // Usar iconos por defecto si no se encuentran
        }
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof String) {
                String text = (String) userObject;
                
                // Determinar si es nodo raíz, nivel 1 (nombre de producción) o nivel 2 (contenido)
                int level = getNodeLevel(node);
                
                String htmlText = formatNodeText(text, level, leaf);
                setText(htmlText);
            }
        }
        
        return this;
    }
    
    /**
     * Obtiene el nivel del nodo en el árbol
     * Nivel 0: Root
     * Nivel 1: Nombre de producciones
     * Nivel 2+: Contenido de producciones
     */
    private int getNodeLevel(DefaultMutableTreeNode node) {
        int level = 0;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        while (parent != null) {
            level++;
            parent = (DefaultMutableTreeNode) parent.getParent();
        }
        return level;
    }
    
    /**
     * Formatea el texto del nodo con HTML para aplicar estilos
     */
    private String formatNodeText(String text, int level, boolean leaf) {
        if (level == 0) {
            // Root node - sin cambios
            return text;
        } else if (level == 1) {
            // Nombre de producción - en negritas azul
            return "<html><b><font color='#0066CC'>" + escapeHtml(text) + "</font></b></html>";
        } else {
            // Contenido de producción - aplicar formato anidado
            return "<html>" + formatProduction(text, 0) + "</html>";
        }
    }
    
    /**
     * Formatea el contenido de la producción con colores y negritas para llaves
     */
    private String formatProduction(String text, int depth) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) {
            char c = text.charAt(i);
            
            if (c == '{') {
                // Llave abierta - en negritas rojo
                result.append("<b><font color='#CC0000'>{</font></b>");
                i++;
            } else if (c == '}') {
                // Llave cerrada - en negritas rojo
                result.append("<b><font color='#CC0000'>}</font></b>");
                i++;
            } else if (c == '"') {
                // Procesar string entre comillas
                int endQuote = text.indexOf('"', i + 1);
                if (endQuote != -1) {
                    String stringContent = text.substring(i, endQuote + 1);
                    result.append("<font color='#009900'>").append(escapeHtml(stringContent)).append("</font>");
                    i = endQuote + 1;
                } else {
                    result.append(escapeHtml(String.valueOf(c)));
                    i++;
                }
            } else if (c == '|' || c == '(' || c == ')') {
                // Operadores especiales - en verde
                result.append("<font color='#008800'>").append(escapeHtml(String.valueOf(c))).append("</font>");
                i++;
            } else if (Character.isWhitespace(c)) {
                result.append(c);
                i++;
            } else {
                // Texto normal - en gris oscuro
                int endText = i;
                while (endText < text.length() && !isSpecialChar(text.charAt(endText))) {
                    endText++;
                }
                String normalText = text.substring(i, endText);
                result.append("<font color='#333333'>").append(escapeHtml(normalText)).append("</font>");
                i = endText;
            }
        }
        
        return result.toString();
    }
    
    private boolean isSpecialChar(char c) {
        return c == '{' || c == '}' || c == '"' || c == '|' || c == '(' || c == ')';
    }
    
    /**
     * Escapa caracteres HTML especiales
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
