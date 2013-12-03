package edu.uc.cs.distsys.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class NodeStatusView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTable viewTable;
	private JTable propertyTable;
	private JScrollPane viewScrollPane;
	private JScrollPane propScrollPane;
	private NodeTableModel nodeTable;
	private NodeSingletonTableModel nodePropertiesTable;
	private static final int tableWidth = 800;
	private static final int tableHeight = 100;

	public NodeTableModel getNodeTable() {
		return nodeTable;
	}

	public void setNodeTable(NodeTableModel nodeTable) {
		this.nodeTable = nodeTable;
	}
	
	public NodeSingletonTableModel getNodePropertiesTable() {
		return this.nodePropertiesTable;
	}

	public NodeStatusView() {
		super(new GridLayout(2,1));
		
		nodePropertiesTable = new NodeSingletonTableModel();
		nodeTable = new NodeTableModel();
		
		viewTable = new JTable(this.nodeTable);
		viewTable.setPreferredScrollableViewportSize(new Dimension(tableWidth,tableHeight));
		viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		viewTable.setFillsViewportHeight(true);
		
		propertyTable = new JTable(this.nodePropertiesTable);
		propertyTable.setPreferredScrollableViewportSize(new Dimension(tableWidth,tableHeight));
		propertyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		propertyTable.setFillsViewportHeight(true);
		
		// attach the table to the UI pane
		viewScrollPane = new JScrollPane(viewTable);
		propScrollPane = new JScrollPane(propertyTable);
		this.add(propScrollPane);
		this.add(viewScrollPane);
	}

	public JTable getViewTable() {
		return viewTable;
	}

	public void setViewTable(JTable viewTable) {
		this.viewTable = viewTable;
	}

	public JScrollPane getViewScrollPane() {
		return viewScrollPane;
	}

	public void setViewScrollPane(JScrollPane viewScrollPane) {
		this.viewScrollPane = viewScrollPane;
	}
	
	public JTable getPropertiesTable() {
		return propertyTable;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
