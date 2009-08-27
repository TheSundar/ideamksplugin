package org.intellij.vcs.mks.model;

import org.jetbrains.annotations.NotNull;

/**
 * How to connect to a given mks server as returned by si servers. <br/>
 */ // when offline you have like "user@host:7001 (offline)"
public final class MksServerInfo {
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

}
