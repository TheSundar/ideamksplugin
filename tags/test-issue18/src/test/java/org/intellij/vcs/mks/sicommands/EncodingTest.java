/*
 * ***************************************************************
 * COPYRIGHT. HSBC HOLDINGS PLC 2006. ALL RIGHTS RESERVED.
 *
 * THIS SOFTWARE IS ONLY TO BE USED FOR THE PURPOSE FOR WHICH IT
 * HAS BEEN PROVIDED. NO PART OF IT IS TO BE REPRODUCED,
 * DISASSEMBLED, TRANSMITTED, STORED IN A RETRIEVAL SYSTEM NOR
 * TRANSLATED IN ANY HUMAN OR COMPUTER LANGUAGE IN ANY WAY OR
 * FOR ANY OTHER PURPOSES WHATSOEVER WITHOUT THE PRIOR WRITTEN
 * CONSENT OF HSBC HOLDINGS PLC.
 * ***************************************************************
 */
package org.intellij.vcs.mks.sicommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import junit.framework.TestCase;

public class EncodingTest extends TestCase {

	public void testEncoding() throws IOException {
		for (String encoding : Charset.availableCharsets().keySet()) {
			if (testFileUsing("viewsandbox/sample1.txt", encoding, "Working file 1 082 bytes larger")) {
				System.out.println("encoding " + encoding + " OK");
			}
		}

	}

	private boolean testFileUsing(final String resourceName, final String encoding, final String expected) throws IOException {
		URL testFile = getClass().getResource(resourceName);
		InputStream inputStream = testFile.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		try {
			String line = null;
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
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

	}
}
