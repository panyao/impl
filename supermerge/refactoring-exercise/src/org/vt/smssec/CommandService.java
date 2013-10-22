package org.vt.smssec;

import java.util.Arrays;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandService extends IntentService {

	private static final String TAG = CommandService.class.getSimpleName();
	private static final String INTENT_COMMAND_KEY = "cmd";

	public static Intent makeCommandIntent(final Context context, final String cmd) {
		if (context == null || cmd == null) {
			throw new NullPointerException();
		}

		final Intent intent = new Intent(context, CommandService.class);
		intent.putExtra(INTENT_COMMAND_KEY, cmd);
		return intent;
	}

	private final List<? extends Command> mCommands;

	public CommandService() {
		super(TAG);

		mCommands = Arrays.asList(new TauntCommand(), new TalkCommand(), new PhotoCommand());
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		/*
		 * TODO We may want to grab a wake lock since this will be invoked from
		 * a broadcast receiver and so the CPU can fall asleep immediately. If
		 * we do grab a lock, it should be grabbed from inside the receiver.
		 */

		Log.d(TAG, "onHandleIntent(" + intent + ")");

		final String cmd = intent.getStringExtra(INTENT_COMMAND_KEY);
		Log.d(TAG, "cmd: [" + cmd + "]");
		if (cmd != null) {
			for (final Command handler : mCommands) {
				if (cmd.startsWith(handler.getCommandName())) {
					Log.d(TAG, "Executing " + cmd + " with " + handler.getClass().getSimpleName());
					handler.execute(this, cmd.replaceFirst(handler.getCommandName(), ""));
				}
			}
		}

		Log.d(TAG, "onHandleIntent(" + intent + ") finished");
	}

}
