package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PreferenceUtil {

	private IPreferenceStore preferenceStore;

	public PreferenceUtil(AbstractUIPlugin plugin) {
		this.preferenceStore = plugin.getPreferenceStore();
	}

	public void addPropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.getPreferenceStore().addPropertyChangeListener(
				propertyChangeListener);
	}

	public void removePropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.getPreferenceStore().removePropertyChangeListener(
				propertyChangeListener);
	}

	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
}
