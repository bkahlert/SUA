package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer;
import de.fu_berlin.imp.apiua.groundedtheory.views.RelationView;

public class RelationInstanceLocatorProvider extends AdaptingLocatorProvider {

	public static final String RELATION_INSTANCE_NAMESPACE = "relationInstance";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(RelationInstanceLocatorProvider.class);

	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	public RelationInstanceLocatorProvider() {
		super(IRelationInstance.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !RELATION_INSTANCE_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return IRelationInstance.class;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		return true;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		for (IRelation relation : codeService.getRelations()) {
			for (IRelationInstance relationInstance : codeService
					.getRelationInstances(relation)) {
				if (relationInstance.getUri().equals(uri)) {
					return relationInstance;
				}
			}
		}

		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length > 0) {
			RelationView relationView = (RelationView) WorkbenchUtils
					.getView(RelationView.ID);
			return this.selectRelationInstances(uris, relationView).length == uris.length;
		}
		return true;
	}

	private URI[] selectRelationInstances(final URI[] uris,
			final RelationView relationView) {
		try {
			List<URI> selectedLocatables = ExecUtils.syncExec(() -> {
				final RelationViewer viewer = relationView.getRelationViewer();
				viewer.setSelection(new StructuredSelection(uris));
				return SelectionUtils.getAdaptableObjects(
						viewer.getSelection(), URI.class);
			});
			return selectedLocatables.toArray(new URI[0]);
		} catch (Exception e) {
			return new URI[0];
		}
	}

}
