package de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.SerializationUtils;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.io.RegexFileFilter;

public class SUADiffPreferenceUtil extends PreferenceUtil {

	public SUADiffPreferenceUtil() {
		super(Activator.getDefault());
	}

	private HashMap<String, FileFilter> fileFilters = new HashMap<String, FileFilter>();

	public String[] getFileFilterPatterns() {
		try {
			String[] fileFilterPatterns = (String[]) SerializationUtils
					.deserialize(getPreferenceStore().getString(
							SUADiffPreferenceConstants.FILE_FILTER_PATTERNS)
							.getBytes());
			return fileFilterPatterns;
		} catch (Exception e) {
			return new String[0];
		}
	}

	public ArrayList<FileFilter> getFileFilters() {
		String[] fileFilterPatterns = this.getFileFilterPatterns();

		ArrayList<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (String fileFilterPattern : fileFilterPatterns) {
			if (!this.fileFilters.containsKey(fileFilterPattern))
				this.fileFilters.put(fileFilterPattern, new RegexFileFilter(
						fileFilterPattern));
			fileFilters.add(this.fileFilters.get(fileFilterPattern));
		}
		return fileFilters;
	}

	public void setFileFilterPatterns(String[] fileFilterPatterns) {
		getPreferenceStore().setValue(
				SUADiffPreferenceConstants.FILE_FILTER_PATTERNS,
				new String(SerializationUtils.serialize(fileFilterPatterns)));
	}

	public boolean fileFilterPatternsChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUADiffPreferenceConstants.FILE_FILTER_PATTERNS);
	}
}
