package org.intellij.vcs.mks;

public interface CommandExecutionListener {
	public final static CommandExecutionListener IDLE = new CommandExecutionListener() {
		public void executionCompleted(String command, long duration) {
		}
	};

	/**
	 * reports that an instance of the command just completed, and lasted duration ms
	 *
	 * @param command  the command name
	 * @param duration in milliseconds
	 */
	void executionCompleted(String command, long duration);
}
