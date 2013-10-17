package edu.uc.cs.idetect;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class NodeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] columnNames = {
			"Node ID",
			"Last Check-in Received",
			"Last Check-in Sent",
			"Seq High Water Mark",
			"Current State"
	};
	private ArrayList<Node> nodeList = new ArrayList<Node>();

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.nodeList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Node n = (Node) nodeList.get(row);
		switch (col) {
			case 0: // Node ID
				return n.getId();
			case 1: // Last check-in received
				return n.getLastCheckinRcv();
			case 2: // Last check-in sent
				return n.getLastCheckinSent();
			case 3: // Seq high water mark
				return n.getSeqHighWaterMark();
			case 4: // Current state
				return n.getState();
		}
		return null;
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

}
