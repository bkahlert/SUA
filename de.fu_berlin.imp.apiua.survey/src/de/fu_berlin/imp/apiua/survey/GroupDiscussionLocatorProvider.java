package de.fu_berlin.imp.apiua.survey;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocument;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocumentField;
import de.fu_berlin.imp.apiua.survey.views.GroupDiscussionCodingView;

public class GroupDiscussionLocatorProvider extends AdaptingLocatorProvider {

	public static final String GROUP_DISCUSSION_NAMESPACE = "groupDiscussion";

	private static final Logger LOGGER = Logger
			.getLogger(GroupDiscussionLocatorProvider.class);

	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	public GroupDiscussionLocatorProvider() {
		super(GroupDiscussionDocument.class, GroupDiscussionDocumentField.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !GROUP_DISCUSSION_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
		case 1:
			return GroupDiscussionDocument.class;
		case 2:
			return GroupDiscussionDocumentField.class;
		}

		LOGGER.error("Unknown " + URI.class.getSimpleName() + " format: " + uri);

		return null;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		return Activator.getDefault().getSurveyContainer() != null;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		Collection<GroupDiscussionDocument> groupDiscussionDocuments = Activator
				.getDefault().getSurveyContainer()
				.getGroupDiscussionDocuments();
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				groupDiscussionDocuments.size());
		for (GroupDiscussionDocument groupDiscussionDocument : groupDiscussionDocuments) {
			if (this.getType(uri) == GroupDiscussionDocument.class) {
				String currUri = groupDiscussionDocument.getUri().toString();
				int minLength = Math.min(currUri.length(), uri.toString()
						.length());
				if (currUri.substring(0, minLength).equals(
						uri.toString().substring(0, minLength))) {
					subMonitor.done();
					return groupDiscussionDocument;
				}
			} else {
				for (GroupDiscussionDocumentField groupDiscussionDocumentField : groupDiscussionDocument) {
					if (groupDiscussionDocumentField.getUri().equals(uri)) {
						subMonitor.done();
						return groupDiscussionDocumentField;
					}
				}
			}
			subMonitor.worked(1);
		}
		subMonitor.done();
		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length == 1 && uris[0] != null) {
			GroupDiscussionCodingView groupDiscussionView = (GroupDiscussionCodingView) WorkbenchUtils
					.getView(GroupDiscussionCodingView.ID);
			this.select(uris, groupDiscussionView);
		}
		return true;
	}

	private URI[] select(final URI[] uris,
			final GroupDiscussionCodingView groupDiscussionView) {
		try {
			List<URI> selectedLocatables = ExecUtils
					.syncExec(() -> {
						final ISelectionProvider selectionProvider = groupDiscussionView
								.getSelectionProvider();
						selectionProvider.setSelection(new StructuredSelection(
								uris));
						return SelectionUtils.getAdaptableObjects(
								selectionProvider.getSelection(), URI.class);
					});
			return selectedLocatables.toArray(new URI[0]);
		} catch (Exception e) {
			return new URI[0];
		}
	}

}
