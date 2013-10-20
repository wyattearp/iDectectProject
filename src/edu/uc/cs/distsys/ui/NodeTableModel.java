package edu.uc.cs.distsys.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import edu.uc.cs.distsys.Node;

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
	private static final String timeStampFormat = "HH:mm:ss.SSS";
	
	public void addItem(Node n) {
		// TODO: Since we only ever add nodes once, this should probably be fine
		nodeList.add(n);
	}

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
		Date d;
		SimpleDateFormat df2;
		switch (col) {
			case 0: // Node ID
				return n.getId();
			case 1: // Last check-in received
				d = new Date(n.getLastCheckinRcv());
				df2 = new SimpleDateFormat(timeStampFormat);
				return df2.format(d);
			case 2: // Last check-in sent
				d = new Date(n.getLastCheckinSent());
				df2 = new SimpleDateFormat(timeStampFormat);
				return df2.format(d);
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
