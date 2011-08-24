package edu.ucla.tft.hilos;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class to create a separate animation thread which issues regular time step
 * events registered {@link Animatable}s.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class AnimationThread {
	private Timer timer;

	private List<Animatable> animatables;

	public AnimationThread() {
		animatables = new ArrayList<Animatable>();
	}

	public void start() {
		if (timer != null) {
			return;
		}
		for (Animatable a : animatables) {
			a.init();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				tick();
			}
		}, 0, 30);
	}

	public void stop() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
		last = 0;
	}

	public void register(Animatable animatable) {
		if (timer != null) {
			throw new RuntimeException(
					"Cannot register while animation is running!");
		}
		animatables.add(animatable);
	}

	private long current, last;

	private void tick() {
		current = System.nanoTime() / 1000000;
		long delta_ms = last == 0 ? 0 : current - last;
		for (Animatable a : animatables) {
			a.update(delta_ms);
		}
		last = current;
	}
}
