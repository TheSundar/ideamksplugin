/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
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
