package de.fu_berlin.imp.seqan.usability_analyzer.diff.gt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;

public class DiffCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DiffCodeableProvider.class);

	public static final String DIFF_NAMESPACE = "diff";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(DIFF_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitor,
			final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				String[] path = codeInstanceID.getRawPath().substring(1)
						.split("/");

				// 0: ID
				ID id = new ID(path[0]);
				DiffFileList diffFiles = Activator.getDefault()
						.getDiffFileDirectory().getDiffFiles(id, monitor.get());

				// 1: Revision
				Integer revision = (path.length >= 1) ? Integer
						.parseInt(path[1]) : null;
				if (revision == null) {
					LOGGER.error("Revision not specified for coded object retrieval for "
							+ ID.class.getSimpleName() + " " + id);
					return null;
				}
				if (diffFiles.size() <= revision) {
					LOGGER.error("There is no revision " + revision
							+ " of the " + DiffFile.class.getSimpleName()
							+ "s with " + ID.class.getSimpleName() + " " + id);
					return null;
				}

				// 2: Record
				DiffFile diffFile = diffFiles.get(revision);
				if (path.length <= 2)
					return diffFile;
				String diffFileRecordName;
				try {

					diffFileRecordName = URLDecoder.decode(path[2], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Could no decode name of "
							+ DiffFileRecord.class.getSimpleName());
					return null;
				}
				for (DiffFileRecord diffFileRecord : diffFile
						.getDiffFileRecords()) {
					if (diffFileRecord.getFilename().equals(diffFileRecordName)) {
						return diffFileRecord;
					}
				}
				return null;
			}
		};
	}

	@Override
	public void showCodedObjectsInWorkspace2(List<ICodeable> codedObjects) {
		try {
			if (codedObjects.size() > 0) {
				DiffExplorerView diffExplorerView = (DiffExplorerView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(DiffExplorerView.ID);
				diffExplorerView.getDiffFileListsViewer().setSelection(
						new StructuredSelection(codedObjects), true);
			}
		} catch (PartInitException e) {
			LOGGER.error("Could not open " + ViewPart.class.getSimpleName()
					+ " " + DiffExplorerView.ID, e);
		}
	}
}
