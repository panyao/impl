package org.cs27x.filewatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Path;

import org.cs27x.dropbox.DropboxProtocol;
import org.cs27x.dropbox.FileManager;

public class DropboxFileEventHandler implements FileEventHandler {

	private final DropboxProtocol protocol_;
	private final FileManager fileManager_;

	public DropboxFileEventHandler(FileManager mgr, DropboxProtocol protocol) {
		super();
		protocol_ = protocol;
		fileManager_ = mgr;
	}

	@Override
	public void handle(FileEvent evt) {
		try {
			evt = fileManager_.resolvePathAndFilter(evt);
			if (evt != null) {

				if (evt.getEventType() == ENTRY_CREATE) {
					protocol_.addFile(evt.getFile());
				} else if (evt.getEventType() == ENTRY_MODIFY) {
					protocol_.updateFile(evt.getFile());
				} else if (evt.getEventType() == ENTRY_DELETE) {
					protocol_.removeFile(evt.getFile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
