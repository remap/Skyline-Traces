package edu.ucla.tft.hilos;

/**
 * An interface for an entity that can be animated. Provides methods which are
 * called once at the beginning of the animation, and subsequently at regular
 * intervals.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface Animatable {
	public void init();

	public void update(long delta_ms);
}
