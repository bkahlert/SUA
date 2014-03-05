package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView;

public class EntityLocatorProvider extends AdaptingLocatorProvider {

	public static final Logger LOGGER = Logger
			.getLogger(EntityLocatorProvider.class);
	public static final String ENTITY_NAMESPACE = "entity";

	@SuppressWarnings("unchecked")
	public EntityLocatorProvider() {
		super(Entity.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !"sua".equalsIgnoreCase(uri.getScheme())
				|| !ENTITY_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return Entity.class;
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

		if (Activator.getDefault() == null
				|| Activator.getDefault().getLoadedData() == null
				|| Activator.getDefault().getLoadedData().getEntityManager() == null) {
			return null;
		}

		for (Entity entity : Activator.getDefault().getLoadedData()
				.getEntityManager().getPersons()) {
			if (entity.getUri().equals(uri)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2 * uris.length);

		if (uris.length > 0) {
			EntityView entityView = (EntityView) WorkbenchUtils
					.getView(EntityView.ID);
			final EntityViewer viewer = entityView.getEntityTableViewer();
			subMonitor.worked(1);
			List<URI> selected;
			try {
				selected = ExecUtils.syncExec(new Callable<List<URI>>() {
					@Override
					public List<URI> call() throws Exception {
						viewer.setSelection(new StructuredSelection(uris), true);
						return SelectionUtils.getAdaptableObjects(
								viewer.getSelection(), URI.class);
					}
				});
				subMonitor.worked(1);
				return uris.length == selected.size();
			} catch (Exception e) {
				LOGGER.error("Error selecting " + uris + " in " + EntityView.ID);
				return false;
			}
		}

		return true;
	}

}
