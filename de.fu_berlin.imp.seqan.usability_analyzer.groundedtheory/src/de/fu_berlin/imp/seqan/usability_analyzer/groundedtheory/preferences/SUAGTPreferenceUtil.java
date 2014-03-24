package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.bkahlert.nebula.utils.EclipsePreferenceUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;

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

	@SuppressWarnings("unchecked")
	public List<URI> getLastOpenedMemos() {
		try {
			String pref = this.getPreferenceStore().getString(
					SUAGTPreferenceConstants.LAST_OPENED_MEMOS);
			if (pref != null && !pref.isEmpty()) {
				List<String> strings = (List<String>) SerializationUtils
						.deserialize(pref.getBytes());
				List<URI> uris = new ArrayList<URI>(strings.size());
				for (String string : strings) {
					uris.add(new URI(string));
				}
				return uris;
			}
		} catch (Exception e) {
			LOGGER.error("Error getting last opened memos", e);
		}
		return new LinkedList<URI>();
	}

	public void setLastOpenedMemos(List<URI> uris) {
		List<String> strings = new ArrayList<String>(uris.size());
		for (URI uri : uris) {
			strings.add(uri.toString());
		}
		try {
			byte[] pref = SerializationUtils.serialize((Serializable) strings);
			this.getPreferenceStore().setValue(
					SUAGTPreferenceConstants.LAST_OPENED_MEMOS,
					new String(pref));
		} catch (Exception e) {
			LOGGER.error("Error saving last opened memos: " + uris, e);
		}
	}

	public void setLastUsedCodes(List<URI> codes) {
		List<String> strings = new ArrayList<String>(codes.size());
		for (URI code : codes) {
			strings.add(code.toString());
		}
		try {
			byte[] pref = SerializationUtils.serialize((Serializable) strings);
			this.getPreferenceStore().setValue(
					SUAGTPreferenceConstants.LAST_USED_CODES, new String(pref));
		} catch (Exception e) {
			LOGGER.error("Error saving last used codes: " + codes, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<URI> getLastUsedCodes() {
		try {
			String pref = this.getPreferenceStore().getString(
					SUAGTPreferenceConstants.LAST_USED_CODES);
			if (pref != null && !pref.isEmpty()) {
				List<String> strings = (List<String>) SerializationUtils
						.deserialize(pref.getBytes());
				List<URI> codes = new ArrayList<URI>(strings.size());
				for (String string : strings) {
					codes.add(new URI(string));
				}
				return codes;
			}
		} catch (Exception e) {
			LOGGER.error("Error getting last used codes", e);
		}
		return new LinkedList<URI>();
	}
}
