package edu.uc.cs.distsys.idetect;

import edu.uc.cs.distsys.Node;

public interface FailureListener {

	public void onFailedNode(Node failed);
	
}
