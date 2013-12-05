package edu.uc.cs.distsys.idetect;

import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.NodeState;

public interface FailureListener {

	public void onFailedNode(Node failed, NodeState oldState);
	
}
