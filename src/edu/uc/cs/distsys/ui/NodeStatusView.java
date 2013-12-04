package edu.uc.cs.distsys.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class NodeStatusView extends JPanel implements TableModelListener {
	
	private static final long serialVersionUID = 1L;
	private JTable viewTable;
	private JTable propertyTable;
	private JTable clickedTable;
	private JScrollPane viewScrollPane;
	private JScrollPane propScrollPane;
	private JScrollPane clickedScrollPane;
	private NodeTableModel nodeTableStorage;
	private NodeSingletonTableModel nodePropertiesTableStorage;
	private NodeSingletonTableModel clickedNodeTableStorage;
	private static final int nodeTableWidth = 800;
	private static final int nodeTableHeight = 100;
	private static final int nodeDetailsTableWidth = 500;
	private static final int nodeDetailsTableHeight = 100;

	public NodeTableModel getNodeTableStorage() {
		return nodeTableStorage;
	}
	
	public JTable getNodeTable() {
		return this.viewTable;
	}
	
	public NodeSingletonTableModel getNodePropertiesTableStorage() {
		return this.nodePropertiesTableStorage;
	}
	
	public NodeSingletonTableModel getClickedNodeTableStorage() {
		return this.clickedNodeTableStorage;
	}

	public NodeStatusView() {
		//super(new GridLayout(2,0));
		super(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();
		GridBagConstraints c3 = new GridBagConstraints();
		
		nodePropertiesTableStorage = new NodeSingletonTableModel();
		nodeTableStorage = new NodeTableModel();
		clickedNodeTableStorage = new NodeSingletonTableModel();
		
		// this is the table that holds details about what the net looks like
		viewTable = new JTable(this.nodeTableStorage);
		viewTable.setPreferredScrollableViewportSize(new Dimension(nodeTableWidth,nodeTableHeight));
		viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		viewTable.setFillsViewportHeight(true);
		
		// this holds the details about this processes node
		propertyTable = new JTable(this.nodePropertiesTableStorage);
		propertyTable.setPreferredScrollableViewportSize(new Dimension(nodeDetailsTableWidth,nodeDetailsTableHeight));
		propertyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		propertyTable.setFillsViewportHeight(true);
		
		// this holds the details about a clicked node in the table
		clickedTable = new JTable(this.clickedNodeTableStorage);
		clickedTable.setPreferredScrollableViewportSize(new Dimension(nodeDetailsTableWidth,nodeDetailsTableHeight));
		clickedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		clickedTable.setFillsViewportHeight(true);
		
		// attach the table to the UI pane
		viewScrollPane = new JScrollPane(viewTable);
		propScrollPane = new JScrollPane(propertyTable);
		clickedScrollPane = new JScrollPane(clickedTable);
		
		/* column = x, row = y
           +------------------+--------------------+
           |                  |                    |
           |                  |                    |
           |    My Node (c1)  |   Clicked Node(c2) |
           |                  |                    |
           |                  |                    |
           +------------------+--------------------+
           |                                       |
           |                                       |
           |          All nodes visible(c3)        |
           |                                       |
           |                                       |
           +---------------------------------------+
		 */
		// setup this node details panel
		c1.gridx = 0;
		c1.gridy = 0;
		this.add(propScrollPane,c1);
		
		// TODO: populate with something real
		// setup the "last clicked node" details" item or whatever we're going to make this
		c2.gridx = 1;
		c2.gridy = 0;
		this.add(clickedScrollPane,c2);
		
		// setup the bottom panel
		c3.gridx = 0;
		c3.gridy = 1;
		c3.gridheight = 2; // since this is the last one, we're going to tell it how big everything is
		c3.gridwidth = 2;  // this is measured in row,col - not pixels
		c3.fill = GridBagConstraints.HORIZONTAL;
		this.add(viewScrollPane,c3);
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

	@Override
	public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
