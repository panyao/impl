package org.cs27x.dropbox.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.cs27x.dropbox.Dropbox;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DropboxTest {

	@Before
	public void setUp() throws Exception {
		servThread = new DropboxThread(servPath, null);
		clientThread = new DropboxThread(clientPath, peer);
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testMain() {
		servThread.start();
		clientThread.start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Is server connected?", clientThread.dp.connected());
	}

	private Dropbox server, client;
	private Path servPath = Paths.get("test-data/test-serv", new String[] {});
	private Path clientPath = Paths.get("test-data/test-client", new String[] {});
	private final String peer = "192.168.1.2";
	private DropboxThread servThread;
	private DropboxThread clientThread;
	
	
	private static class DropboxThread extends Thread {
		public DropboxThread(Path target, String peer) {
			mTarget = target;
			mPeer = peer;
		}
		
		@Override
		public void run() {
			dp = new Dropbox(mTarget);
			try {
				dp.connect(mPeer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private Path mTarget;
		private String mPeer;
		public Dropbox dp;
	}
}
