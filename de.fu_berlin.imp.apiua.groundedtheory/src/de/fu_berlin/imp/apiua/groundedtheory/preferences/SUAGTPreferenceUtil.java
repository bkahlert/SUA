package de.fu_berlin.imp.apiua.groundedtheory.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.bkahlert.nebula.utils.EclipsePreferenceUtil;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.Activator;

public class SUAGTPreferenceUtil extends EclipsePreferenceUtil {

	private static final Logger LOGGER = Logger.getLogger(Logger.class);

	public SUAGTPreferenceUtil() {
		super(Activator.getDefault());
	}

	public File getCodeStoreFile() {
		return new File(this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.CODESTORE_FILE));
	}

	public File getDefaultCodeStoreFile() {
		return new File(this.getPreferenceStore().getDefaultString(
				SUAGTPreferenceConstants.CODESTORE_FILE));
	}

	public void setCodeStoreFile(File file) {
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.CODESTORE_FILE, file.toString());
	}

	public boolean codeStoreFileChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAGTPreferenceConstants.CODESTORE_FILE);
	}

	public long getMemoAutosaveAfterMilliseconds() {
		return this.getPreferenceStore().getLong(
				SUAGTPreferenceConstants.MEMO_AUTOSAVE_AFTER_MILLISECONDS);
	}

	public void setLastOpenedMemos(List<URI> uris) {
		String pref = de.fu_berlin.imp.apiua.core.util.SerializationUtils
				.serialize(uris);
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.LAST_OPENED_MEMOS, pref);
	}

	public List<URI> getLastOpenedMemos() {
		String pref = this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.LAST_OPENED_MEMOS);
		if (pref != null && !pref.isEmpty()) {
			try {
				return new ArrayList<URI>(
						de.fu_berlin.imp.apiua.core.util.SerializationUtils
								.deserialize(pref));
			} catch (Exception e) {
				LOGGER.error("Could not load last opened memos", e);
			}
		}
		return new LinkedList<URI>();
	}

	public void setLastUsedCodes(List<URI> codes) {
		String pref = de.fu_berlin.imp.apiua.core.util.SerializationUtils
				.serialize(codes);
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.LAST_USED_CODES, pref);
	}

	public List<URI> getLastUsedCodes() {
		String pref = this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.LAST_USED_CODES);
		if (pref != null && !pref.isEmpty()) {
			return new ArrayList<URI>(
					de.fu_berlin.imp.apiua.core.util.SerializationUtils
							.deserialize(pref));
		}
		return new LinkedList<URI>();
	}

	public void setLastOpenedAxialCodingModels(List<URI> axialCodingModels) {
		String pref = de.fu_berlin.imp.apiua.core.util.SerializationUtils
				.serialize(axialCodingModels);
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.LAST_OPENED_CODING_MODELS, pref);
	}

	public List<URI> getLastOpenedAxialCodingModels() {
		String pref = this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.LAST_OPENED_CODING_MODELS);
		if (pref != null && !pref.isEmpty()) {
			return new ArrayList<URI>(
					de.fu_berlin.imp.apiua.core.util.SerializationUtils
							.deserialize(pref));
		}
		return new LinkedList<URI>();
	}
}
