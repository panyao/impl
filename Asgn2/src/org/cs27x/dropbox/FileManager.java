package org.cs27x.dropbox;

import java.io.IOException;
import java.nio.file.Path;

import org.cs27x.filewatcher.FileEvent;
import org.cs27x.filewatcher.FileStates;

public interface FileManager extends FileStates{
	
	public Path resolve(String relativePathName);

	public boolean exists(Path p);
	
	public void write(Path p, byte[] data, boolean overwrite) throws IOException;
	
	public void delete(Path p) throws IOException;
	
	public abstract FileEvent resolvePathAndFilter(FileEvent evt) throws IOException;
	
}
