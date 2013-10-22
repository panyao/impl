package org.vt.smssec;

import android.content.Context;

public interface Command {

	public void execute(Context context, String params);

	public String getCommandName();

}
