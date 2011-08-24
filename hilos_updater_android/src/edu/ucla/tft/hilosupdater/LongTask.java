package edu.ucla.tft.hilosupdater;

/**
 * Interface for a long running task. Used to run a task in a background thread,
 * usually with some user interface element showing that the task is underway
 * (e.g. a throbber).
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface LongTask {
	public void run();

	public void done();
}