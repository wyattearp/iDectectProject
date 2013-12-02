package edu.uc.cs.distsys.ui;

import javax.swing.JFrame;

import edu.uc.cs.distsys.Node;

public class NodeStatusViewThread implements Runnable {

	private JFrame viewFrame;
	private NodeStatusView nodeStatusView;
	private Node node;
	private String defaultTitle = "Node Status View - Node ID: ";
	
	public NodeStatusViewThread(Node n) {
		this.node = n;
		this.defaultTitle += this.node.getId();
		//Create and setup the windows
		viewFrame = new JFrame(this.defaultTitle);
		viewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		nodeStatusView = new NodeStatusView();
		//Force the first update
		this.getNodeStatusView().getNodePropertiesLabel().setText(this.node.toHTMLString());
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
		return node.getId();
	}
	
	public void updateUI() {
		this.getNodeStatusView().getNodeTable().fireTableDataChanged();
		this.getNodeStatusView().getNodePropertiesLabel().setText(this.node.toHTMLString());
	}
	
	public void addMonitoredNode(Node n) {
		this.getNodeStatusView().getNodeTable().addItem(n);
	}
	
	public void setUIMessage(String uiMessage) {
		if (uiMessage != null) {
			this.viewFrame.setTitle(this.defaultTitle + " [" + uiMessage + "]");
		} else {
			this.viewFrame.setTitle(this.defaultTitle);
		}
	}

}
