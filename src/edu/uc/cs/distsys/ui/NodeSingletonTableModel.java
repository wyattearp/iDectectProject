package edu.uc.cs.distsys.ui;

import javax.swing.table.AbstractTableModel;

import edu.uc.cs.distsys.Node;

public class NodeSingletonTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {
			"Node Item",
			"Item Details"
	};
	private Node node;
	private boolean renderConsensus = false;
	private boolean consensusPossible = false;
	
	public void setNode(Node n) {
		this.node = n;
	}
	
	public void setConsensusPossible(boolean possible) {
		this.renderConsensus = true;
		this.consensusPossible = possible;
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
		// Statically set, we're only ever reporting the following:
		// Node ID
		// Node State
		// Node Leader ID
		// Node Group ID
		// Node Group Cookie
		// Node Number of Processes Operating
		if (this.renderConsensus)
		// Node Consensus Possible
			return 7;
		else
			return 6;
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		if (this.node == null) {
			return null;
		}
		
		if (col == 0) {
			switch (row) {
				case 0:
					return "Node ID";
				case 1:
					return "Node State";
				case 2:
					return "Node Leader ID";
				case 3:
					return "Node Group ID";
				case 4:
					return "Node Group Cookie";
				case 5:
					return "Node Number of Processes Operating";
				case 6:
					return "Node Consensus Possible";
			}
		}
		if (col == 1) {
			switch (row) {
				case 0: // Node ID
					return this.node.getId();
				case 1: // Node State
					return this.node.getState();
				case 2: // Node Leader ID
					if (this.node.getLeaderId() == this.node.getId()) {
						return "Currently the leader";
					} else {
						return this.node.getLeaderId();
					}
				case 3: // Node Group ID
					return this.node.getGroupId();
				case 4: // Node Group Cookie
					return this.node.getGroupCookie().toString();
				case 5: // Leader ID
					return this.node.getNumProcOperating();
				case 6: // Node Consensus Possible
					return this.consensusPossible;
			}
		}
		
		// someone asked for something dumb
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
