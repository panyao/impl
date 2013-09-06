package org.cs27x.dropbox;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cs27x.dropbox.DropboxCmd.OpCode;
import org.cs27x.filewatcher.FileEvent;
import org.cs27x.filewatcher.FileState;

public class DefaultFileManager implements FileManager {

	private final Path rootDir_;
	private Map<String, FileState> states_ = new ConcurrentHashMap<>();
	
	public DefaultFileManager(Path rootdir){
		rootDir_ = rootdir;
	}
	
	@Override
	public boolean exists(Path p){
		return Files.exists(p);
	}
	
	@Override
	public void write(Path p, byte[] data, boolean overwrite) throws IOException{
		
		if (!Files.exists(p) || overwrite) {
			try (OutputStream out = Files.newOutputStream(p)) {
				out.write(data);
			}
		}
	}
	
	@Override
	public void delete(Path p) throws IOException {
		if(Files.exists(p)){
			Files.delete(p);
		}
	}

	@Override
	public Path resolve(String relativePathName) {
		return rootDir_.resolve(relativePathName);
	}
	
	// Formerly methods of FileStates

	/* (non-Javadoc)
	 * @see org.cs27x.filewatcher.IFilestates#getState(java.nio.file.Path)
	 */
	@Override
	public FileState getState(Path p){
		String key = p.toAbsolutePath().toString();
		return states_.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.cs27x.filewatcher.IFilestates#getOrCreateState(java.nio.file.Path)
	 */
	@Override
	public FileState getOrCreateState(Path p){
		String key = p.toAbsolutePath().toString();
		FileState state = states_.get(key);
		if(state == null){
			state = new FileState(-1, null);
			states_.put(key, state);
		}
		return state;
	}
	
	/* (non-Javadoc)
	 * @see org.cs27x.filewatcher.IFilestates#insert(java.nio.file.Path)
	 */
	@Override
	public FileState insert(Path p) throws IOException {
		String key = p.toAbsolutePath().toString();

		long size = Files.size(p);
		FileTime mod = Files.getLastModifiedTime(p);
		FileState state = new FileState(size, mod);

		states_.put(key, state);

		return state;
	}
	
	private FileEvent filterCreateOrUpdate(FileEvent evt, FileState state, Path p) throws IOException {
		FileEvent rslt = evt;
		
		if(state != null){
			long size = Files.size(p);
			FileTime time = Files.getLastModifiedTime(evt.getFile());
			
			rslt = (size != state.getSize() || 
					time.compareTo(state.getLastModificationDate()) > 0) ?
					evt : null;
		}
		
		return rslt;
	}
	
	private FileEvent filterDelete(FileEvent evt, FileState state, Path p){
		FileEvent rslt = null;
		
		if(state != null && state.getSize() != -1){
			state.setSize(-1);
			rslt = evt;
		}
		
		return rslt;
	}
	

	/* (non-Javadoc)
	 * @see org.cs27x.filewatcher.IFilestates#filter(org.cs27x.filewatcher.FileEvent)
	 */
	@Override
	public FileEvent filter(FileEvent evt) throws IOException {
		Path p = evt.getFile().toAbsolutePath();
		
		FileState state = states_.get(p.toString());

		FileEvent event = (evt.getEventType() == ENTRY_CREATE 
				          || evt.getEventType() == ENTRY_MODIFY)?
				             filterCreateOrUpdate(evt, state, p) :
				             filterDelete(evt, state, p);

		return event;
	}
	
	// @AUSTIN :: Conditional to polymorphism?
	@Override
	public void updateFileState(DropboxCmd cmd, Path resolved) {
		try {
			if (cmd.getOpCode() == OpCode.REMOVE) {
				FileState state = getState(resolved);
				if(state != null){
					state.setSize(-1);
				}
			} else if (cmd.getOpCode() == OpCode.ADD
					|| cmd.getOpCode() == OpCode.UPDATE) {
				FileState state = getOrCreateState(resolved);
				state.setSize(cmd.getData().length);
				state.setLastModificationDate(Files
						.getLastModifiedTime(resolved));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FileEvent resolvePathAndFilter(FileEvent evt) throws IOException {
		Path p = resolve(evt.getFile().getFileName().toString());
		FileEvent resolvedEvent = new FileEvent(evt.getEventType(), p);
		return filter(resolvedEvent);
	}
}
