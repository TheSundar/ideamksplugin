package org.intellij.vcs.mks.sicommands;

import junit.framework.TestCase;

import java.util.regex.Pattern;

public class ReplacePasswordTest extends TestCase {

	public void testIt() {
		String toString = "si connect --batch --hostname=myserver --port=7001 --user=myuser --password=mypassword ";
		final String patternString = "--password=[^\\s]+";
		final Pattern pattern = Pattern.compile(patternString);
		final String[] strings = pattern.split(toString);
		final StringBuffer buf = new StringBuffer(toString.length());
		for (int i = 0; i < strings.length; i++) {
			final String string = strings[i];
			buf.append(string);
			if (i + 1 < strings.length) {
				buf.append("--password=<password>");
			}
		}
		toString = buf.toString();
		System.err.println(toString);
/*
        final Matcher matcher = pattern.matcher(toString);
        if (matcher.find()) {
            toString = matcher.replaceAll("--password=<password>");
        }
*/
		assertFalse(toString.contains("mypassword"));

	}
}
