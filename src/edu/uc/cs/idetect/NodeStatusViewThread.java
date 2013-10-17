package edu.uc.cs.idetect;

import javax.swing.JFrame;

public class NodeStatusViewThread implements Runnable {

	private JFrame viewFrame;
	private NodeStatusView nodeStatusView;

	@Override
	public void run() {
		//Create and setup the window
		viewFrame = new JFrame("Node Status View");
		viewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create and setup the content pane
		nodeStatusView = new NodeStatusView();
		nodeStatusView.setOpaque(true); // apparently you always have to set this true
		viewFrame.setContentPane(nodeStatusView);
		
		// Display the window
		viewFrame.pack();
		viewFrame.setVisible(true);
	}

}
