package org.cs27x.dropbox.test;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.nio.file.Path;

import org.cs27x.dropbox.DropboxProtocol;
import org.cs27x.dropbox.FileManager;
import org.cs27x.filewatcher.DropboxFileEventHandler;
import org.cs27x.filewatcher.FileEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DropBoxFileEventHandlerTest {
	
	@Mock private DropboxProtocol mockProtocol;
	@Mock private FileManager mockManager;
	
	private DropboxFileEventHandler handler;
	@Mock private FileEvent mockEvent;

	@Before
	public void setUp() throws Exception {
		// Different returns on subsequent calls
		doReturn(ENTRY_CREATE)
			.doReturn(ENTRY_DELETE)
			.doReturn(ENTRY_MODIFY).when(mockEvent).getEventType();
		
		doReturn(mockEvent).when(mockManager).
			resolvePathAndFilter(any(FileEvent.class));
		
		handler = new DropboxFileEventHandler(mockManager, mockProtocol);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEvents() {
		doThrow(new RuntimeException("1")).when(mockProtocol).addFile(any(Path.class));
		doThrow(new RuntimeException("2")).when(mockProtocol).removeFile(any(Path.class));
		doThrow(new RuntimeException("3")).when(mockProtocol).updateFile(any(Path.class));
		try {
			handler.handle(mockEvent);
		} catch(RuntimeException e) {
			e.printStackTrace();
			assertTrue(e.getMessage().equals("1"));
		}
		try {
			handler.handle(mockEvent);
		} catch(RuntimeException e) {
			e.printStackTrace();
			assertTrue(e.getMessage().equals("2"));
		}
		try {
			handler.handle(mockEvent);
		} catch(RuntimeException e) {
			e.printStackTrace();
			assertTrue(e.getMessage().equals("3"));
		}
	}
	
	

}
