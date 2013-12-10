package org.intellij.vcs.mks;

import java.lang.ref.SoftReference;
import java.util.ResourceBundle;
import com.intellij.CommonBundle;

public class MksBundle {
	public static String message(String key, Object... params) {
		return CommonBundle.message(getBundle(), key, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = null;
		if (ourBundle != null) {
			bundle = ourBundle.get();
		}
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(BUNDLE);
			ourBundle = new SoftReference<ResourceBundle>(bundle);
		}
		return bundle;
	}

	@SuppressWarnings({"StaticVariableMayNotBeInitialized", "StaticNonFinalField"})
	private static SoftReference<ResourceBundle> ourBundle;
	private static final String BUNDLE = "org.intellij.vcs.mks.mksBundle";
}
