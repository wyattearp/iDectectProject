package edu.uc.cs.distsys.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class NodeConcensusTableCustomRednerer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 358796672819507353L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        try {
	        if(table.getValueAt(row, column).equals(false)) {
	            cellComponent.setBackground(Color.RED);
	            cellComponent.setForeground(Color.BLACK);
	        } else if(table.getValueAt(row, column).equals(true)) {
	            cellComponent.setBackground(Color.GREEN);
	            cellComponent.setForeground(Color.BLACK);
	        } else {
	        	// always reset to default colors
	        	cellComponent.setBackground(Color.LIGHT_GRAY);
	            cellComponent.setForeground(Color.BLACK);
	        }
        } catch (NullPointerException np) {
        	// not sure why this is null, but just return the basic thing.
        }

        return cellComponent;
    }

}
