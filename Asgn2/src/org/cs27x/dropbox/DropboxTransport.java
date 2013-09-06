package org.cs27x.dropbox;

public interface DropboxTransport {

	public void connect(String host);
	
	public void disconnect();
	
	public void awaitConnect(long timeout) throws InterruptedException;
	
	public boolean isConnected();
	
	public void publish(DropboxCmd cmd);
	
	public void addListener(DropboxTransportListener hdlr);
	
}
