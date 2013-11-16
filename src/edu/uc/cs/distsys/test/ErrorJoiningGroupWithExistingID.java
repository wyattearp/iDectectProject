package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.init.GroupJoinException;

public class ErrorJoiningGroupWithExistingID {

	final static int numNodes = 2;
	final static String PASSED_MSG = "Req A8 Passed: A process must report an error if it attempts to join a group that already has a correct process sharing the same identifier)";
	final static String FAILED_MSG = "Req A8 Failed";
	private DetectMain orig;
	private DetectMain impostor;
	
	@Before
	public void setup() {
		orig = new DetectMain(10 * 100, null);
		impostor = new DetectMain(10 * 100, null);
		try {
			orig.start();
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		// Allow system to stabilize
		Thread.sleep(3000);
		
		// Start the imposter
		try {
			impostor.start();
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
		
		// TODO: assert that an error occurs when impostor starts
		String message = "";
		assertTrue(FAILED_MSG, message.equals("Unable to join network"));
				
		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		orig.stop();
		impostor.stop();
	}
}
