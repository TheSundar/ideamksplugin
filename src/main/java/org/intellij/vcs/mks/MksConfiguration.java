package org.intellij.vcs.mks;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * IMPORTANT : keep persisted properties PUBLIC or they won't be persisted !
 */
public class MksConfiguration
		implements JDOMExternalizable, ApplicationComponent, MksCLIConfiguration {
	public static final String DEFAULT_ENCODING = Charset.defaultCharset().name();

	public StringMap SI_ENCODINGS;
	public String defaultEncoding;
	/**
	 * (host:port(,host:port)*)?
	 */
	public String nonSiServers = "";
	public String datePattern;
	public boolean synchronizeNonMembers = true;
	private static final String DEFAULT_DATE_PATTERN = "MMM dd, yyyy - hh:mm a";


	public MksConfiguration() {
		this.SI_ENCODINGS = new StringMap();
		initDefaultEncoding();
	}

	private void initDefaultEncoding() {
		final String defaultEncodingName = MksBundle.message("defaultEncoding");
		this.defaultEncoding = (defaultEncodingName == null) ? MksConfiguration.DEFAULT_ENCODING : defaultEncodingName;
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "MksConfiguration";
	}

	public void initComponent() {
	}

	public void readExternal(final Element element)
			throws InvalidDataException {
		DefaultJDOMExternalizer.readExternal(this, element);
		if (this.defaultEncoding == null) {
			initDefaultEncoding();
		}
		simplifyIgnoredServers();
		if (datePattern == null) {
			datePattern = DEFAULT_DATE_PATTERN;
		}

	}

	private void simplifyIgnoredServers() {
		final StringTokenizer tok = new StringTokenizer(this.nonSiServers, ",", false);
		final StringBuffer newIgnoredServers = new StringBuffer();
		while (tok.hasMoreTokens()) {
			final String token = tok.nextToken().trim();
			if (token.length() > 0) {
				newIgnoredServers.append(token);
				if (tok.hasMoreTokens()) {
					newIgnoredServers.append(',');
				}
			}
		}
		if (!newIgnoredServers.toString().equals(this.nonSiServers)) {
			this.nonSiServers = newIgnoredServers.toString();
		}

	}

	public void writeExternal(final Element element)
			throws WriteExternalException {
		DefaultJDOMExternalizer.writeExternal(this, element);
	}

	public boolean isServerSiServer(final MksServerInfo aServer) {
		return !nonSiServers.contains(aServer.toHostAndPort());
	}

	public void serverIsSiServer(final MksServerInfo aServer, final boolean yesOrNo) {
		if (!yesOrNo) {
			if (!nonSiServers.contains(aServer.toHostAndPort())) {
				synchronized (this) {
					if (nonSiServers.length() > 0) {
						this.nonSiServers = this.nonSiServers + "," + aServer.toHostAndPort();
					} else {
						this.nonSiServers = aServer.toHostAndPort();
					}
				}
			}
		} else {
			if (nonSiServers.contains(aServer.toHostAndPort())) {
				synchronized (this) {
					if (nonSiServers.equals(aServer.toHostAndPort())) {
						this.nonSiServers = "";
					} else {
						this.nonSiServers = nonSiServers.replace(aServer.toHostAndPort(), "");
						this.nonSiServers = nonSiServers.replace(",,", ",");
					}
				}
			}

		}
		simplifyIgnoredServers();
	}

	public String getIgnoredServers() {
		return this.nonSiServers;
	}

	@NotNull
	public String getMksSiEncoding(final String command) {
		final Map<String, String> encodings = SI_ENCODINGS.getMap();
		return (encodings.containsKey(command)) ? encodings.get(command) : this.defaultEncoding;
	}

	public void setDatePattern(String aPattern) {
		datePattern = aPattern;
	}

	@NotNull
	public String getDatePattern() {
		return datePattern;
	}

	public CommandExecutionListener getCommandExecutionListener() {
		return CommandExecutionListener.IDLE;
	}

	public boolean isSynchronizeNonMembers() {
		return synchronizeNonMembers;
	}

	public void setSynchronizeNonMembers(boolean synchronizeNonMembers) {
		this.synchronizeNonMembers = synchronizeNonMembers;
	}

	public static class StringMap implements JDOMExternalizable {
		@NonNls
		private static final String LEN_STRING = "len";

		private Map<String, String> map;

		public StringMap() {
			this.map = new HashMap<String, String>();
		}

		public Map<String, String> getMap() {
			return this.map;
		}

		public void setMap(final Map<String, String> map) {
			this.map = map;
		}

		public void readExternal(final Element element) throws InvalidDataException {
			// Read in map size
			final int size = Integer.parseInt(element.getAttributeValue(StringMap.LEN_STRING));

			// Read in all elements in the proper order.
			for (int index = 0; index < size; index++) {
				this.map.put(element.getAttributeValue('k' + Integer.toString(index)),
						element.getAttributeValue('v' + Integer.toString(index))
				);
			}
		}


		public void writeExternal(final Element element) throws WriteExternalException {
			// Write out map size
			element.setAttribute(StringMap.LEN_STRING, Integer.toString(this.map.size()));

			// Write out all elements in the proper order.
			int index = 0;
			for (final Map.Entry<String, String> entry : this.map.entrySet()) {
				element.setAttribute('k' + Integer.toString(index), entry.getKey());
				element.setAttribute('v' + Integer.toString(index), entry.getValue());
				index++;
			}
		}
	}

	public static class DatePatternValidator implements InputValidator {

		public boolean checkInput(String s) {
			try {
				DateFormat format = new SimpleDateFormat(s);
				format.format(new Date());
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}

		public boolean canClose(String s) {
			return checkInput(s);
		}
	}
}
