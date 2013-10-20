package edu.uc.cs.distsys.ui;

import javax.swing.JFrame;

import edu.uc.cs.distsys.Node;

public class NodeStatusViewThread implements Runnable {

	private JFrame viewFrame;
	private NodeStatusView nodeStatusView;
	private int nodeID;
	private String defaultTitle = "Node Status View - Node ID: ";
	
	public NodeStatusViewThread(int id) {
		this.nodeID = id;
		this.defaultTitle += this.nodeID;
		//Create and setup the windows
		viewFrame = new JFrame(this.defaultTitle);
		viewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		nodeStatusView = new NodeStatusView();
	}

	@Override
	public void run() {
		//Create and setup the content pane		
		nodeStatusView.setOpaque(true); // apparently you always have to set this true
		viewFrame.setContentPane(nodeStatusView);
		
		// Display the window
		viewFrame.pack();
		viewFrame.setVisible(true);
	}

	public JFrame getViewFrame() {
		return viewFrame;
	}

	public void setViewFrame(JFrame viewFrame) {
		this.viewFrame = viewFrame;
	}

	public NodeStatusView getNodeStatusView() {
		return nodeStatusView;
	}

	public void setNodeStatusView(NodeStatusView nodeStatusView) {
		this.nodeStatusView = nodeStatusView;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
	
	public void updateUI() {
		this.getNodeStatusView().getNodeTable().fireTableDataChanged();
	}
	
	public void addMonitoredNode(Node n) {
		this.getNodeStatusView().getNodeTable().addItem(n);
	}
	
	public void setUIMessage(String uiMessage) {
		if (uiMessage != null) {
			this.viewFrame.setTitle(this.defaultTitle + "[" + uiMessage + "]");
		} else {
			this.viewFrame.setTitle(this.defaultTitle);
		}
	}

}
