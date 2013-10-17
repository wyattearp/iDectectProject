package edu.uc.cs.idetect;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class NodeStatusView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTable viewTable;
	private JScrollPane viewScrollPane;

	public NodeStatusView() {
		super(new GridLayout(1,0));
		viewTable = new JTable(new NodeTableModel());
		viewTable.setPreferredScrollableViewportSize(new Dimension(500,70));
		viewTable.setFillsViewportHeight(true);
		
		viewScrollPane = new JScrollPane(viewTable);
		this.add(viewScrollPane);
	}

}
