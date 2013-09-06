package org.cs27x.filewatcher;

import java.io.IOException;
import java.nio.file.Path;

import org.cs27x.dropbox.DropboxCmd;

public interface FileStates {

	public abstract FileState getState(Path p);

	public abstract FileState getOrCreateState(Path p);

	public abstract FileState insert(Path p) throws IOException;

	public abstract FileEvent filter(FileEvent evt) throws IOException;
	
	public abstract void updateFileState(DropboxCmd cmd, Path resolved);

}