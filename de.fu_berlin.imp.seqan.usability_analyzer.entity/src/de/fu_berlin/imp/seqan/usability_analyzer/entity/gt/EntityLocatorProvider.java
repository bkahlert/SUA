package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView;

public class EntityLocatorProvider implements ILocatorProvider {

	public static final String ENTITY_NAMESPACE = "entity";

	@Override
	public String[] getAllowedNamespaces() {
		return new String[] { ENTITY_NAMESPACE };
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		for (Entity entity : Activator.getDefault().getLoadedData()
				.getEntityManager().getPersons()) {
			if (entity.getUri().equals(uri)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public boolean showInWorkspace(ILocatable[] locatables, boolean open,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		EntityView entityView = (EntityView) WorkbenchUtils
				.getView(EntityView.ID);
		EntityViewer viewer = entityView.getEntityTableViewer();
		subMonitor.worked(1);
		viewer.setSelection(new StructuredSelection(locatables), true);
		List<ILocatable> selected = SelectionUtils.getAdaptableObjects(
				viewer.getSelection(), ILocatable.class);
		subMonitor.worked(1);
		return locatables.length == selected.size();
	}

}
