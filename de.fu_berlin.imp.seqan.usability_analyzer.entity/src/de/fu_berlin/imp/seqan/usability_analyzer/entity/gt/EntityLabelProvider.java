package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class EntityLabelProvider extends LabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EntityLabelProvider.class);

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(Object element) {
		Entity entity = (Entity) element;

		String id = entity.getInternalId();
		return (id != null) ? id : "";
	}

	@Override
	public Image getImage(Object element) {
		Entity person = (Entity) element;
		try {
			if (this.codeService.getCodes(person).size() > 0) {
				if (this.codeService.isMemo(person)) {
					return ImageManager.ENTITY_CODED_MEMO;
				} else {
					return ImageManager.ENTITY_CODED;
				}
			} else {
				if (this.codeService.isMemo(person)) {
					return ImageManager.ENTITY_MEMO;
				} else {
					return ImageManager.ENTITY;
				}
			}
		} catch (CodeServiceException e) {
			LOGGER.error("Can't access " + ICodeService.class.getSimpleName());
		}
		return ImageManager.ENTITY;
	}

}