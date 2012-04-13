package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

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
				for (Entity entity : Activator.getDefault().getPersonManager()
						.getPersons()) {
					if (entity.getCodeInstanceID().equals(codeInstanceID))
						return entity;
				}

				return null;
			}
		};
	}

	@Override
	public void showCodedObjectsInWorkspace2(List<ICodeable> codedObjects) {
		try {
			if (codedObjects.size() > 0) {
				EntityView entityView = (EntityView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.showView(EntityView.ID);
				entityView.getEntityTableViewer().setSelection(
						new StructuredSelection(codedObjects), true);
			}
		} catch (PartInitException e) {
			LOGGER.error("Could not open " + ViewPart.class.getSimpleName()
					+ " " + EntityView.ID, e);
		}
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			@Override
			public String getText(Object element) {
				Entity entity = (Entity) element;

				ID id = entity.getId();
				return (id != null) ? id.toString() : "";
			}

			@Override
			public Image getImage(Object element) {
				Entity person = (Entity) element;
				try {
					if (codeService.getCodes(person).size() > 0) {
						return ImageManager.ENTITY_CODED;
					} else {
						return ImageManager.ENTITY;
					}
				} catch (CodeServiceException e) {
					LOGGER.error("Can't access "
							+ ICodeService.class.getSimpleName());
				}
				return ImageManager.ENTITY;
			}
		};
	}
}
