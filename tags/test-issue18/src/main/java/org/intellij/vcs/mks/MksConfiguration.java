package org.intellij.vcs.mks;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MksConfiguration
		implements JDOMExternalizable, ProjectComponent {
	public static final String DEFAULT_ENCODING = Charset.defaultCharset().name();

	public MksConfiguration() {
		SERVER = "";
		PORT = 7001;
		USER = "";
		PASSWORD = "";
		SANDBOX = "";
		PROJECT = "";
		SI_ENCODINGS = new StringMap();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("org.intellij.vcs.mks.mksBundle");
		String defaultEncodingName = resourceBundle.getString("defaultEncoding");
//		defaultEncoding = Charset.defaultCharset().name();
	}

	public void projectClosed() {
	}

	public void projectOpened() {
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "MksConfiguration";
	}

	public void initComponent() {
	}

	public void readExternal(Element element)
			throws InvalidDataException {
		DefaultJDOMExternalizer.readExternal(this, element);
	}

	public void writeExternal(Element element)
			throws WriteExternalException {
		DefaultJDOMExternalizer.writeExternal(this, element);
	}

	public String SERVER;
	public int PORT;
	public String USER;
	public String PASSWORD;
	public String SANDBOX;
	public String PROJECT;
	public StringMap SI_ENCODINGS;
	public String defaultEncoding;

	public static class StringMap implements JDOMExternalizable {
		@NonNls
		private static final String LEN_STRING = "len";

		private Map<String, String> map;

		public StringMap() {
			this.map = new HashMap<String, String>();
		}

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}

		public void readExternal(Element element) throws InvalidDataException {
			// Read in map size
			int size = Integer.parseInt(element.getAttributeValue(LEN_STRING));

			// Read in all elements in the proper order.
			for (int index = 0; index < size; index++) {
				this.map.put(element.getAttributeValue('k' + Integer.toString(index)),
						element.getAttributeValue('v' + Integer.toString(index))
				);
			}
		}


		public void writeExternal(Element element) throws WriteExternalException {
			// Write out map size
			element.setAttribute(LEN_STRING, Integer.toString(this.map.size()));

			// Write out all elements in the proper order.
			int index = 0;
			for (final Map.Entry<String, String> entry : this.map.entrySet()) {
				element.setAttribute('k' + Integer.toString(index), entry.getKey());
				element.setAttribute('v' + Integer.toString(index), entry.getValue());
				index++;
			}
		}
	}

}
