package org.cs27x.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.cs27x.dropbox.DropboxCmd.OpCode;

public class DropboxProtocol {

	private final DropboxTransport transport_;
	
	private final DropboxCmdProcessor cmdProcessor_;

	public DropboxProtocol(DropboxTransport transport, FileManager filemgr) {
		transport_ = transport;
		cmdProcessor_ = new DropboxCmdProcessor(filemgr);
		transport_.addListener(cmdProcessor_);
	}

	public void publish(DropboxCmd cmd) {
		transport_.publish(cmd);
	}

	public void addFile(Path p) {
		DropboxCmd cmd = new DropboxCmd();
		cmd.setOpCode(OpCode.ADD);
		cmd.setPath(p.getFileName().toString());

		try {

			try (InputStream in = Files.newInputStream(p)) {
				byte[] data = IOUtils.toByteArray(in);
				cmd.setData(data);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		publish(cmd);
	}

	public void removeFile(Path p) {
		DropboxCmd cmd = new DropboxCmd();
		cmd.setOpCode(OpCode.REMOVE);
		cmd.setPath(p.getFileName().toString());
		publish(cmd);
	}

	public void updateFile(Path p) {
		DropboxCmd cmd = new DropboxCmd();
		cmd.setOpCode(OpCode.UPDATE);
		cmd.setPath(p.getFileName().toString());
		try {

			try (InputStream in = Files.newInputStream(p)) {
				byte[] data = IOUtils.toByteArray(in);
				cmd.setData(data);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		publish(cmd);
	}
	
	public void doFileOp(Path p, OpCode opCode) {
		if(opCode == OpCode.ADD) {
			addFile(p);
		} else if(opCode == OpCode.REMOVE) {
			removeFile(p);
		} else if(opCode == OpCode.UPDATE) {
			updateFile(p);
		}
	}


}
