package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;

public class EntityCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EntityCodeableProvider.class);

	public static final String ENTITY_NAMESPACE = "entity";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(ENTITY_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			AtomicReference<IProgressMonitor> monitor, final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				for (Entity entity : Activator.getDefault().getLoadedData()
						.getEntityManager().getPersons()) {
					if (entity.getUri().equals(codeInstanceID)) {
						return entity;
					}
				}

				return null;
			}
		};
	}

	@Override
	public ICodeable[] showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			final EntityView entityView = (EntityView) WorkbenchUtils
					.getView(EntityView.ID);
			try {
				return ExecutorUtil.syncExec(new Callable<ICodeable[]>() {
					@Override
					public ICodeable[] call() throws Exception {
						EntityViewer viewer = entityView.getEntityTableViewer();
						viewer.setSelection(new StructuredSelection(
								codedObjects), true);
						List<ICodeable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ICodeable.class);
						return selectedCodeables.toArray(new ICodeable[0]);
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
				return null;
			}
		}
		return null;
	}

}
