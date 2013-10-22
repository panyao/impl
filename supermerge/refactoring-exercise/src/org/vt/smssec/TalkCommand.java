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

import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.media.AudioManager;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class TalkCommand implements Command {

	private static final String TAG = TalkCommand.class.getSimpleName();

	private Future<TextToSpeech> mEngineFuture = null;

	private static class MakeEngineTask implements Callable<TextToSpeech> {

		private final Context mContext;
		private volatile boolean mInitSuccess = false;

		public MakeEngineTask(final Context context) {
			mContext = context.getApplicationContext();
		}

		@Override
		public TextToSpeech call() throws Exception {
			if (Looper.getMainLooper() == Looper.myLooper())
				throw new IllegalStateException("This should be invoked from a background thread");

			final CyclicBarrier barrier = new CyclicBarrier(2);

			final TextToSpeech engine = new TextToSpeech(mContext, new OnInitListener() {
				@Override
				public void onInit(final int status) {
					if (Looper.getMainLooper() != Looper.myLooper())
						throw new IllegalStateException("This should be invoked from the UI thread");

					switch (status) {
					case TextToSpeech.ERROR:
						Log.e(TAG, "TextToSpeech failed to initialize");
						break;
					case TextToSpeech.SUCCESS:
						mInitSuccess = true;
						break;
					default:
						throw new AssertionError();
					}

					try {
						barrier.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (BrokenBarrierException e) {
						throw new RuntimeException(e);
					}
				}
			});

			// Make sure initialization finished
			barrier.await();
			return mInitSuccess ? engine : null;
		}
	}

	@Override
	public void execute(final Context context, final String params) {
		Log.d(TAG, getClass().getSimpleName() + ".execute(Context, \'" + params + "\')");

		synchronized (this) {
			if (mEngineFuture == null)
				mEngineFuture = Executors.newSingleThreadExecutor().submit(new MakeEngineTask(context));
		}

		final String msg = params.replaceAll("^\\s*\"|\"$", "");

		try {
			final TextToSpeech engine = mEngineFuture.get();
			if (engine == null) {
				Log.e(TAG, "No speech engine available. Quitting.");
				return;
			}

			HashMap<String, String> myHashAlarm = new HashMap<String, String>();
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
			engine.speak(msg, TextToSpeech.QUEUE_ADD, myHashAlarm);
			Log.d(TAG, "Speaking " + msg);

			// Spin wait.
			while (engine.isSpeaking())
				Thread.sleep(50);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String getCommandName() {
		return "say";
	}

}
