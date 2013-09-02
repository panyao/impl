package org.cs27x.dropbox.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cs27x.dropbox.DefaultFileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultFileManagerTest {

	@Before
	public void setUp() throws Exception {
		manager = new DefaultFileManager(rootdir);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteDelete() {
		try {
			manager.write(rootdir, testdata, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(Files.exists(rootdir));
		
		try {
			manager.delete(rootdir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(!Files.exists(rootdir));
	}
	
	private DefaultFileManager manager;
	private final Path rootdir = Paths.get("test-data/sandbox/test", new String[]{});
	private final byte[] testdata = "0123456789".getBytes();

}
