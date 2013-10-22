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

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TauntCommand implements Command {

	private static final String TAG = TauntCommand.class.getSimpleName();

	public static final String NUISANCE_MULTIPLIER_PREFS_KEY = "NuisanceMultiplier";
	private static final String DEFAULT_NUISANCE_MULTIPLIER = "3";
	private static final int NUISANCE_INTERVAL_SECONDS = 5;

	public static class TauntReceiver extends BroadcastReceiver {

		private static final String INTENT_MSG_KEY = "msg";
		private static final int TOAST_DURATION = Toast.LENGTH_SHORT;

		public static Intent makeTauntIntent(final Context context, final String msg) {
			if (context == null || msg == null) {
				throw new NullPointerException();
			}

			final Intent intent = new Intent(context, TauntReceiver.class);
			intent.putExtra(INTENT_MSG_KEY, msg);
			return intent;
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {
			Toast.makeText(context, intent.getStringExtra(INTENT_MSG_KEY), TOAST_DURATION).show();
		}

	}

	@Override
	public void execute(final Context context, final String params) {
		Log.d(TAG, getClass().getSimpleName() + ".execute(Context, \'" + params + "\')");

		final int mult = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
				NUISANCE_MULTIPLIER_PREFS_KEY, DEFAULT_NUISANCE_MULTIPLIER));
		Log.d(TAG, "Nuisance Multiplier:" + mult);

		final String msg = params.replaceAll("^\\s*\"|\"$", "");

		final Intent intent = TauntReceiver.makeTauntIntent(context, msg);
		final AlarmManager alarmMan = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		final Calendar time = Calendar.getInstance();

		for (int i = 0; i < mult; ++i, time.add(Calendar.SECOND, NUISANCE_INTERVAL_SECONDS)) {

			final int uniqueRequestCode = i;
			final PendingIntent sender = PendingIntent.getBroadcast(context, uniqueRequestCode, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			final long timeInMs = time.getTimeInMillis();
			alarmMan.set(AlarmManager.RTC_WAKEUP, timeInMs, sender);

			Log.d(TAG, "Scheduled taunt for " + timeInMs);
		}
	}

	@Override
	public String getCommandName() {
		return "taunt";
	}

}
