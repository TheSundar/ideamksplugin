package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.VcsException;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author Thibaut Fagart
 */
public class MksVcsException extends VcsException {

	final Throwable myCause;

	public MksVcsException(final String message, final Throwable cause) {
		super(message);
		myCause = cause;
	}

	@Override
	public void printStackTrace(final PrintStream printStream) {
		super.printStackTrace(printStream);
		if (myCause != null) {
			printStream.print("\n caused by \n");
			myCause.printStackTrace(printStream);
		}
	}

	@Override
	public void printStackTrace(final PrintWriter printWriter) {
		super.printStackTrace(printWriter);
		if (myCause != null) {
			printWriter.print("\n caused by \n");
			myCause.printStackTrace(printWriter);
		}
	}
}
