package org.intellij.vcs.mks;

import com.intellij.openapi.components.*;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IMPORTANT : keep persisted properties PUBLIC or they won't be persisted !
 */
@State (
    name ="Mks.Configuration",
    storages = {
        @Storage(id="other", file = StoragePathMacros.APP_CONFIG + "/other.xml")
    }
)
public class MksConfiguration
		implements PersistentStateComponent<MksConfiguration>, ApplicationComponent, MksCLIConfiguration {
	public static final String DEFAULT_ENCODING = Charset.defaultCharset().name();
	private static final String DEFAULT_DATE_PATTERN = "MMM dd, yyyy - hh:mm a";


    public Map<String, String> SI_ENCODINGS= new HashMap<String, String>();
    public String defaultEncoding = DEFAULT_ENCODING;
    /**
     * (host:port(,host:port)*)?
     */
    public String nonSiServers = "";
    public String datePattern = DEFAULT_DATE_PATTERN;
    public boolean synchronizeNonMembers = true;
    public Map<String, Set<String>> rememberedUsernames = new HashMap<String, Set<String>>();


    @Nullable
    @Override
    public MksConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(MksConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
        if (this.defaultEncoding == null) {
            initDefaultEncoding();
        }
        simplifyIgnoredServers();
        if (datePattern == null) {
            datePattern = DEFAULT_DATE_PATTERN;
        }

    }

    @NotNull
    public Map <String, String> getSiEncodings (){
        return SI_ENCODINGS;
    }

    public MksConfiguration() {
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

	public boolean isServerSiServer(@NotNull final MksServerInfo aServer) {
		return !nonSiServers.contains(aServer.toHostAndPort());
	}

	public void serverIsSiServer(@NotNull final MksServerInfo aServer, final boolean yesOrNo) {
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

    @NotNull
	public String getIgnoredServers() {
		return this.nonSiServers;
	}

	@NotNull
	public String getMksSiEncoding(@NotNull final String command) {
		final Map<String, String> encodings = SI_ENCODINGS;
		return (encodings.containsKey(command)) ? encodings.get(command) : this.defaultEncoding;
	}

	public void setDatePattern(@NotNull String aPattern) {
		datePattern = aPattern;
	}

	@NotNull
	public String getDatePattern() {
		return datePattern;
	}

    @NotNull
	public CommandExecutionListener getCommandExecutionListener() {
		return CommandExecutionListener.IDLE;
	}

	public boolean isMks2007() {
		throw new UnsupportedOperationException("this is not a static parameter");
	}

	public boolean isSynchronizeNonMembers() {
		return synchronizeNonMembers;
	}

	public void setSynchronizeNonMembers(boolean synchronizeNonMembers) {
		this.synchronizeNonMembers = synchronizeNonMembers;
	}

    public void setSiEncodings(@NotNull Map<String, String> siEncodings) {
        this.SI_ENCODINGS = siEncodings;
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

    @NotNull
    public Set<String> getRememberedUsernames(@NotNull String hostAndPort) {
        Set<String> usernames = rememberedUsernames.get(hostAndPort);
        return (null == usernames) ? Collections.EMPTY_SET: usernames;
    }

    public void addRememberedUsername(@NotNull String hostAndPort, @NotNull String username) {
        Set<String> usernames = rememberedUsernames.get(hostAndPort);
        if (null == usernames) {
            usernames = new HashSet<String>();
            rememberedUsernames.put(hostAndPort, usernames);
        }
        usernames.add(username);
    }
}
