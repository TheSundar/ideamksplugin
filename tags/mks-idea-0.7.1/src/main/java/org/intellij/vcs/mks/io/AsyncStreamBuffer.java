package org.intellij.vcs.mks.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsyncStreamBuffer {
	private final InputStream in;

	private IOException cause;
	private byte[] buf;

	private Thread reader = new Thread() {
		private volatile boolean active;

		@Override
		public void run() {
			active = true;
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				byte[] buffer = new byte[2048];
				int readBytes;

				while (active && (readBytes = in.read(buffer)) != -1) {
					out.write(buffer, 0, readBytes);
				}

				buf = out.toByteArray();

			} catch (IOException e) {
				cause = e;
			}
		}

		@Override
		public void interrupt() {
			active = false;
			super.interrupt();
		}
	};

	public AsyncStreamBuffer(InputStream in) {
		this.in = in;
		reader.start();
	}

	public synchronized boolean isOpen() {
		return reader != null && reader.isAlive();
	}

	protected synchronized void waitFor() throws IOException {
		if (isOpen()) {
			//noinspection EmptyCatchBlock
			try {
				reader.join();

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public byte[] get() throws IOException {
		waitFor();

		if (cause != null) {
			throw cause;
		}

		return buf;
	}

	public void close() throws IOException {
		try {
			if (isOpen()) {
				reader.interrupt();

				waitFor();
				in.close();
			}
		} finally {
			reader = null;
		}
	}
}
