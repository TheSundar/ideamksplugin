package org.intellij.vcs.mks.sicommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import junit.framework.TestCase;

public class EncodingTest extends TestCase {

	public void testEncoding() throws IOException {
		for (String encoding : Charset.availableCharsets().keySet()) {
			if (testFileUsing("viewsandbox/viewhistory.txt", encoding, "août")) {
				System.out.println("encoding " + encoding + " OK");
			}
		}

	}

	private boolean testFileUsing(final String resourceName, final String encoding, final String expected) throws IOException {
		URL testFile = getClass().getResource(resourceName);
		InputStream inputStream = testFile.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		try {
			String line;
			boolean found = false;
			int i = 1;
			while (!found && (line = reader.readLine()) != null) {
				found = line.contains(expected);
				if (found) {
					System.out.println("line " + i + " contains [" + expected + "]");
				}
				i++;
			}
			return found;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
