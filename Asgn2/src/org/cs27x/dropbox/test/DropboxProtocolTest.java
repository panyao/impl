package org.cs27x.dropbox.test;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.cs27x.dropbox.DropboxCmd;
import org.cs27x.dropbox.DropboxCmd.OpCode;
import org.cs27x.dropbox.DropboxProtocol;
import org.cs27x.dropbox.DropboxTransport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DropboxProtocolTest {

	@Before
	public void setUp() throws Exception {
		protocol = new DropboxProtocol(transport, null, null);
		when(mockPath.getFileName()).thenReturn(mockPath);
		when(mockPath.getFileName().toString()).thenReturn("test");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAdd() {
		assertTrue(verifyOp(OpCode.ADD));
	}
	@Test
	public void testRemove() {
		assertTrue(verifyOp(OpCode.REMOVE));
	}
	@Test
	public void testUpdate() {
		assertTrue(verifyOp(OpCode.UPDATE));
	}
	
	private boolean verifyOp(OpCode code) {
		ArgumentCaptor<DropboxCmd> captor =
				ArgumentCaptor.forClass(DropboxCmd.class);
		protocol.doFileOp(path, code);
		verify(transport, atLeast(1)).publish(captor.capture());
		return captor.getValue().getOpCode() == code;
	}

	private DropboxProtocol protocol;
	private DropboxTransport transport = mock(DropboxTransport.class);
	private Path mockPath = mock(Path.class);
	private Path path = Paths.get("sandbox/test", new String[] {});
}
