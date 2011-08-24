package edu.ucla.tft.hilos;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

/**
 * Class providing functionality to convert text to speech (TTS). Uses Android's
 * TTS engine to generate speech and provides a queue of strings to be spoken
 * out loud.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class SpeechEngine {
	public static interface Listener {
		public void speechBegin(String text);

		public void speechEnd();
	}

	private TextToSpeech mTts;
	private SpeakQueue mSpeakQueue;
	private Listener mListener;

	// Enum for transforming text for either captioning or speech
	private enum Transform {
		CAPTION, SPEAK
	};

	private class SpeakQueue extends Thread implements
			OnUtteranceCompletedListener {
		private BlockingQueue<String> mQueue;

		private boolean mUtteranceCompleted;

		private boolean mStopped;

		public SpeakQueue() {
			mQueue = new ArrayBlockingQueue<String>(100);
		}

		/**
		 * Removes all entries in queue (if any) and then adds the supplied
		 * string
		 * 
		 * @param text
		 *            Text to be spoken immediately
		 */
		public void put(String text) {
			cancel();
			add(text);
		}

		public void cancel() {
			mQueue.clear();
			mTts.stop();
		}

		/**
		 * Adds an entry to the speech queue.
		 * 
		 * @param text
		 *            Text to be queued up and spoken later.
		 */
		public void add(String text) {
			try {
				mQueue.put(text);
			} catch (InterruptedException e) {
				throw new RuntimeException("Adding to speak queue interrupted!");
			}
		}

		/**
		 * Callback implemented as part of the
		 * {@link OnUtteranceCompletedListener} interface
		 */
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			mUtteranceCompleted = true;
			if (mListener != null) {
				mListener.speechEnd();
			}
			// normalVolume();
			// Wake up thread to get next string in queue
			synchronized (this) {
				notifyAll();
			}
		}

		@Override
		public void run() {
			while (!mStopped) {
				String text = null;
				while (text == null) {
					try {
						text = mQueue.take();
					} catch (InterruptedException e) {
						// ignore and try again
					}
				}
				mUtteranceCompleted = false;
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
				final String caption = transform(text, Transform.CAPTION);
				if (mListener != null) {
					mListener.speechBegin(caption);
				}
				final String speak = transform(text, Transform.SPEAK);
				// lowerVolume();
				mTts.speak(speak, TextToSpeech.QUEUE_ADD, params);
				synchronized (this) {
					while (!mUtteranceCompleted) {
						try {
							wait();
						} catch (InterruptedException e) {
							// ignore
							e.printStackTrace();
						}
					}
				}
			}
		}

		// Strips out marked up text to construct text for either captions or
		// speech
		private String transform(String text, Transform transform) {
			text = text.trim();
			StringBuilder sb = new StringBuilder();
			int start, end = 0;
			while ((start = text.indexOf("[\\", end)) >= 0) {
				sb.append(text.substring(end, start));
				int newEnd = text.indexOf("]", start + 1);
				if (newEnd < 0) {
					newEnd = text.length();
				}
				String extracted = text.substring(start, newEnd);
				int separatorIndex = extracted.indexOf(":");
				if (separatorIndex < 0) {
					separatorIndex = newEnd;
				}
				switch (transform) {
				case CAPTION:
					sb.append(extracted.substring(2, separatorIndex));
					break;
				case SPEAK:
					sb.append(extracted.substring(separatorIndex + 1));
					break;
				}
				end = newEnd + 1;
			}
			sb.append(text.substring(end));
			return sb.toString();
		}
	}

	public SpeechEngine(Context context) {
		if (Hilos.isTextToSpeech()) {
			mSpeakQueue = new SpeakQueue();
			mTts = new TextToSpeech(context, new OnInitListener() {
				@Override
				public void onInit(int status) {
					mTts.setOnUtteranceCompletedListener(mSpeakQueue);
				}
			});
			mSpeakQueue.start();
		}
	}

	public void destroy() {
		if (mTts != null) {
			mTts.shutdown();
		}
	}

	public Listener getListener() {
		return mListener;
	}

	public void setListener(Listener l) {
		this.mListener = l;
	}

	public void speakImmediately(String text) {
		if (mTts == null) {
			return;
		}
		mSpeakQueue.put(text);
	}

	public void speakQueued(String text) {
		if (mTts == null) {
			return;
		}
		mSpeakQueue.add(text);
	}

	public void speakQueued(String[] texts) {
		if (mTts == null) {
			return;
		}
		for (String text : texts) {
			mSpeakQueue.add(text);
		}
	}

	public void cancel() {
		mSpeakQueue.cancel();
	}
}
