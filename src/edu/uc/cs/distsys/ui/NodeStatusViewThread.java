package edu.uc.cs.distsys.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
		this.getNodeStatusView().getNodePropertiesTableStorage().setNode(this.node);
		//Set the listener for when a user clicks a given node to update appropriately
		this.getNodeStatusView().getNodeTable().addMouseListener(new MouseAdapter(){
		    public void mouseClicked(MouseEvent mEvent) {
		    	// process event
		    	int row = 0;	    	
		    	row = getNodeStatusView().getNodeTable().rowAtPoint(mEvent.getPoint());
		    	System.out.println(row);
				if (row >= 0) {
					System.out.println("Selected row: " + row);
					Node clickedNode = getNodeStatusView().getNodeTableStorage().getNodeAtRow(row);
					if (clickedNode != null) {
						System.out.println("Clicked node: " + clickedNode.toString());
						// set the new data
						getNodeStatusView().getClickedNodeTableStorage().setNode(clickedNode);
						// force the UI update
						getNodeStatusView().getClickedNodeTableStorage().fireTableDataChanged();
					} else {
						System.out.println("Node was somehow null");
					}
				}
				mEvent.consume();
	    	}
		});
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
		this.getNodeStatusView().getNodeTableStorage().fireTableDataChanged();
		this.getNodeStatusView().getNodePropertiesTableStorage().fireTableDataChanged();
	}
	
	public void addMonitoredNode(Node n) {
		this.getNodeStatusView().getNodeTableStorage().addItem(n);
	}
	
	public void setUIMessage(String uiMessage) {
		if (uiMessage != null) {
			this.viewFrame.setTitle(this.defaultTitle + " [" + uiMessage + "]");
		} else {
			this.viewFrame.setTitle(this.defaultTitle);
		}
	}
	
}
