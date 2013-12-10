package org.intellij.vcs.mks.realtime;

/**
 * A long running task, notably the si --persist like commands. <br/>
 * Allows to monitor/retart/stop them
 */
public interface LongRunningTask {
	void stop();
	void restart();
	boolean isAlive();

	/**
	 * a human readable description for the task
	 * @return
	 */
	String getDescription();
}
