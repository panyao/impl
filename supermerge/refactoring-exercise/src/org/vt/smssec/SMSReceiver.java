/*************************************************************************
 * Copyright 2010 Jules White
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/
 * LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 **************************************************************************/

package org.vt.smssec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

/*
 * TODO
 * May want to use a WakefulBroadcastReceiver here
 */
public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = SMSReceiver.class.getSimpleName();

	private static final String ENABLE_SMS_SEC = "EnableSMSSec";
	private static final String CMD = "cmd:";
	private static final String MESSAGES = "pdus";

	private static final List<String> WHITE_LIST = Arrays.asList("5555555555", "5555555556");

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// TODO - This broadcast may contain non-command SMSs as well as command
		// SMSs. We need to be able to forward the non-command messages onto the
		// SMS app.

		Log.d(TAG, "onReceive(Context, " + intent + ")");

		final boolean enabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ENABLE_SMS_SEC, true);

		if (!enabled) {
			Log.i(TAG, "Not enabled. Quitting");
			return;
		}

		// Make sure the sms app doesn't get the message and create a
		// notification that the thief might see
		Log.d(TAG, "Aborting broadcast...");
		this.abortBroadcast();

		// extract the list of sms messages from the intent
		final Object messages[] = (Object[]) intent.getExtras().get(MESSAGES);

		final List<String> cmds = new ArrayList<String>();

		// iterate through the sms messages and look for any
		// commands that need to be executed
		for (int i = 0; i < messages.length; i++) {
			final SmsMessage msg = SmsMessage.createFromPdu((byte[]) messages[i]);

			// do some checking to make sure that the sender has
			// permission to execute commands
			if (isPrivelegedNumber(msg.getOriginatingAddress())) {
				final String body = msg.getMessageBody();

				Log.d(TAG, "sms: [" + body + "]");
				// finally extract and add the command from the sms
				// body to the list of commands
				if (isCommand(body)) {
					final String cmd = getCommand(body);
					Log.d(TAG, "It's a command!");
					cmds.add(cmd);
				} else {
					Log.d(TAG, "Looks like a normal sms...");
				}
			} else {
				Log.w(TAG, "Attempted access from unprivileged number");
			}

		}

		// if we didn't find any commands, we need to cancel the abort so that
		// the sms app receives the message and the user (thief) doesn't know
		// that we are silently monitoring sms messages
		if (cmds.isEmpty()) {
			Log.d(TAG, "Clearing broadcast abort");
			this.clearAbortBroadcast();
		} else {
			for (final String cmd : cmds) {
				context.startService(CommandService.makeCommandIntent(context, cmd));
			}
		}
	}

	private boolean isCommand(String msg) {
		return msg.startsWith(CMD);
	}

	private String getCommand(String msg) {
		return msg.replaceFirst(CMD, "");
	}

	private boolean isPrivelegedNumber(final String num) {
		for (final String regExpr : WHITE_LIST) {
			if (num.matches(regExpr))
				return true;
		}

		return false;
	}

}