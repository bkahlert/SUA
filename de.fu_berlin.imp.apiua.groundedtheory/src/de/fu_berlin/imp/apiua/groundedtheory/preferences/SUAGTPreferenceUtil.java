package de.fu_berlin.imp.apiua.groundedtheory.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.nebula.NebulaPreferences;
import com.bkahlert.nebula.utils.EclipsePreferenceUtil;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.Activator;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer;

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
				.serialize(uris != null ? uris : new LinkedList<>());
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
				.serialize(codes != null ? codes : new LinkedList<>());
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.LAST_USED_CODES, pref);
	}

	public List<URI> getLastUsedCodes() {
		String pref = this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.LAST_USED_CODES);
		if (pref != null && !pref.isEmpty()) {
			try {
				return new ArrayList<URI>(
						de.fu_berlin.imp.apiua.core.util.SerializationUtils
								.deserialize(pref));
			} catch (Exception e) {
				LOGGER.error("Error loading last used codes", e);
			}
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
			try {
				return new ArrayList<URI>(
						de.fu_berlin.imp.apiua.core.util.SerializationUtils
								.deserialize(pref));
			} catch (Exception e) {
				LOGGER.error("Could not load last opened axial coding model", e);
			}
		}
		return new LinkedList<URI>();
	}

	@SuppressWarnings("unchecked")
	public static void loadExpandedElementsASync(final TreeViewer viewer,
			final String saveExpandedElementsKey) {
		Job job = new NamedJob(CodeViewer.class, "Loading Expanded Elements") {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				final SubMonitor subMonitor = SubMonitor.convert(monitor);
				new NebulaPreferences()
						.loadExpandedElements(
								saveExpandedElementsKey,
								viewer,
								returnValue -> {
									try {
										URI uri = new URI(returnValue);
										subMonitor.setWorkRemaining(2);
										List<URI> preload = new ArrayList<URI>();
										preload.add(uri);
										for (Object descendant : ViewerUtils
												.getDescendants(viewer, uri)) {
											if (descendant instanceof URI) {
												preload.add((URI) descendant);
											}
										}
										LocatorService.preload(
												CodeViewer.class, viewer,
												preload, subMonitor.newChild(1));
										return uri;
									} catch (Exception e) {
										CodeViewer.LOGGER.error(
												"Error loading expanded element "
														+ returnValue, e);
									}
									return null;
								});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@SuppressWarnings("unchecked")
	public static void saveExpandedElements(TreeViewer viewer,
			String saveExpandedElementsKey) {
		new NebulaPreferences().saveExpandedElements(saveExpandedElementsKey,
				viewer, returnValue -> {
					if (returnValue instanceof URI) {
						return ((URI) returnValue).toString();
					}
					return null;
				});
	}
	public String getLastCreatedRelationName() {
		String s = this.getPreferenceStore().getString(
				SUAGTPreferenceConstants.LAST_CREATED_RELATION_NAME);
		if (s == null) {
			s = "";
		}
		return s;
	}

	public void setLastCreatedRelationName(String name) {
		this.getPreferenceStore().setValue(
				SUAGTPreferenceConstants.LAST_CREATED_RELATION_NAME,
				name != null ? name : "");
	}

}
