package org.cs27x.dropbox.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.cs27x.dropbox.DefaultFileManager;
import org.cs27x.dropbox.FileManager;
import org.cs27x.filewatcher.FileEvent;
import org.cs27x.filewatcher.FileEventHandler;
import org.cs27x.filewatcher.FileReactor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileReactorTest {

	@Before
	public void setUp() throws Exception {
		reactor = new FileReactor(path.getParent());
		manager = new DefaultFileManager(path.getParent());
		reactor.addHandler(mockHandler);
		spy = spy(reactor);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		(new ReactorThread()).start();
		try {
			manager.write(path, testdata, true);
			Thread.sleep(4000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//verify(spy, times(1)).dispatch(any(FileEvent.class));
	}
	
	private class ReactorThread extends Thread {
		@Override
		public void run() {
			try {
				reactor.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private FileReactor reactor;
	private FileReactor spy;
	private FileEventHandler mockHandler = mock(FileEventHandler.class);
	private Path path = Paths.get("test-data/sandbox/test", new String[] {});
	private FileManager manager;
	private final byte[] testdata = "0123456789".getBytes();
}
