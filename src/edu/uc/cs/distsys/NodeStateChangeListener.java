package edu.uc.cs.distsys;

public interface NodeStateChangeListener {

	public void onNodeStateChanged(Node n, NodeState oldState);
	
}
