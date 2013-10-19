package edu.uc.cs.distsys.ui;

import javax.swing.JFrame;

public class NodeStatusViewThread implements Runnable {

	private JFrame viewFrame;
	private NodeStatusView nodeStatusView;
	private int nodeID;
	
	public NodeStatusViewThread(int id) {
		this.nodeID = id;
	}

	@Override
	public void run() {
		//Create and setup the window
		viewFrame = new JFrame("Node Status View - " + this.nodeID);
		viewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create and setup the content pane
		nodeStatusView = new NodeStatusView();
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
	
	

}
