package org.cs27x.dropbox;

import java.io.Serializable;


public interface IDropboxCmd extends Serializable {
	
//	public enum OpCode {
//		ADD, REMOVE, UPDATE, SYNC, GET
//	}

	public abstract String getPath();

//	public abstract OpCode getOpCode();

}