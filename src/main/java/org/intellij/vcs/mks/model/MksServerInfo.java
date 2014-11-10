package org.intellij.vcs.mks.model;

import org.jetbrains.annotations.NotNull;

/**
 * How to connect to a given mks server as returned by si servers. <br/>
 */ // when offline you have like "user@host:7001 (offline)"
public final class MksServerInfo {
	private static final String UNKNOWN_USER = "<unknown>";
	@NotNull
	public final String user;
	@NotNull
	public final String host;
	@NotNull
	public final String port;
	public boolean isSIServer = true;

	public MksServerInfo(@NotNull final String user, @NotNull final String host, @NotNull final String port) {
		this.host = host.toLowerCase();
		this.port = port;
		this.user = user;
	}
	public MksServerInfo(@NotNull final String host, @NotNull final String port) {
		this.host = host.toLowerCase();
		this.port = port;
		this.user = UNKNOWN_USER;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final MksServerInfo that = (MksServerInfo) o;

		return host.equals(that.host) && port.equals(that.port);

	}

	@Override
	public int hashCode() {
		int result;
		result = host.hashCode();
		result = 31 * result + port.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "MksServerInfo{" +
				"host='" + this.host + '\'' +
				", user='" + this.user + '\'' +
				", port='" + this.port + '\'' +
				'}';
	}

	public String toHostAndPort() {
		return this.host + ":" + this.port;
	}
	public static MksServerInfo fromHostAndPort(String hostAndPort) {
		if (! hostAndPort.matches("([^:]+):(\\d+)")) {
			throw new IllegalArgumentException("not a hostAndPort [" + hostAndPort + "]");
		}
		int colonIndex = hostAndPort.indexOf(':');
		String host = hostAndPort.substring(0, colonIndex);
		String port = hostAndPort.substring(colonIndex + 1);
		return new MksServerInfo(host,port);
	}

}
