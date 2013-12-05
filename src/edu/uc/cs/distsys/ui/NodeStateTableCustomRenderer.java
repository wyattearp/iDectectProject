package edu.uc.cs.distsys.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.uc.cs.distsys.NodeState;


public class NodeStateTableCustomRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 5465218306482162759L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(table.getValueAt(row, column).equals(NodeState.UNKNOWN)) {
            cellComponent.setBackground(Color.WHITE);
            cellComponent.setForeground(Color.BLACK);
        } else if(table.getValueAt(row, column).equals(NodeState.ONLINE)) {
            cellComponent.setBackground(Color.GREEN);
            cellComponent.setForeground(Color.BLACK);
        } else if(table.getValueAt(row, column).equals(NodeState.SUSPECT)) {
        	cellComponent.setBackground(Color.YELLOW);
        	cellComponent.setForeground(Color.BLACK);
        } else if(table.getValueAt(row, column).equals(NodeState.OFFLINE)) {
            cellComponent.setBackground(Color.GRAY);
            cellComponent.setForeground(Color.BLACK);
        } else if(table.getValueAt(row, column).equals(NodeState.INCOHERENT)) {
            cellComponent.setBackground(Color.RED);
            cellComponent.setForeground(Color.WHITE);
        }

        return cellComponent;
    }

}
