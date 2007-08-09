package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.diagnostic.Logger;
import org.intellij.vcs.mks.EncodingProvider;

import java.io.*;

/**
 * @author Thibaut Fagart
 */
public abstract class AbstractMKSSynchronizer {
	protected final Logger LOGGER = Logger.getInstance(getClass().getName());
	private final EncodingProvider encodingProvider;
	private String command;
	private String[] args;
	private Process process;
	private boolean isAlive = false;
    private volatile boolean stop = false;
    // todo add support to detect end of updates
    // all lines for an update come in a close succession, should be possible to detect it using a background thread or wait with timeouts

    public AbstractMKSSynchronizer(String command, EncodingProvider encodingProvider, String... args) {
		this.command = command;
		this.encodingProvider = encodingProvider;
		if (args.length > 0) {
			this.args = new String[args.length + 1];
			System.arraycopy(args, 0, this.args, 0, args.length);
			this.args[args.length] = "--persist";
		} else {
			this.args = new String[]{"--persist"};
		}

	}

	public void start() {
		new Thread(new Runnable() {
			public void run() {
                do {
                    executeCommand();
                    if (!stop) {
                        LOGGER.warn("synchronizer terminated unexpectedly, restarting");
                    }
                } while (!stop);
            }
		}, "MksSynchronizer("+getClass().getName()+"").start();
	}

	public void stop() {
		if (process != null) {
			LOGGER.info("stopping");
            stop = true;
            process.destroy();
		}
	}

	protected void executeCommand() {
		String[] processArgs = new String[args.length + 2];
		processArgs[0] = "si";
		processArgs[1] = command;
		System.arraycopy(args, 0, processArgs, 2, args.length);
		ProcessBuilder builder = new ProcessBuilder(processArgs);
		builder.redirectErrorStream(true);
		isAlive = true;
		try {
			process = builder.start();
		} catch (IOException e) {
			LOGGER.error("unable to start MKS persistent synchronizer", e);
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					process.waitFor();
                    isAlive = false;
                } catch (InterruptedException e) {
                    LOGGER.warn("interrupted while waiting for MKS persistent synchronizer process to terminate");
                }
			}
		}, "MksSynchronizer ("+getClass().getName()+") death notifier").start();
		InputStream is = process.getInputStream();
		InputStreamReader streamReader;
		try {
			streamReader = new InputStreamReader(is, encodingProvider.getMksSiEncoding(command));
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("unsupported encoding specified for reading MKS process output", e);
			stop();
			return;
		}
		BufferedReader reader = new BufferedReader(streamReader);
		try {
			String line;
			try {
				while ((line = reader.readLine()) != null) {
//					System.out.println("read line  :[" + line + "]");
					handleLine(line);
				}
			} catch (IOException e) {
				LOGGER.error("error reading process output, stopping synchronizer", e);
				stop();
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.warn("error closing MKS process reader stream", e);
			}
			LOGGER.info("stopped");
		}

	}

	protected abstract void handleLine(String line);

	protected boolean shoudIgnore(String line) {
		return line.startsWith("Reconnecting") || line.startsWith("Connecting");
	}

	public boolean isAlive() {
		return isAlive;
	}
}
